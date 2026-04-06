package com.coe.mobile.ui.screens.recorder

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coe.mobile.data.api.RetrofitInstance
import com.coe.mobile.data.repository.MeetingRepository
import com.coe.mobile.data.repository.MeetingRepositoryImpl
import com.coe.mobile.recording.RecorderForegroundService
import com.coe.mobile.recording.RecorderSessionStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.time.Instant

class RecorderViewModel(
    private val meetingRepository: MeetingRepository = MeetingRepositoryImpl()
) : ViewModel() {

    private val _uiState = MutableStateFlow(RecorderUiState())
    val uiState: StateFlow<RecorderUiState> = _uiState.asStateFlow()
    private var processingPollJob: Job? = null

    init {
        viewModelScope.launch {
            RecorderSessionStore.state.collect { session ->
                _uiState.update { current ->
                    val derivedUploadStatus = when {
                        session.isUploading -> UploadStatus.UPLOADING
                        session.isReadyToSend &&
                            current.uploadStatus != UploadStatus.SUCCESS &&
                            current.uploadStatus != UploadStatus.ERROR -> UploadStatus.READY
                        !session.isReadyToSend &&
                            current.uploadStatus == UploadStatus.READY -> UploadStatus.IDLE
                        else -> current.uploadStatus
                    }

                    current.copy(
                        isRecording = session.isRecording,
                        elapsedTime = session.elapsedSeconds,
                        isReadyToSend = session.isReadyToSend,
                        audioFilePath = session.activeFilePath,
                        uploadStatus = derivedUploadStatus,
                        errorMessage = session.lastError ?: current.errorMessage
                    )
                }
            }
        }
    }

    fun startRecording(context: Context): String? {
        if (_uiState.value.uploadStatus == UploadStatus.UPLOADING) {
            return "Upload in progress. Please wait."
        }
        stopProcessingPolling()
        _uiState.update { state ->
            state.copy(
                uploadStatus = UploadStatus.IDLE,
                meetingId = null,
                errorMessage = null,
                processingOverallStatus = null,
                processingCurrentStage = null,
                processingStages = emptyMap(),
                processingUpdatedAt = null,
                isProcessing = false,
                processingErrorMessage = null
            )
        }
        RecorderForegroundService.requestStartRecording(context)
        return null
    }

    fun stopRecording(context: Context): String? {
        RecorderForegroundService.requestStopRecording(context)
        return null
    }

    fun uploadAudio() {
        if (RecorderSessionStore.state.value.isRecording) {
            setUploadError("Stop recording before upload.")
            return
        }

        val path = RecorderSessionStore.state.value.activeFilePath
        if (path.isNullOrBlank()) {
            setUploadError("No recorded audio file found.")
            return
        }

        val file = File(path)
        if (!file.exists() || !file.isFile) {
            setUploadError("Recorded file is missing.")
            return
        }

        stopProcessingPolling()
        RecorderSessionStore.setUploadState(
            isUploading = true,
            statusText = "Uploading..."
        )
        _uiState.update { state ->
            state.copy(
                uploadStatus = UploadStatus.UPLOADING,
                errorMessage = null,
                meetingId = null,
                processingOverallStatus = null,
                processingCurrentStage = null,
                processingStages = emptyMap(),
                processingUpdatedAt = null,
                isProcessing = false,
                processingErrorMessage = null
            )
        }

        viewModelScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    val audioRequestBody = file.asRequestBody("audio/mp4".toMediaType())
                    val audioPart = MultipartBody.Part.createFormData(
                        "audio_file",
                        file.name,
                        audioRequestBody
                    )
                    val recordedAtBody = Instant.now()
                        .toString()
                        .toRequestBody("text/plain".toMediaType())
                    RetrofitInstance.apiService.uploadAudio(
                        audioFile = audioPart,
                        recordedAt = recordedAtBody
                    )
                }

                if (response.isSuccessful) {
                    val meetingId = response.body()?.meetingId
                    if (meetingId.isNullOrBlank()) {
                        setUploadError("Upload succeeded but meeting_id was missing.")
                    } else {
                        RecorderSessionStore.setUploadState(
                            isUploading = false,
                            statusText = "Uploaded"
                        )
                        _uiState.update { state ->
                            state.copy(
                                uploadStatus = UploadStatus.SUCCESS,
                                meetingId = meetingId,
                                errorMessage = null,
                                isProcessing = true,
                                processingOverallStatus = "processing",
                                processingCurrentStage = null,
                                processingStages = emptyMap(),
                                processingUpdatedAt = null,
                                processingErrorMessage = null
                            )
                        }
                        startProcessingPolling(meetingId)
                    }
                } else {
                    val rawError = response.errorBody()?.string()
                    val message = parseServerErrorMessage(rawError)
                        ?: "Upload failed (${response.code()})."
                    setUploadError(message)
                }
            } catch (_: IOException) {
                setUploadError("Unable to reach laptop. Check network and server URL.")
            } catch (error: Exception) {
                setUploadError(error.message ?: "Upload failed.")
            }
        }
    }

    private fun setUploadError(message: String) {
        RecorderSessionStore.setUploadState(
            isUploading = false,
            statusText = message
        )
        _uiState.update { state ->
            state.copy(
                uploadStatus = UploadStatus.ERROR,
                errorMessage = message
            )
        }
    }

    private fun startProcessingPolling(meetingId: String) {
        stopProcessingPolling()
        processingPollJob = viewModelScope.launch {
            val startedAtMillis = System.currentTimeMillis()
            while (isActive) {
                val statusResult = meetingRepository.getProcessingStatus(meetingId)
                statusResult.fold(
                    onSuccess = { response ->
                        val mappedStages = toStageStatusMap(response.stages)
                        val normalizedOverall = normalizeStatus(response.overallStatus)
                        val normalizedCurrentStage = response.currentStage?.trim()?.lowercase()

                        val hasFailedStage = mappedStages.values.any { stageStatus ->
                            normalizeStatus(stageStatus) == STATUS_FAILED
                        }
                        val isCompleted = normalizedOverall == STATUS_COMPLETED ||
                            (mappedStages.isNotEmpty() && STAGE_ORDER.all { stage ->
                                normalizeStatus(mappedStages[stage]) == STATUS_COMPLETED
                            })
                        val isFailed = normalizedOverall == STATUS_FAILED || hasFailedStage
                        val resolvedOverall = when {
                            isCompleted -> STATUS_COMPLETED
                            isFailed -> STATUS_FAILED
                            else -> STATUS_PROCESSING
                        }

                        _uiState.update { state ->
                            state.copy(
                                processingOverallStatus = resolvedOverall,
                                processingCurrentStage = normalizedCurrentStage,
                                processingStages = mappedStages,
                                processingUpdatedAt = response.updatedAt,
                                isProcessing = resolvedOverall == STATUS_PROCESSING,
                                processingErrorMessage = if (resolvedOverall == STATUS_FAILED) {
                                    "Processing failed."
                                } else {
                                    null
                                }
                            )
                        }

                        if (resolvedOverall == STATUS_COMPLETED || resolvedOverall == STATUS_FAILED) {
                            return@launch
                        }
                    },
                    onFailure = { error ->
                        _uiState.update { state ->
                            state.copy(
                                processingOverallStatus = STATUS_FAILED,
                                isProcessing = false,
                                processingErrorMessage = error.message ?: "Processing status unavailable."
                            )
                        }
                        return@launch
                    }
                )

                val elapsed = System.currentTimeMillis() - startedAtMillis
                if (elapsed >= PROCESSING_TIMEOUT_MILLIS) {
                    _uiState.update { state ->
                        state.copy(
                            processingOverallStatus = STATUS_FAILED,
                            isProcessing = false,
                            processingErrorMessage = "Processing status timeout."
                        )
                    }
                    return@launch
                }

                val pollDelay = if (elapsed < INITIAL_FAST_POLL_WINDOW_MILLIS) {
                    FAST_POLL_INTERVAL_MILLIS
                } else {
                    SLOW_POLL_INTERVAL_MILLIS
                }
                delay(pollDelay)
            }
        }
    }

    private fun stopProcessingPolling() {
        processingPollJob?.cancel()
        processingPollJob = null
    }

    private fun toStageStatusMap(rawStages: Map<String, Any?>?): Map<String, String> {
        if (rawStages.isNullOrEmpty()) return emptyMap()
        return rawStages.mapNotNull { (key, rawValue) ->
            val status = when (rawValue) {
                is String -> rawValue
                is Map<*, *> -> {
                    rawValue["status"]?.toString()
                        ?: rawValue["state"]?.toString()
                        ?: rawValue["stage_status"]?.toString()
                }
                else -> rawValue?.toString()
            }?.trim()?.lowercase()

            if (status.isNullOrBlank()) {
                null
            } else {
                key.trim().lowercase() to status
            }
        }.toMap()
    }

    private fun normalizeStatus(rawStatus: String?): String {
        val normalized = rawStatus?.trim()?.lowercase().orEmpty()
        return when {
            normalized.contains("complete") ||
                normalized == "processed" ||
                normalized == "done" ||
                normalized == "success" -> STATUS_COMPLETED
            normalized.contains("fail") ||
                normalized.contains("error") -> STATUS_FAILED
            normalized.contains("process") ||
                normalized.contains("running") ||
                normalized.contains("queue") ||
                normalized.contains("progress") ||
                normalized.contains("start") -> STATUS_PROCESSING
            else -> normalized
        }
    }

    private fun parseServerErrorMessage(rawError: String?): String? {
        if (rawError.isNullOrBlank()) return null
        return runCatching {
            val json = JSONObject(rawError)
            json.optString("message").takeIf { it.isNotBlank() }
        }.getOrNull()
    }

    override fun onCleared() {
        stopProcessingPolling()
        super.onCleared()
    }

    companion object {
        private const val FAST_POLL_INTERVAL_MILLIS = 2_500L
        private const val SLOW_POLL_INTERVAL_MILLIS = 4_000L
        private const val INITIAL_FAST_POLL_WINDOW_MILLIS = 30_000L
        private const val PROCESSING_TIMEOUT_MILLIS = 300_000L

        private const val STATUS_PROCESSING = "processing"
        private const val STATUS_COMPLETED = "completed"
        private const val STATUS_FAILED = "failed"

        private val STAGE_ORDER = listOf(
            "pipeline_triggered",
            "normalization",
            "transcription",
            "cleanup",
            "intelligence",
            "executive",
            "decision",
            "temporal",
            "calendar",
            "report"
        )
    }
}

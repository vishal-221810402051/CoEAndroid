package com.coe.mobile.ui.screens.recorder

data class RecorderUiState(
    val isRecording: Boolean = false,
    val elapsedTime: Int = 0,
    val isReadyToSend: Boolean = false,
    val audioFilePath: String? = null,
    val uploadStatus: UploadStatus = UploadStatus.IDLE,
    val meetingId: String? = null,
    val errorMessage: String? = null,
    val processingOverallStatus: String? = null,
    val processingCurrentStage: String? = null,
    val processingStages: Map<String, String> = emptyMap(),
    val processingUpdatedAt: String? = null,
    val isProcessing: Boolean = false,
    val processingErrorMessage: String? = null
)

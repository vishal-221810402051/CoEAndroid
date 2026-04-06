package com.coe.mobile.ui.screens.recorder

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.coe.mobile.ui.components.AtmosphereBackground
import com.coe.mobile.ui.components.ScreenEnterReveal
import com.coe.mobile.ui.components.StatusChip
import com.coe.mobile.ui.components.StatusChipVariant
import com.coe.mobile.ui.components.pressFeedbackModifier
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun RecorderScreen(
    modifier: Modifier = Modifier,
    recorderViewModel: RecorderViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by recorderViewModel.uiState.collectAsState()
    val isUploading = uiState.uploadStatus == UploadStatus.UPLOADING
    val hasProcessingError = !uiState.processingErrorMessage.isNullOrBlank() ||
        uiState.processingOverallStatus == "failed"
    val isProcessed = uiState.processingOverallStatus == "completed"
    val activeStage = resolveCurrentStage(
        stageStatuses = uiState.processingStages,
        currentStage = uiState.processingCurrentStage
    )
    val stageLine = activeStage?.let { stage ->
        val index = PROCESSING_STAGE_ORDER.indexOf(stage).takeIf { it >= 0 }
        if (index != null) {
            "Stage: ${formatStageLabel(stage)} (${index + 1}/${PROCESSING_STAGE_ORDER.size})"
        } else {
            "Stage: ${formatStageLabel(stage)}"
        }
    }
    val statusLabel = when {
        isUploading -> "Uploading..."
        uiState.isProcessing -> "Processing..."
        isProcessed -> "Processed"
        hasProcessingError || uiState.uploadStatus == UploadStatus.ERROR -> "Error"
        else -> "Idle"
    }
    val statusVariant = when {
        isUploading -> StatusChipVariant.Warning
        uiState.isProcessing -> StatusChipVariant.Warning
        isProcessed -> StatusChipVariant.Success
        hasProcessingError || uiState.uploadStatus == UploadStatus.ERROR -> StatusChipVariant.Error
        else -> StatusChipVariant.Neutral
    }

    LaunchedEffect(uiState.uploadStatus, uiState.errorMessage) {
        when (uiState.uploadStatus) {
            UploadStatus.SUCCESS -> {
                Toast.makeText(context, "Uploaded successfully", Toast.LENGTH_SHORT).show()
            }

            UploadStatus.ERROR -> {
                val message = uiState.errorMessage ?: "Upload failed."
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            }

            else -> Unit
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val errorMessage = recorderViewModel.startRecording(context)
            if (errorMessage != null) {
                Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "Microphone permission denied.", Toast.LENGTH_SHORT).show()
        }
    }

    AtmosphereBackground(modifier = modifier) {
        ScreenEnterReveal {
            val recordInteraction = remember { MutableInteractionSource() }
            val sendInteraction = remember { MutableInteractionSource() }
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
            Text(
                text = "Capture Console",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            CaptureStateBar(
                isRecording = uiState.isRecording,
                isReady = uiState.isReadyToSend,
                isUploading = isUploading,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp)
            )
            Box(
                modifier = Modifier
                    .padding(top = 22.dp, bottom = 20.dp)
                    .size(248.dp),
                contentAlignment = Alignment.Center
            ) {
                ProcessingStageDots(
                    stageStatuses = uiState.processingStages,
                    currentStage = activeStage,
                    overallStatus = uiState.processingOverallStatus,
                    modifier = Modifier.fillMaxSize()
                )

                RecorderMotionField(
                    isRecording = uiState.isRecording,
                    isReady = uiState.isReadyToSend,
                    isUploading = isUploading,
                    modifier = Modifier.fillMaxSize()
                )

                Button(
                    interactionSource = recordInteraction,
                    onClick = {
                        if (uiState.isRecording) {
                            val errorMessage = recorderViewModel.stopRecording(context)
                            if (errorMessage != null) {
                                Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            if (hasMicrophonePermission(context)) {
                                val errorMessage = recorderViewModel.startRecording(context)
                                if (errorMessage != null) {
                                    Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            }
                        }
                    },
                    enabled = !isUploading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (uiState.isRecording) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.primary
                        }
                    ),
                    modifier = Modifier
                        .then(pressFeedbackModifier(recordInteraction))
                        .size(176.dp)
                ) {
                    Text(
                        text = if (uiState.isRecording) "Stop" else "Grasp",
                        fontSize = 22.sp
                    )
                }
            }

            RecorderStatusBar(
                statusLabel = statusLabel,
                statusVariant = statusVariant,
                stageLine = if (uiState.isProcessing) stageLine else null
            )

            AnimatedVisibility(
                visible = uiState.isReadyToSend,
                enter = fadeIn(animationSpec = tween(160)),
                exit = fadeOut(animationSpec = tween(120))
            ) {
                Button(
                    interactionSource = sendInteraction,
                    onClick = { recorderViewModel.uploadAudio() },
                    enabled = !isUploading,
                    modifier = Modifier
                        .then(pressFeedbackModifier(sendInteraction))
                        .fillMaxWidth()
                        .height(52.dp)
                        .padding(top = 18.dp)
                ) {
                    Text(text = "Send Grasp")
                }
            }

            AnimatedVisibility(
                visible = isUploading,
                enter = fadeIn(animationSpec = tween(160)),
                exit = fadeOut(animationSpec = tween(120))
            ) {
                Text(
                    text = "Uploading to intelligence pipeline...",
                    modifier = Modifier.padding(top = 16.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        }
    }
}

@Composable
private fun CaptureStateBar(
    isRecording: Boolean,
    isReady: Boolean,
    isUploading: Boolean,
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "captureBarSweep")
    val sweepProgress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = if (isRecording) 1450 else 2200,
                easing = LinearEasing
            )
        ),
        label = "captureBarProgress"
    )
    val targetFill = when {
        isUploading -> 0.82f
        isReady -> 1f
        isRecording -> 0.68f
        else -> 0.12f
    }
    val fillFraction by animateFloatAsState(
        targetValue = targetFill,
        animationSpec = tween(360),
        label = "captureBarFill"
    )
    val shape = RoundedCornerShape(999.dp)
    val lime = MaterialTheme.colorScheme.primary
    val accent = MaterialTheme.colorScheme.secondary

    BoxWithConstraints(
        modifier = modifier
            .height(14.dp)
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.52f))
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.48f),
                shape = shape
            )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(fillFraction.coerceIn(0f, 1f))
                .fillMaxHeight()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            lime.copy(alpha = 0.72f),
                            accent.copy(alpha = if (isRecording) 0.64f else 0.48f)
                        )
                    )
                )
        )

        if (isRecording || isUploading) {
            val bandWidth = maxWidth * 0.34f
            val bandOffsetX = ((maxWidth + bandWidth) * sweepProgress) - bandWidth
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .offset(x = bandOffsetX)
                    .fillMaxWidth(0.34f)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.White.copy(alpha = if (isRecording) 0.3f else 0.2f),
                                Color.Transparent
                            )
                        )
                    )
            )
        }
    }
}

private fun hasMicrophonePermission(context: android.content.Context): Boolean {
    return ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.RECORD_AUDIO
    ) == PackageManager.PERMISSION_GRANTED
}

@Composable
private fun RecorderStatusBar(
    statusLabel: String,
    statusVariant: StatusChipVariant,
    stageLine: String?
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        StatusChip(
            label = statusLabel,
            variant = statusVariant
        )
        AnimatedVisibility(
            visible = !stageLine.isNullOrBlank(),
            enter = fadeIn(animationSpec = tween(140)),
            exit = fadeOut(animationSpec = tween(100))
        ) {
            Text(
                text = stageLine.orEmpty(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ProcessingStageDots(
    stageStatuses: Map<String, String>,
    currentStage: String?,
    overallStatus: String?,
    modifier: Modifier = Modifier
) {
    val completedColor = MaterialTheme.colorScheme.primary
    val runningColor = MaterialTheme.colorScheme.error
    val inactiveColor = MaterialTheme.colorScheme.onSurfaceVariant
    val transition = rememberInfiniteTransition(label = "processingDotPulse")
    val pulse by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1100, easing = LinearEasing)
        ),
        label = "processingDotPulseProgress"
    )

    Canvas(modifier = modifier) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val ringRadius = size.minDimension * 0.47f
        val dotBaseRadius = size.minDimension * 0.02f
        val normalizedOverall = normalizeStageStatus(overallStatus)

        PROCESSING_STAGE_ORDER.forEachIndexed { index, stage ->
            val stageState = resolveDotState(
                stage = stage,
                stageStatuses = stageStatuses,
                currentStage = currentStage,
                overallStatus = normalizedOverall
            )
            val angle = (-PI / 2.0) + ((2.0 * PI) * (index / PROCESSING_STAGE_ORDER.size.toDouble()))
            val dotCenter = Offset(
                x = center.x + (ringRadius * cos(angle)).toFloat(),
                y = center.y + (ringRadius * sin(angle)).toFloat()
            )

            val (dotColor, baseAlpha) = when (stageState) {
                DotState.Inactive -> inactiveColor to 0.3f
                DotState.Running -> runningColor to (0.78f + (0.22f * pulse))
                DotState.Completed -> completedColor to 0.92f
                DotState.Failed -> runningColor to 0.95f
            }

            val dotRadius = when (stageState) {
                DotState.Running -> dotBaseRadius + (dotBaseRadius * 0.26f * pulse)
                else -> dotBaseRadius
            }

            drawCircle(
                color = dotColor.copy(alpha = baseAlpha),
                radius = dotRadius,
                center = dotCenter
            )

            if (stageState == DotState.Running) {
                drawCircle(
                    color = dotColor.copy(alpha = 0.25f * (1f - pulse)),
                    radius = dotRadius + (dotBaseRadius * 0.9f * pulse),
                    center = dotCenter,
                    style = Stroke(width = 2f)
                )
            }
        }
    }
}

private fun resolveCurrentStage(
    stageStatuses: Map<String, String>,
    currentStage: String?
): String? {
    val normalizedCurrent = currentStage?.trim()?.lowercase()
    if (!normalizedCurrent.isNullOrBlank()) return normalizedCurrent
    return stageStatuses.entries.firstOrNull { entry ->
        normalizeStageStatus(entry.value) == "running"
    }?.key
}

private fun resolveDotState(
    stage: String,
    stageStatuses: Map<String, String>,
    currentStage: String?,
    overallStatus: String?
): DotState {
    val stageStatus = normalizeStageStatus(stageStatuses[stage])
    return when {
        stageStatus == "failed" -> DotState.Failed
        stageStatus == "completed" -> DotState.Completed
        stageStatus == "running" -> DotState.Running
        overallStatus == "completed" -> DotState.Completed
        overallStatus == "failed" && currentStage == stage -> DotState.Failed
        currentStage == stage && overallStatus == "processing" -> DotState.Running
        else -> DotState.Inactive
    }
}

private fun normalizeStageStatus(raw: String?): String {
    val normalized = raw?.trim()?.lowercase().orEmpty()
    return when {
        normalized.contains("complete") ||
            normalized == "processed" ||
            normalized == "done" ||
            normalized == "success" -> "completed"
        normalized.contains("fail") ||
            normalized.contains("error") -> "failed"
        normalized.contains("run") ||
            normalized.contains("process") ||
            normalized.contains("queue") ||
            normalized.contains("progress") ||
            normalized.contains("start") -> "running"
        else -> normalized
    }
}

private fun formatStageLabel(stage: String): String {
    return stage
        .split('_')
        .filter { it.isNotBlank() }
        .joinToString(separator = " ") { token ->
            token.replaceFirstChar { it.uppercase() }
        }
}

private enum class DotState {
    Inactive,
    Running,
    Completed,
    Failed
}

private val PROCESSING_STAGE_ORDER = listOf(
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

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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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

@Composable
fun RecorderScreen(
    modifier: Modifier = Modifier,
    recorderViewModel: RecorderViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by recorderViewModel.uiState.collectAsState()
    val isUploading = uiState.uploadStatus == UploadStatus.UPLOADING
    val statusLabel = when {
        isUploading -> "Uploading"
        uiState.isRecording -> "Recording"
        uiState.isReadyToSend -> "Ready"
        else -> "Idle"
    }
    val statusVariant = when {
        isUploading -> StatusChipVariant.Warning
        uiState.isRecording -> StatusChipVariant.Error
        uiState.isReadyToSend -> StatusChipVariant.Success
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

            StatusChip(
                label = statusLabel,
                variant = statusVariant
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

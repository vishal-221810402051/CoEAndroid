package com.coe.mobile.ui.screens.recorder

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateFloat
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.Brush
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.util.lerp
import kotlin.math.PI
import kotlin.math.sin
import kotlin.math.cos

@Composable
fun RecorderMotionField(
    isRecording: Boolean,
    isReady: Boolean,
    isUploading: Boolean,
    modifier: Modifier = Modifier
) {
    val targetProfile = when {
        isUploading -> MotionProfile(amplitude = 0.22f, speed = 0.75f, alpha = 0.2f)
        isRecording -> MotionProfile(amplitude = 0.7f, speed = 1.1f, alpha = 0.38f)
        isReady -> MotionProfile(amplitude = 0.35f, speed = 0.6f, alpha = 0.24f)
        else -> MotionProfile(amplitude = 0.16f, speed = 0.42f, alpha = 0.14f)
    }

    val amplitude by animateFloatAsState(
        targetValue = targetProfile.amplitude,
        animationSpec = tween(durationMillis = 500),
        label = "motionAmplitude"
    )
    val speed by animateFloatAsState(
        targetValue = targetProfile.speed,
        animationSpec = tween(durationMillis = 500),
        label = "motionSpeed"
    )
    val alpha by animateFloatAsState(
        targetValue = targetProfile.alpha,
        animationSpec = tween(durationMillis = 500),
        label = "motionAlpha"
    )

    val transition = rememberInfiniteTransition(label = "recorderMotion")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = (PI * 2).toFloat(),
        animationSpec = infiniteRepeatable(animation = tween(durationMillis = 2800)),
        label = "motionProgress"
    )

    val blobPath = remember { Path() }
    val wavePath = remember { Path() }
    val density = LocalDensity.current
    val ringStrokeIdle = 2.2f * density.density
    val ringStrokeActive = 3.6f * density.density
    val primary = MaterialTheme.colorScheme.primary
    val secondary = MaterialTheme.colorScheme.secondary

    Canvas(modifier = modifier.fillMaxSize()) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val minSide = size.minDimension
        val phase = progress * speed

        val ringStroke = lerp(ringStrokeIdle, ringStrokeActive, if (isRecording) 1f else 0f)
        val ringRadiusBase = minSide * 0.36f
        val ringRadius = ringRadiusBase + minSide * 0.03f * sin(phase * 1.15f)
        val ringColor = if (isRecording) {
            secondary.copy(alpha = alpha * 0.6f)
        } else {
            primary.copy(alpha = alpha * 0.65f)
        }

        drawCircle(
            color = ringColor,
            radius = ringRadius,
            center = center,
            style = Stroke(width = ringStroke)
        )
        drawCircle(
            color = primary.copy(alpha = alpha * 0.28f),
            radius = ringRadius + minSide * 0.035f * sin(phase * 0.9f + 0.9f),
            center = center,
            style = Stroke(width = ringStroke * 0.72f)
        )

        blobPath.reset()
        val points = 10
        val blobBase = minSide * 0.29f
        for (i in 0 until points) {
            val angle = ((2f * PI) / points.toFloat()) * i.toFloat()
            val wobbleA = sin(phase + i * 0.63f)
            val wobbleB = sin(phase * 0.7f + i * 1.12f)
            val radius = blobBase + (minSide * 0.045f * amplitude) * (0.65f * wobbleA + 0.35f * wobbleB)
            val x = center.x + radius * cos(angle).toFloat()
            val y = center.y + radius * sin(angle).toFloat()
            if (i == 0) {
                blobPath.moveTo(x, y)
            } else {
                blobPath.lineTo(x, y)
            }
        }
        blobPath.close()

        drawPath(
            path = blobPath,
            brush = Brush.radialGradient(
                colors = listOf(
                    primary.copy(alpha = alpha * 0.24f),
                    secondary.copy(alpha = alpha * 0.12f),
                    primary.copy(alpha = 0f)
                ),
                center = center,
                radius = minSide * 0.42f
            )
        )

        wavePath.reset()
        val baselineY = center.y + minSide * 0.28f
        val waveAmplitude = minSide * 0.03f * amplitude
        val segments = 40
        for (step in 0..segments) {
            val x = size.width * (step / segments.toFloat())
            val xPhase = (step / segments.toFloat()) * (PI * 2).toFloat()
            val y = baselineY +
                waveAmplitude * sin(phase * 1.4f + xPhase * 2.2f) +
                waveAmplitude * 0.45f * sin(phase * 0.9f + xPhase * 4.4f + 0.8f)
            if (step == 0) {
                wavePath.moveTo(x, y)
            } else {
                wavePath.lineTo(x, y)
            }
        }

        drawPath(
            path = wavePath,
            color = primary.copy(alpha = alpha * 0.38f),
            style = Stroke(width = 2f, cap = StrokeCap.Round)
        )
    }
}

private data class MotionProfile(
    val amplitude: Float,
    val speed: Float,
    val alpha: Float
)

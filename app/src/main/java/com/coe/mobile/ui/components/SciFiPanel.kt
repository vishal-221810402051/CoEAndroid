package com.coe.mobile.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween

@Composable
fun SciFiPanel(
    modifier: Modifier = Modifier,
    cornerAccents: Boolean = true,
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val panelShape = RoundedCornerShape(18.dp)
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed && onClick != null) 0.992f else 1f,
        animationSpec = tween(120),
        label = "panelPressScale"
    )
    val panelModifier = if (onClick != null) {
        modifier
            .fillMaxWidth()
            .clip(panelShape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
    } else {
        modifier
            .fillMaxWidth()
            .clip(panelShape)
    }

    Box(
        modifier = panelModifier
            .graphicsLayer(
                scaleX = pressScale,
                scaleY = pressScale
            )
            .shadow(
                elevation = 5.dp,
                shape = panelShape,
                ambientColor = Color.Black.copy(alpha = 0.32f),
                spotColor = Color.Black.copy(alpha = 0.24f)
            )
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.88f),
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.76f)
                    )
                )
            )
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.34f),
                shape = panelShape
            )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.secondary.copy(alpha = 0.06f),
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.03f),
                            Color.Transparent
                        )
                    )
                )
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(1.dp)
                .border(
                    width = 1.dp,
                    color = Color.White.copy(alpha = 0.045f),
                    shape = panelShape
                )
        )
        if (cornerAccents) {
            CornerAccent(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 10.dp, top = 10.dp)
            )
            CornerAccent(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(end = 10.dp, top = 10.dp)
            )
            CornerAccent(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 10.dp, bottom = 10.dp)
            )
            CornerAccent(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 10.dp, bottom = 10.dp)
            )
        }
        Column(
            modifier = Modifier.padding(contentPadding),
            content = content
        )
    }
}

@Composable
private fun CornerAccent(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(width = 12.dp, height = 2.dp)
            .background(
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.46f),
                shape = RoundedCornerShape(999.dp)
            )
    )
}

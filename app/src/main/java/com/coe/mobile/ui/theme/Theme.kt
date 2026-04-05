package com.coe.mobile.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = CoePrimary,
    secondary = CoeSecondary,
    tertiary = CoePrimarySoft,
    error = CoeError,
    background = CoeBackgroundDark,
    surface = CoeTopBarDark,
    surfaceVariant = CoePanelBase,
    primaryContainer = CoePrimary.copy(alpha = 0.16f),
    secondaryContainer = CoePanelOverlay,
    onBackground = CoeTextPrimaryDark,
    onSurface = CoeTextPrimaryDark,
    onSurfaceVariant = CoeTextSecondaryDark,
    outline = CoeTextMutedDark.copy(alpha = 0.46f)
)

private val LightColorScheme = lightColorScheme(
    primary = CoePrimary,
    secondary = CoeSecondary,
    tertiary = CoePrimarySoft,
    error = CoeError
)

@Composable
fun CoEMobileTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        DarkColorScheme
    } else {
        LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = CoeShapes,
        content = content
    )
}

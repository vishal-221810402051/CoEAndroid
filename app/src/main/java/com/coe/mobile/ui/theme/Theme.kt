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
    onBackground = CoeTextPrimaryDark,
    onSurface = CoeTextPrimaryDark,
    onSurfaceVariant = CoeTextSecondaryDark,
    outline = CoePrimarySoft.copy(alpha = 0.34f)
)

private val LightColorScheme = lightColorScheme(
    primary = CoePrimary,
    secondary = CoeSecondary,
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

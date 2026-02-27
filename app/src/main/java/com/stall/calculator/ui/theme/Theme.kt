package com.stall.calculator.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = PrimaryBlue,
    secondary = AccentGreen,
    tertiary = WarnOrange
)

private val DarkColors = darkColorScheme(
    primary = PrimaryBlue,
    secondary = AccentGreen,
    tertiary = WarnOrange
)

@Composable
fun CalculatorTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = Typography,
        content = content
    )
}

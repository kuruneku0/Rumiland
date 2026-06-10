package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = EmeraldAccent,
    onPrimary = Color.White,
    secondary = LightGreenProgress,
    onSecondary = Color.White,
    background = DarkBg,
    onBackground = Color.White,
    surface = ForestGreenSurface,
    onSurface = Color.White,
    error = WarningOrange,
    onError = Color.Black
)

private val LightColorScheme = lightColorScheme(
    primary = EmeraldAccent,
    onPrimary = Color.White,
    secondary = LightGreenProgress,
    onSecondary = Color.White,
    background = LightBg,
    onBackground = LightTextPrimary,
    surface = LightSurface,
    onSurface = LightTextPrimary,
    error = WarningOrange,
    onError = Color.White
)

@Composable
fun RumilandAcademyTheme(
    darkTheme: Boolean = true, // Default theme is dark as requested
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

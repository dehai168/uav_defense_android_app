package com.ai.nativevoicetranslation.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = PrimaryBlue,
    secondary = AccentOrange
)

private val DarkColors = darkColorScheme(
    primary = PrimaryBlue,
    secondary = AccentOrange
)

@Composable
fun AiNativeVoiceTranslationTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (androidx.compose.foundation.isSystemInDarkTheme()) DarkColors else LightColors,
        typography = androidx.compose.material3.Typography(),
        content = content
    )
}

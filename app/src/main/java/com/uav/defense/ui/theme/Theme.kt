package com.uav.defense.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val Scheme = darkColorScheme(
    primary = AccentCyan,
    background = BackgroundDark,
    surface = PanelBg,
    onBackground = TextMain,
    onSurface = TextMain
)

@Composable
fun UavDefenseTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = Scheme, content = content)
}

package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val MicroColorScheme = lightColorScheme(
    primary = MicroPrimary,
    secondary = MicroSecondary,
    tertiary = MicroTertiary,
    background = MicroBackground,
    surface = White,
    onPrimary = White,
    onSecondary = White,
    onBackground = TextDark,
    onSurface = TextDark
)

private val MacroColorScheme = lightColorScheme(
    primary = MacroPrimary,
    secondary = MacroSecondary,
    tertiary = MacroTertiary,
    background = MacroBackground,
    surface = White,
    onPrimary = White,
    onSecondary = White,
    onBackground = TextDark,
    onSurface = TextDark
)

@Composable
fun AgrinexusTheme(
    isMacro: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (isMacro) MacroColorScheme else MicroColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

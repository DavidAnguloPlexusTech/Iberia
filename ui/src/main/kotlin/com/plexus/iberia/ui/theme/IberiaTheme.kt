package com.plexus.iberia.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = AtomicOrange,
    onPrimary = Cream,
    secondary = Teal,
    onSecondary = Cream,
    tertiary = Mustard,
    onTertiary = Ink,
    background = Cream,
    onBackground = Ink,
    surface = Cream,
    onSurface = Ink,
    surfaceVariant = CreamDark,
    onSurfaceVariant = InkSoft,
    surfaceContainerHighest = CreamDark,
    outline = Ink,
    outlineVariant = InkDivider,
)

private val DarkColors = darkColorScheme(
    primary = AtomicOrangeLight,
    onPrimary = NightBlue,
    secondary = TealLight,
    onSecondary = NightBlue,
    tertiary = MustardLight,
    onTertiary = NightBlue,
    background = NightBlue,
    onBackground = Cream,
    surface = NightBlue,
    onSurface = Cream,
    surfaceVariant = NightBlueLight,
    onSurfaceVariant = CreamSoft,
    surfaceContainerHighest = NightBlueLight,
    outline = CreamSoft,
    outlineVariant = CreamDivider,
)

@Composable
fun IberiaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = colorSchemeFor(darkTheme),
        typography = IberiaTypography,
        shapes = IberiaShapes,
        content = content,
    )
}

private fun colorSchemeFor(darkTheme: Boolean): ColorScheme =
    if (darkTheme) DarkColors else LightColors

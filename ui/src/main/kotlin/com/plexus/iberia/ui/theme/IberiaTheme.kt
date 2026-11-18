package com.plexus.iberia.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(primary = Purple40, secondary = PurpleGrey40)
private val DarkColors = darkColorScheme(primary = Purple80, secondary = PurpleGrey80)

@Composable
fun IberiaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(colorScheme = colorSchemeFor(darkTheme), content = content)
}

private fun colorSchemeFor(darkTheme: Boolean): ColorScheme =
    if (darkTheme) DarkColors else LightColors

package com.plexus.iberia.ui.common

import androidx.compose.foundation.border
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

private val FOCUS_BORDER_WIDTH = 2.dp

/**
 * Clear outline on the focused element, so people moving with a switch, keyboard or D-pad
 * always see where they are. The default focus highlight is too faint on this palette.
 */
internal fun Modifier.focusBorder(isFocused: Boolean, color: Color, shape: Shape): Modifier =
    if (isFocused) border(width = FOCUS_BORDER_WIDTH, color = color, shape = shape) else this

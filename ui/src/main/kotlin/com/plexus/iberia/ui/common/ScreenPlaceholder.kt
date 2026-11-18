package com.plexus.iberia.ui.common

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

/** Temporary content for screens that are not implemented yet. */
@Composable
internal fun ScreenPlaceholder(title: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize().safeDrawingPadding(),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = title, style = MaterialTheme.typography.headlineMedium)
    }
}

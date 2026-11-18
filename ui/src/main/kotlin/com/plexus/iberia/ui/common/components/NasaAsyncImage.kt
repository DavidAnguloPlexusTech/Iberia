package com.plexus.iberia.ui.common.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage

/** Remote image with a neutral placeholder while it loads or when it fails. */
@Composable
internal fun NasaAsyncImage(
    url: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
) {
    val placeholder = ColorPainter(MaterialTheme.colorScheme.surfaceVariant)
    AsyncImage(
        model = url,
        contentDescription = contentDescription,
        modifier = modifier,
        contentScale = contentScale,
        placeholder = placeholder,
        error = placeholder,
        fallback = placeholder,
    )
}

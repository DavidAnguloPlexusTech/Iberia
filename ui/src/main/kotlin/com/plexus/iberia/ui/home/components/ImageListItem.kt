package com.plexus.iberia.ui.home.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.plexus.iberia.domain.model.NasaImage
import com.plexus.iberia.ui.common.PreviewData
import com.plexus.iberia.ui.common.components.NasaAsyncImage
import com.plexus.iberia.ui.common.toDisplayDate
import com.plexus.iberia.ui.theme.IberiaTheme

/** One search result: thumbnail, title and where/when it was taken. */
@Composable
internal fun ImageListItem(
    image: NasaImage,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        NasaAsyncImage(
            url = image.thumbnailUrl,
            contentDescription = null,
            modifier = Modifier
                .size(56.dp)
                .clip(MaterialTheme.shapes.small),
        )
        ImageListItemTexts(image = image)
    }
}

@Composable
private fun ImageListItemTexts(image: NasaImage) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = image.title,
            style = MaterialTheme.typography.titleMedium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = listOf(image.dateCreated.toDisplayDate(), image.center).filter { it.isNotBlank() }.joinToString(" · "),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ImageListItemPreview() {
    IberiaTheme { ImageListItem(image = PreviewData.image, onClick = {}) }
}

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewFontScale
import androidx.compose.ui.unit.dp
import com.plexus.iberia.domain.model.NasaImage
import com.plexus.iberia.ui.common.PreviewData
import com.plexus.iberia.ui.common.components.NasaAsyncImage
import com.plexus.iberia.ui.R
import com.plexus.iberia.ui.common.toAccessibleDate
import com.plexus.iberia.ui.common.toDisplayDate
import com.plexus.iberia.ui.theme.IberiaTheme

/** One search result: thumbnail, title and where/when it was taken. */
@Composable
internal fun ImageListItem(
    image: NasaImage,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val description = image.accessibleDescription()
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClickLabel = stringResource(R.string.open_details), role = Role.Button, onClick = onClick)
            // The whole row is read as one item, with the date said in words instead of digits.
            .clearAndSetSemantics { contentDescription = description }
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
        ImageListItemTexts(image = image, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun NasaImage.accessibleDescription(): String = listOfNotNull(
    title,
    dateCreated.takeIf { it.isNotBlank() }?.let { stringResource(R.string.image_item_date, it.toAccessibleDate()) },
    center.takeIf { it.isNotBlank() }?.let { stringResource(R.string.image_item_center, it) },
).joinToString(separator = ". ")

@Composable
private fun ImageListItemTexts(image: NasaImage, modifier: Modifier = Modifier) {
    // No max lines: with large fonts the title wraps instead of being cut.
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = image.title,
            style = MaterialTheme.typography.titleMedium,
        )
        Text(
            text = listOf(image.dateCreated.toDisplayDate(), image.center).filter { it.isNotBlank() }.joinToString(" · "),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@PreviewFontScale
@Preview(showBackground = true)
@Composable
private fun ImageListItemPreview() {
    IberiaTheme { ImageListItem(image = PreviewData.image, onClick = {}) }
}

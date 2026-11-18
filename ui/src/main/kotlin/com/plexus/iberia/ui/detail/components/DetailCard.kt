package com.plexus.iberia.ui.detail.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewFontScale
import androidx.compose.ui.unit.dp
import com.plexus.iberia.domain.model.NasaImage
import com.plexus.iberia.ui.R
import com.plexus.iberia.ui.common.PreviewData
import com.plexus.iberia.ui.common.components.NasaAsyncImage
import com.plexus.iberia.ui.common.toAccessibleDate
import com.plexus.iberia.ui.common.toDisplayDate
import com.plexus.iberia.ui.theme.IberiaTheme

private const val IMAGE_ASPECT_RATIO = 4f / 3f

/** Photo, title, metadata and description of an image. */
@Composable
internal fun DetailCard(image: NasaImage, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Decorative for TalkBack: the title and the description right after it describe the photo.
        NasaAsyncImage(
            url = image.imageUrl,
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(IMAGE_ASPECT_RATIO)
                .clip(MaterialTheme.shapes.small),
        )
        Text(
            text = image.title,
            modifier = Modifier.semantics { heading() },
            style = MaterialTheme.typography.headlineSmall,
        )
        DetailInfo(image = image)
        Text(text = image.description, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun DetailInfo(image: NasaImage) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        DetailInfoRow(
            label = stringResource(R.string.detail_date),
            value = image.dateCreated.toDisplayDate(),
            accessibleValue = image.dateCreated.toAccessibleDate(),
        )
        DetailInfoRow(label = stringResource(R.string.detail_center), value = image.center)
        image.photographer?.let {
            DetailInfoRow(label = stringResource(R.string.detail_photographer), value = it)
        }
    }
}

@PreviewFontScale
@Preview(showBackground = true)
@Composable
private fun DetailCardPreview() {
    IberiaTheme { DetailCard(image = PreviewData.image) }
}

package com.plexus.iberia.data.mapper

import com.plexus.iberia.data.remote.dto.ItemDto
import com.plexus.iberia.domain.model.NasaImage

/** Returns null when the item has no id: without it the image cannot be identified or opened. */
internal fun ItemDto.toNasaImage(): NasaImage? {
    val itemData = data?.firstOrNull() ?: return null
    val id = itemData.nasaId ?: return null
    val imageLinks = links.orEmpty()
    return NasaImage(
        id = id,
        title = itemData.title.orEmpty(),
        description = itemData.description.orEmpty(),
        thumbnailUrl = imageLinks.thumbnailUrl(),
        imageUrl = imageLinks.largestImageUrl(),
        dateCreated = itemData.dateCreated.orEmpty(),
        center = itemData.center.orEmpty(),
        keywords = itemData.keywords.orEmpty(),
        photographer = itemData.photographer,
    )
}

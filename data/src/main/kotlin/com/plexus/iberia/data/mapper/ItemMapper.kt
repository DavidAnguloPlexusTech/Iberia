package com.plexus.iberia.data.mapper

import com.plexus.iberia.data.remote.dto.ItemDto
import com.plexus.iberia.domain.model.NasaImage

internal fun ItemDto.toNasaImage(): NasaImage? {
    val itemData = data?.firstOrNull() ?: return null
    val id = itemData.nasaId ?: return null
    val imageLinks = links.orEmpty()
    val description = itemData.description.orEmpty().withoutHtml()
    return NasaImage(
        id = id,
        title = completeTitle(title = itemData.title.orEmpty().withoutHtml(), description = description),
        description = description,
        thumbnailUrl = imageLinks.thumbnailUrl(),
        imageUrl = imageLinks.largestImageUrl(),
        dateCreated = itemData.dateCreated.orEmpty(),
        center = itemData.center.orEmpty(),
        keywords = itemData.keywords.orEmpty(),
        photographer = itemData.photographer,
    )
}

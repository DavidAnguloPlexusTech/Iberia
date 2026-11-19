package com.plexus.iberia.ui

import com.plexus.iberia.domain.model.NasaImage
import com.plexus.iberia.domain.model.SearchPage

fun image(id: String) = NasaImage(
    id = id,
    title = "Title $id",
    description = "",
    thumbnailUrl = null,
    imageUrl = null,
    dateCreated = "",
    center = "",
    keywords = emptyList(),
    photographer = null,
)

fun searchPage(page: Int, hasNextPage: Boolean = true) =
    SearchPage(images = listOf(image("p$page-a"), image("p$page-b")), hasNextPage = hasNextPage)

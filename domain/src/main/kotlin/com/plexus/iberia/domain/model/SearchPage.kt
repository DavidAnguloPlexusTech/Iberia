package com.plexus.iberia.domain.model

data class SearchPage(
    val images: List<NasaImage>,
    val hasNextPage: Boolean,
)

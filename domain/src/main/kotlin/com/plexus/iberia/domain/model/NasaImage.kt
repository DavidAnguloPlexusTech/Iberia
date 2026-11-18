package com.plexus.iberia.domain.model

data class NasaImage(
    val id: String,
    val title: String,
    val description: String,
    val thumbnailUrl: String?,
    val imageUrl: String?,
    val dateCreated: String,
    val center: String,
    val keywords: List<String>,
    val photographer: String?,
)

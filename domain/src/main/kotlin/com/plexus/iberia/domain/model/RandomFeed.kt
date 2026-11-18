package com.plexus.iberia.domain.model

/** Topics in a random order and the API page they start at; kept to request the following pages. */
data class RandomFeed(
    val topics: List<String>,
    val firstPage: Int,
)

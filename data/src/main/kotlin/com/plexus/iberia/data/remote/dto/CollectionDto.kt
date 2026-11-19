package com.plexus.iberia.data.remote.dto

import com.google.gson.annotations.SerializedName

internal data class CollectionDto(
    @SerializedName("items") val items: List<ItemDto>?,
    @SerializedName("links") val links: List<PageLinkDto>?,
)

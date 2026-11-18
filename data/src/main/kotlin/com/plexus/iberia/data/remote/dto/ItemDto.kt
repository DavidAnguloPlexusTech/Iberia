package com.plexus.iberia.data.remote.dto

import com.google.gson.annotations.SerializedName

internal data class ItemDto(
    @SerializedName("data") val data: List<ItemDataDto>?,
    @SerializedName("links") val links: List<ImageLinkDto>?,
)

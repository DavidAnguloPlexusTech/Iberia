package com.plexus.iberia.data.remote.dto

import com.google.gson.annotations.SerializedName

internal data class SearchResponseDto(
    @SerializedName("collection") val collection: CollectionDto?,
)

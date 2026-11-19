package com.plexus.iberia.data.remote.dto

import com.google.gson.annotations.SerializedName

internal data class ImageLinkDto(
    @SerializedName("href") val href: String?,
    @SerializedName("rel") val rel: String?,
    @SerializedName("width") val width: Int?,
)

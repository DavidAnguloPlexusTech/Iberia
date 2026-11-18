package com.plexus.iberia.data.remote.dto

import com.google.gson.annotations.SerializedName

internal data class PageLinkDto(
    @SerializedName("rel") val rel: String?,
)

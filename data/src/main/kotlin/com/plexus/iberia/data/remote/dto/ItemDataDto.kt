package com.plexus.iberia.data.remote.dto

import com.google.gson.annotations.SerializedName

internal data class ItemDataDto(
    @SerializedName("nasa_id") val nasaId: String?,
    @SerializedName("title") val title: String?,
    @SerializedName("description") val description: String?,
    @SerializedName("date_created") val dateCreated: String?,
    @SerializedName("center") val center: String?,
    @SerializedName("keywords") val keywords: List<String>?,
    @SerializedName("photographer") val photographer: String?,
)

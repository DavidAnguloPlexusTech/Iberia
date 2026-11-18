package com.plexus.iberia.data.mapper

import com.plexus.iberia.data.remote.dto.ImageLinkDto

private const val REL_PREVIEW = "preview"
private const val REL_ALTERNATE = "alternate"

internal fun List<ImageLinkDto>.thumbnailUrl(): String? =
    firstOrNull { it.rel == REL_PREVIEW }?.href

internal fun List<ImageLinkDto>.largestImageUrl(): String? =
    filter { it.rel == REL_ALTERNATE }
        .maxByOrNull { it.width ?: 0 }
        ?.href
        ?: thumbnailUrl()

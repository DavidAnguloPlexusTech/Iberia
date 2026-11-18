package com.plexus.iberia.data.mapper

import com.plexus.iberia.data.remote.dto.PageLinkDto
import com.plexus.iberia.data.remote.dto.SearchResponseDto
import com.plexus.iberia.domain.model.SearchPage

private const val REL_NEXT = "next"

internal fun SearchResponseDto.toSearchPage(): SearchPage = SearchPage(
    images = collection?.items.orEmpty().mapNotNull { it.toNasaImage() },
    hasNextPage = collection?.links.orEmpty().hasNext(),
)

private fun List<PageLinkDto>.hasNext(): Boolean = any { it.rel == REL_NEXT }

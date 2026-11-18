package com.plexus.iberia.ui

import com.plexus.iberia.domain.model.NasaImage
import com.plexus.iberia.domain.model.SearchPage
import com.plexus.iberia.domain.repository.NasaImageRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlin.time.Duration

class FakeNasaImageRepository : NasaImageRepository {

    val searches = mutableListOf<Pair<String, Int>>()
    var searchResults: (page: Int) -> Result<SearchPage> = { Result.success(searchPage(it)) }
    var imageResult: Result<NasaImage> = Result.success(image("id"))
    var responseDelay: Duration = Duration.ZERO

    override fun search(query: String, page: Int): Flow<Result<SearchPage>> = flow {
        searches += query to page
        delay(responseDelay)
        emit(searchResults(page))
    }

    override fun getImage(id: String): Flow<Result<NasaImage>> = flow {
        delay(responseDelay)
        emit(imageResult)
    }
}

fun image(id: String) = NasaImage(
    id = id,
    title = "Title $id",
    description = "",
    thumbnailUrl = null,
    imageUrl = null,
    dateCreated = "",
    center = "",
    keywords = emptyList(),
    photographer = null,
)

fun searchPage(page: Int, hasNextPage: Boolean = true) =
    SearchPage(images = listOf(image("p$page-a"), image("p$page-b")), hasNextPage = hasNextPage)

package com.plexus.iberia.domain.usecase

import com.plexus.iberia.domain.model.RandomFeed
import com.plexus.iberia.domain.model.SearchPage
import com.plexus.iberia.domain.repository.NasaImageRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.random.Random

class GetRandomImagesUseCase(
    private val repository: NasaImageRepository,
    private val random: Random = Random.Default,
) {

    fun newFeed(): RandomFeed = RandomFeed(
        topics = TOPICS.shuffled(random),
        firstPage = random.nextInt(from = 1, until = MAX_FIRST_PAGE + 1),
    )

    operator fun invoke(feed: RandomFeed, page: Int): Flow<Result<SearchPage>> {
        val index = page - 1
        val topic = feed.topics[index % feed.topics.size]
        val apiPage = feed.firstPage + index / feed.topics.size
        return repository.search(query = topic, page = apiPage).map { result ->
            result.map { it.copy(hasNextPage = true) }
        }
    }

    companion object {
        val TOPICS = listOf(
            "galaxy", "nebula", "mars", "moon", "earth", "apollo", "hubble", "saturn",
            "jupiter", "shuttle", "astronaut", "rocket", "aircraft", "sun", "iss",
        )
        const val MAX_FIRST_PAGE = 20
    }
}

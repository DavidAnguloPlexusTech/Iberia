package com.plexus.iberia.data.cache

import com.plexus.iberia.domain.model.NasaImage
import com.plexus.iberia.domain.model.SearchPage
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.TimeMark
import kotlin.time.TimeSource

internal class SearchCache(
    private val timeToLive: Duration = DEFAULT_TIME_TO_LIVE,
    private val timeSource: TimeSource = TimeSource.Monotonic,
    private val maxEntries: Int = DEFAULT_MAX_ENTRIES,
) {

    private val entries = object : LinkedHashMap<Key, Entry>(INITIAL_CAPACITY, LOAD_FACTOR, ACCESS_ORDER) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<Key, Entry>?): Boolean = size > maxEntries
    }

    @Synchronized
    fun get(query: String, page: Int): SearchPage? {
        val key = Key.of(query, page)
        val entry = entries[key] ?: return null
        if (entry.isExpired()) {
            entries.remove(key)
            return null
        }
        return entry.searchPage
    }

    @Synchronized
    fun put(query: String, page: Int, searchPage: SearchPage) {
        entries[Key.of(query, page)] = Entry(searchPage, timeSource.markNow())
    }

    @Synchronized
    fun findImage(id: String): NasaImage? =
        entries.values
            .filterNot { it.isExpired() }
            .firstNotNullOfOrNull { entry -> entry.searchPage.images.firstOrNull { it.id == id } }

    private fun Entry.isExpired(): Boolean = savedAt.elapsedNow() >= timeToLive

    private data class Key(val query: String, val page: Int) {
        companion object {
            fun of(query: String, page: Int) = Key(query.trim().lowercase(), page)
        }
    }

    private class Entry(val searchPage: SearchPage, val savedAt: TimeMark)

    companion object {
        val DEFAULT_TIME_TO_LIVE: Duration = 5.minutes
        const val DEFAULT_MAX_ENTRIES = 100
        private const val INITIAL_CAPACITY = 16
        private const val LOAD_FACTOR = 0.75f
        private const val ACCESS_ORDER = true
    }
}

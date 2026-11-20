package com.plexus.iberia.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.plexus.iberia.domain.error.DomainError
import com.plexus.iberia.domain.model.NasaImage
import com.plexus.iberia.domain.model.SearchPage
import com.plexus.iberia.domain.usecase.GetRandomImagesUseCase
import com.plexus.iberia.domain.usecase.SearchImagesUseCase
import com.plexus.iberia.domain.usecase.SearchImagesUseCase.Companion.FIRST_PAGE
import com.plexus.iberia.ui.common.asDomainError
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

internal data class HomeState(
    val query: String = "",
    val images: List<NasaImage> = emptyList(),
    val page: Int = NO_PAGE,
    val hasNextPage: Boolean = false,
    val isLoading: Boolean = false,
    val isLoadingNextPage: Boolean = false,
    val error: DomainError? = null,
) {
    val canLoadNextPage: Boolean
        get() = hasNextPage && !isLoading && !isLoadingNextPage && error == null

    companion object {
        const val NO_PAGE = 0
    }
}

internal sealed interface HomeEvent {
    data class NavigateToDetail(val id: String) : HomeEvent
}

internal class HomeViewModel(
    private val searchImages: SearchImagesUseCase,
    private val getRandomImages: GetRandomImagesUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(HomeState(isLoading = true))
    val state: StateFlow<HomeState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<HomeEvent>()
    val events: SharedFlow<HomeEvent> = _events.asSharedFlow()

    private val queries = MutableStateFlow("")
    private var searchedQuery: String? = null
    private var randomFeed = getRandomImages.newFeed()
    private var loadJob: Job? = null

    init {
        observeQueries()
    }

    fun onQueryChange(query: String) {
        _state.update { it.copy(query = query) }
        if (query.trim() != searchedQuery) loadJob?.cancel()
        queries.value = query
    }

    fun loadNextPage() {
        if (!_state.value.canLoadNextPage) return
        _state.update { it.copy(isLoadingNextPage = true) }
        load(page = _state.value.page + 1)
    }

    fun retry() {
        if (_state.value.images.isEmpty()) {
            searchFirstPage(searchedQuery.orEmpty())
        } else {
            _state.update { it.copy(error = null) }
            loadNextPage()
        }
    }

    fun onImageClick(id: String) {
        viewModelScope.launch { _events.emit(HomeEvent.NavigateToDetail(id)) }
    }

    @OptIn(FlowPreview::class)
    private fun observeQueries() {
        queries
            .debounce(SEARCH_DEBOUNCE)
            .map { it.trim() }
            .onEach(::onQuerySettled)
            .launchIn(viewModelScope)
    }

    private fun onQuerySettled(query: String) {
        val alreadyShown = query == searchedQuery && loadJob?.isCancelled == false
        if (!alreadyShown) searchFirstPage(query)
    }

    private fun searchFirstPage(query: String) {
        searchedQuery = query
        if (query.isEmpty()) randomFeed = getRandomImages.newFeed()
        _state.update { HomeState(query = it.query, isLoading = true) }
        load(page = FIRST_PAGE)
    }

    private fun load(page: Int) {
        loadJob?.cancel()
        loadJob = pageFlow(page)
            .onEach { result -> onPageResult(result, page) }
            .launchIn(viewModelScope)
    }

    private fun pageFlow(page: Int): Flow<Result<SearchPage>> {
        val query = searchedQuery.orEmpty()
        return if (query.isEmpty()) getRandomImages(randomFeed, page) else searchImages(query, page)
    }

    private fun onPageResult(result: Result<SearchPage>, page: Int) {
        result.fold(
            onSuccess = { searchPage -> onPageLoaded(searchPage, page) },
            onFailure = { error -> onPageFailed(error.asDomainError()) },
        )
    }

    private fun onPageLoaded(searchPage: SearchPage, page: Int) {
        _state.update {
            it.copy(
                images = if (page == FIRST_PAGE) searchPage.images else it.images.plusUnique(searchPage.images),
                page = page,
                hasNextPage = searchPage.hasNextPage,
                isLoading = false,
                isLoadingNextPage = false,
            )
        }
    }

    private fun onPageFailed(error: DomainError) {
        _state.update { it.copy(isLoading = false, isLoadingNextPage = false, error = error) }
    }

    private fun List<NasaImage>.plusUnique(newImages: List<NasaImage>): List<NasaImage> =
        (this + newImages).distinctBy { it.id }

    companion object {
        val SEARCH_DEBOUNCE: Duration = 400.milliseconds
    }
}

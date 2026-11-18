package com.plexus.iberia.ui.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.traversalIndex
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewFontScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.plexus.iberia.ui.R
import com.plexus.iberia.ui.common.PreviewData
import com.plexus.iberia.ui.common.components.EmptyMessage
import com.plexus.iberia.ui.common.components.ErrorMessage
import com.plexus.iberia.ui.common.components.LoadingAnimation
import com.plexus.iberia.ui.common.components.ScrollableCenter
import com.plexus.iberia.ui.common.toMessage
import com.plexus.iberia.ui.home.components.ImageList
import com.plexus.iberia.ui.home.components.SearchBar
import com.plexus.iberia.ui.theme.IberiaTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun HomeRoute(
    onNavigateToDetail: (String) -> Unit,
    viewModel: HomeViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(viewModel, lifecycleOwner) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            withContext(Dispatchers.Main.immediate) {
                viewModel.events.collect { event ->
                    when (event) {
                        is HomeEvent.NavigateToDetail -> onNavigateToDetail(event.id)
                    }
                }
            }
        }
    }
    HomeScreen(
        state = state,
        onQueryChange = viewModel::onQueryChange,
        onLoadNextPage = viewModel::loadNextPage,
        onRetry = viewModel::retry,
        onImageClick = viewModel::onImageClick,
    )
}

@Composable
internal fun HomeScreen(
    state: HomeState,
    onQueryChange: (String) -> Unit,
    onLoadNextPage: () -> Unit,
    onRetry: () -> Unit,
    onImageClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val screenTitle = stringResource(R.string.home_screen)
    Scaffold(
        modifier = modifier.semantics {
            paneTitle = screenTitle
            isTraversalGroup = true
        },
        topBar = {
            SearchBar(
                query = state.query,
                onQueryChange = onQueryChange,
                // Read first: the search field comes before the results.
                modifier = Modifier
                    .semantics { traversalIndex = -1f }
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            )
        },
    ) { innerPadding ->
        HomeContent(
            state = state,
            onLoadNextPage = onLoadNextPage,
            onRetry = onRetry,
            onImageClick = onImageClick,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                // Keeps messages and buttons above the keyboard while searching.
                .consumeWindowInsets(innerPadding)
                .imePadding(),
        )
    }
}

@Composable
private fun HomeContent(
    state: HomeState,
    onLoadNextPage: () -> Unit,
    onRetry: () -> Unit,
    onImageClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        when {
            state.isLoading -> LoadingAnimation()
            state.images.isNotEmpty() -> ImageList(
                images = state.images,
                isLoadingNextPage = state.isLoadingNextPage,
                error = state.error,
                onImageClick = onImageClick,
                onLoadNextPage = onLoadNextPage,
                onRetry = onRetry,
                modifier = Modifier.fillMaxSize(),
            )
            state.error != null -> ScrollableCenter {
                ErrorMessage(message = state.error.toMessage(), onRetry = onRetry)
            }
            state.query.isBlank() -> ScrollableCenter {
                EmptyMessage(
                    title = stringResource(R.string.home_empty_title),
                    message = stringResource(R.string.home_empty_message),
                )
            }
            else -> ScrollableCenter {
                EmptyMessage(message = stringResource(R.string.home_no_results, state.query.trim()))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenEmptyPreview() {
    IberiaTheme { HomeScreenPreview(state = HomeState()) }
}

@PreviewFontScale
@Preview(showBackground = true)
@Composable
private fun HomeScreenResultsPreview() {
    IberiaTheme { HomeScreenPreview(state = HomeState(query = "aircraft", images = PreviewData.images)) }
}

@Composable
private fun HomeScreenPreview(state: HomeState) {
    HomeScreen(state = state, onQueryChange = {}, onLoadNextPage = {}, onRetry = {}, onImageClick = {})
}

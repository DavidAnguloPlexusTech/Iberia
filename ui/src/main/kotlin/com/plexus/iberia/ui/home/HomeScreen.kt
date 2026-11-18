package com.plexus.iberia.ui.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.plexus.iberia.ui.R
import com.plexus.iberia.ui.common.ScreenPlaceholder
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
    // Events are collected only while visible; Main.immediate handles them in the same frame.
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

/** Stateless: renders [state] and reports user actions through the callbacks. */
@Composable
internal fun HomeScreen(
    state: HomeState,
    onQueryChange: (String) -> Unit,
    onLoadNextPage: () -> Unit,
    onRetry: () -> Unit,
    onImageClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    ScreenPlaceholder(title = stringResource(R.string.home_title), modifier = modifier)
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    IberiaTheme {
        HomeScreen(
            state = HomeState(),
            onQueryChange = {},
            onLoadNextPage = {},
            onRetry = {},
            onImageClick = {},
        )
    }
}

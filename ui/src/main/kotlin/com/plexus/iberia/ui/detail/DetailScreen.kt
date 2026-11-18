package com.plexus.iberia.ui.detail

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
import org.koin.core.parameter.parametersOf

@Composable
internal fun DetailRoute(
    nasaId: String,
    onNavigateBack: () -> Unit,
    viewModel: DetailViewModel = koinViewModel { parametersOf(nasaId) },
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current
    // Events are collected only while visible; Main.immediate handles them in the same frame.
    LaunchedEffect(viewModel, lifecycleOwner) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            withContext(Dispatchers.Main.immediate) {
                viewModel.events.collect { event ->
                    when (event) {
                        DetailEvent.NavigateBack -> onNavigateBack()
                    }
                }
            }
        }
    }
    DetailScreen(state = state, onRetry = viewModel::retry, onBackClick = viewModel::onBackClick)
}

/** Stateless: renders [state] and reports user actions through the callbacks. */
@Composable
internal fun DetailScreen(
    state: DetailState,
    onRetry: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ScreenPlaceholder(title = stringResource(R.string.detail_title), modifier = modifier)
}

@Preview(showBackground = true)
@Composable
private fun DetailScreenPreview() {
    IberiaTheme { DetailScreen(state = DetailState(), onRetry = {}, onBackClick = {}) }
}

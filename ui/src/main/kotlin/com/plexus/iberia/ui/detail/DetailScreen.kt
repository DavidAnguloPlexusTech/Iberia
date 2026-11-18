package com.plexus.iberia.ui.detail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.plexus.iberia.ui.common.PreviewData
import com.plexus.iberia.ui.common.components.ErrorMessage
import com.plexus.iberia.ui.common.components.LoadingAnimation
import com.plexus.iberia.ui.common.toMessage
import com.plexus.iberia.ui.detail.components.DetailCard
import com.plexus.iberia.ui.detail.components.DetailTopBar
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
    Scaffold(
        modifier = modifier,
        topBar = { DetailTopBar(onBackClick = onBackClick) },
    ) { innerPadding ->
        DetailContent(
            state = state,
            onRetry = onRetry,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        )
    }
}

@Composable
private fun DetailContent(state: DetailState, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        when {
            state.isLoading -> LoadingAnimation()
            state.error != null -> ErrorMessage(message = state.error.toMessage(), onRetry = onRetry)
            state.image != null -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
            ) {
                DetailCard(image = state.image)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DetailScreenPreview() {
    IberiaTheme {
        DetailScreen(
            state = DetailState(image = PreviewData.image, isLoading = false),
            onRetry = {},
            onBackClick = {},
        )
    }
}

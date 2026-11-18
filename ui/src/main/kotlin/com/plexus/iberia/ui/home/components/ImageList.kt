package com.plexus.iberia.ui.home.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.plexus.iberia.domain.error.DomainError
import com.plexus.iberia.domain.model.NasaImage
import com.plexus.iberia.ui.common.PreviewData
import com.plexus.iberia.ui.theme.IberiaTheme
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter

/** Items left before the end at which the next page is requested. */
private const val LOAD_MORE_THRESHOLD = 5

/** Infinite list: asks for the next page when the user gets close to the end. */
@Composable
internal fun ImageList(
    images: List<NasaImage>,
    isLoadingNextPage: Boolean,
    error: DomainError?,
    onImageClick: (String) -> Unit,
    onLoadNextPage: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    listState: LazyListState = rememberLazyListState(),
) {
    LoadNextPageWhenNearEnd(listState = listState, onLoadNextPage = onLoadNextPage)
    LazyColumn(
        modifier = modifier,
        state = listState,
        contentPadding = PaddingValues(vertical = 8.dp),
    ) {
        items(items = images, key = { it.id }) { image ->
            ImageListItem(image = image, onClick = { onImageClick(image.id) })
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp),
                color = MaterialTheme.colorScheme.outlineVariant,
            )
        }
        item {
            ImageListFooter(isLoading = isLoadingNextPage, error = error, onRetry = onRetry)
        }
    }
}

/** Observes scrolling outside composition, so scrolling never triggers recompositions. */
@Composable
private fun LoadNextPageWhenNearEnd(listState: LazyListState, onLoadNextPage: () -> Unit) {
    val currentOnLoadNextPage by rememberUpdatedState(onLoadNextPage)
    LaunchedEffect(listState) {
        snapshotFlow { listState.isNearEnd() to listState.layoutInfo.totalItemsCount }
            .distinctUntilChanged()
            .filter { (isNearEnd, _) -> isNearEnd }
            .collect { currentOnLoadNextPage() }
    }
}

private fun LazyListState.isNearEnd(): Boolean {
    val lastVisibleIndex = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: return false
    return lastVisibleIndex >= layoutInfo.totalItemsCount - LOAD_MORE_THRESHOLD
}

@Preview(showBackground = true)
@Composable
private fun ImageListPreview() {
    IberiaTheme {
        ImageList(
            images = PreviewData.images,
            isLoadingNextPage = true,
            error = null,
            onImageClick = {},
            onLoadNextPage = {},
            onRetry = {},
        )
    }
}

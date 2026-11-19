package com.plexus.iberia.ui.home.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewFontScale
import androidx.compose.ui.unit.dp
import com.plexus.iberia.domain.error.DomainError
import com.plexus.iberia.ui.common.components.ErrorMessage
import com.plexus.iberia.ui.common.components.LoadingAnimation
import com.plexus.iberia.ui.common.toMessage
import com.plexus.iberia.ui.theme.IberiaTheme

/** End of the list: loader while the next page arrives, or retry if it failed. */
@Composable
internal fun ImageListFooter(
    isLoading: Boolean,
    error: DomainError?,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        when {
            isLoading -> LoadingAnimation(modifier = Modifier.padding(16.dp), size = 24.dp)
            error != null -> ErrorMessage(message = error.toMessage(), onRetry = onRetry)
        }
    }
}

@PreviewFontScale
@Preview(showBackground = true)
@Composable
private fun ImageListFooterErrorPreview() {
    IberiaTheme { ImageListFooter(isLoading = false, error = DomainError.Network(), onRetry = {}) }
}

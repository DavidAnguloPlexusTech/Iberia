package com.plexus.iberia.ui.common.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewFontScale
import androidx.compose.ui.unit.dp
import com.plexus.iberia.ui.R
import com.plexus.iberia.ui.common.focusBorder
import com.plexus.iberia.ui.theme.IberiaTheme

@Composable
internal fun ErrorMessage(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = message,
            modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
        )
        val interactionSource = remember { MutableInteractionSource() }
        val isFocused by interactionSource.collectIsFocusedAsState()
        OutlinedButton(
            onClick = onRetry,
            modifier = Modifier.focusBorder(isFocused, MaterialTheme.colorScheme.primary, MaterialTheme.shapes.small),
            shape = MaterialTheme.shapes.small,
            interactionSource = interactionSource,
        ) {
            Text(text = stringResource(R.string.retry))
        }
    }
}

@PreviewFontScale
@Preview(showBackground = true)
@Composable
private fun ErrorMessagePreview() {
    IberiaTheme { ErrorMessage(message = "Check your connection and try again.", onRetry = {}) }
}

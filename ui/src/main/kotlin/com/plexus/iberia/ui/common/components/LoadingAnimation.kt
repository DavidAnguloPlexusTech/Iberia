package com.plexus.iberia.ui.common.components

import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.plexus.iberia.ui.R
import com.plexus.iberia.ui.theme.IberiaTheme

@Composable
internal fun LoadingAnimation(
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
) {
    val description = stringResource(R.string.loading)
    CircularProgressIndicator(
        modifier = modifier
            .size(size)
            .semantics {
                contentDescription = description
                liveRegion = LiveRegionMode.Polite
            },
        color = MaterialTheme.colorScheme.primary,
        strokeWidth = 2.dp,
    )
}

@Preview(showBackground = true)
@Composable
private fun LoadingAnimationPreview() {
    IberiaTheme { LoadingAnimation() }
}

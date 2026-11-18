package com.plexus.iberia.ui.detail

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.plexus.iberia.ui.R
import com.plexus.iberia.ui.common.ScreenPlaceholder
import com.plexus.iberia.ui.theme.IberiaTheme

@Composable
internal fun DetailScreen(modifier: Modifier = Modifier) {
    ScreenPlaceholder(title = stringResource(R.string.detail_title), modifier = modifier)
}

@Preview(showBackground = true)
@Composable
private fun DetailScreenPreview() {
    IberiaTheme { DetailScreen() }
}

package com.plexus.iberia.ui.detail.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.plexus.iberia.ui.R
import com.plexus.iberia.ui.common.components.FocusableIconButton
import com.plexus.iberia.ui.theme.IberiaTheme

/** Top bar of the detail with the back button. */
@Composable
internal fun DetailTopBar(onBackClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(4.dp),
    ) {
        FocusableIconButton(
            painter = painterResource(R.drawable.ic_arrow_back),
            contentDescription = stringResource(R.string.detail_back),
            onClick = onBackClick,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun DetailTopBarPreview() {
    IberiaTheme { DetailTopBar(onBackClick = {}) }
}

package com.plexus.iberia.ui.common.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.res.painterResource
import com.plexus.iberia.ui.R
import com.plexus.iberia.ui.common.focusBorder
import com.plexus.iberia.ui.theme.IberiaTheme

/** Icon button with a visible focus outline and a required label, for switch and keyboard users. */
@Composable
internal fun FocusableIconButton(
    painter: Painter,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    IconButton(
        onClick = onClick,
        modifier = modifier.focusBorder(isFocused, MaterialTheme.colorScheme.primary, CircleShape),
        interactionSource = interactionSource,
    ) {
        Icon(painter = painter, contentDescription = contentDescription)
    }
}

@Preview(showBackground = true)
@Composable
private fun FocusableIconButtonPreview() {
    IberiaTheme {
        FocusableIconButton(painter = painterResource(R.drawable.ic_close), contentDescription = "Clear", onClick = {})
    }
}

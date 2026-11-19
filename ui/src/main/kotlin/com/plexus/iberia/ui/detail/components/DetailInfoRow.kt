package com.plexus.iberia.ui.detail.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewFontScale
import androidx.compose.ui.unit.dp
import com.plexus.iberia.ui.R
import com.plexus.iberia.ui.theme.IberiaTheme

@Composable
internal fun DetailInfoRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    accessibleValue: String = value,
) {
    val description = stringResource(R.string.detail_info, label, accessibleValue)
    Row(
        modifier = modifier.clearAndSetSemantics { contentDescription = description },
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = label.uppercase(),
            modifier = Modifier.alignByBaseline(),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.secondary,
        )
        Text(
            text = value,
            modifier = Modifier
                .alignByBaseline()
                .weight(1f, fill = false),
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@PreviewFontScale
@Preview(showBackground = true)
@Composable
private fun DetailInfoRowPreview() {
    IberiaTheme { DetailInfoRow(label = "Center", value = "GRC") }
}

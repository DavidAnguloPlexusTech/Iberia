package com.plexus.iberia.ui.home.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import com.plexus.iberia.ui.R
import com.plexus.iberia.ui.theme.IberiaTheme

/** Search field: the search runs while typing, so the keyboard action only hides the keyboard. */
@Composable
internal fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val label = stringResource(R.string.search_placeholder)
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        // The placeholder disappears while typing; the description keeps the field named for TalkBack.
        modifier = modifier
            .fillMaxWidth()
            .semantics { contentDescription = label },
        placeholder = { Text(text = label) },
        leadingIcon = { SearchIcon() },
        trailingIcon = { if (query.isNotEmpty()) ClearButton(onClick = { onQueryChange("") }) },
        singleLine = true,
        shape = MaterialTheme.shapes.small,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            focusedLeadingIconColor = MaterialTheme.colorScheme.primary,
        ),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { keyboardController?.hide() }),
    )
}

/** Decorative: the field itself is already announced as the search field. */
@Composable
private fun SearchIcon() {
    Icon(painter = painterResource(R.drawable.ic_search), contentDescription = null)
}

@Composable
private fun ClearButton(onClick: () -> Unit) {
    IconButton(onClick = onClick) {
        Icon(
            painter = painterResource(R.drawable.ic_close),
            contentDescription = stringResource(R.string.search_clear),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SearchBarPreview() {
    IberiaTheme { SearchBar(query = "apollo", onQueryChange = {}) }
}

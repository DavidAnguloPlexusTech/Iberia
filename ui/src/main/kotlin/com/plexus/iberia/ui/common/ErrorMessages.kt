package com.plexus.iberia.ui.common

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.plexus.iberia.domain.error.DomainError
import com.plexus.iberia.ui.R

/** Text shown to the user for each domain error. */
@Composable
internal fun DomainError.toMessage(): String = when (this) {
    DomainError.Network -> stringResource(R.string.error_network)
    is DomainError.Server -> stringResource(R.string.error_server, code)
    DomainError.NotFound -> stringResource(R.string.error_not_found)
    DomainError.Unknown -> stringResource(R.string.error_unknown)
}

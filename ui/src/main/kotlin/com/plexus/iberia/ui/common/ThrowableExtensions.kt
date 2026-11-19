package com.plexus.iberia.ui.common

import com.plexus.iberia.domain.error.DomainError

internal fun Throwable.asDomainError(): DomainError = this as? DomainError ?: DomainError.Unknown()

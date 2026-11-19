package com.plexus.iberia.ui.common

import com.plexus.iberia.domain.error.DomainError

/** Repositories always fail with a [DomainError]; anything else is treated as unknown. */
internal fun Throwable.asDomainError(): DomainError = this as? DomainError ?: DomainError.Unknown()

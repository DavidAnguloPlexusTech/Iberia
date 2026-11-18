package com.plexus.iberia.domain.error

/** Failures the presentation layer can react to, independent of the data source. */
sealed class DomainError : Exception() {

    /** No connection or timeout. */
    data object Network : DomainError()

    /** The server answered with an error status. */
    data class Server(val code: Int) : DomainError()

    /** Anything not covered above, such as an unreadable response. */
    data object Unknown : DomainError()
}

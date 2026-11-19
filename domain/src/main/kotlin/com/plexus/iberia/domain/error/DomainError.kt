package com.plexus.iberia.domain.error

sealed class DomainError : Exception() {

    class Network : DomainError()

    data class Server(val code: Int) : DomainError()

    class NotFound : DomainError()

    class Unknown : DomainError()
}

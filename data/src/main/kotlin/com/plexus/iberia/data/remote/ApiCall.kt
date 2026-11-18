package com.plexus.iberia.data.remote

import com.plexus.iberia.domain.error.DomainError
import retrofit2.HttpException
import java.io.IOException
import kotlin.coroutines.cancellation.CancellationException

/** Runs a network call and turns any failure into a [DomainError]. */
internal suspend fun <T> apiCall(block: suspend () -> T): Result<T> =
    try {
        Result.success(block())
    } catch (exception: CancellationException) {
        throw exception
    } catch (exception: Exception) {
        Result.failure(exception.toDomainError())
    }

internal fun Exception.toDomainError(): DomainError = when (this) {
    is IOException -> DomainError.Network
    is HttpException -> DomainError.Server(code())
    else -> DomainError.Unknown
}

package com.julianotalora.core.common.result

import kotlin.coroutines.cancellation.CancellationException

/**
 * A generic wrapper for handling success and error states.
 * T is for Success, E is for Error.
 */
sealed class Result<out T, out E> {
    /**
     * Represents a successful result containing data.
     */
    data class Success<out T>(val data: T) : Result<T, Nothing>()

    /**
     * Represents an error result containing error information.
     */
    data class Error<out E>(val error: E) : Result<Nothing, E>()

    /**
     * Returns true if this is a Success result.
     */
    val isSuccess: Boolean get() = this is Success

    /**
     * Returns true if this is an Error result.
     */
    val isError: Boolean get() = this is Error

    /**
     * Returns the data if this is a Success, null otherwise.
     */
    fun getOrNull(): T? = when (this) {
        is Success -> data
        is Error -> null
    }

    /**
     * Returns the error if this is an Error, null otherwise.
     */
    fun errorOrNull(): E? = when (this) {
        is Success -> null
        is Error -> error
    }
}

/**
 * Maps the success value using the provided transform function.
 */
inline fun <T, E, R> Result<T, E>.map(transform: (T) -> R): Result<R, E> = when (this) {
    is Result.Success -> Result.Success(transform(data))
    is Result.Error -> this
}

/**
 * Maps the error value using the provided transform function.
 */
inline fun <T, E, R> Result<T, E>.mapError(transform: (E) -> R): Result<T, R> = when (this) {
    is Result.Success -> this
    is Result.Error -> Result.Error(transform(error))
}

/**
 * Flat maps the success value using the provided transform function.
 */
inline fun <T, E, R> Result<T, E>.flatMap(transform: (T) -> Result<R, E>): Result<R, E> = when (this) {
    is Result.Success -> transform(data)
    is Result.Error -> this
}

/**
 * Executes the given action if this is a Success.
 */
inline fun <T, E> Result<T, E>.onSuccess(action: (T) -> Unit): Result<T, E> {
    if (this is Result.Success) action(data)
    return this
}

/**
 * Executes the given action if this is an Error.
 */
inline fun <T, E> Result<T, E>.onError(action: (E) -> Unit): Result<T, E> {
    if (this is Result.Error) action(error)
    return this
}

/**
 * Returns the contained value if it is a Success, otherwise returns the result of [onFailure].
 */
inline fun <T, E> Result<T, E>.getOrElse(onFailure: (error: E) -> T): T = when (this) {
    is Result.Success -> data
    is Result.Error -> onFailure(error)
}

/**
 * Returns the contained value if it is a Success, otherwise returns [defaultValue].
 */
fun <T, E> Result<T, E>.getOrDefault(defaultValue: T): T = getOrElse { defaultValue }

/**
 * Folds this Result into a single value.
 */
inline fun <T, E, R> Result<T, E>.fold(onSuccess: (T) -> R, onFailure: (E) -> R): R = when (this) {
    is Result.Success -> onSuccess(data)
    is Result.Error -> onFailure(error)
}

/**
 * Creates a Success result.
 */
fun <T> success(data: T): Result<T, Nothing> = Result.Success(data)

/**
 * Creates an Error result.
 */
fun <E> error(error: E): Result<Nothing, E> = Result.Error(error)

/**
 * Converts a nullable value to a Result.
 */
fun <T, E> T?.toResult(error: () -> E): Result<T, E> =
    this?.let { Result.Success(it) } ?: Result.Error(error())

/**
 * Executes a block and catches exceptions, converting them to Error results.
 */
inline fun <T, E> runCatching(errorTransform: (Throwable) -> E, block: () -> T): Result<T, E> {
    return try {
        Result.Success(block())
    } catch (e: Throwable) {
        if (e is CancellationException) throw e
        Result.Error(errorTransform(e))
    }
}

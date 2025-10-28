package com.julianotalora.core.data.countries.error

import com.julianotalora.core.common.error.AppError
import com.julianotalora.features.countriesdatasdk.api.SdkError

/**
 * Maps an [SdkError] from the data SDK to a domain-level [AppError].
 *
 * This function is crucial for decoupling the domain layer from the specific
 * error types of the data layer. It translates low-level SDK errors into
 * meaningful, actionable errors that the application can handle.
 *
 * @return The corresponding [AppError] instance.
 */
fun SdkError.toAppError(): AppError {
    return when (this) {
        is SdkError.NetworkError -> AppError.NetworkError
        is SdkError.Timeout -> AppError.Timeout
        is SdkError.Http -> {
            when (this.code) {
                404 -> AppError.NotFound
                in 400..499 -> AppError.ServerError(this.code, "Client error: ${this.body}")
                in 500..599 -> AppError.ServerError(this.code, "Server error: ${this.body}")
                else -> AppError.UnknownError("HTTP error with code: ${this.code}")
            }
        }
        is SdkError.Serialization -> AppError.SerializationError()
        is SdkError.ServerError -> AppError.ServerError(this.code, this.message)
        is SdkError.Unknown -> AppError.UnknownError(this.message ?: "Unknown error")
        else -> AppError.UnknownError("Unknown error")
    }
}

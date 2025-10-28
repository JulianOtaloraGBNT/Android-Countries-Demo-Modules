package com.julianotalora.core.common.error

/**
 * Sealed class representing all possible application errors
 */
sealed class AppError {
    
    /**
     * Network-related errors
     */
    data object NetworkError : AppError()
    
    /**
     * Server errors with HTTP status code and optional message
     */
    data class ServerError(
        val code: Int,
        val message: String? = null
    ) : AppError()
    
    /**
     * Resource not found error
     */
    data object NotFound : AppError()
    
    /**
     * Data serialization/deserialization errors
     */
    data class SerializationError(
        val message: String? = null
    ) : AppError()
    
    /**
     * Request timeout errors
     */
    data object Timeout : AppError()
    
    /**
     * Unknown or unexpected errors
     */
    data class UnknownError(
        val message: String? = null,
        val cause: Throwable? = null
    ) : AppError()
    
    /**
     * Validation errors for input data
     */
    data class ValidationError(
        val field: String,
        val message: String
    ) : AppError()
    
    /**
     * Cache-related errors
     */
    data class CacheError(
        val message: String? = null
    ) : AppError()
    
    /**
     * Database-related errors
     */
    data class DatabaseError(
        val message: String? = null
    ) : AppError()
}

/**
 * Extension function to get a user-friendly message for each error type
 */
fun AppError.getUserMessage(): String = when (this) {
    is AppError.NetworkError -> "Network connection error. Please check your internet connection."
    is AppError.ServerError -> message ?: "Server error occurred. Please try again later."
    is AppError.NotFound -> "The requested resource was not found."
    is AppError.SerializationError -> "Data processing error occurred."
    is AppError.Timeout -> "Request timed out. Please try again."
    is AppError.UnknownError -> message ?: "An unexpected error occurred."
    is AppError.ValidationError -> "Invalid $field: $message"
    is AppError.CacheError -> "Cache error occurred."
    is AppError.DatabaseError -> "Database error occurred."
}

/**
 * Extension function to check if the error is recoverable
 */
fun AppError.isRecoverable(): Boolean = when (this) {
    is AppError.NetworkError -> true
    is AppError.ServerError -> code in 500..599 // Server errors are potentially recoverable
    is AppError.NotFound -> false
    is AppError.SerializationError -> false
    is AppError.Timeout -> true
    is AppError.UnknownError -> false
    is AppError.ValidationError -> false
    is AppError.CacheError -> true
    is AppError.DatabaseError -> false
}

/**
 * Extension function to check if the error should trigger a retry
 */
fun AppError.shouldRetry(): Boolean = when (this) {
    is AppError.NetworkError -> true
    is AppError.ServerError -> code in 500..599
    is AppError.Timeout -> true
    else -> false
}

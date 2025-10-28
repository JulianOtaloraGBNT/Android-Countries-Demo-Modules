package com.julianotalora.features.countriesdatasdk.api

sealed class SdkError(message: String, cause: Throwable? = null) : Throwable(message, cause) {
    class NetworkError(cause: Throwable) : SdkError("Network error", cause)
    class Timeout(cause: Throwable) : SdkError("Timeout", cause)
    class Http(val code: Int, val body: String?) : SdkError("HTTP error $code", null)
    class Serialization(cause: Throwable) : SdkError("Serialization error", cause)
    class ServerError(val code: Int, message: String) : SdkError("Server error: $code - $message")
    class Unknown(cause: Throwable) : SdkError("Unknown error", cause)
}

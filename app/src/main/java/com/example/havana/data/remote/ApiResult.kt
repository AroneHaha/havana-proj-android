package com.example.havana.data.remote

import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * Result of an API call that distinguishes between server responses and network failures.
 *
 * - [Success]: Server responded with 2xx — data is valid.
 * - [ServerError]: Server responded with 4xx/5xx — [code] and [message] explain what went wrong.
 * - [NetworkError]: Could not reach the server (no internet, timeout, DNS failure, etc.).
 *   This is the ONLY case where falling back to mock data is appropriate during development.
 */
sealed class ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class ServerError(val code: Int, val message: String) : ApiResult<Nothing>()
    data class NetworkError(val error: String) : ApiResult<Nothing>()
}

/**
 * Wrap a suspend API call into an [ApiResult], properly categorizing exceptions.
 *
 * Usage in ViewModel:
 * ```
 * when (val result = safeApiCall { productApi.getProducts() }) {
 *     is ApiResult.Success -> _state.value = State.Success(result.data)
 *     is ApiResult.ServerError -> _state.value = State.Error(result.message)
 *     is ApiResult.NetworkError -> // fall back to mock data during dev
 * }
 * ```
 */
suspend fun <T> safeApiCall(apiCall: suspend () -> T): ApiResult<T> {
    return try {
        ApiResult.Success(apiCall())
    } catch (e: HttpException) {
        val errorBody = e.response()?.errorBody()?.string()
        val userMessage = when (e.code()) {
            401 -> "Session expired. Please log in again."
            403 -> "You don't have permission to access this."
            404 -> "The requested resource was not found."
            422 -> errorBody?.take(200) ?: "Validation error."
            429 -> "Too many requests. Please try again later."
            in 500..599 -> "Server error. Please try again later."
            else -> errorBody?.take(200) ?: "HTTP ${e.code()}: ${e.message()}"
        }
        ApiResult.ServerError(e.code(), userMessage)
    } catch (e: UnknownHostException) {
        ApiResult.NetworkError("Unable to connect to server. Check your internet connection.")
    } catch (e: SocketTimeoutException) {
        ApiResult.NetworkError("Connection timed out. Please try again.")
    } catch (e: IOException) {
        ApiResult.NetworkError("Network error: ${e.message ?: "Check your connection."}")
    } catch (e: Exception) {
        // Unexpected exceptions (serialization, etc.) — treat as server error
        ApiResult.ServerError(0, "Unexpected error: ${e.message ?: "Unknown"}")
    }
}

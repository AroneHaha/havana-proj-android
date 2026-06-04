package com.example.havana.data.remote

import kotlinx.coroutines.CancellationException
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

sealed class ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class ServerError(val code: Int, val message: String) : ApiResult<Nothing>()
    data class NetworkError(val error: String) : ApiResult<Nothing>()
}

suspend fun <T> safeApiCall(apiCall: suspend () -> T): ApiResult<T> {
    return try {
        ApiResult.Success(apiCall())
    } catch (e: CancellationException) {
        throw e
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
        ApiResult.ServerError(0, "Unexpected error: ${e.message ?: "Unknown"}")
    }
}
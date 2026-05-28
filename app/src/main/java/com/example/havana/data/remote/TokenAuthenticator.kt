package com.example.havana.data.remote

import com.example.havana.data.session.SessionManager
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

/**
 * OkHttp Authenticator that handles 401 Unauthorized responses.
 * When the server returns 401, it attempts to refresh the access token
 * using the stored refresh token. If refresh succeeds, the original
 * request is retried with the new token. If refresh fails, the session
 * is cleared and the user is effectively logged out.
 */
class TokenAuthenticator : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        // Don't try to refresh if this was already a refresh request that failed
        val requestUrl = response.request.url.toString()
        if (requestUrl.contains("auth/refresh")) {
            // Refresh token is also invalid — force logout
            SessionManager.clearSession()
            return null
        }

        val refreshToken = SessionManager.refreshToken
        if (refreshToken.isNullOrEmpty()) {
            // No refresh token available — can't recover
            SessionManager.clearSession()
            return null
        }

        return try {
            // Use a separate OkHttpClient (without the authenticator) to avoid infinite loops
            val refreshResponse = runBlocking {
                val refreshApi = ApiClient.refreshRetrofit.create(AuthApiService::class.java)
                refreshApi.refreshToken("Bearer $refreshToken")
            }

            // Save the new tokens
            SessionManager.saveSession(
                SessionManager.currentUser?.let { user ->
                    // Update user from refresh response if available
                    com.example.havana.data.model.HavanaUser(
                        id = refreshResponse.user.id,
                        email = refreshResponse.user.email,
                        firstName = refreshResponse.user.firstName,
                        lastName = refreshResponse.user.lastName,
                        role = refreshResponse.user.role,
                        emailVerified = refreshResponse.user.emailVerifiedAt != null,
                        phone = user.phone,
                        deliveryAddress = user.deliveryAddress,
                    )
                } ?: return null,
                refreshResponse.token
            )
            SessionManager.saveRefreshToken(refreshResponse.refreshToken)

            // Retry the original request with the new token
            response.request.newBuilder()
                .header("Authorization", "Bearer ${refreshResponse.token}")
                .build()
        } catch (_: Exception) {
            // Refresh failed — clear session so user is redirected to login
            SessionManager.clearSession()
            null
        }
    }
}
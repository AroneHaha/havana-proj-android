package com.example.havana.data.remote

import com.example.havana.data.session.SessionManager
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

/**
 * OkHttp Authenticator that handles 401 Unauthorized responses.
 *
 * When the server returns 401, it attempts to refresh the access token
 * using the stored refresh token. If refresh succeeds, the original
 * request is retried with the new token. If refresh fails, the session
 * is cleared and the user is effectively logged out.
 *
 * IMPORTANT: The /auth/refresh endpoint returns { token, refresh_token }
 * with NO user object. We keep the existing HavanaUser in SessionManager
 * and only update the tokens.
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

            // Update tokens in SessionManager — keep the existing user, only swap tokens
            val existingUser = SessionManager.currentUser
            if (existingUser != null) {
                SessionManager.saveSession(existingUser, refreshResponse.token)
            }
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

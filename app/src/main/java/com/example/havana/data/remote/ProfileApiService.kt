package com.example.havana.data.remote

import com.example.havana.data.model.UpdateProfileRequest
import com.example.havana.data.model.UserProfile
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT

interface ProfileApiService {

    /**
     * GET /api/auth/me — fetch the current authenticated user.
     * Backend returns: { user: UserProfile }
     * Auth header is added automatically by ApiClient's auth interceptor.
     */
    @GET("auth/me")
    suspend fun getProfile(): AuthMeResponse

    /**
     * PUT /api/auth/profile — update user profile fields.
     * Backend returns: { data: { user: UserProfile, message } }
     */
    @PUT("auth/profile")
    suspend fun updateProfile(@Body request: UpdateProfileRequest): ProfileUpdateApiResponse
}

// ── Response wrappers ──────────────────────────────────────────────────

/** GET /auth/me → { user: UserProfile } */
data class AuthMeResponse(
    val user: UserProfile,
)

/** PUT /auth/profile → { data: { user: UserProfile, message? } } */
data class ProfileUpdateApiResponse(
    val data: ProfileUpdateInner,
)

data class ProfileUpdateInner(
    val user: UserProfile,
    val message: String? = null,
)
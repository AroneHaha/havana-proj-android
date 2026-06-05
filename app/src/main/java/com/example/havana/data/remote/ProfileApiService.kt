package com.example.havana.data.remote

import com.example.havana.data.model.UpdateProfileRequest
import com.example.havana.data.model.UserProfile
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT

interface ProfileApiService {

    @GET("auth/me")
    suspend fun getProfile(): ProfileMeResponse

    @PUT("auth/profile")
    suspend fun updateProfile(
        @Body request: UpdateProfileRequest,
    ): UpdateProfileDirectResponse
}

data class ProfileMeResponse(
    val user: UserProfile,
)

data class UpdateProfileDirectResponse(
    val user: UserProfile,
    val message: String? = null,
)
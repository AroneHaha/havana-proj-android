package com.example.havana.data.remote

import com.example.havana.data.model.UpdateProfileRequest
import com.example.havana.data.model.UserProfile
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT

interface ProfileApiService {

    @GET("profile")
    suspend fun getProfile(): UserProfile

    @PUT("profile")
    suspend fun updateProfile(
        @Body request: UpdateProfileRequest,
    ): com.example.havana.data.model.UpdateProfileResponse
}
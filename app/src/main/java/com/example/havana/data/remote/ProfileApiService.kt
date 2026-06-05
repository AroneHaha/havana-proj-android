package com.example.havana.data.remote

import com.example.havana.data.model.ProfileMeResponse
import com.example.havana.data.model.UpdateProfileRequest
import com.example.havana.data.model.UpdateProfileResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT

interface ProfileApiService {

    /**
     * GET /auth/me — returns { user: {...} } directly (NO data wrapper).
     * Uses main retrofit which has authInterceptor — NO @Header needed.
     */
    @GET("auth/me")
    suspend fun getProfile(): ProfileMeResponse

    /**
     * PUT /auth/profile — returns { user: {...}, message: "..." } directly (NO data wrapper).
     * Uses main retrofit which has authInterceptor — NO @Header needed.
     */
    @PUT("auth/profile")
    suspend fun updateProfile(@Body request: UpdateProfileRequest): UpdateProfileResponse
}
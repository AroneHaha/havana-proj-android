package com.example.havana.data.remote

import com.example.havana.data.model.LoginRequest
import com.example.havana.data.model.LoginResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>
}
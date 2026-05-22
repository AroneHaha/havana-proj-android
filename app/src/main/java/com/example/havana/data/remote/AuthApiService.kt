package com.example.havana.data.remote

import com.example.havana.data.model.LoginRequest
import com.example.havana.data.model.LoginResponse
import com.example.havana.data.model.SignupRequest
import com.example.havana.data.model.SignupResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    @POST("auth/register")
    suspend fun register(@Body request: SignupRequest): SignupResponse
}
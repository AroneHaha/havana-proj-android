package com.example.havana.data.model

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    val email: String,
    val password: String,
)

data class LoginResponse(
    val user: UserDto,
    val token: String,
    @SerializedName("refresh_token")
    val refreshToken: String? = null,
)

data class UserDto(
    val id: String,
    val email: String,
    @SerializedName("first_name")
    val firstName: String,
    @SerializedName("last_name")
    val lastName: String,
    val role: String,
    @SerializedName("email_verified_at")
    val emailVerifiedAt: String? = null,
)

data class HavanaUser(
    val id: String,
    val email: String,
    val firstName: String,
    val lastName: String,
    val role: String,
    val emailVerified: Boolean,
    val phone: String = "",
    val deliveryAddress: DeliveryAddress? = null,
)

sealed class AuthState {
    data object Idle : AuthState()
    data object Loading : AuthState()
    data class Success(val user: HavanaUser, val token: String) : AuthState()
    data class Error(val message: String) : AuthState()
}

// SIGN UP

data class SignupRequest(
    val name: String,
    val email: String,
    val password: String,
    val confirmPassword: String,
    val phone: String
)

data class SignupResponse(
    val token: String,
    val user: UserDto
)
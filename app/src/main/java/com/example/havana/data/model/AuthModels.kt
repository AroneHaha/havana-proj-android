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
    @SerializedName("first_name")
    val firstName: String,
    @SerializedName("last_name")
    val lastName: String,
    val role: String,
    @SerializedName("email_verified")
    val emailVerified: Boolean,
    val phone: String = "",
    @SerializedName("delivery_address")
    val deliveryAddress: DeliveryAddress? = null,
)

sealed class AuthState {
    data object Idle : AuthState()
    data object Loading : AuthState()
    data class Success(val user: HavanaUser, val token: String) : AuthState()
    data class Error(val message: String) : AuthState()
}

// SIGN UP

/**
 * Registration request body — matches Laravel AuthController@register.
 *
 * Backend expects: first_name, last_name, email, password, password_confirmation, phone
 * The full name entered by the user is split into first_name / last_name
 * in the ViewModel before constructing this object.
 */
data class SignupRequest(
    @SerializedName("first_name")
    val firstName: String,
    @SerializedName("last_name")
    val lastName: String,
    val email: String,
    val password: String,
    @SerializedName("password_confirmation")
    val passwordConfirmation: String,
    val phone: String
)

/**
 * Registration response — backend returns the same shape as login:
 * { user: UserDto, token, refresh_token }
 */
data class SignupResponse(
    val user: UserDto,
    val token: String,
    @SerializedName("refresh_token")
    val refreshToken: String? = null
)

/**
 * Token refresh response — backend /auth/refresh returns only new tokens, NO user.
 * { token, refresh_token }
 */
data class RefreshTokenResponse(
    val token: String,
    @SerializedName("refresh_token")
    val refreshToken: String? = null
)
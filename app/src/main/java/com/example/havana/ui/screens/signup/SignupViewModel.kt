package com.example.havana.ui.screens.signup

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.havana.data.model.AuthState
import com.example.havana.data.model.HavanaUser
import com.example.havana.data.model.SignupRequest
import com.example.havana.data.model.SignupResponse
import com.example.havana.data.model.UserDto
import com.example.havana.data.remote.ApiClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SignupViewModel(application: Application) : AndroidViewModel(application) {

    private val _signupState = MutableStateFlow<AuthState>(AuthState.Idle)
    val signupState: StateFlow<AuthState> = _signupState.asStateFlow()

    private val authApi = ApiClient.retrofit.create(
        com.example.havana.data.remote.AuthApiService::class.java
    )

    fun signup(
        name: String,
        email: String,
        password: String,
        confirmPassword: String,
        phone: String
    ) {
        // Client-side validation
        if (name.isBlank()) {
            _signupState.value = AuthState.Error("Please enter your full name")
            return
        }

        if (email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _signupState.value = AuthState.Error("Please enter a valid email address")
            return
        }

        if (password.length < 6) {
            _signupState.value = AuthState.Error("Password must be at least 6 characters")
            return
        }

        if (password != confirmPassword) {
            _signupState.value = AuthState.Error("Passwords do not match")
            return
        }

        if (phone.isBlank()) {
            _signupState.value = AuthState.Error("Please enter your phone number")
            return
        }

        _signupState.value = AuthState.Loading

        viewModelScope.launch {
            try {
                // Try real API first
                val request = SignupRequest(name, email, password, confirmPassword, phone)
                val response: SignupResponse = authApi.register(request)
                val user = mapToHavanaUser(response.user)
                _signupState.value = AuthState.Success(user, response.token)
            } catch (e: Exception) {
                // Mock fallback for development
                tryMockSignup(name, email, password, confirmPassword, phone)
            }
        }
    }

    private fun tryMockSignup(
        name: String,
        email: String,
        password: String,
        confirmPassword: String,
        phone: String
    ) {
        // Simulate network delay
        Thread.sleep(800)

        // Mock: only user@gmail.com can register successfully
        if (email.lowercase() != "user@gmail.com") {
            _signupState.value = AuthState.Error("Registration failed. Please use user@gmail.com to test.")
            return
        }

        // Mock success
        val nameParts = name.trim().split(" ", limit = 2)
        val mockUser = HavanaUser(
            id = "user-${System.currentTimeMillis()}",
            email = email,
            firstName = nameParts.first(),
            lastName = nameParts.getOrElse(1) { "" },
            role = "customer",
            emailVerified = false,
        )
        _signupState.value = AuthState.Success(mockUser, "mock-signup-token-${System.currentTimeMillis()}")
    }
    private fun mapToHavanaUser(dto: UserDto): HavanaUser {
        return HavanaUser(
            id = dto.id,
            email = dto.email,
            firstName = dto.firstName,
            lastName = dto.lastName,
            role = dto.role,
            emailVerified = dto.emailVerifiedAt != null,
        )
    }
    fun resetState() {
        _signupState.value = AuthState.Idle
    }
}
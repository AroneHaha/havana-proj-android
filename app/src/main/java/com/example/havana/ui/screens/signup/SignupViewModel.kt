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
import com.example.havana.data.session.SessionManager
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

        val cleanPhone = phone.replace("+965", "").replace(" ", "").trim()
        if (cleanPhone.length != 8 || !cleanPhone.first().toString().matches(Regex("[5689]"))) {
            _signupState.value = AuthState.Error("Enter a valid Kuwait phone number (e.g. +965 5XXX XXXX)")
            return
        }

        _signupState.value = AuthState.Loading

        viewModelScope.launch {
            try {
                val request = SignupRequest(name, email, password, confirmPassword, phone)
                val response: SignupResponse = authApi.register(request)
                val user = mapToHavanaUser(response.user)
                SessionManager.saveSession(user, response.token)
                _signupState.value = AuthState.Success(user, response.token)
            } catch (e: Exception) {
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
        Thread.sleep(800)

        if (email.lowercase() != "user@gmail.com") {
            _signupState.value = AuthState.Error("Registration failed. Please use user@gmail.com to test.")
            return
        }

        val nameParts = name.trim().split(" ", limit = 2)
        val mockUser = HavanaUser(
            id = "user-${System.currentTimeMillis()}",
            email = email,
            firstName = nameParts.first(),
            lastName = nameParts.getOrElse(1) { "" },
            role = "customer",
            emailVerified = false,
            phone = phone,
        )
        val mockToken = "mock-signup-token-${System.currentTimeMillis()}"
        SessionManager.saveSession(mockUser, mockToken)
        _signupState.value = AuthState.Success(mockUser, mockToken)
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
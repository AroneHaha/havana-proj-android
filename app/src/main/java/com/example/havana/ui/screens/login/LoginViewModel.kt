package com.example.havana.ui.screens.login

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.havana.data.model.*
import com.example.havana.data.remote.ApiClient
import com.example.havana.data.remote.AuthApiService
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LoginViewModel(application: Application) : AndroidViewModel(application) {

    private val apiService = ApiClient.createService(
        AuthApiService::class.java
    )

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val mockAccounts = mapOf(
        "admin@gmail.com" to MockAccount("admin", "admin", "Admin", "Havana"),
    )

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _authState.value = AuthState.Error("Please fill in all fields")
            return
        }

        viewModelScope.launch {
            _authState.value = AuthState.Loading

            try {
                val response = apiService.login(LoginRequest(email, password))
                if (response.isSuccessful) {
                    val body = response.body()!!
                    _authState.value = AuthState.Success(
                        user = HavanaUser(
                            id = body.user.id,
                            email = body.user.email,
                            firstName = body.user.firstName,
                            lastName = body.user.lastName,
                            role = body.user.role,
                            emailVerified = body.user.emailVerifiedAt != null,
                        ),
                        token = body.token,
                    )
                    return@launch
                }
            } catch (_: Exception) {
                // API unreachable — fall through to mock
            }

            // Mock login
            delay(600)
            val account = mockAccounts[email.lowercase()]
            if (account != null && account.password == password) {
                _authState.value = AuthState.Success(
                    user = HavanaUser(
                        id = "user-${System.currentTimeMillis()}",
                        email = email,
                        firstName = account.firstName,
                        lastName = account.lastName,
                        role = "customer",
                        emailVerified = true,
                    ),
                    token = "mock-token-${System.currentTimeMillis()}",
                )
            } else {
                _authState.value = AuthState.Error("Invalid email or password")
            }
        }
    }

    fun resetState() {
        _authState.value = AuthState.Idle
    }

    private data class MockAccount(
        val password: String,
        val role: String,
        val firstName: String,
        val lastName: String,
    )
}
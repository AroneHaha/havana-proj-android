package com.example.havana.ui.screens.login

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.havana.data.model.*
import com.example.havana.data.remote.ApiClient
import com.example.havana.data.remote.AuthApiService
import com.example.havana.data.session.SessionManager
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
        "admin@gmail.com" to MockAccount("admin", "admin", "Admin", "Havana", "+965 5123 4567"),
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
                val apiUser = HavanaUser(
                    id = response.user.id,
                    email = response.user.email,
                    firstName = response.user.firstName,
                    lastName = response.user.lastName,
                    role = response.user.role,
                    emailVerified = response.user.emailVerifiedAt != null,
                )
                SessionManager.saveSession(apiUser, response.token)
                _authState.value = AuthState.Success(user = apiUser, token = response.token)
                return@launch
            } catch (_: Exception) {
// API unreachable — fall through to mock
            }

// Mock login
            delay(600)
            val account = mockAccounts[email.lowercase()]
            if (account != null && account.password == password) {
                val mockUser = HavanaUser(
                    id = "user-${System.currentTimeMillis()}",
                    email = email,
                    firstName = account.firstName,
                    lastName = account.lastName,
                    role = "customer",
                    emailVerified = true,
                    phone = account.phone,
                    deliveryAddress = DeliveryAddress(
                        fullAddress = "Salmiya, Salem Al Mubarak St, Block 12, Building 8, Floor 3, Hawalli Governorate, Kuwait",
                        area = "Salmiya",
                        block = "12",
                        street = "Salem Al Mubarak St",
                        building = "8",
                        floor = "3",
                        latitude = 29.3375,
                        longitude = 48.0833
                    ),
                )
                val mockToken = "mock-token-${System.currentTimeMillis()}"
                SessionManager.saveSession(mockUser, mockToken)
                _authState.value = AuthState.Success(user = mockUser, token = mockToken)
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
        val phone: String = "",
    )
}
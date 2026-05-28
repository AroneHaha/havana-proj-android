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
import com.example.havana.data.remote.ApiResult
import com.example.havana.data.remote.safeApiCall
import com.example.havana.data.session.SessionManager
import com.example.havana.R
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
            _signupState.value = AuthState.Error(getApplication<Application>().getString(R.string.signup_error_name))
            return
        }

        if (email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _signupState.value = AuthState.Error(getApplication<Application>().getString(R.string.signup_error_email))
            return
        }

        if (password.length < 6) {
            _signupState.value = AuthState.Error(getApplication<Application>().getString(R.string.signup_error_password_length))
            return
        }

        if (password != confirmPassword) {
            _signupState.value = AuthState.Error(getApplication<Application>().getString(R.string.signup_error_password_mismatch))
            return
        }

        if (phone.isBlank()) {
            _signupState.value = AuthState.Error(getApplication<Application>().getString(R.string.signup_error_phone))
            return
        }

        val cleanPhone = phone.replace("+965", "").replace(" ", "").trim()
        if (cleanPhone.length != 8 || !cleanPhone.first().toString().matches(Regex("[5689]"))) {
            _signupState.value = AuthState.Error(getApplication<Application>().getString(R.string.signup_error_phone_invalid))
            return
        }

        _signupState.value = AuthState.Loading

        viewModelScope.launch {
            val request = SignupRequest(name, email, password, confirmPassword, phone)
            when (val result = safeApiCall { authApi.register(request) }) {
                is ApiResult.Success -> {
                    val user = mapToHavanaUser(result.data.user)
                    SessionManager.saveSession(user, result.data.token)
                    _signupState.value = AuthState.Success(user, result.data.token)
                }
                is ApiResult.ServerError -> {
                    // Server responded with error — show it (email taken, validation, etc.)
                    _signupState.value = AuthState.Error(result.message)
                }
                is ApiResult.NetworkError -> {
                    // Server unreachable — fall back to mock signup during development
                    tryMockSignup(name, email, phone)
                }
            }
        }
    }

    private suspend fun tryMockSignup(
        name: String,
        email: String,
        phone: String
    ) {
        kotlinx.coroutines.delay(800)

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

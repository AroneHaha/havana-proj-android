package com.example.havana.ui.screens.profile

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.havana.R
import com.example.havana.data.model.*
import com.example.havana.data.remote.ApiClient
import com.example.havana.data.remote.ApiResult
import com.example.havana.data.remote.AuthApiService
import com.example.havana.data.remote.ProfileApiService
import com.example.havana.data.remote.safeApiCall
import com.example.havana.data.session.SessionManager
import com.example.havana.ui.theme.ThemeManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val profileApi = ApiClient.retrofit.create(ProfileApiService::class.java)
    private val authApi = ApiClient.retrofit.create(AuthApiService::class.java)

    private val _profileState = MutableStateFlow<ProfileState>(ProfileState.Idle)
    val profileState: StateFlow<ProfileState> = _profileState.asStateFlow()

    private val _editState = MutableStateFlow<EditProfileState>(EditProfileState.Idle)
    val editState: StateFlow<EditProfileState> = _editState.asStateFlow()

    private val _editingField = MutableStateFlow<EditableField?>(null)
    val editingField: StateFlow<EditableField?> = _editingField.asStateFlow()

    private val _isDarkMode = MutableStateFlow(ThemeManager.isDarkMode)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    private val _isArabic = MutableStateFlow(SessionManager.isArabic)
    val isArabic: StateFlow<Boolean> = _isArabic.asStateFlow()

    init {
        loadProfile()
    }

    fun loadProfile() {
        _profileState.value = ProfileState.Loading
        viewModelScope.launch {
            val token = SessionManager.token
            if (token == null) {
                // No token — use local session data if available, otherwise error
                val local = SessionManager.getUserProfile()
                if (local != null) {
                    _profileState.value = ProfileState.Success(local)
                } else {
                    _profileState.value = ProfileState.Error("Please log in to view your profile.")
                }
                return@launch
            }

            when (val result = safeApiCall { profileApi.getProfile() }) {
                is ApiResult.Success -> {
                    // Backend returns { user: UserProfile }
                    val profile = result.data.user
                    // Update local session with fresh server data
                    val currentUser = SessionManager.currentUser
                    if (currentUser != null) {
                        SessionManager.updateUser(
                            currentUser.copy(
                                firstName = profile.firstName,
                                lastName = profile.lastName,
                                phone = profile.phone,
                                deliveryAddress = profile.deliveryAddress,
                            )
                        )
                    }
                    _profileState.value = ProfileState.Success(profile)
                }
                is ApiResult.ServerError -> {
                    // Server error — fall back to local session data
                    val local = SessionManager.getUserProfile()
                    if (local != null) {
                        _profileState.value = ProfileState.Success(local)
                    } else {
                        _profileState.value = ProfileState.Error(result.message)
                    }
                }
                is ApiResult.NetworkError -> {
                    // Server unreachable — fall back to local session data
                    val local = SessionManager.getUserProfile()
                    if (local != null) {
                        _profileState.value = ProfileState.Success(local)
                    } else {
                        _profileState.value = ProfileState.Error(result.error)
                    }
                }
            }
        }
    }

    fun updateProfile(
        firstName: String,
        lastName: String,
        phone: String,
        deliveryAddress: DeliveryAddress?,
    ) {
        val currentProfile = (_profileState.value as? ProfileState.Success)?.profile ?: return

        _editState.value = EditProfileState.Saving
        viewModelScope.launch {
            val token = SessionManager.token
            val request = UpdateProfileRequest(firstName, lastName, phone, deliveryAddress)

            if (token != null) {
                when (val result = safeApiCall { profileApi.updateProfile(request) }) {
                    is ApiResult.Success -> {
                        // Backend returns { data: { user: UserProfile, message? } }
                        val updatedProfile = result.data.data.user
                        // Also update the local session
                        val currentUser = SessionManager.currentUser
                        if (currentUser != null) {
                            SessionManager.updateUser(
                                currentUser.copy(
                                    firstName = updatedProfile.firstName,
                                    lastName = updatedProfile.lastName,
                                    phone = updatedProfile.phone,
                                    deliveryAddress = updatedProfile.deliveryAddress,
                                )
                            )
                        }
                        _profileState.value = ProfileState.Success(updatedProfile)
                        _editState.value = EditProfileState.Success(updatedProfile)
                    }
                    is ApiResult.ServerError -> {
                        _editState.value = EditProfileState.Error(result.message)
                    }
                    is ApiResult.NetworkError -> {
                        // Server unreachable — update locally only
                        val updatedProfile = currentProfile.copy(
                            firstName = firstName,
                            lastName = lastName,
                            phone = phone,
                            deliveryAddress = deliveryAddress,
                        )
                        val currentUser = SessionManager.currentUser
                        if (currentUser != null) {
                            SessionManager.updateUser(
                                currentUser.copy(
                                    firstName = firstName,
                                    lastName = lastName,
                                    phone = phone,
                                    deliveryAddress = deliveryAddress,
                                )
                            )
                        }
                        _profileState.value = ProfileState.Success(updatedProfile)
                        _editState.value = EditProfileState.Success(updatedProfile)
                    }
                }
            } else {
                // No token — update locally only
                val updatedProfile = currentProfile.copy(
                    firstName = firstName,
                    lastName = lastName,
                    phone = phone,
                    deliveryAddress = deliveryAddress,
                )
                val currentUser = SessionManager.currentUser
                if (currentUser != null) {
                    SessionManager.updateUser(
                        currentUser.copy(
                            firstName = firstName,
                            lastName = lastName,
                            phone = phone,
                            deliveryAddress = deliveryAddress,
                        )
                    )
                }
                _profileState.value = ProfileState.Success(updatedProfile)
                _editState.value = EditProfileState.Success(updatedProfile)
            }
        }
    }

    fun startEditing(field: EditableField) {
        _editingField.value = field
        _editState.value = EditProfileState.Idle
    }

    fun cancelEditing() {
        _editingField.value = null
        _editState.value = EditProfileState.Idle
    }

    /** Toggle dark mode via the centralized [ThemeManager]. */
    fun toggleDarkMode(enabled: Boolean) {
        _isDarkMode.value = enabled
        ThemeManager.setDarkMode(enabled)
    }

    fun toggleArabic(enabled: Boolean) {
        _isArabic.value = enabled
        SessionManager.setArabic(enabled)
    }

    fun logout() {
        viewModelScope.launch {
            try {
                val token = SessionManager.token
                if (token != null) {
                    authApi.logout("Bearer $token")
                }
            } catch (_: Exception) {
            }
            com.example.havana.data.cart.CartManager.clearCartOnLogout(getApplication())
            SessionManager.clearSession()
        }
    }

    fun resetEditState() {
        _editState.value = EditProfileState.Idle
        _editingField.value = null
    }
}
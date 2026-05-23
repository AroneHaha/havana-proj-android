package com.example.havana.ui.screens.profile

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.havana.data.model.*
import com.example.havana.data.session.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

// TODO: Create ProfileApiService when backend is ready
// private val profileApi = ApiClient.retrofit.create(ProfileApiService::class.java)

    private val _profileState = MutableStateFlow<ProfileState>(ProfileState.Idle)
    val profileState: StateFlow<ProfileState> = _profileState.asStateFlow()

    private val _editState = MutableStateFlow<EditProfileState>(EditProfileState.Idle)
    val editState: StateFlow<EditProfileState> = _editState.asStateFlow()

    private val _editingField = MutableStateFlow<EditableField?>(null)
    val editingField: StateFlow<EditableField?> = _editingField.asStateFlow()

    private val _isDarkMode = MutableStateFlow(SessionManager.isDarkMode)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    private val _isArabic = MutableStateFlow(SessionManager.isArabic)
    val isArabic: StateFlow<Boolean> = _isArabic.asStateFlow()

    init {
        loadProfile()
    }

    fun loadProfile() {
        _profileState.value = ProfileState.Loading
        viewModelScope.launch {
            try {
// TODO: Try API first
// val profile = profileApi.getProfile("Bearer ${SessionManager.token}")
// _profileState.value = ProfileState.Success(profile)

                val profile = SessionManager.getUserProfile()
                if (profile != null) {
                    _profileState.value = ProfileState.Success(profile)
                } else {
                    _profileState.value = ProfileState.Success(SessionManager.getMockProfile())
                }
            } catch (_: Exception) {
                _profileState.value = ProfileState.Error("Failed to load profile")
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
            try {

// TODO: Call API
// val request = UpdateProfileRequest(firstName, lastName, phone, deliveryAddress)
// val response = profileApi.updateProfile("Bearer ${SessionManager.token}", request)
// _editState.value = EditProfileState.Success(response.user)

                kotlinx.coroutines.delay(600)

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
            } catch (_: Exception) {
                _editState.value = EditProfileState.Error("Failed to update profile")
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

    fun toggleDarkMode(enabled: Boolean) {
        _isDarkMode.value = enabled
        SessionManager.setDarkMode(enabled)
// TODO: Trigger theme change via HavanaTheme(darkTheme = enabled)
    }

    fun toggleArabic(enabled: Boolean) {
        _isArabic.value = enabled
        SessionManager.setArabic(enabled)
// TODO: Trigger locale change and activity recreation
    }

    fun logout() {
        SessionManager.clearSession()
    }

    fun resetEditState() {
        _editState.value = EditProfileState.Idle
        _editingField.value = null
    }
}
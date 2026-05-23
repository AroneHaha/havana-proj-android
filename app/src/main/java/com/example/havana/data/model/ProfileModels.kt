package com.example.havana.data.model

import com.google.gson.annotations.SerializedName

data class UserProfile(
    val id: String,
    @SerializedName("first_name")
    val firstName: String,
    @SerializedName("last_name")
    val lastName: String,
    val email: String,
    val phone: String,
    val deliveryAddress: DeliveryAddress?,
    val role: String = "customer",
    @SerializedName("email_verified")
    val emailVerified: Boolean = false,
)

data class UpdateProfileRequest(
    @SerializedName("first_name")
    val firstName: String,
    @SerializedName("last_name")
    val lastName: String,
    val phone: String,
    @SerializedName("delivery_address")
    val deliveryAddress: DeliveryAddress?,
)

data class UpdateProfileResponse(
    val user: UserProfile,
    val message: String? = null,
)

sealed class ProfileState {
    data object Idle : ProfileState()
    data object Loading : ProfileState()
    data class Success(val profile: UserProfile) : ProfileState()
    data class Error(val message: String) : ProfileState()
}

sealed class EditProfileState {
    data object Idle : EditProfileState()
    data object Saving : EditProfileState()
    data class Success(val profile: UserProfile) : EditProfileState()
    data class Error(val message: String) : EditProfileState()
}

enum class EditableField {
    FULL_NAME,
    CONTACT_NUMBER,
    DELIVERY_ADDRESS,
}


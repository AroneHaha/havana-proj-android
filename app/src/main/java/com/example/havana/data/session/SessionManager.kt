package com.example.havana.data.session

import android.content.Context
import android.content.SharedPreferences
import com.example.havana.data.model.DeliveryAddress
import com.example.havana.data.model.HavanaUser
import com.example.havana.data.model.UserProfile

object SessionManager {

    private const val PREFS_NAME = "havana_session"
    private const val KEY_AUTH_TOKEN = "auth_token"
    private const val KEY_USER_ID = "user_id"
    private const val KEY_DARK_MODE = "dark_mode"
    private const val KEY_LANGUAGE_ARABIC = "language_arabic"

    private var _currentUser: HavanaUser? = null
    private var _token: String? = null
    private var _isDarkMode: Boolean = false
    private var _isArabic: Boolean = false

    private var prefs: SharedPreferences? = null

    val currentUser: HavanaUser? get() = _currentUser
    val token: String? get() = _token
    val isDarkMode: Boolean get() = _isDarkMode
    val isArabic: Boolean get() = _isArabic
    val isLoggedIn: Boolean get() = _currentUser != null && _token != null

    fun initialize(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
// TODO: Restore session from SharedPreferences on cold start
// _isDarkMode = prefs?.getBoolean(KEY_DARK_MODE, false) ?: false
// _isArabic = prefs?.getBoolean(KEY_LANGUAGE_ARABIC, false) ?: false
    }

    fun saveSession(user: HavanaUser, token: String) {
        _currentUser = user
        _token = token
// TODO: Persist to SharedPreferences
// prefs?.edit()?.putString(KEY_AUTH_TOKEN, token)?.apply()
// prefs?.edit()?.putString(KEY_USER_ID, user.id)?.apply()
    }

    fun clearSession() {
        _currentUser = null
        _token = null
// TODO: Clear SharedPreferences
// prefs?.edit()?.clear()?.apply()
    }

    fun updateUser(user: HavanaUser) {
        _currentUser = user
// TODO: Persist updated user to SharedPreferences
    }

    fun setDarkMode(enabled: Boolean) {
        _isDarkMode = enabled
// TODO: Persist to SharedPreferences
// prefs?.edit()?.putBoolean(KEY_DARK_MODE, enabled)?.apply()
    }

    fun setArabic(enabled: Boolean) {
        _isArabic = enabled
// TODO: Persist to SharedPreferences and apply locale
// prefs?.edit()?.putBoolean(KEY_LANGUAGE_ARABIC, enabled)?.apply()
    }

    fun getUserProfile(): UserProfile? {
        val user = _currentUser ?: return null
        return UserProfile(
            id = user.id,
            firstName = user.firstName,
            lastName = user.lastName,
            email = user.email,
            phone = user.phone,
            deliveryAddress = user.deliveryAddress,
            role = user.role,
            emailVerified = user.emailVerified,
        )
    }

    fun getMockProfile(): UserProfile {
        return UserProfile(
            id = "demo-1",
            firstName = "Fatima",
            lastName = "Al-Sabah",
            email = "admin@gmail.com",
            phone = "+965 5123 4567",
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
            role = "customer",
            emailVerified = true,
        )
    }
}
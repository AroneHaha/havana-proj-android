package com.example.havana.data.session

import android.content.Context
import android.content.SharedPreferences
import com.example.havana.data.model.DeliveryAddress
import com.example.havana.data.model.HavanaUser
import com.example.havana.data.model.UserProfile
import com.google.gson.Gson

object SessionManager {

    private const val PREFS_NAME = "havana_session"
    private const val KEY_AUTH_TOKEN = "auth_token"
    private const val KEY_USER_ID = "user_id"
    private const val KEY_USER_JSON = "user_json"
    private const val KEY_DARK_MODE = "dark_mode"
    private const val KEY_LANGUAGE_ARABIC = "language_arabic"

    private var _currentUser: HavanaUser? = null
    private var _token: String? = null
    private var _isDarkMode: Boolean = false
    private var _isArabic: Boolean = false

    private var prefs: SharedPreferences? = null
    private val gson = Gson()

    val currentUser: HavanaUser? get() = _currentUser
    val token: String? get() = _token
    val isDarkMode: Boolean get() = _isDarkMode
    val isArabic: Boolean get() = _isArabic
    val isLoggedIn: Boolean get() = _currentUser != null && _token != null

    fun initialize(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        // Restore session from SharedPreferences on cold start
        _token = prefs?.getString(KEY_AUTH_TOKEN, null)
        _isDarkMode = prefs?.getBoolean(KEY_DARK_MODE, false) ?: false
        _isArabic = prefs?.getBoolean(KEY_LANGUAGE_ARABIC, false) ?: false
        val userJson = prefs?.getString(KEY_USER_JSON, null)
        if (userJson != null) {
            try {
                _currentUser = gson.fromJson(userJson, HavanaUser::class.java)
            } catch (_: Exception) {
                _currentUser = null
            }
        }
    }

    fun saveSession(user: HavanaUser, token: String) {
        _currentUser = user
        _token = token
        // Persist to SharedPreferences
        prefs?.edit()?.apply {
            putString(KEY_AUTH_TOKEN, token)
            putString(KEY_USER_ID, user.id)
            putString(KEY_USER_JSON, gson.toJson(user))
            apply()
        }
    }

    fun clearSession() {
        _currentUser = null
        _token = null
        // Clear SharedPreferences (keep dark mode & language prefs)
        prefs?.edit()?.apply {
            remove(KEY_AUTH_TOKEN)
            remove(KEY_USER_ID)
            remove(KEY_USER_JSON)
            apply()
        }
    }

    fun updateUser(user: HavanaUser) {
        _currentUser = user
        // Persist updated user to SharedPreferences
        prefs?.edit()?.apply {
            putString(KEY_USER_JSON, gson.toJson(user))
            apply()
        }
    }

    fun setDarkMode(enabled: Boolean) {
        _isDarkMode = enabled
        // Persist to SharedPreferences
        prefs?.edit()?.putBoolean(KEY_DARK_MODE, enabled)?.apply()
    }

    fun setArabic(enabled: Boolean) {
        _isArabic = enabled
        // Persist to SharedPreferences and apply locale
        prefs?.edit()?.putBoolean(KEY_LANGUAGE_ARABIC, enabled)?.apply()
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
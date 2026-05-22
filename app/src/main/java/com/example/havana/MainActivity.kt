package com.example.havana

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import com.example.havana.ui.screens.login.LoginScreen
import com.example.havana.ui.screens.signup.SignupScreen
import com.example.havana.ui.theme.HavanaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HavanaTheme {
                HavanaApp()
            }
        }
    }
}

@Composable
fun HavanaApp() {
    // Simple navigation state — we'll upgrade to Navigation Compose later
    var currentScreen by remember { mutableStateOf("login") }

    when (currentScreen) {
        "login" -> LoginScreen(
            onLoginSuccess = {
                // TODO: Navigate to Home
            },
            onNavigateToSignup = {
                currentScreen = "signup"
            }
        )
        "signup" -> SignupScreen(
            onNavigateToLogin = {
                currentScreen = "login"
            }
        )
    }
}
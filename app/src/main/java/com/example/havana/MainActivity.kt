package com.example.havana

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.havana.ui.screens.login.LoginScreen
import com.example.havana.ui.theme.HavanaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HavanaTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    LoginScreen(
                        onLoginSuccess = { /* TODO: Navigate to Home */ },
                        onNavigateToSignup = { /* TODO: Navigate to Signup */ },
                    )
                }
            }
        }
    }
}
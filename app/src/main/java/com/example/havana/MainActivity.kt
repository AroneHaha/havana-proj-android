package com.example.havana

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import com.example.havana.ui.screens.home.HomeScreen
import com.example.havana.ui.screens.login.LoginScreen
import com.example.havana.ui.screens.productdetails.ProductDetailsScreen
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
    var currentScreen by remember { mutableStateOf("login") }
    var selectedProductId by remember { mutableStateOf<String?>(null) }

    when (currentScreen) {
        "login" -> LoginScreen(
            onLoginSuccess = {
                currentScreen = "home"
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
        "home" -> HomeScreen(
            onProductClick = { productId ->
                selectedProductId = productId
                currentScreen = "productDetails"
            },
            onCartClick = {
                currentScreen = "cart"
            }
        )
        "productDetails" -> ProductDetailsScreen(
            productId = selectedProductId ?: "",
            onBackClick = {
                currentScreen = "home"
            },
            onCartClick = {
                currentScreen = "cart"
            }
        )
        "cart" -> {
            // TODO: CartScreen — for now go back to home
            HomeScreen(
                onProductClick = { productId ->
                    selectedProductId = productId
                    currentScreen = "productDetails"
                },
                onCartClick = { }
            )
        }
    }
}
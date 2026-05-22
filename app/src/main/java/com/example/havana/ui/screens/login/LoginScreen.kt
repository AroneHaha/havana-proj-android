package com.example.havana.ui.screens.login

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.havana.data.model.AuthState
import com.example.havana.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit = {},
    onNavigateToSignup: () -> Unit = {},
    viewModel: LoginViewModel = viewModel(),
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    val authState by viewModel.authState.collectAsState()

    LaunchedEffect(authState) {
        if (authState is AuthState.Success) {
            onLoginSuccess()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(60.dp))

            AnimatedVisibility(
                visible = true,
                enter = fadeIn() + slideInVertically(initialOffsetY = { -40 }),
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Surface(
                        modifier = Modifier.size(64.dp),
                        shape = RoundedCornerShape(20.dp),
                        color = Maroon.copy(alpha = 0.1f),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("🌸", fontSize = 28.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Havana",
                        style = MaterialTheme.typography.displayLarge,
                        color = Maroon,
                    )

                    Text(
                        text = "Luxury Floral Boutique",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                    )
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            Text(
                text = "Welcome Back",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )

            Text(
                text = "Sign in to your account",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                modifier = Modifier.padding(top = 4.dp),
            )

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it
                    if (authState is AuthState.Error) viewModel.resetState()
                },
                label = { Text("Email Address") },
                placeholder = { Text("you@example.com") },
                leadingIcon = {
                    Icon(
                        Icons.Default.Email,
                        contentDescription = null,
                        tint = if (email.isNotEmpty()) Maroon else TextSecondary,
                    )
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next,
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Maroon,
                    focusedLabelColor = Maroon,
                    cursorColor = Maroon,
                    unfocusedBorderColor = Color(0xFFE5E5E5),
                ),
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    if (authState is AuthState.Error) viewModel.resetState()
                },
                label = { Text("Password") },
                placeholder = { Text("Enter your password") },
                leadingIcon = {
                    Icon(
                        Icons.Default.Lock,
                        contentDescription = null,
                        tint = if (password.isNotEmpty()) Maroon else TextSecondary,
                    )
                },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = if (passwordVisible) "Hide password" else "Show password",
                            tint = TextSecondary,
                        )
                    }
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done,
                ),
                keyboardActions = KeyboardActions(
                    onDone = { viewModel.login(email, password) }
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Maroon,
                    focusedLabelColor = Maroon,
                    cursorColor = Maroon,
                    unfocusedBorderColor = Color(0xFFE5E5E5),
                ),
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.End,
            ) {
                TextButton(onClick = { /* TODO */ }) {
                    Text(
                        text = "Forgot password?",
                        color = Maroon,
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            AnimatedVisibility(visible = authState is AuthState.Error) {
                val errorMessage = (authState as? AuthState.Error)?.message ?: ""
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = Error.copy(alpha = 0.1f),
                ) {
                    Text(
                        text = errorMessage,
                        color = Error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(12.dp),
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            Button(
                onClick = { viewModel.login(email, password) },
                enabled = authState !is AuthState.Loading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Maroon,
                    contentColor = Color.White,
                    disabledContainerColor = Maroon.copy(alpha = 0.5f),
                ),
            ) {
                if (authState is AuthState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp,
                    )
                } else {
                    Text(
                        text = "Sign In",
                        style = MaterialTheme.typography.labelLarge,
                        fontSize = 16.sp,
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFE5E5E5))
                Text(
                    text = "  or  ",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                )
                HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFE5E5E5))
            }

            Spacer(modifier = Modifier.height(24.dp))

            TextButton(onClick = onNavigateToSignup) {
                Text(
                    buildAnnotatedString {
                        append("Don't have an account? ")
                        withStyle(SpanStyle(color = Maroon, fontWeight = FontWeight.SemiBold)) {
                            append("Sign Up")
                        }
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                )
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}
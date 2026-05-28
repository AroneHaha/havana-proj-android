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
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.res.stringResource
import com.example.havana.R
import com.example.havana.data.locale.LocaleHelper
import com.example.havana.data.model.AuthState
import com.example.havana.data.session.SessionManager
import com.example.havana.ui.theme.*
import androidx.compose.ui.text.style.TextDecoration
import com.example.havana.ui.theme.ThemeManager
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    key: Int = 0,
    onLoginSuccess: () -> Unit = {},
    onNavigateToSignup: () -> Unit = {},
) {
    // Use key to force a fresh ViewModel on each logout → re-login cycle.
    // Without this, the cached ViewModel still holds AuthState.Success
    // from the previous login, which triggers onLoginSuccess immediately.
    val viewModel: LoginViewModel = viewModel(key = "login-$key")
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    val authState by viewModel.authState.collectAsState()

    val isDark = ThemeManager.isDarkMode
    val colorScheme = MaterialTheme.colorScheme

    LaunchedEffect(authState) {
        if (authState is AuthState.Success) {
            onLoginSuccess()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.background)
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
                        color = colorScheme.primary.copy(alpha = 0.1f),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("\uD83C\uDF38", fontSize = 28.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = stringResource(R.string.login_brand),
                        style = MaterialTheme.typography.displayLarge,
                        color = colorScheme.primary,
                    )

                    Text(
                        text = stringResource(R.string.login_tagline),
                        style = MaterialTheme.typography.bodyMedium,
                        color = colorScheme.onBackground.copy(alpha = 0.5f),
                    )
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            Text(
                text = stringResource(R.string.login_welcome),
                style = MaterialTheme.typography.headlineMedium,
                color = colorScheme.onBackground,
            )

            Text(
                text = stringResource(R.string.login_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp),
            )

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it
                    if (authState is AuthState.Error) viewModel.resetState()
                },
                label = { Text(stringResource(R.string.login_email_label)) },
                placeholder = { Text(stringResource(R.string.login_email_placeholder)) },
                leadingIcon = {
                    Icon(
                        Icons.Default.Email,
                        contentDescription = null,
                        tint = if (email.isNotEmpty()) colorScheme.primary else colorScheme.onSurfaceVariant,
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
                    focusedBorderColor = colorScheme.primary,
                    focusedLabelColor = colorScheme.primary,
                    cursorColor = colorScheme.primary,
                    unfocusedBorderColor = colorScheme.outline,
                ),
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    if (authState is AuthState.Error) viewModel.resetState()
                },
                label = { Text(stringResource(R.string.login_password_label)) },
                placeholder = { Text(stringResource(R.string.login_password_placeholder)) },
                leadingIcon = {
                    Icon(
                        Icons.Default.Lock,
                        contentDescription = null,
                        tint = if (password.isNotEmpty()) colorScheme.primary else colorScheme.onSurfaceVariant,
                    )
                },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = if (passwordVisible) stringResource(R.string.login_hide_password) else stringResource(R.string.login_show_password),
                            tint = colorScheme.onSurfaceVariant,
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
                    focusedBorderColor = colorScheme.primary,
                    focusedLabelColor = colorScheme.primary,
                    cursorColor = colorScheme.primary,
                    unfocusedBorderColor = colorScheme.outline,
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
                        text = stringResource(R.string.login_forgot_password),
                        color = colorScheme.primary,
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
                    color = if (isDark) BannerErrorBgDark else BannerErrorBgLight,
                ) {
                    Text(
                        text = errorMessage,
                        color = if (isDark) BannerErrorFgDark else BannerErrorFgLight,
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
                    containerColor = colorScheme.primary,
                    contentColor = colorScheme.onPrimary,
                    disabledContainerColor = colorScheme.primary.copy(alpha = 0.5f),
                ),
            ) {
                if (authState is AuthState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = colorScheme.onPrimary,
                        strokeWidth = 2.dp,
                    )
                } else {
                    Text(
                        text = stringResource(R.string.login_button),
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
                HorizontalDivider(modifier = Modifier.weight(1f), color = colorScheme.outline)
                Text(
                    text = "  ${stringResource(R.string.or)}  ",
                    style = MaterialTheme.typography.bodySmall,
                    color = colorScheme.onSurfaceVariant,
                )
                HorizontalDivider(modifier = Modifier.weight(1f), color = colorScheme.outline)
            }

            Spacer(modifier = Modifier.height(24.dp))

            TextButton(onClick = onNavigateToSignup) {
                Text(
                    text = stringResource(R.string.login_no_account),
                    color = colorScheme.onSurfaceVariant,
                    fontSize = 14.sp
                )
                Text(
                    text = stringResource(R.string.login_signup_link),
                    color = colorScheme.primary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    textDecoration = TextDecoration.Underline
                )
            }

            Spacer(modifier = Modifier.height(40.dp))
        }

        // ── Top-right toggles: theme + language (on top of Column for touch) ──
        val activityContext = LocalContext.current
        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Language toggle
            TextButton(
                onClick = {
                    LocaleHelper.setArabic(activityContext as android.app.Activity, !SessionManager.isArabic)
                },
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text = if (SessionManager.isArabic) stringResource(R.string.lang_toggle_en) else stringResource(R.string.lang_toggle_ar),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colorScheme.primary
                )
            }
            // Theme toggle
            IconButton(
                onClick = { ThemeManager.toggle() },
            ) {
                Icon(
                    imageVector = if (isDark) Icons.Outlined.LightMode else Icons.Outlined.DarkMode,
                    contentDescription = if (isDark) stringResource(R.string.login_switch_light) else stringResource(R.string.login_switch_dark),
                    tint = colorScheme.onBackground.copy(alpha = 0.6f),
                )
            }
        }
    }
}
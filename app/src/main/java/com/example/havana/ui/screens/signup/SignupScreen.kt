package com.example.havana.ui.screens.signup

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.res.stringResource
import com.example.havana.R
import com.example.havana.data.locale.LocaleHelper
import com.example.havana.data.model.AuthState
import com.example.havana.data.session.SessionManager
import com.example.havana.ui.theme.*
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignupScreen(
    onNavigateToLogin: () -> Unit = {},
    onSignupSuccess: () -> Unit = {}
) {
    val viewModel: SignupViewModel = viewModel()
    val signupState by viewModel.signupState.collectAsState()

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    // Focus requesters for field navigation
    val emailFocus = remember { FocusRequester() }
    val phoneFocus = remember { FocusRequester() }
    val passwordFocus = remember { FocusRequester() }
    val confirmPasswordFocus = remember { FocusRequester() }

    val isDark = ThemeManager.isDarkMode
    val colorScheme = MaterialTheme.colorScheme

    // Handle success -> show success briefly then redirect to login
    LaunchedEffect(signupState) {
        if (signupState is AuthState.Success) {
            kotlinx.coroutines.delay(1500)
            viewModel.resetState()
            onNavigateToLogin()
        }
    }

    // Clear email field on error
    LaunchedEffect(signupState) {
        if (signupState is AuthState.Error) {
            email = ""
        }
    }

    // Error message to display
    val errorMessage = when (signupState) {
        is AuthState.Error -> (signupState as AuthState.Error).message
        else -> null
    }

    val isLoading = signupState is AuthState.Loading

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 28.dp)
                .padding(top = 60.dp, bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ===== LOGO / BRAND =====
            Text(
                text = stringResource(R.string.signup_brand),
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = colorScheme.primary,
                letterSpacing = 6.sp
            )
            Text(
                text = stringResource(R.string.signup_tagline),
                fontSize = 13.sp,
                color = colorScheme.secondary,
                letterSpacing = 3.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(8.dp))

            // ===== WELCOME TEXT =====
            Text(
                text = stringResource(R.string.signup_title),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = colorScheme.primary
            )
            Text(
                text = stringResource(R.string.signup_subtitle),
                fontSize = 14.sp,
                color = if (isDark) AuthSubtitleDark else AuthSubtitleLight,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(28.dp))

            // ===== SUCCESS BANNER =====
            if (signupState is AuthState.Success) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isDark) BannerSuccessBgDark else BannerSuccessBgLight
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "\u2713",
                            color = if (isDark) BannerSuccessFgDark else BannerSuccessFgLight,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = stringResource(R.string.signup_success),
                            color = if (isDark) BannerSuccessFgDark else BannerSuccessFgLight,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }


            // ===== ERROR BANNER =====
            if (errorMessage != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isDark) BannerErrorBgDark else BannerErrorBgLight
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = stringResource(R.string.error),
                            tint = if (isDark) BannerErrorFgDark else BannerErrorFgLight,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = errorMessage,
                            color = if (isDark) BannerErrorFgDark else BannerErrorFgLight,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // ===== FULL NAME FIELD =====
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(stringResource(R.string.signup_name_label)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colorScheme.primary,
                    focusedLabelColor = colorScheme.primary,
                    cursorColor = colorScheme.primary,
                    unfocusedBorderColor = colorScheme.outlineVariant,
                    unfocusedLabelColor = if (isDark) AuthSubtitleDark else AuthSubtitleLight
                ),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text
                ),
                enabled = !isLoading
            )

            Spacer(modifier = Modifier.height(14.dp))

            // ===== EMAIL FIELD =====
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text(stringResource(R.string.signup_email_label)) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(emailFocus),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colorScheme.primary,
                    focusedLabelColor = colorScheme.primary,
                    cursorColor = colorScheme.primary,
                    unfocusedBorderColor = colorScheme.outlineVariant,
                    unfocusedLabelColor = if (isDark) AuthSubtitleDark else AuthSubtitleLight
                ),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = androidx.compose.ui.text.input.ImeAction.Next
                ),
                enabled = !isLoading
            )

            Spacer(modifier = Modifier.height(14.dp))

            // ===== PHONE FIELD =====
            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text(stringResource(R.string.signup_phone_label)) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(phoneFocus),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colorScheme.primary,
                    focusedLabelColor = colorScheme.primary,
                    cursorColor = colorScheme.primary,
                    unfocusedBorderColor = colorScheme.outlineVariant,
                    unfocusedLabelColor = if (isDark) AuthSubtitleDark else AuthSubtitleLight
                ),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Phone,
                    imeAction = androidx.compose.ui.text.input.ImeAction.Next
                ),
                enabled = !isLoading
            )

            Spacer(modifier = Modifier.height(14.dp))

            // ===== PASSWORD FIELD =====
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text(stringResource(R.string.signup_password_label)) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(passwordFocus),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colorScheme.primary,
                    focusedLabelColor = colorScheme.primary,
                    cursorColor = colorScheme.primary,
                    unfocusedBorderColor = colorScheme.outlineVariant,
                    unfocusedLabelColor = if (isDark) AuthSubtitleDark else AuthSubtitleLight
                ),
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = if (passwordVisible) stringResource(R.string.signup_hide_password) else stringResource(R.string.signup_show_password),
                            tint = if (isDark) AuthSubtitleDark else AuthSubtitleLight
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = androidx.compose.ui.text.input.ImeAction.Next
                ),
                enabled = !isLoading
            )

            Spacer(modifier = Modifier.height(14.dp))

            // ===== CONFIRM PASSWORD FIELD =====
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text(stringResource(R.string.signup_confirm_password_label)) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(confirmPasswordFocus),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colorScheme.primary,
                    focusedLabelColor = colorScheme.primary,
                    cursorColor = colorScheme.primary,
                    unfocusedBorderColor = colorScheme.outlineVariant,
                    unfocusedLabelColor = if (isDark) AuthSubtitleDark else AuthSubtitleLight
                ),
                visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                        Icon(
                            imageVector = if (confirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = if (confirmPasswordVisible) stringResource(R.string.signup_hide_password) else stringResource(R.string.signup_show_password),
                            tint = if (isDark) AuthSubtitleDark else AuthSubtitleLight
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = androidx.compose.ui.text.input.ImeAction.Done
                ),
                enabled = !isLoading
            )

            Spacer(modifier = Modifier.height(24.dp))

            // ===== SIGN UP BUTTON =====
            Button(
                onClick = {
                    viewModel.signup(name, email, password, confirmPassword, phone)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorScheme.primary,
                    disabledContainerColor = colorScheme.primary.copy(alpha = 0.5f)
                ),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = colorScheme.onPrimary,
                        strokeWidth = 2.5.dp
                    )
                } else {
                    Text(
                        text = stringResource(R.string.signup_button),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colorScheme.onPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ===== LOGIN LINK =====
            Row(
                modifier = Modifier.clickable {
                    viewModel.resetState()
                    onNavigateToLogin()
                },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.signup_has_account),
                    color = if (isDark) AuthSubtitleDark else AuthSubtitleLight,
                    fontSize = 14.sp
                )
                Text(
                    text = stringResource(R.string.signup_signin_link),
                    color = colorScheme.primary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    textDecoration = TextDecoration.Underline
                )
            }
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
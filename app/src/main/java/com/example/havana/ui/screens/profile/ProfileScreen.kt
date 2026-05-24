package com.example.havana.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.havana.R
import com.example.havana.data.model.DeliveryAddress
import com.example.havana.data.model.EditableField
import com.example.havana.data.model.EditProfileState
import com.example.havana.data.model.ProfileState
import com.example.havana.data.model.UserProfile
import com.example.havana.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onBackClick: () -> Unit = {},
    onHomeClick: () -> Unit = {},
    onCartClick: () -> Unit = {},
    onOrdersClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {},
    viewModel: ProfileViewModel = viewModel(),
) {
    val profileState by viewModel.profileState.collectAsState()
    val editState by viewModel.editState.collectAsState()
    val editingField by viewModel.editingField.collectAsState()
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val isArabic by viewModel.isArabic.collectAsState()

    // Handle edit success — close edit mode
    LaunchedEffect(editState) {
        if (editState is EditProfileState.Success) {
            viewModel.resetEditState()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.profile_title),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.profile_back),
                            tint = Maroon,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CreamBg,
                ),
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 8.dp,
            ) {
                NavigationBarItem(
                    selected = false,
                    onClick = onHomeClick,
                    icon = { Icon(Icons.Outlined.Home, contentDescription = "Home") },
                    label = { Text("Home", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary,
                    ),
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onCartClick,
                    icon = { Icon(Icons.Outlined.ShoppingCart, contentDescription = "Cart") },
                    label = { Text("Cart", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary,
                    ),
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onOrdersClick,
                    icon = { Icon(Icons.Outlined.ReceiptLong, contentDescription = "Orders") },
                    label = { Text("Orders", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary,
                    ),
                )
                NavigationBarItem(
                    selected = true,
                    onClick = { },
                    icon = { Icon(Icons.Outlined.Person, contentDescription = "Profile") },
                    label = { Text("Profile", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Maroon,
                        selectedTextColor = Maroon,
                        indicatorColor = Maroon.copy(alpha = 0.1f),
                    ),
                )
            }
        },
        containerColor = CreamBg,
    ) { paddingValues ->
        when (profileState) {
            is ProfileState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = Maroon)
                }
            }
            is ProfileState.Success -> {
                val profile = (profileState as ProfileState.Success).profile
                ProfileContent(
                    profile = profile,
                    editingField = editingField,
                    editState = editState,
                    isDarkMode = isDarkMode,
                    isArabic = isArabic,
                    paddingValues = paddingValues,
                    onStartEdit = { viewModel.startEditing(it) },
                    onCancelEdit = { viewModel.cancelEditing() },
                    onSaveEdit = { firstName, lastName, phone, address ->
                        viewModel.updateProfile(firstName, lastName, phone, address)
                    },
                    onDarkModeToggle = { viewModel.toggleDarkMode(it) },
                    onArabicToggle = { viewModel.toggleArabic(it) },
                    onLogout = {
                        viewModel.logout()
                        onLogoutClick()
                    },
                )
            }
            is ProfileState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            (profileState as ProfileState.Error).message,
                            color = Error,
                            fontSize = 14.sp,
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedButton(
                            onClick = { viewModel.loadProfile() },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Maroon),
                        ) {
                            Text(stringResource(R.string.profile_retry))
                        }
                    }
                }
            }
            else -> {}
        }
    }
}

@Composable
private fun ProfileContent(
    profile: UserProfile,
    editingField: EditableField?,
    editState: EditProfileState,
    isDarkMode: Boolean,
    isArabic: Boolean,
    paddingValues: PaddingValues,
    onStartEdit: (EditableField) -> Unit,
    onCancelEdit: () -> Unit,
    onSaveEdit: (String, String, String, DeliveryAddress?) -> Unit,
    onDarkModeToggle: (Boolean) -> Unit,
    onArabicToggle: (Boolean) -> Unit,
    onLogout: () -> Unit,
) {
    // Local edit field values
    var editFirstName by remember { mutableStateOf(profile.firstName) }
    var editLastName by remember { mutableStateOf(profile.lastName) }
    var editPhone by remember { mutableStateOf(profile.phone) }
    var editAddress by remember { mutableStateOf(profile.deliveryAddress?.fullAddress ?: "") }

    // Reset edit values when profile changes or editing starts
    LaunchedEffect(profile, editingField) {
        editFirstName = profile.firstName
        editLastName = profile.lastName
        editPhone = profile.phone
        editAddress = profile.deliveryAddress?.fullAddress ?: ""
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),   // <-- FIX: removed .imePadding()
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // ===== PROFILE HEADER =====
            ProfileHeader(profile = profile)

            Spacer(modifier = Modifier.height(20.dp))

            // ===== PERSONAL INFORMATION =====
            SectionHeader(title = stringResource(R.string.profile_section_personal))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            ) {
                Column(modifier = Modifier.padding(4.dp)) {
                    // Full Name — editable
                    if (editingField == EditableField.FULL_NAME) {
                        EditableFieldEditor(
                            label = stringResource(R.string.profile_full_name),
                            fields = listOf(
                                stringResource(R.string.profile_first_name) to editFirstName,
                                stringResource(R.string.profile_last_name) to editLastName,
                            ),
                            onValueChange = { index, value ->
                                if (index == 0) editFirstName = value
                                else editLastName = value
                            },
                            onSave = {
                                val newAddress = if (editAddress.isNotBlank()) {
                                    profile.deliveryAddress?.copy(fullAddress = editAddress)
                                        ?: DeliveryAddress(fullAddress = editAddress)
                                } else {
                                    profile.deliveryAddress
                                }
                                onSaveEdit(editFirstName, editLastName, editPhone, newAddress)
                            },
                            onCancel = onCancelEdit,
                            isSaving = editState is EditProfileState.Saving,
                        )
                    } else {
                        ProfileInfoRow(
                            label = stringResource(R.string.profile_full_name),
                            value = "${profile.firstName} ${profile.lastName}",
                            editable = true,
                            onEdit = { onStartEdit(EditableField.FULL_NAME) },
                        )
                    }

                    HorizontalDivider(color = Color(0xFFF0ECE8), modifier = Modifier.padding(horizontal = 12.dp))

                    // Email — read-only
                    ProfileInfoRow(
                        label = stringResource(R.string.profile_email),
                        value = profile.email,
                        editable = false,
                        onEdit = null,
                    )

                    HorizontalDivider(color = Color(0xFFF0ECE8), modifier = Modifier.padding(horizontal = 12.dp))

                    // Contact Number — editable
                    if (editingField == EditableField.CONTACT_NUMBER) {
                        EditableFieldEditor(
                            label = stringResource(R.string.profile_contact),
                            fields = listOf(
                                stringResource(R.string.profile_phone_hint) to editPhone,
                            ),
                            onValueChange = { _, value -> editPhone = value },
                            onSave = {
                                val newAddress = if (editAddress.isNotBlank()) {
                                    profile.deliveryAddress?.copy(fullAddress = editAddress)
                                        ?: DeliveryAddress(fullAddress = editAddress)
                                } else {
                                    profile.deliveryAddress
                                }
                                onSaveEdit(editFirstName, editLastName, editPhone, newAddress)
                            },
                            onCancel = onCancelEdit,
                            isSaving = editState is EditProfileState.Saving,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        )
                    } else {
                        ProfileInfoRow(
                            label = stringResource(R.string.profile_contact),
                            value = profile.phone,
                            editable = true,
                            onEdit = { onStartEdit(EditableField.CONTACT_NUMBER) },
                        )
                    }
                }
            }

            // Edit error banner
            if (editState is EditProfileState.Error) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    color = Error.copy(alpha = 0.08f),
                ) {
                    Text(
                        text = (editState as EditProfileState.Error).message,
                        color = Error,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(10.dp),
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ===== DELIVERY ADDRESS =====
            SectionHeader(title = stringResource(R.string.profile_section_address))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            ) {
                Column(modifier = Modifier.padding(4.dp)) {
                    if (editingField == EditableField.DELIVERY_ADDRESS) {
                        EditableFieldEditor(
                            label = stringResource(R.string.profile_address),
                            fields = listOf(
                                stringResource(R.string.profile_address_hint) to editAddress,
                            ),
                            onValueChange = { _, value -> editAddress = value },
                            onSave = {
                                val newAddress = if (editAddress.isNotBlank()) {
                                    profile.deliveryAddress?.copy(fullAddress = editAddress)
                                        ?: DeliveryAddress(fullAddress = editAddress)
                                } else {
                                    profile.deliveryAddress
                                }
                                onSaveEdit(editFirstName, editLastName, editPhone, newAddress)
                            },
                            onCancel = onCancelEdit,
                            isSaving = editState is EditProfileState.Saving,
                            singleLine = false,
                            minLines = 2,
                            maxLines = 4,
                        )
                    } else {
                        ProfileInfoRow(
                            label = stringResource(R.string.profile_address),
                            value = profile.deliveryAddress?.fullAddress
                                ?: stringResource(R.string.profile_address_not_set),
                            editable = true,
                            onEdit = { onStartEdit(EditableField.DELIVERY_ADDRESS) },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = Maroon,
                                    modifier = Modifier.size(18.dp),
                                )
                            },
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ===== PREFERENCES =====
            SectionHeader(title = stringResource(R.string.profile_section_preferences))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            ) {
                Column(modifier = Modifier.padding(4.dp)) {
                    // Dark/Light Mode Toggle
                    PreferenceRow(
                        label = stringResource(R.string.profile_dark_mode),
                        subtitle = stringResource(R.string.profile_dark_mode_desc),
                        checked = isDarkMode,
                        onCheckedChange = onDarkModeToggle,
                    )

                    HorizontalDivider(color = Color(0xFFF0ECE8), modifier = Modifier.padding(horizontal = 12.dp))

                    // Arabic/English Toggle
                    PreferenceRow(
                        label = if (isArabic) "العربية / English" else stringResource(R.string.profile_language),
                        subtitle = if (isArabic)
                            stringResource(R.string.profile_language_arabic)
                        else
                            stringResource(R.string.profile_language_desc),
                        checked = isArabic,
                        onCheckedChange = onArabicToggle,
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ===== LOGOUT BUTTON =====
            OutlinedButton(
                onClick = onLogout,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Error,
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, Error.copy(alpha = 0.5f)),
            ) {
                Text(
                    text = stringResource(R.string.profile_logout),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// ===== PROFILE HEADER COMPOSABLE =====

@Composable
private fun ProfileHeader(profile: UserProfile) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Maroon.copy(alpha = 0.06f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Avatar placeholder — circle with initial
            Surface(
                modifier = Modifier.size(64.dp),
                shape = CircleShape,
                color = Maroon,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    val initial = (profile.firstName.firstOrNull() ?: '?').uppercaseChar()
                    Text(
                        text = initial.toString(),
                        color = Color.White,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${profile.firstName} ${profile.lastName}",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = profile.email,
                    fontSize = 13.sp,
                    color = TextSecondary,
                )
                if (profile.emailVerified) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Success.copy(alpha = 0.1f),
                    ) {
                        Text(
                            text = stringResource(R.string.profile_verified),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = Success,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        )
                    }
                }
            }
        }
    }
}

// ===== SECTION HEADER =====

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold,
        color = TextSecondary,
        letterSpacing = 0.5.sp,
        modifier = Modifier.padding(start = 4.dp, bottom = 8.dp),
    )
}

// ===== PROFILE INFO ROW =====

@Composable
private fun ProfileInfoRow(
    label: String,
    value: String,
    editable: Boolean,
    onEdit: (() -> Unit)?,
    leadingIcon: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (leadingIcon != null) {
            leadingIcon()
            Spacer(modifier = Modifier.width(8.dp))
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                fontSize = 11.sp,
                color = TextSecondary,
                fontWeight = FontWeight.Medium,
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                fontSize = 14.sp,
                color = TextPrimary,
                fontWeight = FontWeight.Normal,
                lineHeight = 19.sp,
            )
        }

        if (editable && onEdit != null) {
            IconButton(
                onClick = onEdit,
                modifier = Modifier.size(32.dp),
            ) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = stringResource(R.string.profile_edit),
                    tint = Maroon,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
    }
}

// ===== EDITABLE FIELD EDITOR =====

@Composable
private fun EditableFieldEditor(
    label: String,
    fields: List<Pair<String, String>>,
    onValueChange: (Int, String) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit,
    isSaving: Boolean,
    singleLine: Boolean = true,
    minLines: Int = 1,
    maxLines: Int = 1,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
) {
    Column(
        modifier = Modifier.padding(12.dp),
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            color = TextSecondary,
            fontWeight = FontWeight.Medium,
        )
        Spacer(modifier = Modifier.height(6.dp))

        fields.forEachIndexed { index, (placeholder, value) ->
            OutlinedTextField(
                value = value,
                onValueChange = { onValueChange(index, it) },
                placeholder = { Text(placeholder, fontSize = 13.sp, color = TextSecondary) },
                singleLine = singleLine,
                minLines = minLines,
                maxLines = maxLines,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Maroon,
                    focusedLabelColor = Maroon,
                    cursorColor = Maroon,
                    unfocusedBorderColor = Color(0xFFE5E5E5),
                ),
                keyboardOptions = keyboardOptions,
                enabled = !isSaving,
            )
            if (index < fields.lastIndex) {
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Save / Cancel buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
        ) {
            TextButton(
                onClick = onCancel,
                enabled = !isSaving,
            ) {
                Text(
                    text = stringResource(R.string.profile_cancel),
                    color = TextSecondary,
                    fontSize = 13.sp,
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = onSave,
                enabled = !isSaving,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Maroon,
                    disabledContainerColor = Maroon.copy(alpha = 0.5f),
                ),
                modifier = Modifier.height(36.dp),
                contentPadding = PaddingValues(horizontal = 20.dp),
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = Color.White,
                        strokeWidth = 2.dp,
                    )
                } else {
                    Text(
                        text = stringResource(R.string.profile_save),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White,
                    )
                }
            }
        }
    }
}

// ===== PREFERENCE ROW WITH SWITCH =====

@Composable
private fun PreferenceRow(
    label: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimary,
            )
            Spacer(modifier = Modifier.height(1.dp))
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = TextSecondary,
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedTrackColor = Maroon,
                checkedThumbColor = Color.White,
                uncheckedTrackColor = Color(0xFFE0E0E0),
                uncheckedThumbColor = Color.White,
            ),
        )
    }
}
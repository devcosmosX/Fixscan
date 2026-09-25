package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import com.example.R
import com.example.ui.components.FrostedBackground
import com.example.ui.components.FrostedGlassButton
import com.example.ui.components.FrostedGlassCard
import com.example.ui.components.FrostedGlassSurface
import com.example.ui.theme.*
import com.example.ui.viewmodel.FixScanViewModel
import com.example.ui.viewmodel.InAppNotification
import kotlinx.coroutines.delay

@Composable
fun AuthScreen(
    authMode: FixScanViewModel.AuthMode,
    email: String,
    password: String,
    confirmPassword: String,
    displayName: String,
    rememberMe: Boolean,
    authError: String?,
    isLoading: Boolean,
    inAppNotification: InAppNotification?,
    onModeChange: (FixScanViewModel.AuthMode) -> Unit,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onDisplayNameChange: (String) -> Unit,
    onRememberMeChange: (Boolean) -> Unit,
    onDismissInAppNotification: () -> Unit,
    onClearAuthError: () -> Unit = {},
    onForgotPassword: (String) -> Unit,
    onSubmit: () -> Unit,
    onGoogleSignIn: () -> Unit,
    onContinueAsGuest: () -> Unit
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var showForgotPasswordDialog by remember { mutableStateOf(false) }
    var forgotEmail by remember { mutableStateOf("") }
    val isSignUp = authMode == FixScanViewModel.AuthMode.SIGN_UP

    // Notification Permission Handling (Android 13+)
    var hasNotificationPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            } else {
                true
            }
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasNotificationPermission = isGranted
    }

    // Auto-dismiss in-app notification after 3.5 seconds
    LaunchedEffect(inAppNotification) {
        if (inAppNotification != null) {
            delay(3500)
            onDismissInAppNotification()
        }
    }

    FrostedBackground {
        Box(modifier = Modifier.fillMaxSize()) {
            Scaffold(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("auth_screen"),
                containerColor = Color.Transparent
            ) { innerPadding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(18.dp)
                ) {

                    // ── Header & Branding ────────────────────────────────────
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        FrostedGlassSurface(
                            shape = RoundedCornerShape(26.dp),
                            backgroundColor = Color.White.copy(alpha = 0.88f),
                            borderColor = Color.White,
                            modifier = Modifier
                                .size(92.dp)
                                .shadow(8.dp, RoundedCornerShape(26.dp), ambientColor = PrimaryBlue, spotColor = PrimaryBlue)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.img_app_icon),
                                contentDescription = "FixScan Logo",
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(RoundedCornerShape(20.dp)),
                                contentScale = ContentScale.Crop
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "FixScan",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = NeutralDark,
                                letterSpacing = 0.5.sp
                            )
                        )

                        Text(
                            text = "AI Appliance Diagnostics & Cost Estimator",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = NeutralMedium,
                                fontWeight = FontWeight.Medium
                            ),
                            textAlign = TextAlign.Center
                        )
                    }

                    // ── Notification Permission Prompt Banner (Android 13+) ──
                    if (!hasNotificationPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        NotificationPermissionBanner(
                            onRequestPermission = {
                                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            }
                        )
                    }

                    // ── Mode Switcher Tabs ────────────────────────────────────
                    FrostedGlassSurface(
                        shape = RoundedCornerShape(16.dp),
                        backgroundColor = Color.White.copy(alpha = 0.65f),
                        borderColor = Color.White.copy(alpha = 0.85f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .shadow(2.dp, RoundedCornerShape(16.dp))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            AuthTabButton(
                                label = "Sign In",
                                selected = !isSignUp,
                                modifier = Modifier.weight(1f),
                                onClick = { onModeChange(FixScanViewModel.AuthMode.SIGN_IN) }
                            )
                            AuthTabButton(
                                label = "Create Account",
                                selected = isSignUp,
                                modifier = Modifier.weight(1f),
                                onClick = { onModeChange(FixScanViewModel.AuthMode.SIGN_UP) }
                            )
                        }
                    }

                    // ── Form Container Card ───────────────────────────────────
                    FrostedGlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(22.dp),
                        backgroundColor = Color.White.copy(alpha = 0.82f),
                        borderColor = Color.White.copy(alpha = 0.9f),
                        elevation = 6.dp
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {

                            // Full Name field (Sign Up only)
                            AnimatedVisibility(
                                visible = isSignUp,
                                enter = fadeIn() + expandVertically(),
                                exit = fadeOut() + shrinkVertically()
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(
                                        text = "Full Name",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            color = NeutralDark,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    )
                                    AuthTextField(
                                        value = displayName,
                                        onValueChange = onDisplayNameChange,
                                        placeholder = "e.g. Deepak Gavand",
                                        leadingIcon = {
                                            Icon(
                                                Icons.Default.Person,
                                                contentDescription = null,
                                                tint = SecondaryTeal,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        },
                                        keyboardOptions = KeyboardOptions(
                                            keyboardType = KeyboardType.Text,
                                            imeAction = ImeAction.Next
                                        ),
                                        keyboardActions = KeyboardActions(
                                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                                        ),
                                        testTag = "auth_name_field"
                                    )
                                }
                            }

                            // Email Address field
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = "Email Address",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        color = NeutralDark,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                )
                                AuthTextField(
                                    value = email,
                                    onValueChange = onEmailChange,
                                    placeholder = "name@example.com",
                                    leadingIcon = {
                                        Icon(
                                            Icons.Default.Email,
                                            contentDescription = null,
                                            tint = SecondaryTeal,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    },
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Email,
                                        imeAction = ImeAction.Next
                                    ),
                                    keyboardActions = KeyboardActions(
                                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                                    ),
                                    testTag = "auth_email_field"
                                )
                            }

                            // Password field
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = "Password",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        color = NeutralDark,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                )
                                AuthTextField(
                                    value = password,
                                    onValueChange = onPasswordChange,
                                    placeholder = "At least 6 characters",
                                    leadingIcon = {
                                        Icon(
                                            Icons.Default.Lock,
                                            contentDescription = null,
                                            tint = SecondaryTeal,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    },
                                    trailingIcon = {
                                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                            Icon(
                                                imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                                contentDescription = if (passwordVisible) "Hide password" else "Show password",
                                                tint = NeutralMedium,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    },
                                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Password,
                                        imeAction = if (isSignUp) ImeAction.Next else ImeAction.Done
                                    ),
                                    keyboardActions = KeyboardActions(
                                        onNext = { focusManager.moveFocus(FocusDirection.Down) },
                                        onDone = {
                                            focusManager.clearFocus()
                                            onSubmit()
                                        }
                                    ),
                                    testTag = "auth_password_field"
                                )

                                // Password Strength Meter (Sign Up only)
                                if (isSignUp && password.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    PasswordStrengthMeter(password = password)
                                }
                            }

                            // Confirm Password field (Sign Up only)
                            AnimatedVisibility(
                                visible = isSignUp,
                                enter = fadeIn() + expandVertically(),
                                exit = fadeOut() + shrinkVertically()
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(
                                        text = "Confirm Password",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            color = NeutralDark,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    )
                                    AuthTextField(
                                        value = confirmPassword,
                                        onValueChange = onConfirmPasswordChange,
                                        placeholder = "Re-enter your password",
                                        leadingIcon = {
                                            Icon(
                                                Icons.Default.CheckCircle,
                                                contentDescription = null,
                                                tint = if (confirmPassword.isNotEmpty() && confirmPassword == password) SecondaryTeal else NeutralMedium,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        },
                                        trailingIcon = {
                                            IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                                                Icon(
                                                    imageVector = if (confirmPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                                    contentDescription = if (confirmPasswordVisible) "Hide confirm password" else "Show confirm password",
                                                    tint = NeutralMedium,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        },
                                        visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                        keyboardOptions = KeyboardOptions(
                                            keyboardType = KeyboardType.Password,
                                            imeAction = ImeAction.Done
                                        ),
                                        keyboardActions = KeyboardActions(
                                            onDone = {
                                                focusManager.clearFocus()
                                                onSubmit()
                                            }
                                        ),
                                        testTag = "auth_confirm_password_field"
                                    )
                                    if (confirmPassword.isNotEmpty() && confirmPassword != password) {
                                        Text(
                                            text = "⚠️ Passwords do not match",
                                            style = MaterialTheme.typography.labelSmall.copy(color = SafetyRed),
                                            modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                                        )
                                    }
                                }
                            }

                            // Remember Me & Forgot Password (Sign In mode)
                            if (!isSignUp) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.clickable { onRememberMeChange(!rememberMe) }
                                    ) {
                                        Checkbox(
                                            checked = rememberMe,
                                            onCheckedChange = onRememberMeChange,
                                            colors = CheckboxDefaults.colors(
                                                checkedColor = PrimaryBlue,
                                                checkmarkColor = Color.White
                                            ),
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Remember me",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = NeutralDark,
                                                fontWeight = FontWeight.Medium
                                            )
                                        )
                                    }

                                    TextButton(
                                        onClick = {
                                            forgotEmail = email
                                            showForgotPasswordDialog = true
                                        },
                                        contentPadding = PaddingValues(0.dp)
                                    ) {
                                        Text(
                                            text = "Forgot Password?",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = SecondaryTeal,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        )
                                    }
                                }
                            }

                            // Error Message Banner
                            AnimatedVisibility(visible = authError != null) {
                                authError?.let {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(SafetyRedLight)
                                            .padding(horizontal = 14.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Default.Info,
                                            contentDescription = null,
                                            tint = SafetyRed,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Text(
                                            text = it,
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = SafetyRed,
                                                fontWeight = FontWeight.Medium
                                            ),
                                            modifier = Modifier.weight(1f)
                                        )
                                        IconButton(
                                            onClick = onClearAuthError,
                                            modifier = Modifier.size(20.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.Close,
                                                contentDescription = "Dismiss error",
                                                tint = SafetyRed,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            // Primary Submit Button
                            FrostedGlassButton(
                                onClick = onSubmit,
                                enabled = !isLoading,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                                    .testTag("auth_submit_button"),
                                containerColor = PrimaryBlue
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(22.dp),
                                        color = Color.White,
                                        strokeWidth = 2.5.dp
                                    )
                                    Spacer(Modifier.width(12.dp))
                                    Text(
                                        text = if (isSignUp) "Creating Account…" else "Signing In…",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                } else {
                                    Icon(
                                        imageVector = if (isSignUp) Icons.Default.PersonAdd else Icons.AutoMirrored.Filled.Login,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        text = if (isSignUp) "Create FixScan Account" else "Sign In to FixScan",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                }
                            }
                        }
                    }

                    // ── Or Divider ────────────────────────────────────────────
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
                    ) {
                        HorizontalDivider(
                            modifier = Modifier.weight(1f),
                            color = NeutralMedium.copy(alpha = 0.25f)
                        )
                        Text(
                            text = "  or continue with  ",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = NeutralMedium,
                                fontWeight = FontWeight.Medium
                            )
                        )
                        HorizontalDivider(
                            modifier = Modifier.weight(1f),
                            color = NeutralMedium.copy(alpha = 0.25f)
                        )
                    }

                    // ── Branded Google Sign-In Button ─────────────────────────
                    GoogleSignInButton(
                        isLoading = isLoading,
                        onClick = onGoogleSignIn
                    )

                    // ── Guest Mode Link ───────────────────────────────────────
                    TextButton(
                        onClick = onContinueAsGuest,
                        modifier = Modifier.testTag("auth_guest_button")
                    ) {
                        Text(
                            text = "Skip for now & Continue as Guest →",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = NeutralDark,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }

                    // ── Security Footer ───────────────────────────────────────
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(bottom = 12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = NeutralMedium,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Encrypted Diagnostics · Instant Push Notifications",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = NeutralMedium,
                                fontSize = 11.5.sp
                            )
                        )
                    }
                }
            }

            // ── Top Animated In-App Notification Toast ────────────────────────
            AnimatedVisibility(
                visible = inAppNotification != null,
                enter = fadeIn() + slideInVertically { -it },
                exit = fadeOut() + slideOutVertically { -it },
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 16.dp, start = 16.dp, end = 16.dp)
            ) {
                inAppNotification?.let { notif ->
                    InAppNotificationBanner(
                        notification = notif,
                        onDismiss = onDismissInAppNotification
                    )
                }
            }

            // ── Forgot Password Modal Dialog ──────────────────────────────────
            if (showForgotPasswordDialog) {
                ForgotPasswordDialog(
                    initialEmail = forgotEmail,
                    onDismiss = { showForgotPasswordDialog = false },
                    onSend = { targetEmail ->
                        showForgotPasswordDialog = false
                        onForgotPassword(targetEmail)
                    }
                )
            }
        }
    }
}

// ── Google Sign-In Button Component ──────────────────────────────────────────

@Composable
fun GoogleSignInButton(
    isLoading: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        enabled = !isLoading,
        shape = RoundedCornerShape(16.dp),
        color = Color.White.copy(alpha = 0.92f),
        border = BorderStroke(1.dp, Color.White),
        shadowElevation = 3.dp,
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp)
            .testTag("auth_google_button")
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_google_logo),
                contentDescription = "Google Logo",
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Continue with Google",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = NeutralDark
                )
            )
        }
    }
}

// ── Notification Permission Prompt Banner ────────────────────────────────────

@Composable
private fun NotificationPermissionBanner(
    onRequestPermission: () -> Unit
) {
    FrostedGlassSurface(
        shape = RoundedCornerShape(14.dp),
        backgroundColor = SecondaryTeal.copy(alpha = 0.15f),
        borderColor = SecondaryTeal.copy(alpha = 0.4f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.NotificationsActive,
                    contentDescription = null,
                    tint = PrimaryBlue,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Enable Notifications",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = PrimaryBlue
                        )
                    )
                    Text(
                        text = "Get real-time scan results & login safety alerts",
                        style = MaterialTheme.typography.bodySmall.copy(color = NeutralDark)
                    )
                }
            }
            Spacer(Modifier.width(8.dp))
            Button(
                onClick = onRequestPermission,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "Allow",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}

// ── In-App Notification Banner Component ─────────────────────────────────────

@Composable
private fun InAppNotificationBanner(
    notification: InAppNotification,
    onDismiss: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF0F3A3D).copy(alpha = 0.94f),
        border = BorderStroke(1.dp, BrightCyan.copy(alpha = 0.5f)),
        shadowElevation = 8.dp,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onDismiss() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .background(Color.White.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = null,
                    tint = BrightCyan,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = notification.title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    text = notification.message,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color.White.copy(alpha = 0.85f)
                    )
                )
            }
            IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Dismiss",
                    tint = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

// ── Password Strength Meter Component ────────────────────────────────────────

@Composable
private fun PasswordStrengthMeter(password: String) {
    val strength = remember(password) {
        var score = 0
        if (password.length >= 6) score++
        if (password.length >= 8) score++
        if (password.any { it.isDigit() }) score++
        if (password.any { !it.isLetterOrDigit() }) score++
        score
    }

    val (color, label) = when {
        strength <= 1 -> SafetyRed to "Weak"
        strength in 2..3 -> AccentAmber to "Good"
        else -> SecondaryTeal to "Strong"
    }

    Column(modifier = Modifier.fillMaxWidth().padding(top = 2.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Password Strength: $label",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = color,
                    fontWeight = FontWeight.Bold
                )
            )
            Text(
                text = "Min. 6 characters",
                style = MaterialTheme.typography.labelSmall.copy(color = NeutralMedium)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            for (i in 1..4) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(if (i <= strength) color else Color.Black.copy(alpha = 0.08f))
                )
            }
        }
    }
}

// ── Forgot Password Dialog ───────────────────────────────────────────────────

@Composable
private fun ForgotPasswordDialog(
    initialEmail: String,
    onDismiss: () -> Unit,
    onSend: (String) -> Unit
) {
    var emailInput by remember { mutableStateOf(initialEmail) }

    Dialog(onDismissRequest = onDismiss) {
        FrostedGlassCard(
            shape = RoundedCornerShape(20.dp),
            backgroundColor = Color.White.copy(alpha = 0.95f),
            borderColor = Color.White,
            elevation = 10.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(22.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Reset Password",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = NeutralDark
                        )
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = NeutralMedium)
                    }
                }

                Text(
                    text = "Enter your registered email address. We'll send you instructions to recover your account.",
                    style = MaterialTheme.typography.bodyMedium.copy(color = NeutralMedium)
                )

                OutlinedTextField(
                    value = emailInput,
                    onValueChange = { emailInput = it },
                    label = { Text("Email Address") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SecondaryTeal,
                        unfocusedBorderColor = NeutralMedium.copy(alpha = 0.3f),
                        focusedLabelColor = SecondaryTeal
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f).height(46.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Cancel", style = MaterialTheme.typography.labelLarge)
                    }
                    Button(
                        onClick = { onSend(emailInput) },
                        modifier = Modifier.weight(1f).height(46.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                    ) {
                        Text("Send Link", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
                    }
                }
            }
        }
    }
}

// ── Tab Button Helper ────────────────────────────────────────────────────────

@Composable
private fun AuthTabButton(
    label: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier.fillMaxHeight(),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selected) PrimaryBlue else Color.Transparent,
            contentColor = if (selected) Color.White else NeutralDark
        ),
        elevation = if (selected) ButtonDefaults.buttonElevation(defaultElevation = 2.dp) else null
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
            ),
            maxLines = 1
        )
    }
}

// ── Custom Auth Outlined TextField Helper ────────────────────────────────────

@Composable
private fun AuthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    testTag: String = ""
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = {
            Text(
                placeholder,
                style = MaterialTheme.typography.bodyMedium.copy(color = NeutralMedium.copy(alpha = 0.6f))
            )
        },
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        singleLine = true,
        modifier = Modifier
            .fillMaxWidth()
            .testTag(testTag),
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = SecondaryTeal,
            unfocusedBorderColor = NeutralMedium.copy(alpha = 0.25f),
            focusedLabelColor = SecondaryTeal,
            unfocusedLabelColor = NeutralMedium,
            cursorColor = SecondaryTeal,
            focusedContainerColor = Color.White.copy(alpha = 0.7f),
            unfocusedContainerColor = Color.White.copy(alpha = 0.45f)
        )
    )
}

package com.example.ui.navigation

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.screens.*
import com.example.ui.viewmodel.FixScanViewModel
import com.example.ui.viewmodel.ScanUiState
import kotlinx.coroutines.launch

import com.example.ui.components.PaywallDialog
import com.example.model.PaywallTriggerReason

@Composable
fun FixScanNavGraph(
    viewModel: FixScanViewModel,
    navController: NavHostController = rememberNavController()
) {
    val context = LocalContext.current
    val userSession by viewModel.userSession.collectAsState()
    val scanHistory by viewModel.scanHistory.collectAsState()
    val selectedPhotoUri by viewModel.selectedPhotoUri.collectAsState()
    val applianceType by viewModel.applianceType.collectAsState()
    val brand by viewModel.brand.collectAsState()
    val approxAge by viewModel.approxAge.collectAsState()
    val selectedSymptoms by viewModel.selectedSymptoms.collectAsState()
    val note by viewModel.note.collectAsState()
    val scanUiState by viewModel.scanUiState.collectAsState()
    val activeDiagnosis by viewModel.activeDiagnosis.collectAsState()
    val chatMessages by viewModel.chatMessages.collectAsState()
    val isSendingChat by viewModel.isSendingChat.collectAsState()

    // Vision and Places states
    val visionResult by viewModel.visionResult.collectAsState()
    val isDetectingObjects by viewModel.isDetectingObjects.collectAsState()
    val mechanicsUiState by viewModel.mechanicsUiState.collectAsState()

    val usageStatusCopy by viewModel.usageStatusCopy.collectAsState()
    val accountWelcomeCopy by viewModel.accountWelcomeCopy.collectAsState()
    val showPaywallDialog by viewModel.showPaywallDialog.collectAsState()
    val paywallCopy by viewModel.paywallCopy.collectAsState()

    // Auth state
    val authMode by viewModel.authMode.collectAsState()
    val authEmail by viewModel.authEmail.collectAsState()
    val authPassword by viewModel.authPassword.collectAsState()
    val authConfirmPassword by viewModel.authConfirmPassword.collectAsState()
    val authDisplayName by viewModel.authDisplayName.collectAsState()
    val authRememberMe by viewModel.authRememberMe.collectAsState()
    val authError by viewModel.authError.collectAsState()
    val isAuthLoading by viewModel.isAuthLoading.collectAsState()
    val inAppNotification by viewModel.inAppNotification.collectAsState()

    if (showPaywallDialog) {
        PaywallDialog(
            headline = paywallCopy?.headline ?: "Upgrade FixScan Plan",
            subtext = paywallCopy?.subtext ?: "Get unlimited appliance scans and access to all verified local mechanics.",
            onDismiss = { viewModel.dismissPaywall() },
            onUpgradeClicked = { viewModel.dismissPaywall() }
        )
    }

    NavHost(
        navController = navController,
        startDestination = Screen.Auth.route
    ) {
        composable(Screen.Auth.route) {
            AuthScreen(
                authMode = authMode,
                email = authEmail,
                password = authPassword,
                confirmPassword = authConfirmPassword,
                displayName = authDisplayName,
                rememberMe = authRememberMe,
                authError = authError,
                isLoading = isAuthLoading,
                inAppNotification = inAppNotification,
                onModeChange = { viewModel.setAuthMode(it) },
                onEmailChange = { viewModel.setAuthEmail(it) },
                onPasswordChange = { viewModel.setAuthPassword(it) },
                onConfirmPasswordChange = { viewModel.setAuthConfirmPassword(it) },
                onDisplayNameChange = { viewModel.setAuthDisplayName(it) },
                onRememberMeChange = { viewModel.setAuthRememberMe(it) },
                onDismissInAppNotification = { viewModel.dismissInAppNotification() },
                onClearAuthError = { viewModel.clearAuthError() },
                onForgotPassword = { email -> viewModel.sendPasswordReset(email) {} },
                onSubmit = {
                    viewModel.submitAuth {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Auth.route) { inclusive = true }
                        }
                    }
                },
                onGoogleSignIn = {
                    viewModel.signInWithGoogle(context) {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Auth.route) { inclusive = true }
                        }
                    }
                },
                onContinueAsGuest = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Auth.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.SignIn.route) {
            SignInScreen(
                userSession = userSession,
                onToggleAuth = { viewModel.signOut() },
                onContinueAsGuest = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.SignIn.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.UserDashboard.route) {
            UserDashboardScreen(
                userSession = userSession,
                scanHistory = scanHistory,
                onBack = { navController.popBackStack() },
                onStartNewScan = {
                    viewModel.resetForm()
                    navController.navigate(Screen.CameraCapture.route)
                },
                onOpenHistory = {
                    navController.navigate(Screen.ScanHistory.route)
                },
                onOpenScanDetail = { diag ->
                    viewModel.openDiagnosisResult(diag)
                    navController.navigate(Screen.ScanResults.route)
                },
                onOpenBillingAssistant = {
                    navController.navigate(Screen.BillingAssistant.route)
                },
                onOpenDiagnosticEngine = {
                    navController.navigate(Screen.DiagnosticEngine.route)
                },
                onOpenMechanicsMap = {
                    navController.navigate(Screen.MechanicsMap.route)
                },
                onOpenPaywall = { triggerReason ->
                    viewModel.openPaywall(triggerReason)
                },
                onSignOut = {
                    viewModel.signOut()
                    navController.navigate(Screen.Auth.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                },
                onSignIn = {
                    navController.navigate(Screen.Auth.route)
                }
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                userSession = userSession,
                scanHistory = scanHistory,
                usageStatusCopy = usageStatusCopy,
                accountWelcomeCopy = accountWelcomeCopy,
                onStartNewScan = {
                    viewModel.resetForm()
                    navController.navigate(Screen.CameraCapture.route)
                },
                onSelectCategory = { catName ->
                    viewModel.resetForm()
                    viewModel.setApplianceType(catName)
                    navController.navigate(Screen.GuidedForm.route)
                },
                onOpenScanDetail = { diag ->
                    viewModel.openDiagnosisResult(diag)
                    navController.navigate(Screen.ScanResults.route)
                },
                onOpenHistory = {
                    navController.navigate(Screen.ScanHistory.route)
                },
                onOpenDashboard = {
                    navController.navigate(Screen.UserDashboard.route)
                },
                onOpenSignIn = {
                    viewModel.signOut()
                    navController.navigate(Screen.Auth.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                },
                onOpenBillingAssistant = {
                    navController.navigate(Screen.BillingAssistant.route)
                },
                onOpenDiagnosticEngine = {
                    navController.navigate(Screen.DiagnosticEngine.route)
                },
                onOpenPaywall = { triggerReason ->
                    viewModel.openPaywall(triggerReason)
                },
                onOpenMechanicsMap = {
                    navController.navigate(Screen.MechanicsMap.route)
                }
            )
        }

        composable(Screen.CameraCapture.route) {
            CameraCaptureScreen(
                currentPhotoUri = selectedPhotoUri,
                visionResult = visionResult,
                isDetectingObjects = isDetectingObjects,
                onPhotoCaptured = { uri -> viewModel.setPhotoUri(uri) },
                onDetectObjects = { uri -> viewModel.detectObjectsWithVision(uri) },
                onSelectDetectedObject = { name ->
                    viewModel.setApplianceType(name)
                    navController.navigate(Screen.GuidedForm.route)
                },
                onOpenMechanicsMap = { navController.navigate(Screen.MechanicsMap.route) },
                onProceedToForm = { navController.navigate(Screen.GuidedForm.route) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.GuidedForm.route) {
            GuidedFormScreen(
                applianceType = applianceType,
                brand = brand,
                approxAge = approxAge,
                selectedSymptoms = selectedSymptoms,
                note = note,
                photoUri = selectedPhotoUri,
                visionResult = visionResult,
                isAnalyzing = scanUiState is ScanUiState.Analyzing,
                errorMessage = (scanUiState as? ScanUiState.Error)?.message,
                onApplianceTypeChanged = { viewModel.setApplianceType(it) },
                onBrandChanged = { viewModel.setBrand(it) },
                onApproxAgeChanged = { viewModel.setApproxAge(it) },
                onSymptomToggled = { viewModel.toggleSymptom(it) },
                onNoteChanged = { viewModel.setNote(it) },
                onChangePhoto = { navController.navigate(Screen.CameraCapture.route) },
                onSubmitScan = {
                    viewModel.analyzeAppliance { result ->
                        navController.navigate(Screen.ScanResults.route) {
                            popUpTo(Screen.Home.route)
                        }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.ScanResults.route) {
            activeDiagnosis?.let { diagnosis ->
                ScanResultsScreen(
                    diagnosis = diagnosis,
                    onOpenChat = { navController.navigate(Screen.FollowUpChat.route) },
                    onNewScan = {
                        viewModel.resetForm()
                        navController.navigate(Screen.CameraCapture.route) {
                            popUpTo(Screen.Home.route)
                        }
                    },
                    onOpenMechanicsMap = { navController.navigate(Screen.MechanicsMap.route) },
                    onBack = { navController.navigate(Screen.Home.route) { popUpTo(Screen.Home.route) { inclusive = true } } }
                )
            }
        }

        composable(Screen.FollowUpChat.route) {
            activeDiagnosis?.let { diagnosis ->
                FollowUpChatScreen(
                    diagnosis = diagnosis,
                    chatMessages = chatMessages,
                    isSending = isSendingChat,
                    onSendMessage = { text -> viewModel.sendChatMessage(text) },
                    onBack = { navController.popBackStack() }
                )
            }
        }

        composable(Screen.ScanHistory.route) {
            ScanHistoryScreen(
                scanHistory = scanHistory,
                onSelectScan = { diag ->
                    viewModel.openDiagnosisResult(diag)
                    navController.navigate(Screen.ScanResults.route)
                },
                onDeleteScan = { id -> viewModel.deleteScan(id) },
                onClearHistory = { viewModel.clearAllHistory() },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.BillingAssistant.route) {
            BillingAssistantScreen(
                onGenerateScenarioCopy = { scenario, inputMap, onResult ->
                    viewModel.generateBillingCopy(scenario, inputMap, onResult)
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.DiagnosticEngine.route) {
            val diagnosticAssessment by viewModel.diagnosticAssessment.collectAsState()
            val isAnalyzingDiagnosticPhotos by viewModel.isAnalyzingDiagnosticPhotos.collectAsState()

            DiagnosticEngineScreen(
                assessment = diagnosticAssessment,
                isAnalyzing = isAnalyzingDiagnosticPhotos,
                onAnalyzePhotos = { appUri, damUri ->
                    viewModel.analyzeAppliancePhotos(appUri, damUri)
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.MechanicsMap.route) {
            val coroutineScope = rememberCoroutineScope()
            LaunchedEffect(Unit) {
                val loc = com.example.util.LocationHelper.getCurrentLocation(context)
                viewModel.loadNearbyMechanics(loc.latitude, loc.longitude)
            }

            MechanicsMapScreen(
                uiState = mechanicsUiState,
                onRefresh = {
                    coroutineScope.launch {
                        val loc = com.example.util.LocationHelper.getCurrentLocation(context)
                        viewModel.loadNearbyMechanics(loc.latitude, loc.longitude)
                    }
                },
                onSelectMechanic = { id -> viewModel.selectMechanic(id) },
                onBack = { navController.popBackStack() }
            )
        }
    }
}



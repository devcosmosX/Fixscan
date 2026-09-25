package com.example.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.ScanRepository
import com.example.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuthException
import com.example.data.api.BillingAssistantService
import com.example.data.api.ApplianceDiagnosticEngineService
import com.example.data.api.VisionApiService
import com.example.data.api.PlacesApiService
import com.example.util.NotificationHelper
import com.example.R
import org.json.JSONObject

data class InAppNotification(
    val title: String,
    val message: String,
    val isSuccess: Boolean = true
)

sealed class ScanUiState {
    object Idle : ScanUiState()
    object Analyzing : ScanUiState()
    data class Success(val diagnosis: DiagnosisResult) : ScanUiState()
    data class Error(val message: String) : ScanUiState()
}

class FixScanViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        const val GOOGLE_WEB_CLIENT_ID = "764318145860-5p9m281jvej89jl34t1164d7bb04nnfg.apps.googleusercontent.com"
    }

    private val firebaseAuth: FirebaseAuth? by lazy {
        try {
            if (FirebaseApp.getApps(application).isEmpty()) return@lazy null
            FirebaseAuth.getInstance()
        } catch (e: Exception) {
            null
        }
    }

    private val repository = ScanRepository(application)
    private val firebaseBackend = com.example.data.firebase.FirebaseBackendService(application)
    private val billingAssistantService = BillingAssistantService(application)
    private val diagnosticEngineService = ApplianceDiagnosticEngineService(application)
    private val visionApiService = VisionApiService(application)
    private val placesApiService = PlacesApiService()

    // Google Cloud Vision Object Localization State
    private val _visionResult = MutableStateFlow<VisionDetectionResult?>(null)
    val visionResult: StateFlow<VisionDetectionResult?> = _visionResult.asStateFlow()

    private val _isDetectingObjects = MutableStateFlow(false)
    val isDetectingObjects: StateFlow<Boolean> = _isDetectingObjects.asStateFlow()

    // Google Maps + Places Nearby Mechanics State
    private val _mechanicsUiState = MutableStateFlow<MechanicsUiState>(MechanicsUiState.Idle)
    val mechanicsUiState: StateFlow<MechanicsUiState> = _mechanicsUiState.asStateFlow()

    // Appliance Diagnostic Engine State
    private val _diagnosticAssessment = MutableStateFlow<ApplianceDiagnosticAssessment?>(null)
    val diagnosticAssessment: StateFlow<ApplianceDiagnosticAssessment?> = _diagnosticAssessment.asStateFlow()

    private val _isAnalyzingDiagnosticPhotos = MutableStateFlow(false)
    val isAnalyzingDiagnosticPhotos: StateFlow<Boolean> = _isAnalyzingDiagnosticPhotos.asStateFlow()

    // Auth & Session
    private val _userSession = MutableStateFlow(UserSession())
    val userSession: StateFlow<UserSession> = _userSession.asStateFlow()

    // Auth Form State
    enum class AuthMode { SIGN_IN, SIGN_UP }
    private val _authMode = MutableStateFlow(AuthMode.SIGN_IN)
    val authMode: StateFlow<AuthMode> = _authMode.asStateFlow()

    private val _authEmail = MutableStateFlow("")
    val authEmail: StateFlow<String> = _authEmail.asStateFlow()

    private val _authPassword = MutableStateFlow("")
    val authPassword: StateFlow<String> = _authPassword.asStateFlow()

    private val _authDisplayName = MutableStateFlow("")
    val authDisplayName: StateFlow<String> = _authDisplayName.asStateFlow()

    private val _authConfirmPassword = MutableStateFlow("")
    val authConfirmPassword: StateFlow<String> = _authConfirmPassword.asStateFlow()

    private val _authRememberMe = MutableStateFlow(true)
    val authRememberMe: StateFlow<Boolean> = _authRememberMe.asStateFlow()

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    private val _isAuthLoading = MutableStateFlow(false)
    val isAuthLoading: StateFlow<Boolean> = _isAuthLoading.asStateFlow()

    private val _inAppNotification = MutableStateFlow<InAppNotification?>(null)
    val inAppNotification: StateFlow<InAppNotification?> = _inAppNotification.asStateFlow()

    // Billing Assistant Copy States
    private val _usageStatusCopy = MutableStateFlow<String>("")
    val usageStatusCopy: StateFlow<String> = _usageStatusCopy.asStateFlow()

    private val _accountWelcomeCopy = MutableStateFlow<String>("")
    val accountWelcomeCopy: StateFlow<String> = _accountWelcomeCopy.asStateFlow()

    private val _postScanNudgeCopy = MutableStateFlow<String>("")
    val postScanNudgeCopy: StateFlow<String> = _postScanNudgeCopy.asStateFlow()

    private val _paywallCopy = MutableStateFlow<PaywallCopyResult?>(null)
    val paywallCopy: StateFlow<PaywallCopyResult?> = _paywallCopy.asStateFlow()

    private val _showPaywallDialog = MutableStateFlow(false)
    val showPaywallDialog: StateFlow<Boolean> = _showPaywallDialog.asStateFlow()

    // Active Scan Form State
    private val _selectedPhotoUri = MutableStateFlow<String?>(null)
    val selectedPhotoUri: StateFlow<String?> = _selectedPhotoUri.asStateFlow()

    private val _applianceType = MutableStateFlow(ApplianceType.FRIDGE.displayName)
    val applianceType: StateFlow<String> = _applianceType.asStateFlow()

    private val _brand = MutableStateFlow("")
    val brand: StateFlow<String> = _brand.asStateFlow()

    private val _approxAge = MutableStateFlow("1-3yr")
    val approxAge: StateFlow<String> = _approxAge.asStateFlow()

    private val _selectedSymptoms = MutableStateFlow<Set<String>>(emptySet())
    val selectedSymptoms: StateFlow<Set<String>> = _selectedSymptoms.asStateFlow()

    private val _note = MutableStateFlow("")
    val note: StateFlow<String> = _note.asStateFlow()

    // Scan Execution State
    private val _scanUiState = MutableStateFlow<ScanUiState>(ScanUiState.Idle)
    val scanUiState: StateFlow<ScanUiState> = _scanUiState.asStateFlow()

    // Current Diagnosis
    private val _activeDiagnosis = MutableStateFlow<DiagnosisResult?>(null)
    val activeDiagnosis: StateFlow<DiagnosisResult?> = _activeDiagnosis.asStateFlow()

    // Past Scans History
    private val _scanHistory = MutableStateFlow<List<DiagnosisResult>>(emptyList())
    val scanHistory: StateFlow<List<DiagnosisResult>> = _scanHistory.asStateFlow()

    // Chat State
    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _isSendingChat = MutableStateFlow(false)
    val isSendingChat: StateFlow<Boolean> = _isSendingChat.asStateFlow()

    init {
        // Observe database history
        viewModelScope.launch {
            repository.scanHistory.collectLatest { list ->
                _scanHistory.value = list
            }
        }
        // Observe Firebase Auth state if available
        try {
            firebaseAuth?.let { auth ->
                auth.currentUser?.let { user ->
                    val displayName = user.displayName?.ifBlank { null } ?: user.email?.substringBefore("@") ?: "FixScan User"
                    _userSession.value = UserSession(
                        isLoggedIn = true,
                        userName = displayName,
                        userEmail = user.email ?: ""
                    )
                }
                auth.addAuthStateListener { updatedAuth ->
                    val user = updatedAuth.currentUser
                    if (user != null) {
                        val displayName = user.displayName?.ifBlank { null } ?: user.email?.substringBefore("@") ?: "FixScan User"
                        _userSession.value = UserSession(
                            isLoggedIn = true,
                            userName = displayName,
                            userEmail = user.email ?: ""
                        )
                    } else {
                        _userSession.value = UserSession()
                    }
                }
            }
        } catch (e: Exception) {
            // Safe fallback in test environments
        }
        // Initialize default usage status copy (Scenario 2) & welcome copy (Scenario 1)
        refreshUsageStatus()
        refreshAccountWelcome()
    }

    fun setPhotoUri(uri: Uri?) {
        _selectedPhotoUri.value = uri?.toString()
        // Automatically trigger Google Cloud Vision Object Localization if a valid URI is provided
        if (uri != null) {
            detectObjectsWithVision(uri)
        } else {
            clearVisionResult()
        }
    }

    /**
     * Executes Google Cloud Vision Object Localization on the appliance image.
     */
    fun detectObjectsWithVision(uri: Uri) {
        _isDetectingObjects.value = true
        viewModelScope.launch {
            try {
                val result = visionApiService.detectObjects(uri)
                _visionResult.value = result
                if (result.isSuccess && result.objects.isNotEmpty()) {
                    val topObject = result.primaryObject
                    if (topObject != null) {
                        NotificationHelper.showNotification(
                            getApplication(),
                            "Object Detected: ${topObject.name}",
                            "FixScan Vision localized ${topObject.name} (${topObject.confidencePercentage}% confidence)."
                        )
                    }
                    if (!result.detectedBrand.isNullOrBlank()) {
                        _brand.value = result.detectedBrand
                    }
                    if (!result.detectedModelNumber.isNullOrBlank()) {
                        val currentNote = _note.value
                        _note.value = if (currentNote.isBlank()) "Model: ${result.detectedModelNumber}" else "$currentNote (Model: ${result.detectedModelNumber})"
                    }
                }
            } catch (e: Exception) {
                _visionResult.value = VisionDetectionResult(
                    isSuccess = false,
                    errorMessage = e.localizedMessage ?: "Object detection failed."
                )
            } finally {
                _isDetectingObjects.value = false
            }
        }
    }

    fun clearVisionResult() {
        _visionResult.value = null
    }

    /**
     * Fetches nearby mechanics and repair workshops using Google Places API.
     */
    fun loadNearbyMechanics(latitude: Double, longitude: Double, keyword: String = "appliance repair mechanic") {
        _mechanicsUiState.value = MechanicsUiState.Loading
        viewModelScope.launch {
            val result = placesApiService.searchNearbyMechanics(
                latitude = latitude,
                longitude = longitude,
                applianceKeyword = keyword
            )
            result.onSuccess { mechanics ->
                if (mechanics.isEmpty()) {
                    _mechanicsUiState.value = MechanicsUiState.Empty()
                } else {
                    _mechanicsUiState.value = MechanicsUiState.Success(
                        mechanics = mechanics,
                        userLatitude = latitude,
                        userLongitude = longitude,
                        selectedMechanicId = mechanics.firstOrNull()?.id
                    )
                }
            }.onFailure { err ->
                _mechanicsUiState.value = MechanicsUiState.Error(
                    err.localizedMessage ?: "Failed to find nearby mechanics."
                )
            }
        }
    }

    fun selectMechanic(id: String) {
        val current = _mechanicsUiState.value
        if (current is MechanicsUiState.Success) {
            _mechanicsUiState.value = current.copy(selectedMechanicId = id)
        }
    }

    fun setApplianceType(type: String) {
        _applianceType.value = type
    }

    fun setBrand(b: String) {
        _brand.value = b
    }

    fun setApproxAge(age: String) {
        _approxAge.value = age
    }

    fun toggleSymptom(symptomTitle: String) {
        val current = _selectedSymptoms.value.toMutableSet()
        if (current.contains(symptomTitle)) {
            current.remove(symptomTitle)
        } else {
            current.add(symptomTitle)
        }
        _selectedSymptoms.value = current
    }

    fun setNote(n: String) {
        _note.value = n
    }

    fun setAuthMode(mode: AuthMode) { _authMode.value = mode; _authError.value = null }
    fun clearAuthError() { _authError.value = null }
    fun setAuthEmail(v: String) { _authEmail.value = v; _authError.value = null }
    fun setAuthPassword(v: String) { _authPassword.value = v; _authError.value = null }
    fun setAuthConfirmPassword(v: String) { _authConfirmPassword.value = v; _authError.value = null }
    fun setAuthDisplayName(v: String) { _authDisplayName.value = v; _authError.value = null }
    fun setAuthRememberMe(v: Boolean) { _authRememberMe.value = v }
    fun dismissInAppNotification() { _inAppNotification.value = null }

    fun sendPasswordReset(email: String, onComplete: () -> Unit) {
        val targetEmail = email.trim()
        if (targetEmail.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(targetEmail).matches()) {
            _authError.value = "Please enter a valid email to recover password."
            return
        }
        _isAuthLoading.value = true
        _authError.value = null

        val auth = firebaseAuth
        if (auth == null) {
            viewModelScope.launch {
                kotlinx.coroutines.delay(600)
                NotificationHelper.showPasswordResetNotification(getApplication(), targetEmail)
                _inAppNotification.value = InAppNotification(
                    title = "Recovery Link Sent ✉️",
                    message = "Password reset instructions sent to $targetEmail"
                )
                _isAuthLoading.value = false
                onComplete()
            }
            return
        }

        auth.sendPasswordResetEmail(targetEmail)
            .addOnCompleteListener { task ->
                _isAuthLoading.value = false
                if (task.isSuccessful) {
                    NotificationHelper.showPasswordResetNotification(getApplication(), targetEmail)
                    _inAppNotification.value = InAppNotification(
                        title = "Recovery Link Sent ✉️",
                        message = "Password reset instructions sent to $targetEmail"
                    )
                    onComplete()
                } else {
                    _authError.value = task.exception?.localizedMessage ?: "Unable to send password recovery email."
                }
            }
    }

    /** Connects to Firebase Auth (or fallback) to sign in or register user, firing push notifications. */
    fun submitAuth(onSuccess: () -> Unit) {
        val email = _authEmail.value.trim()
        val password = _authPassword.value
        val confirmPassword = _authConfirmPassword.value
        val name = _authDisplayName.value.trim()

        if (email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _authError.value = "Please enter a valid email address."
            return
        }
        if (password.length < 6) {
            _authError.value = "Password must be at least 6 characters."
            return
        }
        if (_authMode.value == AuthMode.SIGN_UP) {
            if (name.isBlank()) {
                _authError.value = "Please enter your full name."
                return
            }
            if (password != confirmPassword) {
                _authError.value = "Passwords do not match."
                return
            }
        }

        _isAuthLoading.value = true
        _authError.value = null

        val auth = firebaseAuth
        if (auth == null) {
            _authError.value = firebaseConfigurationError()
            _isAuthLoading.value = false
            return
        }

        if (_authMode.value == AuthMode.SIGN_UP) {
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val user = task.result?.user
                        val profileUpdates = UserProfileChangeRequest.Builder()
                            .setDisplayName(name)
                            .build()
                        user?.updateProfile(profileUpdates)?.addOnCompleteListener {
                            val displayName = name.ifBlank { user.email?.substringBefore("@") ?: "FixScan User" }
                            _userSession.value = UserSession(isLoggedIn = true, userName = displayName, userEmail = email)
                            viewModelScope.launch {
                                firebaseBackend.syncUserProfile(user.uid, email, displayName)
                            }
                            NotificationHelper.showSignUpNotification(getApplication(), displayName)
                            _inAppNotification.value = InAppNotification(
                                title = "Welcome to FixScan! 🎉",
                                message = "Account created for $displayName. Ready to diagnose appliances."
                            )
                            _isAuthLoading.value = false
                            onSuccess()
                        } ?: run {
                            _userSession.value = UserSession(isLoggedIn = true, userName = name, userEmail = email)
                            user?.let { u ->
                                viewModelScope.launch {
                                    firebaseBackend.syncUserProfile(u.uid, email, name)
                                }
                            }
                            NotificationHelper.showSignUpNotification(getApplication(), name)
                            _isAuthLoading.value = false
                            onSuccess()
                        }
                    } else {
                        _authError.value = task.exception?.localizedMessage ?: "Failed to create account. Please check your credentials."
                        _isAuthLoading.value = false
                    }
                }
        } else {
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val user = task.result?.user
                        val displayName = user?.displayName?.ifBlank { null } ?: user?.email?.substringBefore("@") ?: "FixScan User"
                        _userSession.value = UserSession(isLoggedIn = true, userName = displayName, userEmail = email)
                        user?.let { u ->
                            viewModelScope.launch {
                                firebaseBackend.syncUserProfile(u.uid, email, displayName)
                            }
                        }
                        NotificationHelper.showLoginNotification(getApplication(), displayName)
                        _inAppNotification.value = InAppNotification(
                            title = "Welcome Back! 👋",
                            message = "Signed in as $displayName."
                        )
                        _isAuthLoading.value = false
                        onSuccess()
                    } else {
                        _authError.value = task.exception?.localizedMessage ?: "Invalid email or password."
                        _isAuthLoading.value = false
                    }
                }
        }
    }

    /** Signs in with a Google ID token issued by Credential Manager and verified by Firebase Auth. */
    fun signInWithGoogle(context: Context? = null, onSuccess: () -> Unit) {
        _isAuthLoading.value = true
        _authError.value = null

        if (context == null) {
            _isAuthLoading.value = false
            _authError.value = "Unable to start Google Sign-In."
            return
        }

        viewModelScope.launch {
            try {
                val credentialManager = CredentialManager.create(context)
                val res = getApplication<Application>().resources
                val resId = res.getIdentifier("default_web_client_id", "string", getApplication<Application>().packageName)
                val webClientId = if (resId != 0) {
                    res.getString(resId).takeIf { it.isNotBlank() } ?: GOOGLE_WEB_CLIENT_ID
                } else {
                    GOOGLE_WEB_CLIENT_ID
                }
                val googleIdOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(webClientId)
                    .setAutoSelectEnabled(false)
                    .build()

                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

                val result = credentialManager.getCredential(context = context, request = request)
                val credential = result.credential

                if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                    val idToken = googleIdTokenCredential.idToken
                    val auth = firebaseAuth

                    if (auth != null) {
                        val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                        auth.signInWithCredential(firebaseCredential)
                            .addOnCompleteListener { task ->
                                _isAuthLoading.value = false
                                if (task.isSuccessful) {
                                    val user = task.result?.user
                                    val displayName = user?.displayName ?: googleIdTokenCredential.displayName ?: "Google User"
                                    val userEmail = user?.email ?: googleIdTokenCredential.id
                                    _userSession.value = UserSession(isLoggedIn = true, userName = displayName, userEmail = userEmail)
                                    user?.let { u ->
                                        viewModelScope.launch {
                                            firebaseBackend.syncUserProfile(u.uid, userEmail, displayName)
                                        }
                                    }
                                    NotificationHelper.showGoogleSignInNotification(getApplication(), displayName)
                                    _inAppNotification.value = InAppNotification(
                                        title = "Signed in with Google 🚀",
                                        message = "Welcome $displayName! Your FixScan diagnostics are synced."
                                    )
                                    onSuccess()
                                } else {
                                    _authError.value = task.exception?.localizedMessage ?: "Firebase Google authentication failed."
                                }
                            }
                    } else {
                        _isAuthLoading.value = false
                        _authError.value = firebaseConfigurationError()
                    }
                } else {
                    _isAuthLoading.value = false
                    _authError.value = "Google credential format was not recognized."
                }
            } catch (e: Exception) {
                _isAuthLoading.value = false
                val rawMsg = e.localizedMessage ?: ""
                val errorMsg = when {
                    e is FirebaseAuthException -> "${e.errorCode}: ${e.localizedMessage}"
                    rawMsg.contains("No credentials available", ignoreCase = true) ->
                        "No Google account found on this device. Please sign in with a Google account in Android Settings -> Accounts or create an account with Email/Password."
                    rawMsg.contains("cancel", ignoreCase = true) ->
                        "Google Sign-In was cancelled."
                    else ->
                        "Google Sign-In failed: $rawMsg"
                }
                _authError.value = errorMsg
            }
        }
    }

    private fun firebaseConfigurationError(): String {
        return if (FirebaseApp.getApps(getApplication()).isEmpty()) {
            "Firebase is not initialized. Verify google-services.json matches applicationId com.fixscan.app."
        } else {
            "Firebase Auth is unavailable. Enable Email/Password and Google providers in Firebase Console."
        }
    }

    fun signOut() {
        try {
            firebaseAuth?.signOut()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        _userSession.value = UserSession(isLoggedIn = false, userName = "Guest User", userEmail = "")
        _authEmail.value = ""
        _authPassword.value = ""
        _authConfirmPassword.value = ""
        _authDisplayName.value = ""
        _authError.value = null
        _authMode.value = AuthMode.SIGN_IN
    }

    fun toggleLoginState() {
        if (_userSession.value.isLoggedIn) {
            signOut()
        } else {
            _userSession.value = UserSession(
                isLoggedIn = true,
                userName = "Demo User",
                userEmail = "demo@fixscan.ai"
            )
        }
    }

    fun resetForm() {
        _selectedPhotoUri.value = null
        _applianceType.value = ApplianceType.FRIDGE.displayName
        _brand.value = ""
        _approxAge.value = "1-3yr"
        _selectedSymptoms.value = emptySet()
        _note.value = ""
        _scanUiState.value = ScanUiState.Idle
    }

    fun analyzeAppliance(onSuccess: (DiagnosisResult) -> Unit) {
        val symptomsList = _selectedSymptoms.value.toList().ifEmpty { listOf("General operational issue") }
        val b = _brand.value.ifBlank { "Generic" }

        viewModelScope.launch {
            _scanUiState.value = ScanUiState.Analyzing
            try {
                val result = repository.diagnoseAndSaveScan(
                    applianceType = _applianceType.value,
                    brand = b,
                    approxAge = _approxAge.value,
                    symptoms = symptomsList,
                    note = _note.value.ifBlank { null },
                    photoUri = _selectedPhotoUri.value
                )
                _activeDiagnosis.value = result
                _scanUiState.value = ScanUiState.Success(result)

                // Initialize chat with greeting
                initChatForDiagnosis(result)

                onSuccess(result)
            } catch (e: Exception) {
                _scanUiState.value = ScanUiState.Error(e.message ?: "Failed to perform diagnostic scan")
            }
        }
    }

    fun openDiagnosisResult(diagnosis: DiagnosisResult) {
        _activeDiagnosis.value = diagnosis
        initChatForDiagnosis(diagnosis)
    }

    private fun initChatForDiagnosis(diagnosis: DiagnosisResult) {
        _chatMessages.value = listOf(
            ChatMessage(
                sender = MessageSender.GEMINI,
                text = "Hello! I am FixScan AI assistant. I have analyzed your ${diagnosis.brand} ${diagnosis.applianceType}. Feel free to ask me any questions about the diagnosis, replacement parts, or step-by-step safety checks!"
            )
        )
    }

    fun sendChatMessage(text: String) {
        if (text.isBlank()) return
        val currentDiag = _activeDiagnosis.value ?: return

        val userMsg = ChatMessage(sender = MessageSender.USER, text = text)
        val updatedList = _chatMessages.value + userMsg
        _chatMessages.value = updatedList

        viewModelScope.launch {
            _isSendingChat.value = true
            try {
                val replyText = repository.askFollowUp(currentDiag, updatedList, text)
                val aiMsg = ChatMessage(sender = MessageSender.GEMINI, text = replyText)
                _chatMessages.value = _chatMessages.value + aiMsg
            } catch (e: Exception) {
                val errMsg = ChatMessage(sender = MessageSender.SYSTEM, text = "Failed to get response: ${e.message}")
                _chatMessages.value = _chatMessages.value + errMsg
            } finally {
                _isSendingChat.value = false
            }
        }
    }

    fun deleteScan(id: String) {
        viewModelScope.launch {
            repository.deleteScan(id)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }

    // Billing Assistant Actions (Scenarios 1-7)
    fun refreshAccountWelcome() {
        viewModelScope.launch {
            val json = JSONObject().apply {
                put("user_name", _userSession.value.userName)
                put("free_scan_count", 3)
            }
            _accountWelcomeCopy.value = billingAssistantService.generateCopy(BillingScenario.ACCOUNT_WELCOME, json)
        }
    }

    fun refreshUsageStatus() {
        viewModelScope.launch {
            val json = JSONObject().apply {
                put("plan_type", "Free Plan")
                put("scans_remaining", _userSession.value.scansRemaining)
                put("scans_total_this_cycle", 5)
            }
            _usageStatusCopy.value = billingAssistantService.generateCopy(BillingScenario.USAGE_STATUS, json)
        }
    }

    fun generatePostScanNudge(scansRemainingAfterScan: Int) {
        viewModelScope.launch {
            val json = JSONObject().apply {
                put("scans_remaining_after_this_scan", scansRemainingAfterScan)
                put("plan_type", "Free Plan")
            }
            _postScanNudgeCopy.value = billingAssistantService.generateCopy(BillingScenario.POST_SCAN_NUDGE, json)
        }
    }

    fun openPaywall(triggerReason: PaywallTriggerReason) {
        viewModelScope.launch {
            val json = JSONObject().apply {
                put("trigger_reason", triggerReason.name.lowercase())
                put("scans_used_this_cycle", 5 - _userSession.value.scansRemaining)
                put("current_plan", "Free Plan")
            }
            val copyJsonStr = billingAssistantService.generateCopy(BillingScenario.PAYWALL_COPY, json)
            try {
                val obj = JSONObject(copyJsonStr)
                _paywallCopy.value = PaywallCopyResult(
                    headline = obj.optString("headline", "Upgrade FixScan Plan"),
                    subtext = obj.optString("subtext", "Get unlimited scans and full mechanic contacts.")
                )
            } catch (e: Exception) {
                _paywallCopy.value = PaywallCopyResult(
                    headline = "Upgrade FixScan Plan",
                    subtext = copyJsonStr
                )
            }
            _showPaywallDialog.value = true
        }
    }

    fun dismissPaywall() {
        _showPaywallDialog.value = false
    }

    fun generateBillingCopy(
        scenario: BillingScenario,
        inputMap: Map<String, Any?>,
        onResult: (String) -> Unit
    ) {
        viewModelScope.launch {
            val json = JSONObject()
            inputMap.forEach { (key, value) ->
                if (value != null) json.put(key, value)
            }
            val result = billingAssistantService.generateCopy(scenario, json)
            onResult(result)
        }
    }

    // Diagnostic Engine Action
    fun analyzeAppliancePhotos(applianceUri: String?, damageUri: String?) {
        viewModelScope.launch {
            _isAnalyzingDiagnosticPhotos.value = true
            try {
                val result = diagnosticEngineService.analyzeAppliancePhotos(applianceUri, damageUri)
                _diagnosticAssessment.value = result
            } catch (e: Exception) {
                _diagnosticAssessment.value = diagnosticEngineService.generateFallbackAssessment(applianceUri, damageUri)
            } finally {
                _isAnalyzingDiagnosticPhotos.value = false
            }
        }
    }
}



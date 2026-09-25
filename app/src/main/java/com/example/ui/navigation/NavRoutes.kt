package com.example.ui.navigation

sealed class Screen(val route: String) {
    object Auth : Screen("auth")
    object SignIn : Screen("sign_in")
    object UserDashboard : Screen("user_dashboard")
    object Home : Screen("home")
    object CameraCapture : Screen("camera_capture")
    object GuidedForm : Screen("guided_form")
    object ScanResults : Screen("scan_results")
    object FollowUpChat : Screen("follow_up_chat")
    object ScanHistory : Screen("scan_history")
    object BillingAssistant : Screen("billing_assistant")
    object DiagnosticEngine : Screen("diagnostic_engine")
    object MechanicsMap : Screen("mechanics_map")
}

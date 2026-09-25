package com.example.model

enum class BillingScenario {
    ACCOUNT_WELCOME,
    USAGE_STATUS,
    PAYMENT_EVENT,
    POST_SCAN_NUDGE,
    SUBSCRIPTION_LIFECYCLE,
    PAYWALL_COPY,
    SCAN_HISTORY_SUMMARY
}

// Scenario 1 Input
data class AccountWelcomeInput(
    val userName: String? = null,
    val freeScanCount: Int? = null
)

// Scenario 2 Input
data class UsageStatusInput(
    val planType: String? = null,
    val scansRemaining: Int? = null,
    val scansTotalThisCycle: Int? = null,
    val creditsBalance: Int? = null,
    val planRenewalDate: String? = null
)

// Scenario 3 Input
enum class PaymentEventType {
    PAYMENT_SUCCESS,
    PAYMENT_FAILED,
    PAYMENT_PENDING
}

data class PaymentEventInput(
    val eventType: PaymentEventType? = null,
    val amount: String? = null,
    val itemPurchased: String? = null,
    val failureReason: String? = null
)

// Scenario 4 Input
data class PostScanNudgeInput(
    val scansRemainingAfterThisScan: Int? = null,
    val planType: String? = null
)

// Scenario 5 Input
enum class SubscriptionLifecycleEvent {
    RENEWED,
    CANCELLED,
    EXPIRING_SOON,
    EXPIRED,
    PAYMENT_FAILED_RETRY
}

data class SubscriptionLifecycleInput(
    val lifecycleEvent: SubscriptionLifecycleEvent? = null,
    val planName: String? = null,
    val renewalDate: String? = null,
    val daysUntilExpiry: Int? = null
)

// Scenario 6 Input
enum class PaywallTriggerReason {
    OUT_OF_SCANS,
    WANTS_FULL_MECHANIC_LIST,
    WANTS_SCAN_HISTORY,
    BROWSING_PLANS
}

data class PaywallCopyInput(
    val triggerReason: PaywallTriggerReason? = null,
    val scansUsedThisCycle: Int? = null,
    val currentPlan: String? = null
)

data class PaywallCopyResult(
    val headline: String,
    val subtext: String
)

// Scenario 7 Input
data class ScanHistorySummaryInput(
    val applianceType: String? = null,
    val damageDescription: String? = null,
    val costEstimateMin: Int? = null,
    val costEstimateMax: Int? = null,
    val scanDate: String? = null
)

package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.components.FrostedBackground
import com.example.ui.components.FrostedGlassCard
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BillingAssistantScreen(
    onGenerateScenarioCopy: (BillingScenario, Map<String, Any?>, (String) -> Unit) -> Unit,
    onBack: () -> Unit
) {
    var selectedScenario by remember { mutableStateOf(BillingScenario.ACCOUNT_WELCOME) }
    var outputText by remember { mutableStateOf("") }
    var isGenerating by remember { mutableStateOf(false) }

    // Input state fields for testing
    var userName by remember { mutableStateOf("Rajesh Kumar") }
    var freeScanCount by remember { mutableStateOf("3") }
    var planType by remember { mutableStateOf("Free Plan") }
    var scansRemaining by remember { mutableStateOf("2") }
    var scansTotalThisCycle by remember { mutableStateOf("5") }
    var creditsBalance by remember { mutableStateOf("10") }
    var planRenewalDate by remember { mutableStateOf("28 Aug 2026") }

    var paymentEventType by remember { mutableStateOf(PaymentEventType.PAYMENT_SUCCESS) }
    var amount by remember { mutableStateOf("₹299") }
    var itemPurchased by remember { mutableStateOf("Pro 10-Scan Pass") }
    var failureReason by remember { mutableStateOf("Bank Server Timeout") }

    var postScanRemAfter by remember { mutableStateOf("1") }

    var subLifecycleEvent by remember { mutableStateOf(SubscriptionLifecycleEvent.RENEWED) }
    var planName by remember { mutableStateOf("FixScan Pro") }
    var daysUntilExpiry by remember { mutableStateOf("3") }

    var paywallTrigger by remember { mutableStateOf(PaywallTriggerReason.OUT_OF_SCANS) }

    var historyAppliance by remember { mutableStateOf("LG Refrigerator") }
    var historyDamage by remember { mutableStateOf("Cooling coil leak causing high temperature") }
    var historyCostMin by remember { mutableStateOf("1200") }
    var historyCostMax by remember { mutableStateOf("2500") }
    var historyDate by remember { mutableStateOf("12 Aug 2026") }

    fun triggerGeneration() {
        isGenerating = true
        val map = mutableMapOf<String, Any?>()

        when (selectedScenario) {
            BillingScenario.ACCOUNT_WELCOME -> {
                if (userName.isNotBlank()) map["user_name"] = userName
                if (freeScanCount.isNotBlank()) map["free_scan_count"] = freeScanCount.toIntOrNull()
            }
            BillingScenario.USAGE_STATUS -> {
                map["plan_type"] = planType
                if (scansRemaining.isNotBlank()) map["scans_remaining"] = scansRemaining.toIntOrNull()
                if (scansTotalThisCycle.isNotBlank()) map["scans_total_this_cycle"] = scansTotalThisCycle.toIntOrNull()
                if (creditsBalance.isNotBlank()) map["credits_balance"] = creditsBalance.toIntOrNull()
                if (planRenewalDate.isNotBlank()) map["plan_renewal_date"] = planRenewalDate
            }
            BillingScenario.PAYMENT_EVENT -> {
                map["event_type"] = paymentEventType.name.lowercase()
                map["amount"] = amount
                map["item_purchased"] = itemPurchased
                if (failureReason.isNotBlank()) map["failure_reason"] = failureReason
            }
            BillingScenario.POST_SCAN_NUDGE -> {
                if (postScanRemAfter.isNotBlank()) map["scans_remaining_after_this_scan"] = postScanRemAfter.toIntOrNull()
                map["plan_type"] = planType
            }
            BillingScenario.SUBSCRIPTION_LIFECYCLE -> {
                map["lifecycle_event"] = subLifecycleEvent.name.lowercase()
                map["plan_name"] = planName
                if (planRenewalDate.isNotBlank()) map["renewal_date"] = planRenewalDate
                if (daysUntilExpiry.isNotBlank()) map["days_until_expiry"] = daysUntilExpiry.toIntOrNull()
            }
            BillingScenario.PAYWALL_COPY -> {
                map["trigger_reason"] = paywallTrigger.name.lowercase()
                map["scans_used_this_cycle"] = 5
                map["current_plan"] = planType
            }
            BillingScenario.SCAN_HISTORY_SUMMARY -> {
                map["appliance_type"] = historyAppliance
                map["damage_description"] = historyDamage
                if (historyCostMin.isNotBlank()) map["cost_estimate_min"] = historyCostMin.toIntOrNull()
                if (historyCostMax.isNotBlank()) map["cost_estimate_max"] = historyCostMax.toIntOrNull()
                map["scan_date"] = historyDate
            }
        }

        onGenerateScenarioCopy(selectedScenario, map) { result ->
            outputText = result
            isGenerating = false
        }
    }

    // Auto-generate copy on scenario change
    LaunchedEffect(selectedScenario) {
        triggerGeneration()
    }

    FrostedBackground {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = "Unified Billing Assistant",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = NeutralDark
                                )
                            )
                            Text(
                                text = "FixScan Dynamic AI Copy Playground",
                                style = MaterialTheme.typography.labelSmall.copy(color = NeutralMedium)
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = NeutralDark
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            },
            containerColor = Color.Transparent,
            modifier = Modifier.testTag("billing_assistant_screen")
        ) { innerPadding ->
            LazyColumn(
                contentPadding = innerPadding,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Scenario Selector
                item {
                    FrostedGlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Select Billing Scenario (1-7)",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = NeutralDark
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            BillingScenario.values().forEach { scenario ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                ) {
                                    RadioButton(
                                        selected = (selectedScenario == scenario),
                                        onClick = { selectedScenario = scenario }
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = scenario.name,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = if (selectedScenario == scenario) FontWeight.Bold else FontWeight.Normal
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                // Scenario Parameters Input Form
                item {
                    FrostedGlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Input Parameters for ${selectedScenario.name}",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = PrimaryBlue
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            when (selectedScenario) {
                                BillingScenario.ACCOUNT_WELCOME -> {
                                    OutlinedTextField(
                                        value = userName,
                                        onValueChange = { userName = it },
                                        label = { Text("user_name (optional)") },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    OutlinedTextField(
                                        value = freeScanCount,
                                        onValueChange = { freeScanCount = it },
                                        label = { Text("free_scan_count (required)") },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }

                                BillingScenario.USAGE_STATUS -> {
                                    OutlinedTextField(
                                        value = planType,
                                        onValueChange = { planType = it },
                                        label = { Text("plan_type") },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    OutlinedTextField(
                                        value = scansRemaining,
                                        onValueChange = { scansRemaining = it },
                                        label = { Text("scans_remaining") },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    OutlinedTextField(
                                        value = scansTotalThisCycle,
                                        onValueChange = { scansTotalThisCycle = it },
                                        label = { Text("scans_total_this_cycle") },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }

                                BillingScenario.PAYMENT_EVENT -> {
                                    Text("event_type:", style = MaterialTheme.typography.labelLarge)
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        PaymentEventType.values().forEach { type ->
                                            FilterChip(
                                                selected = paymentEventType == type,
                                                onClick = { paymentEventType = type },
                                                label = { Text(type.name.lowercase()) }
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    OutlinedTextField(
                                        value = amount,
                                        onValueChange = { amount = it },
                                        label = { Text("amount") },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    OutlinedTextField(
                                        value = itemPurchased,
                                        onValueChange = { itemPurchased = it },
                                        label = { Text("item_purchased") },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }

                                BillingScenario.POST_SCAN_NUDGE -> {
                                    OutlinedTextField(
                                        value = postScanRemAfter,
                                        onValueChange = { postScanRemAfter = it },
                                        label = { Text("scans_remaining_after_this_scan") },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }

                                BillingScenario.SUBSCRIPTION_LIFECYCLE -> {
                                    Text("lifecycle_event:", style = MaterialTheme.typography.labelLarge)
                                    Column {
                                        SubscriptionLifecycleEvent.values().forEach { event ->
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                RadioButton(
                                                    selected = subLifecycleEvent == event,
                                                    onClick = { subLifecycleEvent = event }
                                                )
                                                Text(event.name.lowercase())
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    OutlinedTextField(
                                        value = planName,
                                        onValueChange = { planName = it },
                                        label = { Text("plan_name") },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }

                                BillingScenario.PAYWALL_COPY -> {
                                    Text("trigger_reason:", style = MaterialTheme.typography.labelLarge)
                                    Column {
                                        PaywallTriggerReason.values().forEach { trigger ->
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                RadioButton(
                                                    selected = paywallTrigger == trigger,
                                                    onClick = { paywallTrigger = trigger }
                                                )
                                                Text(trigger.name.lowercase())
                                            }
                                        }
                                    }
                                }

                                BillingScenario.SCAN_HISTORY_SUMMARY -> {
                                    OutlinedTextField(
                                        value = historyAppliance,
                                        onValueChange = { historyAppliance = it },
                                        label = { Text("appliance_type") },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    OutlinedTextField(
                                        value = historyDamage,
                                        onValueChange = { historyDamage = it },
                                        label = { Text("damage_description") },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        OutlinedTextField(
                                            value = historyCostMin,
                                            onValueChange = { historyCostMin = it },
                                            label = { Text("min_cost") },
                                            modifier = Modifier.weight(1f)
                                        )
                                        OutlinedTextField(
                                            value = historyCostMax,
                                            onValueChange = { historyCostMax = it },
                                            label = { Text("max_cost") },
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = { triggerGeneration() },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                            ) {
                                if (isGenerating) {
                                    CircularProgressIndicator(
                                        color = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Generating...")
                                } else {
                                    Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Generate AI Billing Copy")
                                }
                            }
                        }
                    }
                }

                // AI Copy Output Display Card
                item {
                    FrostedGlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Generated Output",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = NeutralDark
                                )
                                Text(
                                    text = if (selectedScenario == BillingScenario.PAYWALL_COPY) "Format: JSON" else "Format: Plain Text",
                                    style = MaterialTheme.typography.labelSmall.copy(color = SecondaryTeal)
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(NeutralDark)
                                    .padding(14.dp)
                            ) {
                                Text(
                                    text = outputText.ifEmpty { "Generating..." },
                                    color = Color(0xFF64FFDA),
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 13.sp,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

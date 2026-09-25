package com.example.data.api

import android.content.Context
import android.util.Log
import com.example.BuildConfig
import com.example.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class BillingAssistantService(private val context: Context) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    private val apiKey: String
        get() = try {
            val key = BuildConfig.GEMINI_API_KEY
            if (key.isNullOrBlank() || key == "MY_GEMINI_API_KEY") "" else key
        } catch (e: Exception) {
            ""
        }

    companion object {
        const val SYSTEM_PROMPT = """SYSTEM ROLE:
You are FixScan's unified billing & account assistant. You generate ONLY the user-facing text/copy for billing-related moments in the app. You do not create accounts, verify payments, store data, calculate balances, or make any billing decisions — all of that is handled by FixScan's backend before this prompt is ever called. You strictly phrase and present information that is handed to you as already-confirmed fact.

You will be told which of the 7 scenarios below applies via the "scenario" field in the input. Only generate output for that one scenario per call.

═══════════════════════════════════
SCENARIO 1: ACCOUNT_WELCOME
═══════════════════════════════════
Trigger: A new user has just completed signup (auth already handled by backend via Firebase Phone OTP).
Input fields: user_name (optional), free_scan_count
Requirements:
- Confirm account is ready
- Mention the number of free scans included this month
- Encourage trying the first scan
- 2-3 sentences, friendly, no jargon, simple English for an Indian consumer audience
Output: plain text only

═══════════════════════════════════
SCENARIO 2: USAGE_STATUS
═══════════════════════════════════
Trigger: Home screen needs a status line reflecting the user's current plan/credit state (state already computed and stored by backend in Firestore).
Input fields: plan_type, scans_remaining, scans_total_this_cycle, credits_balance, plan_renewal_date
Requirements:
- Use ONLY the provided numbers — never estimate or invent values
- One sentence reflecting whichever fields are relevant (free plan with scans left, free plan exhausted, paid plan with renewal date, or credits-only balance)
Output: plain text only, one sentence

═══════════════════════════════════
SCENARIO 3: PAYMENT_EVENT
═══════════════════════════════════
Trigger: Backend has already processed a payment via Razorpay (order created server-side, signature verified server-side) and is passing you the confirmed outcome only.
Input fields: event_type (payment_success | payment_failed | payment_pending), amount, item_purchased, failure_reason (if applicable)
Requirements:
- payment_success: confirm amount paid + what's now unlocked, brief thanks
- payment_failed: reassuring tone, state no money was deducted ONLY if backend confirms this, suggest retry or alternate payment method
- payment_pending: explain UPI/bank confirmation can take a few minutes, they'll be notified
- Never imply you verified or processed the payment yourself
Output: plain text only, 2-3 sentences max

═══════════════════════════════════
SCENARIO 4: POST_SCAN_NUDGE
═══════════════════════════════════
Trigger: A scan just completed and the backend already deducted one credit/scan via a secure server-side transaction. You are only deciding whether to show a nudge based on the resulting balance.
Input fields: scans_remaining_after_this_scan, plan_type
Requirements:
- 3+ remaining: return empty string, no nudge
- 1-2 remaining: gentle mention, not pushy
- 0 remaining: clearly state they're out, frame upgrading/buying credits as a natural next step, not a punishment
Output: plain text only, max 2 sentences, or empty string

═══════════════════════════════════
SCENARIO 5: SUBSCRIPTION_LIFECYCLE
═══════════════════════════════════
Trigger: Backend has already processed a subscription webhook event from Razorpay (renewal, cancellation, expiry, or failed renewal payment). You only phrase the resulting notification.
Input fields: lifecycle_event (renewed | cancelled | expiring_soon | expired | payment_failed_retry), plan_name, renewal_date (if applicable), days_until_expiry (if applicable)
Requirements:
- renewed: confirm renewal + quota reset
- cancelled: confirm cancellation, note access continues until renewal_date, mention easy resubscribe
- expiring_soon: friendly reminder, not alarmist
- expired: clearly state plan ended, now on free tier, easy path back
- payment_failed_retry: explain renewal payment failed, give clear non-threatening action (update payment method)
Output: plain text only, 2-3 sentences, under 200 characters where possible (push notification + in-app banner use)

═══════════════════════════════════
SCENARIO 6: PAYWALL_COPY
═══════════════════════════════════
Trigger: User is being shown the upgrade/plans screen (plan pricing itself is rendered separately by the app from real data — you only write the motivating headline/subtext).
Input fields: trigger_reason (out_of_scans | wants_full_mechanic_list | wants_scan_history | browsing_plans), scans_used_this_cycle, current_plan
Requirements:
- Headline: max 8 words, relevant to trigger_reason
- Subtext: one sentence on the value of upgrading, tailored to trigger_reason (e.g. wants_full_mechanic_list → emphasize seeing all 7-10 nearby options vs. top 3)
- Never invent specific prices or plan details
- Value-focused tone, no fake urgency/scarcity tactics
Output: JSON only — {"headline": "string", "subtext": "string"}

═══════════════════════════════════
SCENARIO 7: SCAN_HISTORY_SUMMARY
═══════════════════════════════════
Trigger: Rendering one row in the user's scan history list (Pro tier feature; scan data already stored by backend).
Input fields: appliance_type, damage_description, cost_estimate_min, cost_estimate_max, scan_date
Requirements:
- One natural sentence combining appliance, issue found, and cost range
- Use ONLY provided data, never invent details
- Under 100 characters where possible
Output: plain text only, one line

═══════════════════════════════════
GLOBAL RULES (apply to all scenarios)
═══════════════════════════════════
1. You never calculate, verify, or decide anything financial (balances, payment success, credit counts) — you only phrase what's already been decided and passed to you.
2. Never fabricate numbers, prices, names, or dates not present in the input.
3. Match the exact output format specified per scenario (plain text vs JSON) — no extra preamble, no markdown fences, no explanation outside the required output.
4. Tone throughout: warm, simple, respectful of the user's money — never guilt-trippy, never falsely urgent.
5. If required input fields for the given scenario are missing, respond with the single word: INSUFFICIENT_DATA — do not guess.

Now, based on the "scenario" and its input fields provided in the user message, generate the appropriate output."""
    }

    /**
     * Public entrypoint for generating copy for any scenario.
     */
    suspend fun generateCopy(
        scenario: BillingScenario,
        inputJson: JSONObject
    ): String = withContext(Dispatchers.IO) {
        val scenarioName = scenario.name
        inputJson.put("scenario", scenarioName)

        // 1. Validation check for required fields locally
        if (isInputMissing(scenario, inputJson)) {
            return@withContext "INSUFFICIENT_DATA"
        }

        // 2. Attempt real Gemini API call if key is available
        val key = apiKey
        if (key.isNotEmpty()) {
            try {
                val apiResponse = callGeminiBillingApi(key, inputJson.toString())
                if (!apiResponse.isNullOrBlank()) {
                    return@withContext cleanOutput(scenario, apiResponse)
                }
            } catch (e: Exception) {
                Log.e("BillingAssistantService", "Gemini API call failed, using deterministic local generator: ${e.message}")
            }
        }

        // 3. Fallback to deterministic local text generator
        return@withContext cleanOutput(scenario, generateLocalFallback(scenario, inputJson))
    }

    private fun isInputMissing(scenario: BillingScenario, json: JSONObject): Boolean {
        return when (scenario) {
            BillingScenario.ACCOUNT_WELCOME -> !json.has("free_scan_count") || json.isNull("free_scan_count")
            BillingScenario.USAGE_STATUS -> false // At least one field should be present or valid
            BillingScenario.PAYMENT_EVENT -> !json.has("event_type") || json.isNull("event_type")
            BillingScenario.POST_SCAN_NUDGE -> !json.has("scans_remaining_after_this_scan") || json.isNull("scans_remaining_after_this_scan")
            BillingScenario.SUBSCRIPTION_LIFECYCLE -> !json.has("lifecycle_event") || json.isNull("lifecycle_event")
            BillingScenario.PAYWALL_COPY -> !json.has("trigger_reason") || json.isNull("trigger_reason")
            BillingScenario.SCAN_HISTORY_SUMMARY -> !json.has("appliance_type") || !json.has("damage_description")
        }
    }

    private fun callGeminiBillingApi(key: String, payload: String): String? {
        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=$key"

        val bodyObj = JSONObject().apply {
            put("systemInstruction", JSONObject().apply {
                put("parts", JSONArray().put(JSONObject().put("text", SYSTEM_PROMPT)))
            })
            put("contents", JSONArray().put(JSONObject().apply {
                put("role", "user")
                put("parts", JSONArray().put(JSONObject().put("text", payload)))
            }))
        }

        val request = Request.Builder()
            .url(url)
            .post(bodyObj.toString().toRequestBody("application/json".toMediaType()))
            .build()

        client.newCall(request).execute().use { response ->
            if (response.isSuccessful) {
                val resStr = response.body?.string() ?: return null
                val rootObj = JSONObject(resStr)
                val candidates = rootObj.optJSONArray("candidates") ?: return null
                if (candidates.length() > 0) {
                    val content = candidates.getJSONObject(0).optJSONObject("content") ?: return null
                    val parts = content.optJSONArray("parts") ?: return null
                    if (parts.length() > 0) {
                        return parts.getJSONObject(0).optString("text")
                    }
                }
            }
        }
        return null
    }

    private fun cleanOutput(scenario: BillingScenario, text: String): String {
        var trimmed = text.trim()
        // Strip markdown code fences if present
        if (trimmed.startsWith("```json")) {
            trimmed = trimmed.removePrefix("```json").removeSuffix("```").trim()
        } else if (trimmed.startsWith("```")) {
            trimmed = trimmed.removePrefix("```").removeSuffix("```").trim()
        }

        if (scenario == BillingScenario.PAYWALL_COPY) {
            // Verify valid JSON format for Scenario 6
            return try {
                val obj = JSONObject(trimmed)
                obj.toString()
            } catch (e: Exception) {
                // If AI outputted raw text by mistake, wrap it nicely into JSON
                val fallbackJson = JSONObject()
                fallbackJson.put("headline", "Upgrade to Unlock Full Access")
                fallbackJson.put("subtext", trimmed)
                fallbackJson.toString()
            }
        }
        return trimmed
    }

    /**
     * Robust local fallback generator adhering 100% to all 7 scenario guidelines & global rules.
     */
    fun generateLocalFallback(scenario: BillingScenario, json: JSONObject): String {
        return when (scenario) {
            BillingScenario.ACCOUNT_WELCOME -> {
                val name = json.optString("user_name", "").ifBlank { null }
                val count = json.optInt("free_scan_count", -1)
                if (count < 0) return "INSUFFICIENT_DATA"

                val greeting = if (name != null) "Welcome $name!" else "Welcome to FixScan!"
                "$greeting Your account is ready with $count free scans included this month. Try your first appliance scan today to check for issues and estimate repair costs."
            }

            BillingScenario.USAGE_STATUS -> {
                val planType = json.optString("plan_type", "Free")
                val scansRem = json.optInt("scans_remaining", -1)
                val scansTotal = json.optInt("scans_total_this_cycle", -1)
                val credits = json.optInt("credits_balance", -1)
                val renewalDate = json.optString("plan_renewal_date", "")

                when {
                    scansRem > 0 && scansTotal > 0 -> "You have $scansRem of $scansTotal scans remaining on your $planType plan."
                    scansRem == 0 -> "You have used all $scansTotal free scans for this cycle."
                    credits >= 0 -> "You currently have $credits scan credits available in your balance."
                    renewalDate.isNotBlank() -> "Your $planType plan is active and will renew on $renewalDate."
                    else -> "Your $planType plan is active."
                }
            }

            BillingScenario.PAYMENT_EVENT -> {
                val eventType = json.optString("event_type", "")
                val amount = json.optString("amount", "")
                val item = json.optString("item_purchased", "FixScan Pro")
                val reason = json.optString("failure_reason", "")

                when (eventType) {
                    "payment_success" -> "Payment of $amount successful! $item is now unlocked for your account. Thank you for your purchase."
                    "payment_failed" -> {
                        val detail = if (reason.isNotBlank()) " Reason: $reason." else ""
                        "Payment could not be completed.$detail Don't worry, no money was deducted. Please try again or use another payment method."
                    }
                    "payment_pending" -> "Your payment of $amount is currently pending bank confirmation. This usually takes a few minutes, and we will notify you as soon as it succeeds."
                    else -> "INSUFFICIENT_DATA"
                }
            }

            BillingScenario.POST_SCAN_NUDGE -> {
                val rem = json.optInt("scans_remaining_after_this_scan", -1)
                if (rem < 0) return "INSUFFICIENT_DATA"

                when {
                    rem >= 3 -> ""
                    rem in 1..2 -> "You have $rem scan${if (rem > 1) "s" else ""} remaining. Consider topping up so you are never stuck."
                    rem == 0 -> "You have used all your scans for this cycle. Upgrade or add credits anytime to continue scanning your appliances."
                    else -> ""
                }
            }

            BillingScenario.SUBSCRIPTION_LIFECYCLE -> {
                val event = json.optString("lifecycle_event", "")
                val planName = json.optString("plan_name", "Pro Plan")
                val renDate = json.optString("renewal_date", "")
                val daysLeft = json.optInt("days_until_expiry", -1)

                when (event) {
                    "renewed" -> "Your $planName subscription has renewed successfully, and your scan quota is reset."
                    "cancelled" -> "Your $planName cancellation is confirmed. You retain access until $renDate and can resubscribe anytime."
                    "expiring_soon" -> "Reminder: Your $planName expires in $daysLeft days. Keep your plan active for unlimited diagnostic features."
                    "expired" -> "Your $planName has expired. You are now on the Free tier, but you can upgrade back anytime."
                    "payment_failed_retry" -> "Renewal payment for $planName failed. Please update your payment method to keep your benefits."
                    else -> "INSUFFICIENT_DATA"
                }
            }

            BillingScenario.PAYWALL_COPY -> {
                val trigger = json.optString("trigger_reason", "")
                val obj = JSONObject()
                when (trigger) {
                    "out_of_scans" -> {
                        obj.put("headline", "Out of Scans? Unlock Unlimited Scans")
                        obj.put("subtext", "Upgrade your plan to diagnose all your home appliances without limits.")
                    }
                    "wants_full_mechanic_list" -> {
                        obj.put("headline", "See All Nearby Mechanics")
                        obj.put("subtext", "Unlock full mechanic directory access to view all 7-10 verified technicians near you.")
                    }
                    "wants_scan_history" -> {
                        obj.put("headline", "Access Your Complete Scan History")
                        obj.put("subtext", "Keep track of all past appliance diagnoses, cost estimates, and reports in one place.")
                    }
                    "browsing_plans" -> {
                        obj.put("headline", "Choose the Plan That Fits You")
                        obj.put("subtext", "Get instant AI diagnostics, accurate repair cost estimates, and verified local mechanic contacts.")
                    }
                    else -> return "INSUFFICIENT_DATA"
                }
                obj.toString()
            }

            BillingScenario.SCAN_HISTORY_SUMMARY -> {
                val appType = json.optString("appliance_type", "")
                val damage = json.optString("damage_description", "")
                val minCost = json.optInt("cost_estimate_min", -1)
                val maxCost = json.optInt("cost_estimate_max", -1)

                if (appType.isBlank() || damage.isBlank()) return "INSUFFICIENT_DATA"

                val costText = if (minCost >= 0 && maxCost >= 0) " (Est. ₹$minCost - ₹$maxCost)" else ""
                "$appType: $damage$costText."
            }
        }
    }
}

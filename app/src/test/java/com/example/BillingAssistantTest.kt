package com.example

import androidx.test.core.app.ApplicationProvider
import com.example.data.api.BillingAssistantService
import com.example.model.BillingScenario
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class BillingAssistantTest {

    private lateinit var service: BillingAssistantService

    @Before
    fun setup() {
        service = BillingAssistantService(ApplicationProvider.getApplicationContext())
    }

    @Test
    fun scenario1_accountWelcome_validInput_returnsPlainText() {
        val json = JSONObject().apply {
            put("user_name", "Amit")
            put("free_scan_count", 3)
        }
        val output = service.generateLocalFallback(BillingScenario.ACCOUNT_WELCOME, json)
        assertTrue(output.contains("Welcome Amit"))
        assertTrue(output.contains("3 free scans"))
    }

    @Test
    fun scenario1_accountWelcome_missingCount_returnsInsufficientData() {
        val json = JSONObject().apply {
            put("user_name", "Amit")
        }
        val output = service.generateLocalFallback(BillingScenario.ACCOUNT_WELCOME, json)
        assertEquals("INSUFFICIENT_DATA", output)
    }

    @Test
    fun scenario2_usageStatus_freePlan_returnsOneSentence() {
        val json = JSONObject().apply {
            put("plan_type", "Free Plan")
            put("scans_remaining", 2)
            put("scans_total_this_cycle", 5)
        }
        val output = service.generateLocalFallback(BillingScenario.USAGE_STATUS, json)
        assertEquals("You have 2 of 5 scans remaining on your Free Plan plan.", output)
    }

    @Test
    fun scenario3_paymentEvent_success_returnsConfirmation() {
        val json = JSONObject().apply {
            put("event_type", "payment_success")
            put("amount", "₹199")
            put("item_purchased", "5 Scan Credits")
        }
        val output = service.generateLocalFallback(BillingScenario.PAYMENT_EVENT, json)
        assertTrue(output.contains("Payment of ₹199 successful!"))
        assertTrue(output.contains("5 Scan Credits"))
    }

    @Test
    fun scenario4_postScanNudge_3Remaining_returnsEmptyString() {
        val json = JSONObject().apply {
            put("scans_remaining_after_this_scan", 4)
            put("plan_type", "Free")
        }
        val output = service.generateLocalFallback(BillingScenario.POST_SCAN_NUDGE, json)
        assertEquals("", output)
    }

    @Test
    fun scenario4_postScanNudge_0Remaining_returnsClearNudge() {
        val json = JSONObject().apply {
            put("scans_remaining_after_this_scan", 0)
            put("plan_type", "Free")
        }
        val output = service.generateLocalFallback(BillingScenario.POST_SCAN_NUDGE, json)
        assertTrue(output.contains("used all your scans"))
    }

    @Test
    fun scenario5_subscriptionLifecycle_renewed_returnsConfirmation() {
        val json = JSONObject().apply {
            put("lifecycle_event", "renewed")
            put("plan_name", "FixScan Pro")
        }
        val output = service.generateLocalFallback(BillingScenario.SUBSCRIPTION_LIFECYCLE, json)
        assertTrue(output.contains("FixScan Pro subscription has renewed"))
    }

    @Test
    fun scenario6_paywallCopy_outOfScans_returnsValidJson() {
        val json = JSONObject().apply {
            put("trigger_reason", "out_of_scans")
            put("scans_used_this_cycle", 5)
            put("current_plan", "Free Tier")
        }
        val output = service.generateLocalFallback(BillingScenario.PAYWALL_COPY, json)
        val resObj = JSONObject(output)
        assertTrue(resObj.has("headline"))
        assertTrue(resObj.has("subtext"))
        assertTrue(resObj.getString("headline").split(" ").size <= 8)
    }

    @Test
    fun scenario7_scanHistorySummary_validInput_returnsOneLine() {
        val json = JSONObject().apply {
            put("appliance_type", "Air Conditioner")
            put("damage_description", "Gas leak in cooling tube")
            put("cost_estimate_min", 1500)
            put("cost_estimate_max", 3000)
            put("scan_date", "12 Aug 2026")
        }
        val output = service.generateLocalFallback(BillingScenario.SCAN_HISTORY_SUMMARY, json)
        assertEquals("Air Conditioner: Gas leak in cooling tube (Est. ₹1500 - ₹3000).", output)
        assertTrue(output.length <= 100)
    }
}

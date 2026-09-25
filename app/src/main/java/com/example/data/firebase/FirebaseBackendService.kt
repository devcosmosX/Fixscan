package com.example.data.firebase

import android.content.Context
import android.os.Bundle
import android.util.Log
import com.example.model.*
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

class FirebaseBackendService(private val context: Context) {

    private val firestore: FirebaseFirestore? by lazy {
        try {
            FirebaseFirestore.getInstance()
        } catch (e: Exception) {
            Log.e(TAG, "Firestore initialization error: ${e.message}")
            null
        }
    }

    private val auth: FirebaseAuth? by lazy {
        try {
            FirebaseAuth.getInstance()
        } catch (e: Exception) {
            Log.e(TAG, "FirebaseAuth initialization error: ${e.message}")
            null
        }
    }

    private val analytics: FirebaseAnalytics? by lazy {
        try {
            FirebaseAnalytics.getInstance(context)
        } catch (e: Exception) {
            Log.e(TAG, "FirebaseAnalytics initialization error: ${e.message}")
            null
        }
    }

    /**
     * Saves a diagnosis scan into the Firestore backend.
     * Saved under users/{userId}/scans/{scanId} and globally under scans/{scanId}.
     */
    suspend fun saveScanToFirestore(diagnosis: DiagnosisResult): Boolean {
        val fs = firestore ?: return false
        val currentUserId = auth?.currentUser?.uid

        try {
            val faultsList = diagnosis.likelyFaults.map { f ->
                mapOf(
                    "fault" to f.fault,
                    "confidence" to f.confidence.name,
                    "explanation" to f.explanation
                )
            }

            val safetyMap = diagnosis.safetyWarning?.let { s ->
                mapOf(
                    "isCritical" to s.isCritical,
                    "warningText" to s.warningText,
                    "recommendedAction" to s.recommendedAction
                )
            }

            val sourcesList = diagnosis.costEstimate.sources.map { src ->
                mapOf("title" to src.title, "url" to src.url)
            }

            val costMap = mapOf(
                "minCost" to diagnosis.costEstimate.minCost,
                "maxCost" to diagnosis.costEstimate.maxCost,
                "currency" to diagnosis.costEstimate.currency,
                "basisNote" to diagnosis.costEstimate.basisNote,
                "sources" to sourcesList
            )

            val mechanicsList = diagnosis.mechanics.map { m ->
                mapOf(
                    "id" to m.id,
                    "name" to m.name,
                    "rating" to m.rating,
                    "reviewCount" to m.reviewCount,
                    "distanceKm" to m.distanceKm,
                    "address" to m.address,
                    "phone" to m.phone,
                    "specialties" to m.specialties,
                    "isVerified" to m.isVerified,
                    "latitude" to m.latitude,
                    "longitude" to m.longitude
                )
            }

            val scanData = hashMapOf<String, Any?>(
                "id" to diagnosis.id,
                "userId" to (currentUserId ?: "guest"),
                "applianceType" to diagnosis.applianceType,
                "brand" to diagnosis.brand,
                "approxAge" to diagnosis.approxAge,
                "symptoms" to diagnosis.symptoms,
                "note" to diagnosis.note,
                "photoUri" to diagnosis.photoUri,
                "likelyFaults" to faultsList,
                "safetyWarning" to safetyMap,
                "costEstimate" to costMap,
                "mechanics" to mechanicsList,
                "timestamp" to diagnosis.timestamp,
                "syncedAt" to System.currentTimeMillis()
            )

            // 1. Save to global scans collection
            fs.collection("scans").document(diagnosis.id).set(scanData, SetOptions.merge()).await()

            // 2. If authenticated, also save to user's personal collection
            if (!currentUserId.isNullOrBlank()) {
                fs.collection("users")
                    .document(currentUserId)
                    .collection("scans")
                    .document(diagnosis.id)
                    .set(scanData, SetOptions.merge())
                    .await()
            }

            // 3. Track Firebase Analytics event
            logScanEvent(diagnosis)

            Log.d(TAG, "Diagnosis scan saved to Firestore backend: ${diagnosis.id}")
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save scan to Firestore: ${e.message}", e)
            return false
        }
    }

    /**
     * Deletes a scan from Firestore.
     */
    suspend fun deleteScanFromFirestore(scanId: String): Boolean {
        val fs = firestore ?: return false
        val currentUserId = auth?.currentUser?.uid

        try {
            fs.collection("scans").document(scanId).delete().await()
            if (!currentUserId.isNullOrBlank()) {
                fs.collection("users")
                    .document(currentUserId)
                    .collection("scans")
                    .document(scanId)
                    .delete()
                    .await()
            }
            Log.d(TAG, "Scan deleted from Firestore: $scanId")
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete scan from Firestore: ${e.message}", e)
            return false
        }
    }

    /**
     * Updates or creates the user's profile document in Firestore.
     */
    suspend fun syncUserProfile(
        userId: String,
        email: String,
        displayName: String,
        planType: String = "Free Plan",
        scansRemaining: Int = 3
    ) {
        val fs = firestore ?: return
        try {
            val userMap = hashMapOf<String, Any>(
                "uid" to userId,
                "email" to email,
                "displayName" to displayName,
                "planType" to planType,
                "scansRemaining" to scansRemaining,
                "lastActive" to System.currentTimeMillis()
            )
            fs.collection("users").document(userId).set(userMap, SetOptions.merge()).await()
            Log.d(TAG, "User profile synced in Firestore for: $userId")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to sync user profile in Firestore: ${e.message}", e)
        }
    }

    /**
     * Log Firebase Analytics events.
     */
    fun logEvent(eventName: String, params: Bundle? = null) {
        try {
            analytics?.logEvent(eventName, params)
            Log.d(TAG, "Analytics event logged: $eventName")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to log analytics event: ${e.message}")
        }
    }

    private fun logScanEvent(diagnosis: DiagnosisResult) {
        val bundle = Bundle().apply {
            putString("appliance_type", diagnosis.applianceType)
            putString("brand", diagnosis.brand)
            putBoolean("has_safety_hazard", diagnosis.safetyWarning?.isCritical == true)
            putInt("min_cost", diagnosis.costEstimate.minCost)
            putInt("max_cost", diagnosis.costEstimate.maxCost)
        }
        logEvent("appliance_scan_completed", bundle)
    }

    companion object {
        private const val TAG = "FirebaseBackendService"
    }
}

package com.fixmate.data

import android.content.Context
import android.net.Uri
import android.util.Log
import com.fixmate.BuildConfig
import com.fixmate.model.Confidence
import com.fixmate.model.CostEstimate
import com.fixmate.model.DiagnosisRequest
import com.fixmate.model.DiagnosisResult
import com.fixmate.model.LikelyFault
import com.fixmate.model.SafetyWarning
import com.fixmate.util.ImageUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * Talks to Anthropic's Claude Messages API to diagnose an appliance from the
 * user's description and (optionally) a photo. If no API key is configured, or
 * the network call fails, it transparently falls back to an offline demo generator
 * so the app is always usable.
 */
class ClaudeService(private val context: Context) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val apiKey: String get() = BuildConfig.CLAUDE_API_KEY.trim()

    val hasApiKey: Boolean get() = apiKey.isNotEmpty()

    suspend fun diagnose(request: DiagnosisRequest): DiagnosisResult = withContext(Dispatchers.IO) {
        if (apiKey.isNotEmpty()) {
            try {
                callClaude(request)?.let { return@withContext it }
            } catch (e: Exception) {
                Log.e(TAG, "Claude call failed, using offline generator: ${e.message}")
            }
        }
        MockDiagnosisGenerator.generate(request)
    }

    private fun callClaude(request: DiagnosisRequest): DiagnosisResult? {
        val content = JSONArray().apply {
            put(JSONObject().apply {
                put("type", "text")
                put("text", buildUserPrompt(request))
            })
            // Attach the photo as an image block for vision analysis.
            request.photoUri?.let { uriStr ->
                ImageUtils.uriToBase64Jpeg(context, Uri.parse(uriStr))?.let { base64 ->
                    put(JSONObject().apply {
                        put("type", "image")
                        put("source", JSONObject().apply {
                            put("type", "base64")
                            put("media_type", "image/jpeg")
                            put("data", base64)
                        })
                    })
                }
            }
        }

        val body = JSONObject().apply {
            put("model", MODEL)
            put("max_tokens", 1024)
            put("temperature", 0.2)
            put("system", SYSTEM_PROMPT)
            put("messages", JSONArray().put(JSONObject().apply {
                put("role", "user")
                put("content", content)
            }))
        }

        val httpRequest = Request.Builder()
            .url(ENDPOINT)
            .addHeader("x-api-key", apiKey)
            .addHeader("anthropic-version", ANTHROPIC_VERSION)
            .addHeader("content-type", "application/json")
            .post(body.toString().toRequestBody("application/json".toMediaType()))
            .build()

        client.newCall(httpRequest).execute().use { response ->
            val bodyStr = response.body?.string()
            if (!response.isSuccessful || bodyStr == null) {
                Log.e(TAG, "HTTP ${response.code}: $bodyStr")
                return null
            }
            val blocks = JSONObject(bodyStr).optJSONArray("content") ?: return null
            val text = buildString {
                for (i in 0 until blocks.length()) {
                    val block = blocks.getJSONObject(i)
                    if (block.optString("type") == "text") append(block.optString("text"))
                }
            }
            return parseDiagnosis(text)
        }
    }

    private fun buildUserPrompt(r: DiagnosisRequest): String {
        val symptoms = if (r.symptoms.isEmpty()) "Not specified" else r.symptoms.joinToString(", ")
        val photoNote = if (r.photoUri != null)
            "A photo of the appliance is attached — use it in your analysis."
        else
            "No photo was provided."
        return """
            Diagnose this home appliance problem.

            - Appliance: ${r.applianceType}
            - Brand: ${r.brand.ifBlank { "Unknown" }}
            - Approximate age: ${r.approxAge}
            - Reported symptoms: $symptoms
            - Additional notes: ${r.note?.ifBlank { null } ?: "None"}
            - $photoNote

            Return ONLY the JSON object described in the system prompt.
        """.trimIndent()
    }

    private fun parseDiagnosis(raw: String): DiagnosisResult? {
        return try {
            val clean = raw.trim()
                .removePrefix("```json")
                .removePrefix("```")
                .removeSuffix("```")
                .trim()
            val start = clean.indexOf('{')
            val end = clean.lastIndexOf('}')
            if (start < 0 || end <= start) return null
            val obj = JSONObject(clean.substring(start, end + 1))

            val faults = mutableListOf<LikelyFault>()
            obj.optJSONArray("likelyFaults")?.let { arr ->
                for (i in 0 until arr.length()) {
                    val f = arr.getJSONObject(i)
                    faults.add(
                        LikelyFault(
                            fault = f.optString("fault", "Unknown fault"),
                            confidence = Confidence.from(f.optString("confidence")),
                            explanation = f.optString("explanation", "")
                        )
                    )
                }
            }
            if (faults.isEmpty()) return null

            var safety: SafetyWarning? = null
            obj.optJSONObject("safetyWarning")?.let { s ->
                val warn = s.optString("warningText", "")
                if (warn.isNotBlank()) {
                    safety = SafetyWarning(
                        critical = s.optBoolean("isCritical", false),
                        warningText = warn,
                        recommendedAction = s.optString("recommendedAction", "Disconnect power before inspecting.")
                    )
                }
            }

            val costObj = obj.optJSONObject("costEstimate")
            val cost = CostEstimate(
                minCost = costObj?.optInt("minCost", 500) ?: 500,
                maxCost = costObj?.optInt("maxCost", 2000) ?: 2000,
                currency = costObj?.optString("currency", "₹") ?: "₹",
                basisNote = costObj?.optString("basisNote", "Estimated from typical local repair rates.")
                    ?: "Estimated from typical local repair rates."
            )

            DiagnosisResult(faults, safety, cost)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse diagnosis JSON: ${e.message}")
            null
        }
    }

    companion object {
        private const val TAG = "ClaudeService"
        private const val ENDPOINT = "https://api.anthropic.com/v1/messages"
        private const val ANTHROPIC_VERSION = "2023-06-01"

        // Model ID. If Anthropic releases a newer model, update this string.
        // See https://docs.anthropic.com/en/docs/about-claude/models
        private const val MODEL = "claude-sonnet-4-20250514"

        val SYSTEM_PROMPT = """
            You are FixMate, an expert master technician for home-appliance diagnostics serving customers in India.
            Given the appliance details (and a photo if one is attached), identify the most likely faults, flag any
            safety hazards, and estimate a repair cost range in Indian Rupees.

            Respond with ONLY a raw JSON object (no markdown, no code fences, no extra prose) in exactly this shape:
            {
              "likelyFaults": [
                { "fault": "short fault name", "confidence": "MOST_LIKELY", "explanation": "1-2 sentence technical cause" },
                { "fault": "...", "confidence": "POSSIBLE", "explanation": "..." }
              ],
              "safetyWarning": {
                "isCritical": true,
                "warningText": "only if there is an electrical, fire, gas, or water hazard",
                "recommendedAction": "the single most important immediate step for the user"
              },
              "costEstimate": {
                "minCost": 800,
                "maxCost": 2200,
                "currency": "₹",
                "basisNote": "what the estimate covers (parts + typical labour in India)"
              }
            }

            Rules:
            - "confidence" must be one of: MOST_LIKELY, POSSIBLE, LESS_LIKELY.
            - Provide 2 to 3 faults, ordered most to least likely.
            - If there is no meaningful safety hazard, set "safetyWarning" to null.
            - Never leave "likelyFaults" or "costEstimate" empty.
            - Keep explanations concise and jargon-light.
        """.trimIndent()
    }
}

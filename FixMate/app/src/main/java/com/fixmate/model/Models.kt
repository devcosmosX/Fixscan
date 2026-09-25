package com.fixmate.model

/** The appliance categories FixMate can diagnose. */
enum class ApplianceType(val displayName: String) {
    FRIDGE("Fridge / Refrigerator"),
    WASHING_MACHINE("Washing Machine"),
    AIR_CONDITIONER("Air Conditioner (AC)"),
    MICROWAVE("Microwave Oven"),
    MIXER_GRINDER("Mixer / Grinder"),
    WATER_PURIFIER("Water Purifier / RO"),
    TV("Television"),
    GEYSER("Geyser / Water Heater"),
    OTHER("Other Appliance")
}

/** A selectable symptom. Safety-critical ones (burning, sparks) get special treatment. */
data class Symptom(
    val id: String,
    val title: String,
    val safetyCritical: Boolean = false
) {
    companion object {
        val ALL = listOf(
            Symptom("not_on", "Not turning on"),
            Symptom("noise", "Strange noise / vibration"),
            Symptom("leak", "Water leaking"),
            Symptom("temp", "Not cooling / heating"),
            Symptom("burning", "Burning smell", safetyCritical = true),
            Symptom("damage", "Physical damage / cracked"),
            Symptom("spark", "Sparks / tripping breaker", safetyCritical = true),
            Symptom("error", "Error code on display"),
            Symptom("other", "Other symptom")
        )
    }
}

enum class Confidence(val label: String) {
    MOST_LIKELY("Most likely"),
    POSSIBLE("Possible"),
    LESS_LIKELY("Less likely");

    companion object {
        fun from(raw: String?): Confidence = when (raw?.uppercase()?.replace(" ", "_")) {
            "MOST_LIKELY" -> MOST_LIKELY
            "LESS_LIKELY" -> LESS_LIKELY
            else -> POSSIBLE
        }
    }
}

data class LikelyFault(
    val fault: String,
    val confidence: Confidence,
    val explanation: String
)

data class SafetyWarning(
    val critical: Boolean,
    val warningText: String,
    val recommendedAction: String
)

data class CostEstimate(
    val minCost: Int,
    val maxCost: Int,
    val currency: String = "₹",
    val basisNote: String
)

/** What the user submits from the guided form. */
data class DiagnosisRequest(
    val applianceType: String,
    val brand: String,
    val approxAge: String,
    val symptoms: List<String>,
    val note: String?,
    val photoUri: String?
)

/** What the AI (or the offline generator) returns. */
data class DiagnosisResult(
    val likelyFaults: List<LikelyFault>,
    val safetyWarning: SafetyWarning?,
    val costEstimate: CostEstimate
)

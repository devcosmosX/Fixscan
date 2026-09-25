package com.example.model

enum class ApplianceType(val displayName: String, val iconResName: String) {
    FRIDGE("Fridge / Refrigerator", "ic_fridge"),
    WASHING_MACHINE("Washing Machine", "ic_washing_machine"),
    AIR_CONDITIONER("Air Conditioner (AC)", "ic_ac"),
    MICROWAVE("Microwave Oven", "ic_microwave"),
    MIXER_GRINDER("Mixer / Grinder", "ic_mixer"),
    WATER_PURIFIER("Water Purifier / RO", "ic_water_purifier"),
    TV("Television / Smart TV", "ic_tv"),
    OTHER("Other Appliance", "ic_other")
}

data class Symptom(
    val id: String,
    val title: String,
    val isSafetyCritical: Boolean = false
) {
    companion object {
        val ALL_SYMPTOMS = listOf(
            Symptom("not_turning_on", "Not turning on"),
            Symptom("strange_noise", "Strange noise / Vibration"),
            Symptom("leaking", "Water leaking"),
            Symptom("not_cooling_heating", "Not cooling / heating"),
            Symptom("burning_smell", "Burning smell", isSafetyCritical = true),
            Symptom("physical_damage", "Physical damage / Cracked"),
            Symptom("sparks_short", "Sparks / Tripping breaker", isSafetyCritical = true),
            Symptom("error_code", "Error code on display"),
            Symptom("other", "Other symptom")
        )
    }
}

enum class Confidence(val label: String) {
    MOST_LIKELY("Most likely"),
    POSSIBLE("Possible"),
    LESS_LIKELY("Less likely")
}

data class LikelyFault(
    val fault: String,
    val confidence: Confidence,
    val explanation: String
)

data class SafetyWarning(
    val isCritical: Boolean,
    val warningText: String,
    val recommendedAction: String
)

data class CostSource(
    val title: String,
    val url: String
)

data class CostEstimate(
    val minCost: Int,
    val maxCost: Int,
    val currency: String = "₹",
    val basisNote: String,
    val sources: List<CostSource> = emptyList()
)

data class Mechanic(
    val id: String,
    val name: String,
    val rating: Float,
    val reviewCount: Int,
    val distanceKm: Double,
    val address: String,
    val phone: String,
    val specialties: List<String>,
    val isVerified: Boolean = true,
    val latitude: Double = 19.0760,
    val longitude: Double = 72.8777
)

data class DiagnosisResult(
    val id: String = java.util.UUID.randomUUID().toString(),
    val applianceType: String,
    val brand: String,
    val approxAge: String,
    val symptoms: List<String>,
    val note: String? = null,
    val photoUri: String? = null,
    val likelyFaults: List<LikelyFault>,
    val safetyWarning: SafetyWarning? = null,
    val costEstimate: CostEstimate,
    val mechanics: List<Mechanic>,
    val timestamp: Long = System.currentTimeMillis()
)

enum class MessageSender {
    USER,
    GEMINI,
    SYSTEM
}

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val sender: MessageSender,
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class UserSession(
    val isLoggedIn: Boolean = true, // Default stub logged in state
    val userName: String = "John Doe",
    val userEmail: String = "john.doe@example.com",
    val scansRemaining: Int = 5
)

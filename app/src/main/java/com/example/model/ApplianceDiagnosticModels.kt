package com.example.model

data class ApplianceIdentification(
    val type: String,
    val confidence: String // "low" | "medium" | "high"
)

data class DamageAssessment(
    val detected: Boolean,
    val component: String, // e.g. "charging pin", "door seal", "compressor housing", "power cable", or "none"
    val damageType: String, // e.g. "bent", "cracked", "leaking", "burnt", or "none"
    val severity: String, // "minor" | "moderate" | "severe" | "none"
    val description: String // 1-2 sentences
)

data class CostEstimateAssessment(
    val currency: String = "INR",
    val min: Int,
    val max: Int,
    val basis: String
)

data class ApplianceDiagnosticAssessment(
    val appliance: ApplianceIdentification,
    val damage: DamageAssessment,
    val costEstimate: CostEstimateAssessment,
    val notes: String
)

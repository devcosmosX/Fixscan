package com.fixmate.data

import com.fixmate.model.Confidence
import com.fixmate.model.CostEstimate
import com.fixmate.model.DiagnosisRequest
import com.fixmate.model.DiagnosisResult
import com.fixmate.model.LikelyFault
import com.fixmate.model.SafetyWarning

/**
 * Deterministic, offline diagnosis generator. Used when no Claude API key is set
 * or the network call fails, so the app is always demoable. Results are plausible
 * but rule-based — not real AI output.
 */
object MockDiagnosisGenerator {

    fun generate(r: DiagnosisRequest): DiagnosisResult {
        val s = r.symptoms.joinToString(" ").lowercase()
        val faults = mutableListOf<LikelyFault>()
        var safety: SafetyWarning? = null

        when {
            s.contains("burning") || s.contains("spark") -> {
                safety = SafetyWarning(
                    critical = true,
                    warningText = "Possible electrical / fire hazard: burning smell or sparking reported.",
                    recommendedAction = "Unplug the ${r.applianceType} immediately and do not use it until a technician inspects it."
                )
                faults += LikelyFault(
                    "Burnt control board / wiring", Confidence.MOST_LIKELY,
                    "A power surge or failing component has scorched the main circuit board or its wiring."
                )
                faults += LikelyFault(
                    "Short-circuited motor winding", Confidence.POSSIBLE,
                    "Degraded insulation on the motor coils can cause internal arcing over time."
                )
            }
            s.contains("not turning on") || s.contains("not on") -> {
                faults += LikelyFault(
                    "Blown thermal fuse or faulty relay", Confidence.MOST_LIKELY,
                    "A tripped thermal fuse or a failed control-board relay is cutting power to the unit."
                )
                faults += LikelyFault(
                    "Damaged power cord / loose terminal", Confidence.POSSIBLE,
                    "A broken cord or a loose internal connector can stop the appliance from powering up."
                )
            }
            s.contains("cooling") || s.contains("heating") -> {
                faults += LikelyFault(
                    "Compressor relay or low refrigerant", Confidence.MOST_LIKELY,
                    "The start relay may be failing to run the compressor, or refrigerant has leaked from the coil."
                )
                faults += LikelyFault(
                    "Faulty thermostat / temperature sensor", Confidence.POSSIBLE,
                    "A drifting sensor reports the wrong temperature to the controller."
                )
            }
            s.contains("leak") -> {
                safety = SafetyWarning(
                    critical = false,
                    warningText = "Water pooling near electrical parts.",
                    recommendedAction = "Turn off the water supply and unplug the unit before wiping the area dry."
                )
                faults += LikelyFault(
                    "Worn door seal or clogged drain", Confidence.MOST_LIKELY,
                    "A perished rubber gasket or a blocked drain line is letting water escape."
                )
                faults += LikelyFault(
                    "Cracked water inlet valve", Confidence.POSSIBLE,
                    "A hairline crack in the inlet valve housing can leak under mains pressure."
                )
            }
            s.contains("noise") -> {
                faults += LikelyFault(
                    "Worn bearing or drive belt", Confidence.MOST_LIKELY,
                    "A worn bearing or a loose belt creates grinding or rattling during operation."
                )
                faults += LikelyFault(
                    "Loose internal component", Confidence.POSSIBLE,
                    "A fan blade or mounting bracket has come loose and is vibrating against the housing."
                )
            }
            else -> {
                faults += LikelyFault(
                    "General wear of moving parts", Confidence.MOST_LIKELY,
                    "Given the age and symptoms, a primary moving component is the most likely culprit."
                )
                faults += LikelyFault(
                    "Control board glitch", Confidence.POSSIBLE,
                    "A voltage dip can leave the controller in a stuck state; a reset or board repair may help."
                )
            }
        }

        val (min, max) = when (r.applianceType.lowercase()) {
            "air conditioner (ac)" -> 1200 to 3500
            "fridge / refrigerator" -> 900 to 2800
            "washing machine" -> 850 to 2400
            "microwave oven" -> 600 to 1600
            "television" -> 1000 to 4000
            "geyser / water heater" -> 700 to 2200
            else -> 500 to 1800
        }

        val unit = r.brand.ifBlank { "unit" }
        return DiagnosisResult(
            likelyFaults = faults,
            safetyWarning = safety,
            costEstimate = CostEstimate(
                minCost = min,
                maxCost = max,
                currency = "₹",
                basisNote = "Offline estimate for a ${r.approxAge} old $unit, based on typical parts + labour rates in India."
            )
        )
    }
}

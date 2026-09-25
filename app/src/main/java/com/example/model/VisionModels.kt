package com.example.model

/**
 * Normalized 2D coordinate for bounding polygons in Google Cloud Vision API.
 * Values are proportional from 0.0 to 1.0 relative to image width and height.
 */
data class NormalizedPoint(
    val x: Float,
    val y: Float
)

/**
 * An individual object detected by Google Cloud Vision Object Localization.
 */
data class LocalizedObject(
    val name: String,
    val score: Float,
    val boundingBox: List<NormalizedPoint> = emptyList(),
    val mid: String = ""
) {
    val confidencePercentage: Int
        get() = (score * 100).coerceIn(0f, 100f).toInt()
}

/**
 * Complete result containing all localized objects detected from an appliance image.
 */
data class VisionDetectionResult(
    val objects: List<LocalizedObject> = emptyList(),
    val detectedModelNumber: String? = null,
    val detectedBrand: String? = null,
    val isSuccess: Boolean = true,
    val errorMessage: String? = null,
    val timestamp: Long = System.currentTimeMillis()
) {
    val primaryObject: LocalizedObject?
        get() = objects.maxByOrNull { it.score }

    val summary: String
        get() = if (objects.isNotEmpty()) {
            objects.take(3).joinToString(", ") { "${it.name} (${it.confidencePercentage}%)" }
        } else {
            "No objects detected"
        }
}

package com.example.model

/**
 * Model representing a nearby appliance, auto, or electronics repair mechanic shop
 * retrieved from the Google Places API.
 */
data class NearbyMechanic(
    val id: String,
    val name: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val rating: Double = 0.0,
    val userRatingsTotal: Int = 0,
    val isOpenNow: Boolean? = null,
    val distanceMeters: Int = 0,
    val phoneNumber: String? = null,
    val photoReference: String? = null,
    val types: List<String> = emptyList()
) {
    /**
     * Human-readable distance formatted in meters or kilometers.
     */
    val formattedDistance: String
        get() = if (distanceMeters < 1000) {
            "${distanceMeters} m"
        } else {
            String.format("%.1f km", distanceMeters / 1000.0)
        }

    /**
     * Star rating formatted to 1 decimal place.
     */
    val formattedRating: String
        get() = if (rating > 0.0) String.format("%.1f", rating) else "New"
}

/**
 * Status of the nearby repair shops query.
 */
sealed class MechanicsUiState {
    object Idle : MechanicsUiState()
    object Loading : MechanicsUiState()
    data class Success(
        val mechanics: List<NearbyMechanic>,
        val userLatitude: Double,
        val userLongitude: Double,
        val selectedMechanicId: String? = null
    ) : MechanicsUiState()
    data class Empty(val message: String = "No repair mechanics found nearby.") : MechanicsUiState()
    data class Error(val message: String) : MechanicsUiState()
}

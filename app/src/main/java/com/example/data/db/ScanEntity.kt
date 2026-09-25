package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scans")
data class ScanEntity(
    @PrimaryKey val id: String,
    val applianceType: String,
    val brand: String,
    val approxAge: String,
    val symptomsJson: String,
    val note: String?,
    val photoUri: String?,
    val likelyFaultsJson: String,
    val safetyWarningJson: String?,
    val costEstimateJson: String,
    val mechanicsJson: String,
    val timestamp: Long
)

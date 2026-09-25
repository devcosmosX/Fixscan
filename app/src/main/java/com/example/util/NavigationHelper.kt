package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast

object NavigationHelper {

    private const val TAG = "NavigationHelper"

    /**
     * Launches Google Maps turn-by-turn navigation to the specified latitude and longitude coordinates.
     * Falls back to standard browser navigation if the Google Maps app is not installed.
     */
    fun startNavigation(context: Context, latitude: Double, longitude: Double, destinationName: String? = null) {
        try {
            // Intent 1: Direct Google Maps Turn-by-Turn Navigation mode
            val navUri = Uri.parse("google.navigation:q=$latitude,$longitude&mode=d")
            val mapIntent = Intent(Intent.ACTION_VIEW, navUri).apply {
                setPackage("com.google.android.apps.maps")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            if (mapIntent.resolveActivity(context.packageManager) != null) {
                context.startActivity(mapIntent)
                return
            }
        } catch (e: Exception) {
            Log.w(TAG, "Direct google.navigation intent failed: ${e.message}")
        }

        // Intent 2: Universal Google Maps web/app directions intent
        try {
            val encodedName = destinationName?.let { Uri.encode(it) } ?: ""
            val webUri = if (encodedName.isNotEmpty()) {
                Uri.parse("https://www.google.com/maps/dir/?api=1&destination=$latitude,$longitude&destination_place_id=$encodedName")
            } else {
                Uri.parse("https://www.google.com/maps/dir/?api=1&destination=$latitude,$longitude")
            }

            val webIntent = Intent(Intent.ACTION_VIEW, webUri).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(webIntent)
        } catch (e: Exception) {
            Log.e(TAG, "All navigation intents failed: ${e.message}")
            Toast.makeText(context, "Could not open Google Maps navigation.", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Opens location marker on Google Maps.
     */
    fun viewLocationOnMap(context: Context, latitude: Double, longitude: Double, label: String? = null) {
        try {
            val geoUri = if (!label.isNullOrEmpty()) {
                Uri.parse("geo:0,0?q=$latitude,$longitude(${Uri.encode(label)})")
            } else {
                Uri.parse("geo:$latitude,$longitude?q=$latitude,$longitude")
            }
            val intent = Intent(Intent.ACTION_VIEW, geoUri).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            startNavigation(context, latitude, longitude, label)
        }
    }

    /**
     * Dials a phone number for contacting a repair technician.
     */
    fun callMechanic(context: Context, phoneNumber: String) {
        try {
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${phoneNumber.trim()}"))
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Cannot open dialer: $phoneNumber", Toast.LENGTH_SHORT).show()
        }
    }
}

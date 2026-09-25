package com.example

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings

class FixScanApplication : Application() {

    lateinit var analytics: FirebaseAnalytics
        private set

    lateinit var firestore: FirebaseFirestore
        private set

    override fun onCreate() {
        super.onCreate()
        initFirebase()
    }

    private fun initFirebase() {
        try {
            if (FirebaseApp.getApps(this).isEmpty()) {
                val options = FirebaseOptions.fromResource(this)
                if (options != null) {
                    FirebaseApp.initializeApp(this, options)
                    Log.d(TAG, "Firebase initialized successfully from resources (Project ID: ${options.projectId})")
                } else {
                    FirebaseApp.initializeApp(this)
                    Log.d(TAG, "Firebase initialized with default configuration")
                }
            } else {
                Log.d(TAG, "Firebase already initialized by content provider")
            }

            // Initialize Firebase Analytics
            analytics = FirebaseAnalytics.getInstance(this)

            // Initialize Firestore
            firestore = FirebaseFirestore.getInstance()

            Log.d(TAG, "Firebase services (Auth, Firestore, Analytics) connected successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize Firebase SDK: ${e.message}", e)
        }
    }

    companion object {
        private const val TAG = "FixScanApplication"
    }
}

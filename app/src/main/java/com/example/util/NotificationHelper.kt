package com.example.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity
import com.example.R

/**
 * Helper utility to manage push and system notifications for FixScan,
 * styled to match the FixScan Ocean Teal theme.
 */
object NotificationHelper {

    const val AUTH_CHANNEL_ID = "fixscan_auth_channel"
    private const val AUTH_CHANNEL_NAME = "Account & Authentication"
    private const val AUTH_CHANNEL_DESC = "Notifications for login, sign up, and account activity"

    // FixScan Ocean Teal color
    private const val THEME_COLOR = 0xFF0F3A3D.toInt()

    private const val NOTIF_ID_LOGIN = 1001
    private const val NOTIF_ID_SIGNUP = 1002
    private const val NOTIF_ID_GOOGLE = 1003
    private const val NOTIF_ID_PASSWORD_RESET = 1004
    private const val NOTIF_ID_GENERIC = 2001

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                AUTH_CHANNEL_ID,
                AUTH_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = AUTH_CHANNEL_DESC
                enableLights(true)
                lightColor = THEME_COLOR
                enableVibration(true)
                setShowBadge(true)
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            notificationManager?.createNotificationChannel(channel)
        }
    }

    private fun getPendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        return PendingIntent.getActivity(context, 0, intent, flags)
    }

    private fun getLargeIcon(context: Context) = try {
        BitmapFactory.decodeResource(context.resources, R.drawable.img_app_icon)
    } catch (e: Exception) {
        null
    }

    fun showLoginNotification(context: Context, userName: String) {
        createNotificationChannels(context)
        val title = "Welcome Back to FixScan! 👋"
        val message = "Signed in as $userName. Your AI Appliance Diagnostics workstation is ready."

        sendNotification(
            context = context,
            notificationId = NOTIF_ID_LOGIN,
            title = title,
            message = message
        )
    }

    fun showSignUpNotification(context: Context, userName: String) {
        createNotificationChannels(context)
        val title = "Account Created Successfully! 🎉"
        val message = "Welcome to FixScan, $userName! Snap or upload your first appliance photo to run AI diagnosis."

        sendNotification(
            context = context,
            notificationId = NOTIF_ID_SIGNUP,
            title = title,
            message = message
        )
    }

    fun showGoogleSignInNotification(context: Context, userName: String) {
        createNotificationChannels(context)
        val title = "Google Sign-In Successful 🚀"
        val message = "Signed in as $userName. Encrypted history and verified repair quotes are now synced."

        sendNotification(
            context = context,
            notificationId = NOTIF_ID_GOOGLE,
            title = title,
            message = message
        )
    }

    fun showPasswordResetNotification(context: Context, email: String) {
        createNotificationChannels(context)
        val title = "Password Recovery Sent ✉️"
        val message = "We sent recovery instructions to $email. Follow the link to reset your credentials."

        sendNotification(
            context = context,
            notificationId = NOTIF_ID_PASSWORD_RESET,
            title = title,
            message = message
        )
    }

    fun showNotification(
        context: Context,
        title: String,
        message: String,
        notificationId: Int = NOTIF_ID_GENERIC
    ) {
        createNotificationChannels(context)
        sendNotification(
            context = context,
            notificationId = notificationId,
            title = title,
            message = message
        )
    }

    private fun sendNotification(
        context: Context,
        notificationId: Int,
        title: String,
        message: String
    ) {
        try {
            val pendingIntent = getPendingIntent(context)
            val largeIcon = getLargeIcon(context)

            val builder = NotificationCompat.Builder(context, AUTH_CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(NotificationCompat.BigTextStyle().bigText(message))
                .setColor(THEME_COLOR)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setContentIntent(pendingIntent)

            if (largeIcon != null) {
                builder.setLargeIcon(largeIcon)
            }

            val manager = NotificationManagerCompat.from(context)
            // On Android 13+, if permission isn't granted, notify() throws SecurityException or is silently dropped
            manager.notify(notificationId, builder.build())
        } catch (e: SecurityException) {
            // Notifications permission not granted yet by user on Android 13+
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

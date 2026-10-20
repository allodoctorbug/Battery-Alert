package com.example

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

class BatteryAlertApp : Application() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Channel 1: Ongoing Foreground Service Monitor
            val monitorChannel = NotificationChannel(
                CHANNEL_MONITOR_ID,
                "Moniteur de Batterie Actif",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Notification permanente indiquant que la surveillance de batterie est en cours"
                setShowBadge(false)
            }

            // Channel 2: High Priority Critical Battery Alert
            val alertChannel = NotificationChannel(
                CHANNEL_ALERT_ID,
                "Alerte Batterie Critique (<20%)",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alertes sonores critiques lorsque la batterie descend sous le seuil d'alerte"
                enableVibration(true)
                setShowBadge(true)
            }

            notificationManager.createNotificationChannel(monitorChannel)
            notificationManager.createNotificationChannel(alertChannel)
        }
    }

    companion object {
        const val CHANNEL_MONITOR_ID = "battery_monitor_channel"
        const val CHANNEL_ALERT_ID = "battery_critical_alert_channel"
    }
}

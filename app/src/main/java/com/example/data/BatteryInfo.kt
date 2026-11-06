package com.example.data

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager

data class BatteryInfo(
    val level: Int = 100,
    val isCharging: Boolean = false,
    val isPlugged: Boolean = false
) {
    val isLow: Boolean
        get() = level <= 20

    fun isUnderThreshold(threshold: Int): Boolean = level <= threshold

    companion object {
        fun fromIntent(intent: Intent?): BatteryInfo {
            if (intent == null) return BatteryInfo()

            val rawLevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
            val level = if (rawLevel >= 0 && scale > 0) {
                ((rawLevel.toFloat() / scale.toFloat()) * 100).toInt()
            } else {
                50
            }

            val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
            val plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0)
            val isPlugged = plugged > 0 ||
                    plugged == BatteryManager.BATTERY_PLUGGED_AC ||
                    plugged == BatteryManager.BATTERY_PLUGGED_USB ||
                    plugged == BatteryManager.BATTERY_PLUGGED_WIRELESS

            val isCharging = isPlugged ||
                    status == BatteryManager.BATTERY_STATUS_CHARGING ||
                    status == BatteryManager.BATTERY_STATUS_FULL

            return BatteryInfo(
                level = level.coerceIn(0, 100),
                isCharging = isCharging,
                isPlugged = isPlugged
            )
        }

        fun getCurrent(context: Context): BatteryInfo {
            val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            val batteryIntent = context.registerReceiver(null, filter)
            return fromIntent(batteryIntent)
        }
    }
}

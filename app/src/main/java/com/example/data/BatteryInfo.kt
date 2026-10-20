package com.example.data

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager

data class BatteryInfo(
    val level: Int = 100,
    val isCharging: Boolean = false
) {
    val isLow: Boolean
        get() = level <= 20

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
            val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                    status == BatteryManager.BATTERY_STATUS_FULL

            return BatteryInfo(
                level = level.coerceIn(0, 100),
                isCharging = isCharging
            )
        }

        fun getCurrent(context: Context): BatteryInfo {
            val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            val batteryIntent = context.registerReceiver(null, filter)
            return fromIntent(batteryIntent)
        }
    }
}

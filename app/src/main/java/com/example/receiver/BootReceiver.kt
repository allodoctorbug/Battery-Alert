package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.data.PreferencesManager
import com.example.service.BatteryMonitorService

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null) return
        val action = intent?.action
        if (action == Intent.ACTION_BOOT_COMPLETED || action == Intent.ACTION_MY_PACKAGE_REPLACED) {
            val prefs = PreferencesManager.getInstance(context)
            if (prefs.settingsFlow.value.backgroundServiceEnabled) {
                BatteryMonitorService.start(context)
            }
        }
    }
}

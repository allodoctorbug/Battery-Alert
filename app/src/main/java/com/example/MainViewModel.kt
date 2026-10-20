package com.example

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.audio.BuzzerManager
import com.example.data.AppSettings
import com.example.data.BatteryInfo
import com.example.data.PreferencesManager
import com.example.service.BatteryMonitorService
import kotlinx.coroutines.flow.StateFlow

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val prefsManager = PreferencesManager.getInstance(application)

    val settings: StateFlow<AppSettings> = prefsManager.settingsFlow
    val batteryInfo: StateFlow<BatteryInfo> = BatteryMonitorService.currentBatteryInfo
    val isServiceRunning: StateFlow<Boolean> = BatteryMonitorService.isServiceRunning
    val isBuzzerPlaying: StateFlow<Boolean> = BuzzerManager.isPlaying

    init {
        val initialInfo = BatteryInfo.getCurrent(application)
        if (BatteryMonitorService.currentBatteryInfo.value.level == 100 &&
            !BatteryMonitorService.isServiceRunning.value
        ) {
            if (settings.value.backgroundServiceEnabled) {
                BatteryMonitorService.start(application)
            }
        }
    }

    fun updateSettings(newSettings: AppSettings) {
        prefsManager.updateSettings(newSettings)
        if (newSettings.backgroundServiceEnabled && !isServiceRunning.value) {
            BatteryMonitorService.start(getApplication())
        } else if (!newSettings.backgroundServiceEnabled && isServiceRunning.value) {
            BatteryMonitorService.stop(getApplication())
        }
    }

    fun setBeepInterval(seconds: Int) {
        updateSettings(settings.value.copy(beepIntervalSeconds = seconds))
    }

    fun setThreshold(threshold: Int) {
        updateSettings(settings.value.copy(thresholdPercent = threshold))
    }

    fun setBeepDuration(durationMs: Int) {
        updateSettings(settings.value.copy(beepDurationMs = durationMs))
    }

    fun toggleService(enabled: Boolean) {
        updateSettings(settings.value.copy(backgroundServiceEnabled = enabled))
    }

    fun toggleIgnoreWhenCharging(ignore: Boolean) {
        updateSettings(settings.value.copy(ignoreWhenCharging = ignore))
    }

    fun playTestBeep() {
        val currentSettings = settings.value
        BuzzerManager.playLongBeep(
            context = getApplication(),
            durationMs = currentSettings.beepDurationMs,
            frequencyHz = 880,
            vibrate = currentSettings.vibrationEnabled
        )
    }

    fun stopBeep() {
        BuzzerManager.stop()
    }
}

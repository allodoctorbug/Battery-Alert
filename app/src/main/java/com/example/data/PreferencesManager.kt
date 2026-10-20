package com.example.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PreferencesManager private constructor(context: Context) {

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _settingsFlow = MutableStateFlow(loadSettings())
    val settingsFlow: StateFlow<AppSettings> = _settingsFlow.asStateFlow()

    private fun loadSettings(): AppSettings {
        val threshold = prefs.getInt(KEY_THRESHOLD, 10)
        val duration = prefs.getInt(KEY_DURATION_MS, 800)
        val interval = prefs.getInt(KEY_INTERVAL_SEC, 180)
        val serviceEnabled = prefs.getBoolean(KEY_SERVICE_ENABLED, true)
        val vibration = prefs.getBoolean(KEY_VIBRATION, true)
        val ignoreCharging = prefs.getBoolean(KEY_IGNORE_CHARGING, true)

        return AppSettings(
            thresholdPercent = threshold,
            beepDurationMs = duration,
            beepIntervalSeconds = interval,
            backgroundServiceEnabled = serviceEnabled,
            vibrationEnabled = vibration,
            ignoreWhenCharging = ignoreCharging
        )
    }

    fun updateSettings(newSettings: AppSettings) {
        prefs.edit()
            .putInt(KEY_THRESHOLD, newSettings.thresholdPercent)
            .putInt(KEY_DURATION_MS, newSettings.beepDurationMs)
            .putInt(KEY_INTERVAL_SEC, newSettings.beepIntervalSeconds)
            .putBoolean(KEY_SERVICE_ENABLED, newSettings.backgroundServiceEnabled)
            .putBoolean(KEY_VIBRATION, newSettings.vibrationEnabled)
            .putBoolean(KEY_IGNORE_CHARGING, newSettings.ignoreWhenCharging)
            .apply()

        _settingsFlow.value = newSettings
    }

    var lastAlertTimestamp: Long
        get() = prefs.getLong(KEY_LAST_ALERT_TIMESTAMP, 0L)
        set(value) = prefs.edit().putLong(KEY_LAST_ALERT_TIMESTAMP, value).apply()

    var lastObservedLevel: Int
        get() = prefs.getInt(KEY_LAST_OBSERVED_LEVEL, -1)
        set(value) = prefs.edit().putInt(KEY_LAST_OBSERVED_LEVEL, value).apply()

    companion object {
        private const val PREFS_NAME = "battery_alert_prefs"
        private const val KEY_THRESHOLD = "threshold_percent"
        private const val KEY_DURATION_MS = "beep_duration_ms"
        private const val KEY_INTERVAL_SEC = "beep_interval_sec"
        private const val KEY_SERVICE_ENABLED = "service_enabled"
        private const val KEY_VIBRATION = "vibration_enabled"
        private const val KEY_IGNORE_CHARGING = "ignore_charging"
        private const val KEY_LAST_ALERT_TIMESTAMP = "last_alert_timestamp"
        private const val KEY_LAST_OBSERVED_LEVEL = "last_observed_level"

        @Volatile
        private var instance: PreferencesManager? = null

        fun getInstance(context: Context): PreferencesManager {
            return instance ?: synchronized(this) {
                instance ?: PreferencesManager(context).also { instance = it }
            }
        }
    }
}

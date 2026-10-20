package com.example.data

data class AppSettings(
    val thresholdPercent: Int = 20,           // Seuil par défaut : 20%
    val beepDurationMs: Int = 2500,            // Bip long : 2.5 secondes
    val beepIntervalSeconds: Int = 60,         // Délai variable entre les bips (ex: 60s)
    val backgroundServiceEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val ignoreWhenCharging: Boolean = true
)

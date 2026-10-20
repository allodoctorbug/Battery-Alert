package com.example.data

data class AppSettings(
    val thresholdPercent: Int = 10,            // Seuil d'alerte réduit (10% par défaut)
    val beepDurationMs: Int = 800,             // Temps du bip raccourci (0.8 seconde)
    val beepIntervalSeconds: Int = 180,        // Délai augmenté entre les bips (3 minutes = 180s)
    val backgroundServiceEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val ignoreWhenCharging: Boolean = true
)

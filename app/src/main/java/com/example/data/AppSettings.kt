package com.example.data

enum class AlertSoundType(val label: String, val subtitle: String) {
    SENSUAL_VOICE("Voix sensuelle", "Voix féminine : « Batterie déchargée »"),
    BEEP("Bip sonore", "Signal sonore classique")
}

data class AppSettings(
    val thresholdPercent: Int = 10,
    val beepDurationMs: Int = 800,
    val beepIntervalSeconds: Int = 180,
    val alertSoundType: AlertSoundType = AlertSoundType.SENSUAL_VOICE,
    val backgroundServiceEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val ignoreWhenCharging: Boolean = true
)

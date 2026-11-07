package com.example.ui.screens

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Power
import androidx.compose.material.icons.filled.PowerOff
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AlertSoundType
import com.example.data.AppSettings
import com.example.data.BatteryInfo
import com.example.ui.components.BatteryGauge
import com.example.ui.theme.BatteryAmber
import com.example.ui.theme.BatteryGreen
import com.example.ui.theme.BatteryRed
import com.example.ui.theme.PrimaryBlue

@Composable
fun DashboardScreen(
    batteryInfo: BatteryInfo,
    settings: AppSettings,
    isServiceRunning: Boolean,
    isBuzzerPlaying: Boolean,
    hasNotificationPermission: Boolean,
    onRequestNotificationPermission: () -> Unit,
    onToggleService: (Boolean) -> Unit,
    onToggleIgnoreCharging: (Boolean) -> Unit,
    onUpdateAlertSoundType: (AlertSoundType) -> Unit,
    onUpdateBeepDuration: (Int) -> Unit,
    onUpdateInterval: (Int) -> Unit,
    onUpdateThreshold: (Int) -> Unit,
    onTestBeep: () -> Unit,
    onStopBeep: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isCritical = batteryInfo.level <= settings.thresholdPercent &&
            !(batteryInfo.isCharging && settings.ignoreWhenCharging)

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Slim Notification Permission Banner (if needed on Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasNotificationPermission) {
            Card(
                colors = CardDefaults.cardColors(containerColor = BatteryAmber.copy(alpha = 0.15f)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("notification_permission_banner")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.NotificationsActive,
                        contentDescription = null,
                        tint = BatteryAmber,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Autorisation requise pour alerter écran verrouillé",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        modifier = Modifier.weight(1f)
                    )
                    Button(
                        onClick = onRequestNotificationPermission,
                        colors = ButtonDefaults.buttonColors(containerColor = BatteryAmber),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier
                            .height(28.dp)
                            .testTag("request_permission_button")
                    ) {
                        Text("Activer", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Header Card: Gauge + Live Status + Action Button side-by-side
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isCritical) BatteryRed.copy(alpha = 0.12f)
                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 12.dp, vertical = 8.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Scalable Compact Gauge (98.dp)
                BatteryGauge(
                    level = batteryInfo.level,
                    isCharging = batteryInfo.isCharging,
                    threshold = settings.thresholdPercent,
                    size = 98.dp
                )

                Spacer(modifier = Modifier.width(12.dp))

                // Status info & Test/Stop Action Button
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (batteryInfo.isCharging) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(BatteryGreen.copy(alpha = 0.2f))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = "EN CHARGE",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BatteryGreen
                                )
                            }
                        } else if (isCritical) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(BatteryRed.copy(alpha = 0.25f))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = "CRITIQUE (<${settings.thresholdPercent}%)",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BatteryRed
                                )
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(BatteryGreen.copy(alpha = 0.15f))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = if (isServiceRunning) "SURVEILLANCE ACTIVE" else "EN PAUSE",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isServiceRunning) BatteryGreen else Color.Gray
                                )
                            }
                        }
                    }

                    Text(
                        text = if (batteryInfo.isCharging) {
                            "En charge sur secteur • Alerte coupée"
                        } else if (isCritical) {
                            val sound = if (settings.alertSoundType == AlertSoundType.SENSUAL_VOICE) "Voix sensuelle" else "Bip"
                            "$sound toutes les ${formatDelay(settings.beepIntervalSeconds)}"
                        } else {
                            "Alerte si niveau ≤ ${settings.thresholdPercent}%"
                        },
                        fontSize = 12.sp,
                        color = if (batteryInfo.isCharging) BatteryGreen else MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Test / Stop Button
                    if (isBuzzerPlaying) {
                        Button(
                            onClick = onStopBeep,
                            colors = ButtonDefaults.buttonColors(containerColor = BatteryRed),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(38.dp)
                                .testTag("stop_beep_button")
                        ) {
                            Icon(imageVector = Icons.Default.Stop, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "ARRÊTER L'ALERTE", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Button(
                            onClick = onTestBeep,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isCritical) BatteryRed else PrimaryBlue
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(38.dp)
                                .testTag("test_beep_button")
                        ) {
                            val icon = if (settings.alertSoundType == AlertSoundType.SENSUAL_VOICE) {
                                Icons.Default.RecordVoiceOver
                            } else {
                                Icons.AutoMirrored.Filled.VolumeUp
                            }
                            val buttonText = if (settings.alertSoundType == AlertSoundType.SENSUAL_VOICE) {
                                "Écouter la voix sensuelle"
                            } else {
                                "Tester le bip (${"%.1f".format(settings.beepDurationMs / 1000f)}s)"
                            }

                            Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = buttonText,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Bandeau 1: Type d'alerte (Voix sensuelle féminine "Batterie déchargée" ou Bip sonore)
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("alert_type_card")
        ) {
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.RecordVoiceOver,
                            contentDescription = null,
                            tint = PrimaryBlue,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Alerte sonore",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        AlertSoundType.values().forEach { type ->
                            val isSelected = settings.alertSoundType == type
                            FilterChip(
                                selected = isSelected,
                                onClick = { onUpdateAlertSoundType(type) },
                                label = {
                                    Text(
                                        text = if (type == AlertSoundType.SENSUAL_VOICE) "Voix sensuelle ★" else "Bip",
                                        fontSize = 10.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = PrimaryBlue,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }

                if (settings.alertSoundType == AlertSoundType.SENSUAL_VOICE) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Voix féminine sensuelle suave disant « Batterie déchargée »",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Temps du bip", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(text = "%.1f s".format(settings.beepDurationMs / 1000f), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BatteryAmber)
                    }
                    Slider(
                        value = (settings.beepDurationMs / 100).toFloat(),
                        onValueChange = {
                            val ms = (it.toInt() * 100).coerceIn(200, 2000)
                            onUpdateBeepDuration(ms)
                        },
                        valueRange = 2f..20f,
                        steps = 17,
                        colors = SliderDefaults.colors(
                            thumbColor = BatteryAmber,
                            activeTrackColor = BatteryAmber
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(26.dp)
                            .testTag("beep_duration_slider")
                    )
                }
            }
        }

        // Bandeau 2: Barre horizontale variable pour le DÉLAI ENTRE LES ALERTES (1 min à 15 min)
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("beep_interval_card")
        ) {
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.HourglassBottom,
                            contentDescription = null,
                            tint = PrimaryBlue,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Délai entre les alertes",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = formatDelay(settings.beepIntervalSeconds),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = PrimaryBlue
                    )
                }

                Slider(
                    value = (settings.beepIntervalSeconds / 30).toFloat(),
                    onValueChange = {
                        val sec = (it.toInt() * 30).coerceIn(60, 900)
                        onUpdateInterval(sec)
                    },
                    valueRange = 2f..30f,
                    steps = 27,
                    colors = SliderDefaults.colors(
                        thumbColor = PrimaryBlue,
                        activeTrackColor = PrimaryBlue
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(28.dp)
                        .testTag("interval_slider")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "1 min", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(text = "5 min", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(text = "10 min", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(text = "15 min", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        // Bandeau 3: Barre horizontale variable pour le SEUIL D'ALERTE (3% à 20%)
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("threshold_card")
        ) {
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = null,
                            tint = PrimaryBlue,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Seuil d'alerte",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = "${settings.thresholdPercent}%",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = PrimaryBlue
                    )
                }

                Slider(
                    value = settings.thresholdPercent.toFloat(),
                    onValueChange = {
                        val th = it.toInt().coerceIn(3, 20)
                        onUpdateThreshold(th)
                    },
                    valueRange = 3f..20f,
                    steps = 16,
                    colors = SliderDefaults.colors(
                        thumbColor = PrimaryBlue,
                        activeTrackColor = PrimaryBlue
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(28.dp)
                        .testTag("threshold_slider")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "3%", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(text = "10% (défaut)", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(text = "20%", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        // Bandeau 4: Commutateurs essentiels (Surveillance + Muet en charge)
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)) {
                // Surveillance continue
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = if (isServiceRunning) Icons.Default.CheckCircle else Icons.Default.Power,
                            contentDescription = null,
                            tint = if (isServiceRunning) BatteryGreen else Color.Gray,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Surveillance active en arrière-plan",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Switch(
                        checked = isServiceRunning,
                        onCheckedChange = onToggleService,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = BatteryGreen
                        ),
                        modifier = Modifier.testTag("service_switch")
                    )
                }

                // Muet si en charge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PowerOff,
                            contentDescription = null,
                            tint = PrimaryBlue,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Ne pas alerter si le téléphone est branché",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Switch(
                        checked = settings.ignoreWhenCharging,
                        onCheckedChange = onToggleIgnoreCharging,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = PrimaryBlue
                        ),
                        modifier = Modifier.testTag("ignore_charging_switch")
                    )
                }
            }
        }
    }
}

private fun formatDelay(seconds: Int): String {
    return if (seconds >= 60) {
        val min = seconds / 60
        val sec = seconds % 60
        if (sec > 0) "${min}m ${sec}s" else "${min} min"
    } else {
        "${seconds}s"
    }
}

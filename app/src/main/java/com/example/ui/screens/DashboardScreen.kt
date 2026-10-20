package com.example.ui.screens

import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PowerOff
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AppSettings
import com.example.data.BatteryInfo
import com.example.ui.components.BatteryGauge
import com.example.ui.theme.PicassoBlue
import com.example.ui.theme.PicassoInk
import com.example.ui.theme.PicassoOchre
import com.example.ui.theme.PicassoTerracotta
import com.example.ui.theme.PicassoVermilion

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
    onUpdateInterval: (Int) -> Unit,
    onUpdateThreshold: (Int) -> Unit,
    onTestBeep: () -> Unit,
    onStopBeep: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val isCritical = batteryInfo.level <= settings.thresholdPercent

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Notification permission banner if needed
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasNotificationPermission) {
            Card(
                colors = CardDefaults.cardColors(containerColor = PicassoOchre.copy(alpha = 0.15f)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .testTag("notification_permission_banner")
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.NotificationsActive,
                        contentDescription = null,
                        tint = PicassoOchre,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Autorisez les notifications pour biper écran verrouillé",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.weight(1f)
                    )
                    Button(
                        onClick = onRequestNotificationPermission,
                        colors = ButtonDefaults.buttonColors(containerColor = PicassoOchre),
                        modifier = Modifier.testTag("request_permission_button")
                    ) {
                        Text("Activer", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Active Alert Warning (Picasso Vermilion Banner)
        AnimatedVisibility(visible = isCritical) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 14.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(PicassoVermilion.copy(alpha = 0.12f))
                    .border(1.5.dp, PicassoVermilion, RoundedCornerShape(8.dp))
                    .padding(horizontal = 14.dp, vertical = 10.dp)
                    .testTag("critical_alert_banner")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = PicassoVermilion,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Batterie Critique : ${batteryInfo.level}% !",
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Serif,
                            color = PicassoVermilion,
                            fontSize = 15.sp
                        )
                        Text(
                            text = "Bip long répété toutes les ${formatDelay(settings.beepIntervalSeconds)} tant que < ${settings.thresholdPercent}%.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Minimalist Picasso Cubist Battery Gauge
        BatteryGauge(
            level = batteryInfo.level,
            isCharging = batteryInfo.isCharging,
            threshold = settings.thresholdPercent,
            modifier = Modifier.padding(vertical = 4.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Action Button: Primary Play Test or Stop
        if (isBuzzerPlaying) {
            Button(
                onClick = onStopBeep,
                colors = ButtonDefaults.buttonColors(containerColor = PicassoVermilion),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("stop_beep_button")
            ) {
                Icon(imageVector = Icons.Default.Stop, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "ARRÊTER LE BIP",
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = Color.White
                )
            }
        } else {
            Button(
                onClick = onTestBeep,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isCritical) PicassoVermilion else PicassoBlue
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("test_beep_button")
            ) {
                Icon(imageVector = Icons.Default.VolumeUp, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "TESTER LE BIP LONG (2.5s)",
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Card: Variable delay between beeps ("délai variable entre les bips")
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, PicassoInk.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                .testTag("interval_card")
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.HourglassTop,
                            contentDescription = null,
                            tint = PicassoTerracotta,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Délai entre les bips",
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }

                    Text(
                        text = formatDelay(settings.beepIntervalSeconds),
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Serif,
                        color = PicassoTerracotta,
                        fontSize = 17.sp
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Délai d'attente avant le prochain bip lorsque la batterie est sous les ${settings.thresholdPercent}%.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Slider(
                    value = settings.beepIntervalSeconds.toFloat(),
                    onValueChange = { onUpdateInterval(it.toInt()) },
                    valueRange = 15f..300f,
                    steps = 18,
                    colors = SliderDefaults.colors(
                        thumbColor = PicassoTerracotta,
                        activeTrackColor = PicassoTerracotta
                    ),
                    modifier = Modifier.testTag("interval_slider")
                )

                // Quick preset buttons for delay
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(
                        15 to "15s",
                        30 to "30s",
                        60 to "1 min",
                        120 to "2 min",
                        300 to "5 min"
                    ).forEach { (seconds, label) ->
                        val selected = settings.beepIntervalSeconds == seconds
                        FilterChip(
                            selected = selected,
                            onClick = { onUpdateInterval(seconds) },
                            label = { Text(label, fontSize = 11.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PicassoTerracotta,
                                selectedLabelColor = Color.White
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Card: Minimalist Essential Controls (Threshold & Service Toggles)
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, PicassoInk.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Seuil de déclenchement
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Seuil critique",
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(15, 20, 25).forEach { th ->
                            val isSel = settings.thresholdPercent == th
                            FilterChip(
                                selected = isSel,
                                onClick = { onUpdateThreshold(th) },
                                label = { Text(if (th == 20) "20% ★" else "$th%", fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = PicassoBlue,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Surveillance continue toggle
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
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = if (isServiceRunning) PicassoBlue else PicassoInk.copy(alpha = 0.5f),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Surveillance active",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = if (isServiceRunning) "Actif en arrière-plan" else "Désactivé",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Switch(
                        checked = isServiceRunning,
                        onCheckedChange = onToggleService,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = PicassoBlue
                        ),
                        modifier = Modifier.testTag("service_switch")
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Muet si en charge toggle
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
                            tint = PicassoInk.copy(alpha = 0.6f),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Muet si en charge",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "Ne bipe pas si branché",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Switch(
                        checked = settings.ignoreWhenCharging,
                        onCheckedChange = onToggleIgnoreCharging,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = PicassoBlue
                        ),
                        modifier = Modifier.testTag("ignore_charging_switch")
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
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

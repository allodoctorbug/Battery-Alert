package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.screens.DashboardScreen
import com.example.ui.theme.BatteryRed
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.PrimaryBlue

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                val batteryInfo by viewModel.batteryInfo.collectAsStateWithLifecycle()
                val settings by viewModel.settings.collectAsStateWithLifecycle()
                val isServiceRunning by viewModel.isServiceRunning.collectAsStateWithLifecycle()
                val isBuzzerPlaying by viewModel.isBuzzerPlaying.collectAsStateWithLifecycle()

                var hasNotificationPermission by remember {
                    mutableStateOf(
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            ContextCompat.checkSelfPermission(
                                this,
                                Manifest.permission.POST_NOTIFICATIONS
                            ) == PackageManager.PERMISSION_GRANTED
                        } else {
                            true
                        }
                    )
                }

                val permissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission()
                ) { isGranted ->
                    hasNotificationPermission = isGranted
                }

                LaunchedEffect(Unit) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasNotificationPermission) {
                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = MaterialTheme.colorScheme.background,
                    topBar = {
                        CenterAlignedTopAppBar(
                            title = {
                                Text(
                                    text = "Alerte Batterie (<${settings.thresholdPercent}%)",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            },
                            actions = {
                                IconButton(
                                    onClick = { viewModel.playTestBeep() },
                                    modifier = Modifier.testTag("top_bar_test_beep_button")
                                ) {
                                    Icon(
                                        imageVector = if (settings.alertSoundType == com.example.data.AlertSoundType.SENSUAL_VOICE) {
                                            Icons.Default.RecordVoiceOver
                                        } else {
                                            Icons.AutoMirrored.Filled.VolumeUp
                                        },
                                        contentDescription = "Tester l'alerte",
                                        tint = if (isBuzzerPlaying) BatteryRed else PrimaryBlue
                                    )
                                }
                            },
                            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            )
                        )
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        DashboardScreen(
                            batteryInfo = batteryInfo,
                            settings = settings,
                            isServiceRunning = isServiceRunning,
                            isBuzzerPlaying = isBuzzerPlaying,
                            hasNotificationPermission = hasNotificationPermission,
                            onRequestNotificationPermission = {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                }
                            },
                            onToggleService = { viewModel.toggleService(it) },
                            onToggleIgnoreCharging = { viewModel.toggleIgnoreWhenCharging(it) },
                            onUpdateAlertSoundType = { viewModel.setAlertSoundType(it) },
                            onUpdateBeepDuration = { viewModel.setBeepDuration(it) },
                            onUpdateInterval = { viewModel.setBeepInterval(it) },
                            onUpdateThreshold = { viewModel.setThreshold(it) },
                            onTestBeep = { viewModel.playTestBeep() },
                            onStopBeep = { viewModel.stopBeep() }
                        )
                    }
                }
            }
        }
    }
}

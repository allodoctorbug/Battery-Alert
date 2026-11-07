package com.example.service

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import com.example.BatteryAlertApp
import com.example.MainActivity
import com.example.audio.BuzzerManager
import com.example.data.BatteryInfo
import com.example.data.PreferencesManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class BatteryMonitorService : Service() {

    private lateinit var prefsManager: PreferencesManager
    private var powerManager: PowerManager? = null
    private var wakeLock: PowerManager.WakeLock? = null
    private val serviceScope = CoroutineScope(Dispatchers.Main)
    private var alertLoopJob: Job? = null

    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val action = intent?.action
            if (action == Intent.ACTION_BATTERY_CHANGED) {
                handleBatteryChanged(intent)
            } else if (action == Intent.ACTION_POWER_CONNECTED || action == Intent.ACTION_POWER_DISCONNECTED) {
                context?.let { ctx ->
                    val info = BatteryInfo.getCurrent(ctx)
                    _currentBatteryInfo.value = info
                    updateMonitorNotification(info)
                    checkAndManageAlertLoop(info)
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        prefsManager = PreferencesManager.getInstance(this)
        powerManager = getSystemService(Context.POWER_SERVICE) as? PowerManager

        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_BATTERY_CHANGED)
            addAction(Intent.ACTION_POWER_CONNECTED)
            addAction(Intent.ACTION_POWER_DISCONNECTED)
        }
        registerReceiver(batteryReceiver, filter)
        _isServiceRunning.value = true

        // React immediately if user alters threshold, ignoreWhenCharging or duration in the UI
        serviceScope.launch {
            prefsManager.settingsFlow.collect {
                val currentInfo = _currentBatteryInfo.value
                updateMonitorNotification(currentInfo)
                checkAndManageAlertLoop(currentInfo)
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP_SERVICE -> {
                stopAlertLoop()
                stopSelf()
                return START_NOT_STICKY
            }
            ACTION_STOP_BEEP -> {
                BuzzerManager.stop()
                dismissCriticalAlertNotification()
                return START_STICKY
            }
        }

        startInForeground()

        val initialInfo = BatteryInfo.getCurrent(this)
        _currentBatteryInfo.value = initialInfo
        updateMonitorNotification(initialInfo)
        checkAndManageAlertLoop(initialInfo)

        return START_STICKY
    }

    private fun startInForeground() {
        val initialNotification = buildMonitorNotification(BatteryInfo.getCurrent(this))

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                startForeground(
                    MONITOR_NOTIFICATION_ID,
                    initialNotification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
                )
            } else {
                startForeground(MONITOR_NOTIFICATION_ID, initialNotification)
            }
        } else {
            startForeground(MONITOR_NOTIFICATION_ID, initialNotification)
        }
    }

    private fun handleBatteryChanged(intent: Intent) {
        val batteryInfo = BatteryInfo.fromIntent(intent)
        _currentBatteryInfo.value = batteryInfo
        updateMonitorNotification(batteryInfo)
        checkAndManageAlertLoop(batteryInfo)
    }

    private fun checkAndManageAlertLoop(batteryInfo: BatteryInfo) {
        val settings = prefsManager.settingsFlow.value
        val isCritical = batteryInfo.level <= settings.thresholdPercent &&
                !(batteryInfo.isCharging && settings.ignoreWhenCharging)

        if (isCritical) {
            if (alertLoopJob == null || alertLoopJob?.isActive == false) {
                alertLoopJob = serviceScope.launch {
                    while (isActive) {
                        val currentSettings = prefsManager.settingsFlow.value
                        val currentInfo = _currentBatteryInfo.value
                        val isStillCritical = currentInfo.level <= currentSettings.thresholdPercent &&
                                !(currentInfo.isCharging && currentSettings.ignoreWhenCharging)

                        if (!isStillCritical) {
                            stopAlertLoop()
                            break
                        }

                        triggerAlert(currentInfo.level)

                        val delayMs = (currentSettings.beepIntervalSeconds * 1000L).coerceAtLeast(5000L)
                        delay(delayMs)
                    }
                }
            }
        } else {
            // Dismiss notification and cancel any ongoing beep immediately
            stopAlertLoop()
        }
    }

    private fun stopAlertLoop() {
        alertLoopJob?.cancel()
        alertLoopJob = null
        try {
            wakeLock?.let {
                if (it.isHeld) it.release()
            }
        } catch (_: Exception) {}
        BuzzerManager.stop()
        dismissCriticalAlertNotification()
    }

    private fun dismissCriticalAlertNotification() {
        try {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(ALERT_NOTIFICATION_ID)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun triggerAlert(level: Int) {
        val settings = prefsManager.settingsFlow.value
        val currentInfo = _currentBatteryInfo.value
        val isStillCritical = currentInfo.level <= settings.thresholdPercent &&
                !(currentInfo.isCharging && settings.ignoreWhenCharging)

        if (!isStillCritical) {
            stopAlertLoop()
            return
        }

        acquireWakeLock(8000L)

        BuzzerManager.playAlert(
            context = this,
            alertType = settings.alertSoundType,
            durationMs = settings.beepDurationMs,
            frequencyHz = 880,
            vibrate = settings.vibrationEnabled
        )

        prefsManager.lastAlertTimestamp = System.currentTimeMillis()
        showCriticalAlertNotification(level)
    }

    private fun showCriticalAlertNotification(level: Int) {
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openPendingIntent = PendingIntent.getActivity(
            this,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopBeepIntent = Intent(this, BatteryMonitorService::class.java).apply {
            action = ACTION_STOP_BEEP
        }
        val stopPendingIntent = PendingIntent.getService(
            this,
            1,
            stopBeepIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val settings = prefsManager.settingsFlow.value
        val title = "Batterie Critique : $level% !"
        val soundLabel = when (settings.alertSoundType) {
            com.example.data.AlertSoundType.SENSUAL_VOICE -> "Voix : « Batterie déchargée »"
            com.example.data.AlertSoundType.BEEP -> "Bip sonore"
        }
        val message = "Batterie sous ${settings.thresholdPercent}%. $soundLabel répété toutes les ${formatDelay(settings.beepIntervalSeconds)}."

        val notification = NotificationCompat.Builder(this, BatteryAlertApp.CHANNEL_ALERT_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_low_battery)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setContentIntent(openPendingIntent)
            .setAutoCancel(true)
            .addAction(
                android.R.drawable.ic_media_pause,
                "Arrêter l'alerte",
                stopPendingIntent
            )
            .build()

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(ALERT_NOTIFICATION_ID, notification)
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

    private fun buildMonitorNotification(info: BatteryInfo): Notification {
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openPendingIntent = PendingIntent.getActivity(
            this,
            2,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val settings = prefsManager.settingsFlow.value
        val chargeText = if (info.isCharging) " (En charge sur secteur)" else ""
        val content = "Niveau : ${info.level}%$chargeText • Seuil : ${settings.thresholdPercent}% • Délai : ${formatDelay(settings.beepIntervalSeconds)}"

        return NotificationCompat.Builder(this, BatteryAlertApp.CHANNEL_MONITOR_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_charging)
            .setContentTitle("Surveillance de batterie")
            .setContentText(content)
            .setOngoing(true)
            .setContentIntent(openPendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun updateMonitorNotification(info: BatteryInfo) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(MONITOR_NOTIFICATION_ID, buildMonitorNotification(info))
    }

    private fun acquireWakeLock(durationMs: Long) {
        try {
            if (wakeLock == null) {
                wakeLock = powerManager?.newWakeLock(
                    PowerManager.PARTIAL_WAKE_LOCK,
                    "BatteryAlertApp:BeepWakeLock"
                )
            }
            wakeLock?.acquire(durationMs)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopAlertLoop()
        try {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(ALERT_NOTIFICATION_ID)
            notificationManager.cancel(MONITOR_NOTIFICATION_ID)
        } catch (_: Exception) {}
        try {
            unregisterReceiver(batteryReceiver)
        } catch (_: Exception) {}
        try {
            wakeLock?.let {
                if (it.isHeld) it.release()
            }
        } catch (_: Exception) {}
        BuzzerManager.stop()
        _isServiceRunning.value = false
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val MONITOR_NOTIFICATION_ID = 1001
        const val ALERT_NOTIFICATION_ID = 1002

        const val ACTION_START_SERVICE = "com.example.action.START_SERVICE"
        const val ACTION_STOP_SERVICE = "com.example.action.STOP_SERVICE"
        const val ACTION_STOP_BEEP = "com.example.action.STOP_BEEP"

        private val _isServiceRunning = MutableStateFlow(false)
        val isServiceRunning: StateFlow<Boolean> = _isServiceRunning.asStateFlow()

        private val _currentBatteryInfo = MutableStateFlow(BatteryInfo())
        val currentBatteryInfo: StateFlow<BatteryInfo> = _currentBatteryInfo.asStateFlow()

        fun start(context: Context) {
            val intent = Intent(context, BatteryMonitorService::class.java).apply {
                action = ACTION_START_SERVICE
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, BatteryMonitorService::class.java).apply {
                action = ACTION_STOP_SERVICE
            }
            context.startService(intent)
        }
    }
}

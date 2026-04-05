package com.z2a.vpn

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.net.VpnService
import android.os.Build
import android.os.ParcelFileDescriptor
import android.util.Log
import com.z2a.MainActivity
import com.z2a.R
import com.z2a.Z2aApplication

class Z2aVpnService : VpnService() {

    companion object {
        private const val TAG = "z2a-vpn"
        private const val CHANNEL_ID = "z2a_vpn"
        private const val NOTIFICATION_ID = 1
        const val ACTION_START = "com.z2a.vpn.START"
        const val ACTION_STOP = "com.z2a.vpn.STOP"
    }

    private var vpnInterface: ParcelFileDescriptor? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> {
                stopVpn()
                return START_NOT_STICKY
            }
            ACTION_START -> {
                startVpn()
                return START_STICKY
            }
        }
        return START_NOT_STICKY
    }

    private fun startVpn() {
        val app = application as Z2aApplication

        // Extract assets (lua, files, hostlists) to internal storage
        app.engineManager.extractAssets()

        // Build VPN interface
        val builder = Builder()
            .setSession("z2a")
            .setMtu(1500)
            .addAddress("10.10.10.1", 30)
            .addRoute("0.0.0.0", 0)
            .addDnsServer("1.1.1.1")
            .addDnsServer("8.8.8.8")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            builder.setMetered(false)
        }

        // Exclude our own app to prevent loops
        try {
            builder.addDisallowedApplication(packageName)
        } catch (_: Exception) {}

        vpnInterface = builder.establish()

        if (vpnInterface == null) {
            Log.e(TAG, "Failed to establish VPN interface")
            app.vpnManager.onConnectionError("VPN interface creation failed")
            stopSelf()
            return
        }

        // Protect the engine socket from VPN routing
        val fd = vpnInterface!!.fd

        // Build engine arguments from profile config
        val profileConfigText = app.engineManager.profileConfig.loadProfilesConfig()
        val args = app.engineManager.profileConfig.buildEngineArgs(
            profileConfig = profileConfigText,
            dataDir = filesDir.absolutePath,
            enabledProfileIds = app.profileRepository.getEnabledProfiles().map { it.id }.toSet(),
            silentFallback = false,
            rstFilter = true,
            austerusMode = false
        )

        // Start engine with TUN fd
        val started = app.engineManager.start(fd, args)
        if (!started) {
            Log.e(TAG, "Failed to start engine")
            app.vpnManager.onConnectionError(app.engineManager.lastError.value ?: "Engine start failed")
            vpnInterface?.close()
            vpnInterface = null
            stopSelf()
            return
        }

        app.vpnManager.onConnected()

        // Start foreground notification
        startForeground(NOTIFICATION_ID, buildNotification())

        Log.i(TAG, "VPN started successfully")
    }

    private fun stopVpn() {
        val app = application as Z2aApplication
        app.engineManager.stop()
        vpnInterface?.close()
        vpnInterface = null
        app.vpnManager.onDisconnected()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
        Log.i(TAG, "VPN stopped")
    }

    override fun onDestroy() {
        stopVpn()
        super.onDestroy()
    }

    override fun onRevoke() {
        stopVpn()
        super.onRevoke()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.vpn_notification_channel),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                setShowBadge(false)
            }
            val nm = getSystemService(NotificationManager::class.java)
            nm.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = PendingIntent.getService(
            this, 0,
            Intent(this, Z2aVpnService::class.java).apply { action = ACTION_STOP },
            PendingIntent.FLAG_IMMUTABLE
        )

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, CHANNEL_ID)
        } else {
            @Suppress("DEPRECATION")
            Notification.Builder(this)
        }
            .setContentTitle(getString(R.string.vpn_notification_title))
            .setContentText(getString(R.string.vpn_notification_text))
            .setSmallIcon(android.R.drawable.ic_lock_lock)
            .setContentIntent(pendingIntent)
            .addAction(
                Notification.Action.Builder(
                    null, getString(R.string.btn_disconnect), stopIntent
                ).build()
            )
            .setOngoing(true)
            .build()
    }
}

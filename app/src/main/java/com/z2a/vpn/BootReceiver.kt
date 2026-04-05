package com.z2a.vpn

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.z2a.data.SettingsRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        val settings = SettingsRepository(context)
        val autostart = runBlocking { settings.autostart.first() }

        if (autostart) {
            val vpnIntent = Intent(context, Z2aVpnService::class.java).apply {
                action = Z2aVpnService.ACTION_START
            }
            context.startForegroundService(vpnIntent)
        }
    }
}

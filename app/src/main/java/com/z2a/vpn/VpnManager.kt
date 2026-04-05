package com.z2a.vpn

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.VpnService
import com.z2a.data.models.ConnectionState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class VpnManager(private val context: Context) {

    private val _connectionState = MutableStateFlow(ConnectionState.DISCONNECTED)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private val _connectedSince = MutableStateFlow<Long?>(null)
    val connectedSince: StateFlow<Long?> = _connectedSince.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun prepareVpn(activity: Activity): Intent? {
        return VpnService.prepare(activity)
    }

    fun startVpn() {
        _connectionState.value = ConnectionState.CONNECTING
        _errorMessage.value = null
        val intent = Intent(context, Z2aVpnService::class.java).apply {
            action = Z2aVpnService.ACTION_START
        }
        context.startForegroundService(intent)
    }

    fun stopVpn() {
        val intent = Intent(context, Z2aVpnService::class.java).apply {
            action = Z2aVpnService.ACTION_STOP
        }
        context.startService(intent)
    }

    fun toggleVpn(activity: Activity): Intent? {
        return when (_connectionState.value) {
            ConnectionState.DISCONNECTED, ConnectionState.ERROR -> {
                val prepareIntent = prepareVpn(activity)
                if (prepareIntent != null) {
                    return prepareIntent
                }
                startVpn()
                null
            }
            ConnectionState.CONNECTED, ConnectionState.CONNECTING -> {
                stopVpn()
                null
            }
        }
    }

    fun onConnected() {
        _connectionState.value = ConnectionState.CONNECTED
        _connectedSince.value = System.currentTimeMillis()
    }

    fun onDisconnected() {
        _connectionState.value = ConnectionState.DISCONNECTED
        _connectedSince.value = null
    }

    fun onConnectionError(message: String) {
        _connectionState.value = ConnectionState.ERROR
        _errorMessage.value = message
        _connectedSince.value = null
    }
}

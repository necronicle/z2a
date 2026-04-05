package com.z2a.ui.screens.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.z2a.Z2aApplication
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as Z2aApplication
    private val settings = app.settingsRepository

    val autostart: Flow<Boolean> = settings.autostart
    val silentFallback: Flow<Boolean> = settings.silentFallback
    val rstFilter: Flow<Boolean> = settings.rstFilter
    val austerusMode: Flow<Boolean> = settings.austerusMode
    val dnsServer: Flow<String> = settings.dnsServer

    fun setAutostart(value: Boolean) = viewModelScope.launch { settings.setAutostart(value) }
    fun setSilentFallback(value: Boolean) = viewModelScope.launch { settings.setSilentFallback(value) }
    fun setRstFilter(value: Boolean) = viewModelScope.launch { settings.setRstFilter(value) }
    fun setAusterusMode(value: Boolean) = viewModelScope.launch { settings.setAusterusMode(value) }
    fun setDnsServer(value: String) = viewModelScope.launch { settings.setDnsServer(value) }

    fun clearAutocircularState() {
        app.engineManager.autocircularState.clearState()
    }
}

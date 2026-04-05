package com.z2a.ui.screens.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.z2a.Z2aApplication
import com.z2a.data.models.ConnectionState
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.combine

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as Z2aApplication

    val connectionState: StateFlow<ConnectionState> = app.vpnManager.connectionState
    val connectedSince: StateFlow<Long?> = app.vpnManager.connectedSince
    val errorMessage: StateFlow<String?> = app.vpnManager.errorMessage
    val isEngineRunning: StateFlow<Boolean> = app.engineManager.isRunning

    fun getActiveProfileCount(): Int = app.profileRepository.getEnabledProfiles().size

    fun getActiveStrategyCount(): Int =
        app.profileRepository.getEnabledProfiles().sumOf { it.strategyCount }

    fun getAutocircularEntryCount(): Int =
        app.engineManager.autocircularState.getStateEntryCount()
}

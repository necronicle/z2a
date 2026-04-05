package com.z2a

import android.app.Application
import com.z2a.data.ProfileRepository
import com.z2a.data.SettingsRepository
import com.z2a.engine.EngineManager
import com.z2a.vpn.VpnManager

class Z2aApplication : Application() {

    lateinit var engineManager: EngineManager
        private set
    lateinit var vpnManager: VpnManager
        private set
    lateinit var profileRepository: ProfileRepository
        private set
    lateinit var settingsRepository: SettingsRepository
        private set

    override fun onCreate() {
        super.onCreate()
        engineManager = EngineManager(this)
        vpnManager = VpnManager(this)
        profileRepository = ProfileRepository(this)
        settingsRepository = SettingsRepository(this)
    }
}

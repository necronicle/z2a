package com.z2a.ui.screens.profiles

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.z2a.Z2aApplication
import com.z2a.data.models.Profile
import kotlinx.coroutines.flow.StateFlow

class ProfilesViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as Z2aApplication

    val profiles: StateFlow<List<Profile>> = app.profileRepository.profiles

    fun toggleProfile(profileId: String) {
        app.profileRepository.toggleProfile(profileId)
    }
}

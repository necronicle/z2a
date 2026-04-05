package com.z2a.ui.screens.logs

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.z2a.Z2aApplication
import com.z2a.data.models.LogEntry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LogsViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as Z2aApplication

    private val _logs = MutableStateFlow<List<LogEntry>>(emptyList())
    val logs: StateFlow<List<LogEntry>> = _logs.asStateFlow()

    private val _filterProfile = MutableStateFlow<String?>(null)
    val filterProfile: StateFlow<String?> = _filterProfile.asStateFlow()

    init {
        viewModelScope.launch {
            app.engineManager.logs.collect { entry ->
                val current = _logs.value.toMutableList()
                current.add(0, entry)
                // Keep last 500 entries
                if (current.size > 500) {
                    _logs.value = current.take(500)
                } else {
                    _logs.value = current
                }
            }
        }
    }

    fun setFilter(profileId: String?) {
        _filterProfile.value = profileId
    }

    fun getFilteredLogs(): List<LogEntry> {
        val filter = _filterProfile.value
        return if (filter == null) _logs.value
        else _logs.value.filter { it.profileId == filter }
    }

    fun clearLogs() {
        _logs.value = emptyList()
    }
}

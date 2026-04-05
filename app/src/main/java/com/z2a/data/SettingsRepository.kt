package com.z2a.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "z2a_settings")

class SettingsRepository(private val context: Context) {

    companion object {
        val KEY_AUTOSTART = booleanPreferencesKey("autostart")
        val KEY_SILENT_FALLBACK = booleanPreferencesKey("silent_fallback")
        val KEY_RST_FILTER = booleanPreferencesKey("rst_filter")
        val KEY_AUSTERUS_MODE = booleanPreferencesKey("austerus_mode")
        val KEY_DNS_SERVER = stringPreferencesKey("dns_server")
        val KEY_THEME = stringPreferencesKey("theme")
    }

    val autostart: Flow<Boolean> = context.dataStore.data.map { it[KEY_AUTOSTART] ?: false }
    val silentFallback: Flow<Boolean> = context.dataStore.data.map { it[KEY_SILENT_FALLBACK] ?: false }
    val rstFilter: Flow<Boolean> = context.dataStore.data.map { it[KEY_RST_FILTER] ?: true }
    val austerusMode: Flow<Boolean> = context.dataStore.data.map { it[KEY_AUSTERUS_MODE] ?: false }
    val dnsServer: Flow<String> = context.dataStore.data.map { it[KEY_DNS_SERVER] ?: "" }
    val theme: Flow<String> = context.dataStore.data.map { it[KEY_THEME] ?: "dark" }

    suspend fun setAutostart(value: Boolean) {
        context.dataStore.edit { it[KEY_AUTOSTART] = value }
    }

    suspend fun setSilentFallback(value: Boolean) {
        context.dataStore.edit { it[KEY_SILENT_FALLBACK] = value }
    }

    suspend fun setRstFilter(value: Boolean) {
        context.dataStore.edit { it[KEY_RST_FILTER] = value }
    }

    suspend fun setAusterusMode(value: Boolean) {
        context.dataStore.edit { it[KEY_AUSTERUS_MODE] = value }
    }

    suspend fun setDnsServer(value: String) {
        context.dataStore.edit { it[KEY_DNS_SERVER] = value }
    }

    suspend fun setTheme(value: String) {
        context.dataStore.edit { it[KEY_THEME] = value }
    }
}

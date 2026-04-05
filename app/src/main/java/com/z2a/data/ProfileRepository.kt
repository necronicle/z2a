package com.z2a.data

import android.content.Context
import com.z2a.data.models.Profile
import com.z2a.data.models.ProfileType
import com.z2a.data.models.ProtocolType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.BufferedReader
import java.io.InputStreamReader

class ProfileRepository(private val context: Context) {

    private val _profiles = MutableStateFlow<List<Profile>>(emptyList())
    val profiles: StateFlow<List<Profile>> = _profiles.asStateFlow()

    init {
        loadProfiles()
    }

    private fun loadProfiles() {
        val domainCounts = loadDomainCounts()
        _profiles.value = listOf(
            Profile(
                id = "rkn_tcp",
                name = "RKN",
                type = ProfileType.RKN_TCP,
                hostlistFile = "hostlists/TCP_RKN_List.txt",
                strategyCount = 45,
                domainCount = domainCounts["TCP_RKN_List.txt"] ?: 0,
                ports = "443,2053,2083,2087,2096,8443",
                protocol = ProtocolType.TCP
            ),
            Profile(
                id = "yt_tcp",
                name = "YouTube TCP",
                type = ProfileType.YOUTUBE_TCP,
                hostlistFile = "hostlists/TCP_YT_List.txt",
                strategyCount = 22,
                domainCount = domainCounts["TCP_YT_List.txt"] ?: 0,
                ports = "443,2053,2083,2087,2096,8443",
                protocol = ProtocolType.TCP
            ),
            Profile(
                id = "yt_gv",
                name = "Google Video",
                type = ProfileType.YOUTUBE_GV,
                hostlistFile = "hostlists/TCP_YT_List.txt",
                strategyCount = 22,
                domainCount = 1,
                ports = "443",
                protocol = ProtocolType.TCP
            ),
            Profile(
                id = "yt_quic",
                name = "YouTube QUIC",
                type = ProfileType.YOUTUBE_QUIC,
                hostlistFile = "hostlists/UDP_YT_List.txt",
                strategyCount = 12,
                domainCount = domainCounts["UDP_YT_List.txt"] ?: 0,
                ports = "443",
                protocol = ProtocolType.UDP
            ),
            Profile(
                id = "discord_udp",
                name = "Discord Voice",
                type = ProfileType.DISCORD_UDP,
                hostlistFile = "hostlists/TCP_Discord.txt",
                strategyCount = 12,
                domainCount = domainCounts["TCP_Discord.txt"] ?: 0,
                ports = "50000-50099",
                protocol = ProtocolType.UDP
            ),
            Profile(
                id = "discord_stun",
                name = "Discord STUN",
                type = ProfileType.DISCORD_STUN,
                hostlistFile = "hostlists/TCP_Discord.txt",
                strategyCount = 6,
                domainCount = domainCounts["TCP_Discord.txt"] ?: 0,
                ports = "3478-3481,5349,19294-19344",
                protocol = ProtocolType.UDP
            )
        )
    }

    private fun loadDomainCounts(): Map<String, Int> {
        val counts = mutableMapOf<String, Int>()
        val files = listOf("TCP_RKN_List.txt", "TCP_YT_List.txt", "TCP_Discord.txt", "UDP_YT_List.txt")
        for (file in files) {
            try {
                val reader = BufferedReader(InputStreamReader(context.assets.open("hostlists/$file")))
                counts[file] = reader.useLines { lines -> lines.count { it.isNotBlank() && !it.startsWith("#") } }
            } catch (_: Exception) {
                counts[file] = 0
            }
        }
        return counts
    }

    fun toggleProfile(profileId: String) {
        _profiles.value = _profiles.value.map {
            if (it.id == profileId) it.copy(enabled = !it.enabled) else it
        }
    }

    fun getEnabledProfiles(): List<Profile> = _profiles.value.filter { it.enabled }
}

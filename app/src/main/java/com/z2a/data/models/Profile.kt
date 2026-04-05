package com.z2a.data.models

data class Profile(
    val id: String,
    val name: String,
    val type: ProfileType,
    val enabled: Boolean = true,
    val hostlistFile: String,
    val strategyCount: Int,
    val domainCount: Int,
    val currentStrategy: Int = 1,
    val ports: String,
    val protocol: ProtocolType
)

enum class ProfileType {
    RKN_TCP,
    YOUTUBE_TCP,
    YOUTUBE_GV,
    YOUTUBE_QUIC,
    DISCORD_UDP,
    DISCORD_STUN,
    DISCORD_IP_DISCOVERY,
    CUSTOM
}

enum class ProtocolType {
    TCP,
    UDP,
    BOTH
}

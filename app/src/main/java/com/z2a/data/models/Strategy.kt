package com.z2a.data.models

data class Strategy(
    val number: Int,
    val profileKey: String,
    val protocol: String,
    val params: String
)

data class AutocircularEntry(
    val key: String,
    val hostNorm: String,
    val strategy: Int,
    val timestamp: Long
)

data class TelemetryEntry(
    val key: String,
    val strategy: Int,
    val successes: Int,
    val failures: Int,
    val lastUsed: Long,
    val cooldownUntil: Long
)

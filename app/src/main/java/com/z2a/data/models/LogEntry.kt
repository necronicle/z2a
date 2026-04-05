package com.z2a.data.models

data class LogEntry(
    val timestamp: Long,
    val profileId: String,
    val domain: String,
    val strategyNumber: Int,
    val event: LogEvent,
    val message: String
)

enum class LogEvent {
    STRATEGY_APPLIED,
    STRATEGY_ROTATED,
    STRATEGY_FAILED,
    CONNECTION_OK,
    CONNECTION_TIMEOUT,
    RST_DETECTED,
    TLS_ALERT,
    SILENT_FALLBACK
}

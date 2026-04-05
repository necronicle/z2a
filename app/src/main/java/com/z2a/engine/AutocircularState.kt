package com.z2a.engine

import com.z2a.data.models.AutocircularEntry
import com.z2a.data.models.TelemetryEntry
import java.io.File

class AutocircularState(private val dataDir: File) {

    private val cacheDir = File(dataDir, "cache/autocircular").also { it.mkdirs() }
    val stateFile = File(cacheDir, "state.tsv")
    val telemetryFile = File(cacheDir, "telemetry.tsv")

    fun readState(): List<AutocircularEntry> {
        if (!stateFile.exists()) return emptyList()
        return stateFile.readLines()
            .filter { it.isNotBlank() && !it.startsWith("askey") }
            .mapNotNull { line ->
                val parts = line.split("\t")
                if (parts.size >= 4) {
                    AutocircularEntry(
                        key = parts[0],
                        hostNorm = parts[1],
                        strategy = parts[2].toIntOrNull() ?: 1,
                        timestamp = parts[3].toLongOrNull() ?: 0
                    )
                } else null
            }
    }

    fun readTelemetry(): List<TelemetryEntry> {
        if (!telemetryFile.exists()) return emptyList()
        return telemetryFile.readLines()
            .filter { it.isNotBlank() && !it.startsWith("key") }
            .mapNotNull { line ->
                val parts = line.split("\t")
                if (parts.size >= 6) {
                    TelemetryEntry(
                        key = parts[0],
                        strategy = parts[1].toIntOrNull() ?: 0,
                        successes = parts[2].toIntOrNull() ?: 0,
                        failures = parts[3].toIntOrNull() ?: 0,
                        lastUsed = parts[4].toLongOrNull() ?: 0,
                        cooldownUntil = parts[5].toLongOrNull() ?: 0
                    )
                } else null
            }
    }

    fun clearState() {
        stateFile.delete()
        telemetryFile.delete()
    }

    fun getStateEntryCount(): Int {
        if (!stateFile.exists()) return 0
        return stateFile.readLines().count { it.isNotBlank() && !it.startsWith("askey") }
    }

    fun getCacheDir(): File = cacheDir
}

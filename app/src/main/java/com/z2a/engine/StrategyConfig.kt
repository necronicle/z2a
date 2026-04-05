package com.z2a.engine

import android.content.Context
import com.z2a.data.models.Strategy
import java.io.BufferedReader
import java.io.InputStreamReader

class StrategyConfig(private val context: Context) {

    fun loadStrategies(assetPath: String): List<Strategy> {
        val strategies = mutableListOf<Strategy>()
        try {
            val reader = BufferedReader(InputStreamReader(context.assets.open(assetPath)))
            reader.useLines { lines ->
                lines.forEach { line ->
                    if (line.isNotBlank() && !line.startsWith("#")) {
                        parseStrategyLine(line)?.let { strategies.add(it) }
                    }
                }
            }
        } catch (_: Exception) {}
        return strategies
    }

    private fun parseStrategyLine(line: String): Strategy? {
        // Format: number|protocol|params
        val parts = line.split("|", limit = 3)
        if (parts.size >= 3) {
            return Strategy(
                number = parts[0].trim().toIntOrNull() ?: return null,
                profileKey = "",
                protocol = parts[1].trim(),
                params = parts[2].trim()
            )
        }
        return null
    }

    fun getStrategyCount(assetPath: String): Int {
        return try {
            val reader = BufferedReader(InputStreamReader(context.assets.open(assetPath)))
            reader.useLines { lines ->
                lines.count { it.isNotBlank() && !it.startsWith("#") }
            }
        } catch (_: Exception) {
            0
        }
    }
}

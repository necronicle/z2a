package com.z2a.engine

import android.content.Context
import java.io.BufferedReader
import java.io.InputStreamReader

class ProfileConfig(private val context: Context) {

    fun loadProfilesConfig(): String {
        return try {
            val reader = BufferedReader(InputStreamReader(context.assets.open("profiles.default.txt")))
            reader.readText()
        } catch (_: Exception) {
            ""
        }
    }

    fun buildEngineArgs(
        profileConfig: String,
        dataDir: String,
        enabledProfileIds: Set<String>,
        silentFallback: Boolean,
        rstFilter: Boolean,
        austerusMode: Boolean
    ): List<String> {
        val args = mutableListOf<String>()

        // Base paths — engine resolves lua/, files/, hostlists/ relative to data dir
        args.add("--lua-dir=$dataDir/lua")
        args.add("--blob-dir=$dataDir/files")

        // Autocircular persistence directory
        args.add("--state-dir=$dataDir/cache/autocircular")

        // Profile configuration
        if (profileConfig.isNotBlank()) {
            // Split profiles.default.txt into lines, each line is a profile config
            val lines = profileConfig.lines().filter { it.isNotBlank() && !it.startsWith("#") }
            for (line in lines) {
                args.add(line.trim())
            }
        }

        // Silent fallback
        if (silentFallback) {
            args.add("--silent-fallback")
        }

        // RST filter
        if (rstFilter) {
            args.add("--rst-filter")
        }

        // Austerus mode (all TCP/443 instead of hostlist)
        if (austerusMode) {
            args.add("--all-tcp443")
        }

        return args
    }
}

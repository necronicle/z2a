package com.z2a.engine

import android.content.Context
import android.util.Log
import com.z2a.data.models.LogEntry
import com.z2a.data.models.LogEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

class EngineManager(private val context: Context) {

    companion object {
        private const val TAG = "z2a-engine"
        private const val ENGINE_BINARY = "nfqws2"
    }

    private var engineProcess: Process? = null
    private var outputJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.IO)

    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

    private val _logs = MutableSharedFlow<LogEntry>(extraBufferCapacity = 100)
    val logs: SharedFlow<LogEntry> = _logs.asSharedFlow()

    private val _lastError = MutableStateFlow<String?>(null)
    val lastError: StateFlow<String?> = _lastError.asStateFlow()

    val autocircularState = AutocircularState(context.filesDir)
    val profileConfig = ProfileConfig(context)
    val strategyConfig = StrategyConfig(context)

    fun extractAssets() {
        val dataDir = context.filesDir
        extractDir("lua", File(dataDir, "lua"))
        extractDir("files", File(dataDir, "files"))
        extractDir("hostlists", File(dataDir, "hostlists"))

        // Copy profiles config
        val profilesFile = File(dataDir, "profiles.default.txt")
        if (!profilesFile.exists()) {
            context.assets.open("profiles.default.txt").use { input ->
                profilesFile.outputStream().use { output -> input.copyTo(output) }
            }
        }
    }

    private fun extractDir(assetDir: String, targetDir: File) {
        targetDir.mkdirs()
        try {
            val files = context.assets.list(assetDir) ?: return
            for (file in files) {
                val targetFile = File(targetDir, file)
                if (!targetFile.exists()) {
                    context.assets.open("$assetDir/$file").use { input ->
                        targetFile.outputStream().use { output -> input.copyTo(output) }
                    }
                    // Make lua files readable
                    targetFile.setReadable(true)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to extract $assetDir: ${e.message}")
        }
    }

    fun start(tunFd: Int, args: List<String>): Boolean {
        if (_isRunning.value) return true

        val enginePath = getEngineBinaryPath() ?: run {
            _lastError.value = "Engine binary not found"
            return false
        }

        try {
            val command = mutableListOf(enginePath)
            command.add("--tun-fd=$tunFd")
            command.addAll(args)

            Log.i(TAG, "Starting engine: ${command.joinToString(" ")}")

            val pb = ProcessBuilder(command)
            pb.directory(context.filesDir)
            pb.redirectErrorStream(true)
            pb.environment()["HOME"] = context.filesDir.absolutePath

            engineProcess = pb.start()
            _isRunning.value = true
            _lastError.value = null

            // Read engine output
            outputJob = scope.launch {
                val reader = BufferedReader(InputStreamReader(engineProcess!!.inputStream))
                try {
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        Log.d(TAG, line!!)
                        parseLogLine(line!!)?.let { _logs.emit(it) }
                    }
                } catch (_: Exception) {
                } finally {
                    _isRunning.value = false
                    Log.i(TAG, "Engine process ended")
                }
            }

            return true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start engine: ${e.message}")
            _lastError.value = e.message
            _isRunning.value = false
            return false
        }
    }

    fun stop() {
        outputJob?.cancel()
        outputJob = null
        engineProcess?.let {
            it.destroy()
            try { it.waitFor() } catch (_: Exception) {}
        }
        engineProcess = null
        _isRunning.value = false
    }

    private fun getEngineBinaryPath(): String? {
        // Check native libs directory for prebuilt binary
        val nativeLibDir = context.applicationInfo.nativeLibraryDir
        val binary = File(nativeLibDir, "lib${ENGINE_BINARY}.so")
        if (binary.exists() && binary.canExecute()) {
            return binary.absolutePath
        }

        // Check files directory for manually placed binary
        val filesBinary = File(context.filesDir, ENGINE_BINARY)
        if (filesBinary.exists()) {
            filesBinary.setExecutable(true)
            return filesBinary.absolutePath
        }

        return null
    }

    private fun parseLogLine(line: String): LogEntry? {
        val timestamp = System.currentTimeMillis()

        // Parse autocircular log lines
        // Format examples:
        // [circular:rkn_tcp] youtube.com strategy=12 OK
        // [circular:rkn_tcp] example.com strategy=5 FAIL:retrans → strategy=6
        // [circular:yt_quic] youtube.com strategy=3 ROTATED → strategy=4

        val circularRegex = Regex("""\[circular:(\w+)] (\S+) strategy=(\d+) (\w+)(.*)""")
        val match = circularRegex.find(line)
        if (match != null) {
            val (profileKey, domain, stratNum, eventStr, extra) = match.destructured
            val event = when {
                eventStr == "OK" -> LogEvent.CONNECTION_OK
                eventStr == "FAIL" && extra.contains("retrans") -> LogEvent.CONNECTION_TIMEOUT
                eventStr == "FAIL" && extra.contains("RST") -> LogEvent.RST_DETECTED
                eventStr == "FAIL" && extra.contains("tls_alert") -> LogEvent.TLS_ALERT
                eventStr == "ROTATED" -> LogEvent.STRATEGY_ROTATED
                eventStr == "SILENT" -> LogEvent.SILENT_FALLBACK
                else -> LogEvent.STRATEGY_APPLIED
            }
            return LogEntry(
                timestamp = timestamp,
                profileId = profileKey,
                domain = domain,
                strategyNumber = stratNum.toIntOrNull() ?: 0,
                event = event,
                message = line
            )
        }

        return null
    }
}

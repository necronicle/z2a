package com.z2a.vpn.packet

import android.util.Log
import java.io.OutputStream
import java.nio.ByteBuffer

/**
 * DPI bypass techniques applied to outgoing TCP data.
 * Splits TLS ClientHello to evade deep packet inspection.
 */
object DpiBypass {
    private const val TAG = "z2a-dpi"

    // TLS content type
    private const val TLS_HANDSHAKE: Byte = 0x16
    // TLS handshake type
    private const val TLS_CLIENT_HELLO: Byte = 0x01

    /**
     * Detect if payload is a TLS ClientHello
     */
    fun isTlsClientHello(data: ByteArray, offset: Int, length: Int): Boolean {
        if (length < 6) return false
        // TLS record: content_type(1) + version(2) + length(2) + handshake_type(1)
        return data[offset] == TLS_HANDSHAKE &&
                data[offset + 1].toInt() == 0x03 && // TLS major version
                data[offset + 5] == TLS_CLIENT_HELLO
    }

    /**
     * Find SNI (Server Name Indication) position in TLS ClientHello.
     * Returns the offset of the hostname within data, or -1 if not found.
     */
    fun findSniOffset(data: ByteArray, offset: Int, length: Int): Int {
        if (length < 43) return -1
        try {
            var pos = offset + 5 // skip TLS record header
            pos += 4 // skip handshake header (type + length)
            pos += 2 // skip client version
            pos += 32 // skip random
            // session id
            if (pos >= offset + length) return -1
            val sessionIdLen = data[pos].toInt() and 0xFF
            pos += 1 + sessionIdLen
            // cipher suites
            if (pos + 2 > offset + length) return -1
            val cipherSuitesLen = ((data[pos].toInt() and 0xFF) shl 8) or (data[pos + 1].toInt() and 0xFF)
            pos += 2 + cipherSuitesLen
            // compression methods
            if (pos >= offset + length) return -1
            val compMethodsLen = data[pos].toInt() and 0xFF
            pos += 1 + compMethodsLen
            // extensions length
            if (pos + 2 > offset + length) return -1
            val extensionsLen = ((data[pos].toInt() and 0xFF) shl 8) or (data[pos + 1].toInt() and 0xFF)
            pos += 2
            val extensionsEnd = pos + extensionsLen
            // search for SNI extension (type 0x0000)
            while (pos + 4 <= extensionsEnd && pos + 4 <= offset + length) {
                val extType = ((data[pos].toInt() and 0xFF) shl 8) or (data[pos + 1].toInt() and 0xFF)
                val extLen = ((data[pos + 2].toInt() and 0xFF) shl 8) or (data[pos + 3].toInt() and 0xFF)
                if (extType == 0) {
                    // SNI extension found, return position of extension start
                    return pos
                }
                pos += 4 + extLen
            }
        } catch (_: Exception) {}
        return -1
    }

    /**
     * Write data with TLS ClientHello splitting for DPI bypass.
     * Splits the ClientHello at the SNI boundary to confuse DPI.
     * If not a ClientHello, writes data as-is.
     *
     * Returns true if split was applied.
     */
    fun writeWithBypass(output: OutputStream, data: ByteArray, offset: Int, length: Int): Boolean {
        if (!isTlsClientHello(data, offset, length)) {
            output.write(data, offset, length)
            output.flush()
            return false
        }

        val sniOffset = findSniOffset(data, offset, length)
        if (sniOffset < 0) {
            // No SNI found, just split at a fixed position in the middle
            val splitPos = minOf(length / 2, 40)
            output.write(data, offset, splitPos)
            output.flush()
            Thread.sleep(1) // tiny delay between fragments
            output.write(data, offset + splitPos, length - splitPos)
            output.flush()
            Log.d(TAG, "Split ClientHello at fixed pos $splitPos (no SNI)")
            return true
        }

        // Split right before the SNI extension
        val splitAt = sniOffset - offset
        output.write(data, offset, splitAt)
        output.flush()
        Thread.sleep(1)
        output.write(data, offset + splitAt, length - splitAt)
        output.flush()
        Log.d(TAG, "Split ClientHello at SNI offset $splitAt")
        return true
    }
}

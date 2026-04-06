package com.z2a.vpn.packet

import java.nio.ByteBuffer

data class IpHeader(
    val version: Int,
    val headerLength: Int,
    val totalLength: Int,
    val identification: Int,
    val flags: Int,
    val fragmentOffset: Int,
    val ttl: Int,
    val protocol: Int,
    val sourceAddress: Int,
    val destinationAddress: Int
) {
    companion object {
        const val PROTOCOL_TCP = 6
        const val PROTOCOL_UDP = 17

        fun parse(buffer: ByteBuffer): IpHeader? {
            if (buffer.remaining() < 20) return null
            val pos = buffer.position()
            val versionIhl = buffer.get(pos).toInt() and 0xFF
            val version = versionIhl shr 4
            if (version != 4) return null
            val ihl = (versionIhl and 0x0F) * 4
            if (buffer.remaining() < ihl) return null
            val totalLength = buffer.getShort(pos + 2).toInt() and 0xFFFF
            val identification = buffer.getShort(pos + 4).toInt() and 0xFFFF
            val flagsFragment = buffer.getShort(pos + 6).toInt() and 0xFFFF
            val flags = flagsFragment shr 13
            val fragmentOffset = flagsFragment and 0x1FFF
            val ttl = buffer.get(pos + 8).toInt() and 0xFF
            val protocol = buffer.get(pos + 9).toInt() and 0xFF
            val srcAddr = buffer.getInt(pos + 12)
            val dstAddr = buffer.getInt(pos + 16)
            return IpHeader(version, ihl, totalLength, identification, flags, fragmentOffset, ttl, protocol, srcAddr, dstAddr)
        }

        fun intToAddress(addr: Int): ByteArray = byteArrayOf(
            (addr shr 24).toByte(),
            (addr shr 16).toByte(),
            (addr shr 8).toByte(),
            addr.toByte()
        )
    }
}

data class TcpHeader(
    val sourcePort: Int,
    val destinationPort: Int,
    val sequenceNumber: Long,
    val acknowledgmentNumber: Long,
    val dataOffset: Int,
    val flags: Int,
    val window: Int
) {
    val isSyn get() = flags and FLAG_SYN != 0
    val isAck get() = flags and FLAG_ACK != 0
    val isFin get() = flags and FLAG_FIN != 0
    val isRst get() = flags and FLAG_RST != 0
    val isPsh get() = flags and FLAG_PSH != 0

    companion object {
        const val FLAG_FIN = 0x01
        const val FLAG_SYN = 0x02
        const val FLAG_RST = 0x04
        const val FLAG_PSH = 0x08
        const val FLAG_ACK = 0x10

        fun parse(buffer: ByteBuffer, offset: Int): TcpHeader? {
            if (buffer.remaining() < offset + 20) return null
            val srcPort = buffer.getShort(offset).toInt() and 0xFFFF
            val dstPort = buffer.getShort(offset + 2).toInt() and 0xFFFF
            val seq = buffer.getInt(offset + 4).toLong() and 0xFFFFFFFFL
            val ack = buffer.getInt(offset + 8).toLong() and 0xFFFFFFFFL
            val dataOffsetFlags = buffer.getShort(offset + 12).toInt() and 0xFFFF
            val dataOffset = ((dataOffsetFlags shr 12) and 0x0F) * 4
            val flags = dataOffsetFlags and 0x3F
            val window = buffer.getShort(offset + 14).toInt() and 0xFFFF
            return TcpHeader(srcPort, dstPort, seq, ack, dataOffset, flags, window)
        }
    }
}

data class UdpHeader(
    val sourcePort: Int,
    val destinationPort: Int,
    val length: Int
) {
    companion object {
        fun parse(buffer: ByteBuffer, offset: Int): UdpHeader? {
            if (buffer.remaining() < offset + 8) return null
            val srcPort = buffer.getShort(offset).toInt() and 0xFFFF
            val dstPort = buffer.getShort(offset + 2).toInt() and 0xFFFF
            val length = buffer.getShort(offset + 4).toInt() and 0xFFFF
            return UdpHeader(srcPort, dstPort, length)
        }
    }
}

object PacketBuilder {

    fun buildTcpPacket(
        srcAddr: Int, dstAddr: Int,
        srcPort: Int, dstPort: Int,
        seq: Long, ack: Long,
        flags: Int, window: Int,
        payload: ByteArray = ByteArray(0)
    ): ByteArray {
        val ipHeaderLen = 20
        val tcpHeaderLen = 20
        val totalLen = ipHeaderLen + tcpHeaderLen + payload.size
        val packet = ByteArray(totalLen)
        val buf = ByteBuffer.wrap(packet)

        // IP header
        buf.put((0x45).toByte()) // version=4, ihl=5
        buf.put(0) // DSCP/ECN
        buf.putShort(totalLen.toShort())
        buf.putShort(0) // identification
        buf.putShort(0x4000.toShort()) // don't fragment
        buf.put(64) // TTL
        buf.put(IpHeader.PROTOCOL_TCP.toByte())
        buf.putShort(0) // checksum placeholder
        buf.putInt(srcAddr)
        buf.putInt(dstAddr)

        // IP checksum
        var sum = 0L
        for (i in 0 until ipHeaderLen step 2) {
            sum += (packet[i].toInt() and 0xFF shl 8) or (packet[i + 1].toInt() and 0xFF)
        }
        while (sum shr 16 != 0L) sum = (sum and 0xFFFF) + (sum shr 16)
        val ipChecksum = sum.inv().toInt() and 0xFFFF
        buf.putShort(10, ipChecksum.toShort())

        // TCP header
        buf.position(ipHeaderLen)
        buf.putShort(srcPort.toShort())
        buf.putShort(dstPort.toShort())
        buf.putInt(seq.toInt())
        buf.putInt(ack.toInt())
        buf.putShort(((5 shl 12) or flags).toShort()) // dataOffset=5, flags
        buf.putShort(window.toShort())
        buf.putShort(0) // checksum placeholder
        buf.putShort(0) // urgent pointer

        if (payload.isNotEmpty()) {
            buf.put(payload)
        }

        // TCP pseudo-header checksum
        var tcpSum = 0L
        // pseudo header
        tcpSum += (srcAddr.toLong() shr 16) and 0xFFFF
        tcpSum += srcAddr.toLong() and 0xFFFF
        tcpSum += (dstAddr.toLong() shr 16) and 0xFFFF
        tcpSum += dstAddr.toLong() and 0xFFFF
        tcpSum += IpHeader.PROTOCOL_TCP.toLong()
        tcpSum += (tcpHeaderLen + payload.size).toLong()
        // TCP header + payload
        for (i in ipHeaderLen until totalLen step 2) {
            val hi = packet[i].toInt() and 0xFF
            val lo = if (i + 1 < totalLen) packet[i + 1].toInt() and 0xFF else 0
            tcpSum += (hi shl 8) or lo
        }
        while (tcpSum shr 16 != 0L) tcpSum = (tcpSum and 0xFFFF) + (tcpSum shr 16)
        val tcpChecksum = tcpSum.inv().toInt() and 0xFFFF
        buf.putShort(ipHeaderLen + 16, tcpChecksum.toShort())

        return packet
    }

    fun buildUdpPacket(
        srcAddr: Int, dstAddr: Int,
        srcPort: Int, dstPort: Int,
        payload: ByteArray
    ): ByteArray {
        val ipHeaderLen = 20
        val udpHeaderLen = 8
        val totalLen = ipHeaderLen + udpHeaderLen + payload.size
        val packet = ByteArray(totalLen)
        val buf = ByteBuffer.wrap(packet)

        // IP header
        buf.put((0x45).toByte())
        buf.put(0)
        buf.putShort(totalLen.toShort())
        buf.putShort(0)
        buf.putShort(0x4000.toShort())
        buf.put(64)
        buf.put(IpHeader.PROTOCOL_UDP.toByte())
        buf.putShort(0) // checksum placeholder
        buf.putInt(srcAddr)
        buf.putInt(dstAddr)

        // IP checksum
        var sum = 0L
        for (i in 0 until ipHeaderLen step 2) {
            sum += (packet[i].toInt() and 0xFF shl 8) or (packet[i + 1].toInt() and 0xFF)
        }
        while (sum shr 16 != 0L) sum = (sum and 0xFFFF) + (sum shr 16)
        buf.putShort(10, (sum.inv().toInt() and 0xFFFF).toShort())

        // UDP header
        buf.position(ipHeaderLen)
        buf.putShort(srcPort.toShort())
        buf.putShort(dstPort.toShort())
        buf.putShort((udpHeaderLen + payload.size).toShort())
        buf.putShort(0) // checksum (optional for IPv4)
        buf.put(payload)

        return packet
    }
}

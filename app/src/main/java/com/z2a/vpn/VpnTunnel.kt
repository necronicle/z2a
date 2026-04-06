package com.z2a.vpn

import android.net.VpnService
import android.os.ParcelFileDescriptor
import android.util.Log
import com.z2a.vpn.packet.DpiBypass
import com.z2a.vpn.packet.IpHeader
import com.z2a.vpn.packet.PacketBuilder
import com.z2a.vpn.packet.TcpHeader
import com.z2a.vpn.packet.UdpHeader
import java.io.FileInputStream
import java.io.FileOutputStream
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

class VpnTunnel(
    private val vpnService: VpnService,
    private val tunFd: ParcelFileDescriptor
) {
    companion object {
        private const val TAG = "z2a-tunnel"
        private const val MTU = 1500
        const val MTU_PAYLOAD = 1400
    }

    private val running = AtomicBoolean(false)
    private val executor: ExecutorService = Executors.newCachedThreadPool()
    private val tcpConnections = ConcurrentHashMap<String, TcpProxyConnection>()
    private lateinit var tunOutput: FileOutputStream

    fun start() {
        if (running.getAndSet(true)) return
        executor.submit { runTunnel() }
    }

    fun stop() {
        running.set(false)
        tcpConnections.values.forEach { it.close() }
        tcpConnections.clear()
        executor.shutdownNow()
    }

    private fun runTunnel() {
        val tunInput = FileInputStream(tunFd.fileDescriptor)
        tunOutput = FileOutputStream(tunFd.fileDescriptor)
        val buffer = ByteArray(MTU)

        Log.i(TAG, "Tunnel started")

        try {
            while (running.get()) {
                val length = tunInput.read(buffer)
                if (length <= 0) {
                    Thread.sleep(10)
                    continue
                }
                handlePacket(buffer, length)
            }
        } catch (e: Exception) {
            if (running.get()) {
                Log.e(TAG, "Tunnel error: ${e.message}")
            }
        }

        Log.i(TAG, "Tunnel stopped")
    }

    private fun handlePacket(data: ByteArray, length: Int) {
        val buf = ByteBuffer.wrap(data, 0, length)
        val ipHeader = IpHeader.parse(buf) ?: return

        when (ipHeader.protocol) {
            IpHeader.PROTOCOL_TCP -> handleTcp(data, length, ipHeader)
            IpHeader.PROTOCOL_UDP -> handleUdp(data, length, ipHeader)
        }
    }

    private fun handleTcp(data: ByteArray, length: Int, ip: IpHeader) {
        val buf = ByteBuffer.wrap(data, 0, length)
        val tcp = TcpHeader.parse(buf, ip.headerLength) ?: return
        val key = "${ip.sourceAddress}:${tcp.sourcePort}-${ip.destinationAddress}:${tcp.destinationPort}"

        if (tcp.isRst) {
            tcpConnections.remove(key)?.close()
            return
        }

        if (tcp.isSyn && !tcp.isAck) {
            // New connection
            tcpConnections.remove(key)?.close()

            val conn = TcpProxyConnection(
                vpnService = vpnService,
                srcAddr = ip.sourceAddress,
                dstAddr = ip.destinationAddress,
                srcPort = tcp.sourcePort,
                dstPort = tcp.destinationPort,
                clientIsn = tcp.sequenceNumber,
                onResponse = { packet -> writeTun(packet) },
                onClose = { tcpConnections.remove(key) }
            )

            tcpConnections[key] = conn
            executor.submit { conn.connect() }
            return
        }

        val conn = tcpConnections[key] ?: return

        if (tcp.isFin) {
            conn.onClientFin(tcp.sequenceNumber, tcp.acknowledgmentNumber)
            tcpConnections.remove(key)
            return
        }

        // Data packet
        val payloadOffset = ip.headerLength + tcp.dataOffset
        val payloadLength = length - payloadOffset
        if (payloadLength > 0 && tcp.isAck) {
            val payload = data.copyOfRange(payloadOffset, payloadOffset + payloadLength)
            conn.onClientData(payload, tcp.sequenceNumber, tcp.acknowledgmentNumber)
        } else if (tcp.isAck) {
            conn.onClientAck(tcp.acknowledgmentNumber)
        }
    }

    private fun handleUdp(data: ByteArray, length: Int, ip: IpHeader) {
        val buf = ByteBuffer.wrap(data, 0, length)
        val udp = UdpHeader.parse(buf, ip.headerLength) ?: return

        val payloadOffset = ip.headerLength + 8
        val payloadLength = length - payloadOffset
        if (payloadLength <= 0) return

        val payload = data.copyOfRange(payloadOffset, payloadOffset + payloadLength)

        executor.submit {
            try {
                val dstAddr = InetAddress.getByAddress(IpHeader.intToAddress(ip.destinationAddress))
                val socket = DatagramSocket()
                vpnService.protect(socket)

                val sendPacket = DatagramPacket(payload, payload.size, dstAddr, udp.destinationPort)
                socket.soTimeout = 5000
                socket.send(sendPacket)

                val recvBuf = ByteArray(MTU)
                val recvPacket = DatagramPacket(recvBuf, recvBuf.size)
                socket.receive(recvPacket)

                val responsePayload = recvBuf.copyOf(recvPacket.length)
                val responsePacket = PacketBuilder.buildUdpPacket(
                    srcAddr = ip.destinationAddress,
                    dstAddr = ip.sourceAddress,
                    srcPort = udp.destinationPort,
                    dstPort = udp.sourcePort,
                    payload = responsePayload
                )
                writeTun(responsePacket)
                socket.close()
            } catch (e: Exception) {
                Log.v(TAG, "UDP forward error: ${e.message}")
            }
        }
    }

    @Synchronized
    private fun writeTun(packet: ByteArray) {
        try {
            tunOutput.write(packet)
            tunOutput.flush()
        } catch (e: Exception) {
            Log.e(TAG, "TUN write error: ${e.message}")
        }
    }
}

/**
 * Manages a single proxied TCP connection.
 * Terminates client TCP on TUN side, creates real connection to server,
 * relays data with DPI bypass applied on first TLS ClientHello.
 */
class TcpProxyConnection(
    private val vpnService: VpnService,
    private val srcAddr: Int,
    private val dstAddr: Int,
    private val srcPort: Int,
    private val dstPort: Int,
    private val clientIsn: Long,
    private val onResponse: (ByteArray) -> Unit,
    private val onClose: () -> Unit
) {
    companion object {
        private const val TAG = "z2a-tcp"
    }

    private var serverSocket: java.net.Socket? = null
    private var serverSeq: Long = (System.nanoTime() and 0xFFFFFFFFL)
    private var clientNextSeq: Long = clientIsn + 1 // after SYN
    private var serverNextAck: Long = clientIsn + 1
    private var firstDataSent = false
    private val closed = AtomicBoolean(false)

    fun connect() {
        try {
            val addr = InetAddress.getByAddress(IpHeader.intToAddress(dstAddr))
            val socket = java.net.Socket()
            vpnService.protect(socket)
            socket.connect(InetSocketAddress(addr, dstPort), 10000)
            socket.tcpNoDelay = true
            serverSocket = socket

            // Send SYN-ACK back to client
            sendTcpToClient(
                flags = TcpHeader.FLAG_SYN or TcpHeader.FLAG_ACK,
                seq = serverSeq,
                ack = clientNextSeq
            )
            serverSeq++ // SYN consumes 1 seq

            // Start reading from server
            readFromServer()
        } catch (e: Exception) {
            Log.d(TAG, "Connect failed to ${intToIp(dstAddr)}:$dstPort: ${e.message}")
            sendTcpToClient(flags = TcpHeader.FLAG_RST, seq = serverSeq, ack = clientNextSeq)
            close()
        }
    }

    fun onClientData(payload: ByteArray, seq: Long, ack: Long) {
        if (closed.get()) return
        clientNextSeq = seq + payload.size
        try {
            val out = serverSocket?.getOutputStream() ?: return

            if (!firstDataSent) {
                firstDataSent = true
                // Apply DPI bypass on first data (likely TLS ClientHello)
                DpiBypass.writeWithBypass(out, payload, 0, payload.size)
            } else {
                out.write(payload)
                out.flush()
            }

            // ACK the client data
            sendTcpToClient(flags = TcpHeader.FLAG_ACK, seq = serverSeq, ack = clientNextSeq)
        } catch (e: Exception) {
            Log.v(TAG, "Send to server failed: ${e.message}")
            close()
        }
    }

    fun onClientAck(ack: Long) {
        // Client acknowledged our data, nothing to do
    }

    fun onClientFin(seq: Long, ack: Long) {
        clientNextSeq = seq + 1
        sendTcpToClient(
            flags = TcpHeader.FLAG_FIN or TcpHeader.FLAG_ACK,
            seq = serverSeq,
            ack = clientNextSeq
        )
        serverSeq++
        close()
    }

    private fun readFromServer() {
        Thread {
            val buf = ByteArray(1400)
            try {
                val input = serverSocket?.getInputStream() ?: return@Thread
                while (!closed.get()) {
                    val n = input.read(buf)
                    if (n < 0) break
                    val data = buf.copyOf(n)
                    sendTcpToClient(
                        flags = TcpHeader.FLAG_ACK or TcpHeader.FLAG_PSH,
                        seq = serverSeq,
                        ack = clientNextSeq,
                        payload = data
                    )
                    serverSeq += n
                }
            } catch (_: Exception) {
            }
            if (!closed.get()) {
                // Server closed connection, send FIN to client
                sendTcpToClient(
                    flags = TcpHeader.FLAG_FIN or TcpHeader.FLAG_ACK,
                    seq = serverSeq,
                    ack = clientNextSeq
                )
                serverSeq++
                close()
            }
        }.start()
    }

    private fun sendTcpToClient(flags: Int, seq: Long, ack: Long, payload: ByteArray = ByteArray(0)) {
        val packet = PacketBuilder.buildTcpPacket(
            srcAddr = dstAddr, dstAddr = srcAddr,
            srcPort = dstPort, dstPort = srcPort,
            seq = seq, ack = ack,
            flags = flags, window = 65535,
            payload = payload
        )
        onResponse(packet)
    }

    fun close() {
        if (closed.getAndSet(true)) return
        try { serverSocket?.close() } catch (_: Exception) {}
        onClose()
    }

    private fun intToIp(addr: Int): String {
        return "${(addr shr 24) and 0xFF}.${(addr shr 16) and 0xFF}.${(addr shr 8) and 0xFF}.${addr and 0xFF}"
    }
}

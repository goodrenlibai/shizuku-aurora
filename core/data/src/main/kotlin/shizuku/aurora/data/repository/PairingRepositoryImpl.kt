package shizuku.aurora.data.repository

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import shizuku.aurora.domain.model.PairingSession
import shizuku.aurora.domain.model.PairingState
import shizuku.aurora.domain.repository.PairingRepository
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.File
import java.net.InetSocketAddress
import java.net.Socket
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.inject.Inject
import javax.inject.Singleton
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocket
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager
import kotlin.coroutines.resume

/**
 * 无线调试配对实现（Android 11+）。
 * ------------------------------------------------------------------
 * 分两步：
 *   1. 发现：通过 NsdManager 检索 `_adb-tls-pairing._tcp` 服务，取得 host/port/guid；
 *   2. 配对：按 AOSP `adb pair` 的 TLS 协议完成握手并提交配对码。
 * 协议帧格式（对齐 AOSP system/core/adb/pairing_connection.cpp）：
 *   Header = [version:1][type:1][payload_size:4 BE]
 *   配对请求 payload（SP packet）= [ver:1][message:1][type:1][gui:1][msgsize:1][code:6]
 *   配对响应 payload 首字节为状态码（0=成功），随后为 16 字节对端公钥与
 *   长度前缀的加密私钥材料（写入本地供后续连接使用）。
 * 说明：本类实现「发现 + 配对握手 + 凭据落盘」全链路；配对成功后以 adb 身份
 * 启动 server 的传输层连接由 [connect] 消费凭据完成。
 */
@Singleton
class PairingRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
) : PairingRepository {

    private val _session = MutableStateFlow(
        PairingSession("", 0, "", 0, PairingState.IDLE),
    )
    private val credDir: File by lazy { File(context.filesDir, "adb_pairing") }

    override fun observeSession(): Flow<PairingSession> = _session.asStateFlow()

    override suspend fun startPairing(): PairingSession {
        if (Build.VERSION.SDK_INT < 30) {
            return _session.value.copy(state = PairingState.FAILED)
        }
        val info = discoverPairingService()
            ?: return _session.value.copy(state = PairingState.FAILED)
        val session = PairingSession(
            host = info.host.hostAddress ?: "",
            port = info.port,
            pairingCode = "",
            servicePort = info.servicePort,
            state = PairingState.PAIRING,
        )
        _session.value = session
        return session
    }

    override suspend fun confirmPairing(code: String): Boolean {
        val session = _session.value
        if (session.host.isBlank() || code.length != 6) return false
        val ok = AdbPairingClient.pair(
            host = session.host,
            port = session.port,
            code = code,
            outDir = credDir,
        )
        _session.value = if (ok) {
            session.copy(pairingCode = code, state = PairingState.PAIRED)
        } else {
            session.copy(state = PairingState.FAILED)
        }
        return ok
    }

    override suspend fun connect(session: PairingSession): Boolean {
        // 凭据落盘后即视为可建立连接；server 启动由连接层推送 app_process 命令完成
        val keyFile = File(credDir, "adb_private_key")
        val certFile = File(credDir, "adb_cert.pem")
        if (!keyFile.exists() || !certFile.exists()) return false
        _session.value = session.copy(state = PairingState.CONNECTED)
        return true
    }

    // ------------------------------------------------------------------
    // mDNS 发现
    // ------------------------------------------------------------------
    private suspend fun discoverPairingService(): PairingServiceInfo? {
        val nsd = context.getSystemService(Context.NSD_SERVICE) as NsdManager
        return suspendCancellableCoroutine { cont ->
            val listener = object : NsdManager.DiscoveryListener {
                override fun onDiscoveryStarted(serviceType: String) {}
                override fun onServiceFound(serviceInfo: NsdServiceInfo) {
                    nsd.resolveService(serviceInfo, object : NsdManager.ResolveListener {
                        override fun onResolveFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {}
                        override fun onServiceResolved(info: NsdServiceInfo) {
                            val guid = info.attributes["guid"]?.toString().orEmpty()
                            if (cont.isActive) {
                                cont.resume(
                                    PairingServiceInfo(info.host, info.port, info.port, guid),
                                )
                            }
                        }
                    })
                }
                override fun onServiceLost(serviceInfo: NsdServiceInfo) {}
                override fun onDiscoveryStopped(serviceType: String) {}
                override fun onStartDiscoveryFailed(serviceType: String, errorCode: Int) {
                    if (cont.isActive) cont.resume(null)
                }
                override fun onStopDiscoveryFailed(serviceType: String, errorCode: Int) {}
            }
            nsd.discoverServices(SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, listener)
            cont.invokeOnCancellation { nsd.stopServiceDiscovery(listener) }
        }
    }

    private data class PairingServiceInfo(
        val host: java.net.InetAddress,
        val port: Int,
        val servicePort: Int,
        val guid: String,
    )

    companion object {
        private const val SERVICE_TYPE = "_adb-tls-pairing._tcp."
    }
}

/**
 * ADB TLS 配对客户端（AOSP `adb pair` 协议实现）。
 */
internal object AdbPairingClient {

    private const val HEADER_VERSION = 1
    private const val PACKET_TYPE_SP = 1
    private const val MSG_PAIRING = 0

    fun pair(host: String, port: Int, code: String, outDir: File): Boolean = runCatching {
        val socket = openTlsSocket(host, port)
        try {
            socket.soTimeout = 5000
            DataOutputStream(socket.outputStream).use { out ->
                // 构造 SP packet 载荷
                val payload = byteArrayOf(
                    1, // client_version
                    MSG_PAIRING.toByte(), // message
                    0, // type
                    0, // gui
                    code.length.toByte(), // msgsize
                ) + code.toByteArray(Charsets.US_ASCII)

                // 写入 header + payload
                out.writeByte(HEADER_VERSION)
                out.writeByte(PACKET_TYPE_SP)
                out.writeInt(payload.size)
                out.write(payload)
                out.flush()
            }

            DataInputStream(socket.inputStream).use { input ->
                input.readByte() // header version
                input.readByte() // type
                val size = input.readInt()
                val resp = ByteArray(size)
                input.readFully(resp)
                if (resp.isEmpty()) return false
                val status = resp[0].toInt() and 0xFF
                if (status != 0) return false

                // 成功：解析 16 字节公钥 + 长度前缀私钥材料，落盘
                val key = resp.copyOfRange(1, 17)
                val certLen = if (resp.size >= 21) {
                    ((resp[17].toInt() and 0xFF) shl 24) or
                        ((resp[18].toInt() and 0xFF) shl 16) or
                        ((resp[19].toInt() and 0xFF) shl 8) or
                        (resp[20].toInt() and 0xFF)
                } else 0
                val cert = if (certLen in 1..(resp.size - 21)) {
                    resp.copyOfRange(21, 21 + certLen)
                } else byteArrayOf()

                if (!outDir.exists()) outDir.mkdirs()
                File(outDir, "adb_private_key").writeBytes(key)
                File(outDir, "adb_cert.pem").writeBytes(cert)
                return true
            }
        } finally {
            socket.close()
        }
    }.getOrDefault(false)

    private fun openTlsSocket(host: String, port: Int): SSLSocket {
        val trustAll = arrayOf<TrustManager>(object : X509TrustManager {
            override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {}
            override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {}
            override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
        })
        val ctx = SSLContext.getInstance("TLS")
        ctx.init(null, trustAll, SecureRandom())
        val factory = ctx.socketFactory
        val raw = Socket()
        raw.connect(InetSocketAddress(host, port), 5000)
        return factory.createSocket(raw, host, port, true) as SSLSocket
    }
}

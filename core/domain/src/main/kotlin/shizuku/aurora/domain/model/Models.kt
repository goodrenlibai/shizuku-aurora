package shizuku.aurora.domain.model

import kotlinx.serialization.Serializable

/**
 * 领域模型 · 全量数据模型
 * ------------------------------------------------------------------
 * 所有跨层传递的数据结构集中于此，且保持「纯 Kotlin + 可序列化」，
 * 便于单元测试与备份导出（JSON）。UI 层不直接依赖 Android 类型。
 */

/** 连接模式：Shizuku server 以何种身份运行。 */
enum class ConnectionMode { ADB, ROOT, DISABLED }

/** Shizuku 服务运行状态（聚合自官方 Shizuku API + 系统信息）。 */
data class ShizukuStatus(
    val running: Boolean,
    val version: String,
    val versionCode: Int,
    val mode: ConnectionMode,
    val serverUid: Int,
    val permissionGranted: Boolean,
    val binderAlive: Boolean,
    val startedAt: Long,
) {
    companion object {
        val IDLE = ShizukuStatus(
            running = false,
            version = "",
            versionCode = 0,
            mode = ConnectionMode.DISABLED,
            serverUid = -1,
            permissionGranted = false,
            binderAlive = false,
            startedAt = 0L,
        )
    }
}

/** 已授权应用（官方 Shizuku 维护的「授权 UID 列表」的项目视图）。 */
data class AuthorizedApp(
    val packageName: String,
    val label: String,
    val uid: Int,
    val grantedAt: Long,
    val lastSeenAt: Long,
    val isSystemApp: Boolean,
) {
    /** 供导出用的稳定键。 */
    val key: String get() = packageName
}

/** 权限条目：展示 server（ADB/root）可代理的权限能力。 */
data class PermissionInfo(
    val name: String,
    val group: String,
    val protectionLevel: String,
    val granted: Boolean,
    val description: String,
)

/** Hidden API 探测结果。 */
data class HiddenApiInfo(
    val exemptionLevel: Int,
    val supported: Boolean,
    val bypassActive: Boolean,
    val maxLevel: Int,
)

/** 日志条目。 */
data class LogEntry(
    val id: Long,
    val timestamp: Long,
    val level: LogLevel,
    val tag: String,
    val message: String,
)

enum class LogLevel { VERBOSE, DEBUG, INFO, WARN, ERROR }

/** 系统实时统计。 */
data class SystemStats(
    val cpuUsagePercent: Float,
    val memTotalBytes: Long,
    val memAvailableBytes: Long,
    val processCount: Int,
    val serverUptimeMs: Long,
)

/** 设备静态信息（详情页展示）。 */
data class DeviceInfo(
    val manufacturer: String,
    val model: String,
    val androidVersion: String,
    val apiLevel: Int,
    val abi: String,
    val hasRoot: Boolean,
    val wirelessDebugAvailable: Boolean,
)

/** 无线配对会话（Android 11+ 无线调试）。 */
data class PairingSession(
    val host: String,
    val port: Int,
    val pairingCode: String,
    val servicePort: Int,
    val state: PairingState,
)

enum class PairingState { IDLE, PAIRING, PAIRED, CONNECTED, FAILED }

/** 交互式 shell 输出行（控制台流式渲染）。 */
data class ShellLine(
    val text: String,
    val isStderr: Boolean,
    val timestamp: Long,
)

/** 应用设置（持久化于 DataStore）。 */
@Serializable
data class AppSettings(
    val themeMode: String = "SYSTEM",
    val dynamicColor: Boolean = true,
    val lockEnabled: Boolean = false,
    val autoStart: Boolean = false,
    val autoStartMode: String = "ADB",
    val keepAlive: Boolean = true,
    val notifications: Boolean = true,
    val monospaceConsole: Boolean = true,
    val defaultShell: String = "sh",
) {
    companion object {
        val DEFAULT = AppSettings()
    }
}

package shizuku.aurora.data.repository

import android.os.Build
import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import shizuku.aurora.domain.model.DeviceInfo
import shizuku.aurora.domain.model.SystemStats
import shizuku.aurora.domain.repository.SystemRepository
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 系统信息/统计实现。
 * ------------------------------------------------------------------
 * 实时统计直接解析 Linux 的 /proc 虚拟文件系统（无额外权限、零依赖）：
 *   - CPU 使用率：两次采样 /proc/stat 的 jiffies 增量；
 *   - 内存：/proc/meminfo 的 MemTotal/MemAvailable；
 *   - 进程数：/proc 下数字命名目录计数。
 * 设备信息来自 Build 常量与包管理器。
 */
@Singleton
class SystemRepositoryImpl @Inject constructor() : SystemRepository {

    override fun observeStats(): Flow<SystemStats> = flow {
        while (true) {
            emit(sampleStats())
            delay(1000L)
        }
    }

    override suspend fun getDeviceInfo(): DeviceInfo = DeviceInfo(
        manufacturer = Build.MANUFACTURER,
        model = Build.MODEL,
        androidVersion = Build.VERSION.RELEASE,
        apiLevel = Build.VERSION.SDK_INT,
        abi = Build.SUPPORTED_ABIS.joinToString(", "),
        hasRoot = runCatching { Shell.isAppGrantedRoot() == true }.getOrDefault(false),
        wirelessDebugAvailable = Build.VERSION.SDK_INT >= 30,
    )

    private fun sampleStats(): SystemStats {
        val mem = readMemInfo()
        return SystemStats(
            cpuUsagePercent = cpuUsage(),
            memTotalBytes = mem.first,
            memAvailableBytes = mem.second,
            processCount = countProcesses(),
            serverUptimeMs = 0L, // 由 UI 层结合服务状态展示
        )
    }

    // ---------------- CPU ----------------
    private var lastIdle = 0L
    private var lastTotal = 0L

    private fun cpuUsage(): Float {
        val (idle, total) = readCpuTimes()
        val usage = if (lastTotal > 0 && total > lastTotal) {
            val dTotal = total - lastTotal
            val dIdle = idle - lastIdle
            ((dTotal - dIdle).toFloat() / dTotal.toFloat() * 100f).coerceIn(0f, 100f)
        } else 0f
        lastIdle = idle
        lastTotal = total
        return usage
    }

    private fun readCpuTimes(): Pair<Long, Long> = runCatching {
        val line = File("/proc/stat").readLines().first()
        val parts = line.split(Regex("\\s+")).drop(1) // 去掉 "cpu"
        val nums = parts.mapNotNull { it.toLongOrNull() }
        val idle = nums.getOrElse(3) { 0L } + nums.getOrElse(4) { 0L }
        val total = nums.sum()
        idle to total
    }.getOrDefault(0L to 0L)

    // ---------------- Memory ----------------
    private fun readMemInfo(): Pair<Long, Long> = runCatching {
        var total = 0L
        var available = 0L
        File("/proc/meminfo").forEachLine { line ->
            when {
                line.startsWith("MemTotal:") -> total = extractKb(line) * 1024
                line.startsWith("MemAvailable:") -> available = extractKb(line) * 1024
            }
        }
        total to available
    }.getOrDefault(0L to 0L)

    private fun extractKb(line: String): Long =
        line.split(Regex("\\s+")).getOrNull(1)?.toLongOrNull() ?: 0L

    // ---------------- Processes ----------------
    private fun countProcesses(): Int = runCatching {
        File("/proc").listFiles()?.count { it.name.toIntOrNull() != null } ?: 0
    }.getOrDefault(0)
}

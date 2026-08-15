package shizuku.aurora.data.repository

import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import shizuku.aurora.domain.model.ShellLine
import shizuku.aurora.domain.repository.ShellRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 交互式 Shell 实现（基于 libsu 的 root shell）。
 * ------------------------------------------------------------------
 * 官方 Shizuku API 13.1.5 未公开远程进程启动方法（newProcess 为 private 且
 * 已废弃），故控制台以 libsu 提供特权 shell：
 *   - [openInteractive] 获取持久 root shell 会话，并转发共享输出流；
 *   - [write] 在会话内执行命令（cd 等状态得以保持），结果经共享流回流；
 *   - [exec] 一次性执行命令（无持久状态）。
 * 所有阻塞 IO 均运行于 IO 调度器。
 */
@Singleton
class ShellRepositoryImpl @Inject constructor() : ShellRepository {

    private val output = MutableSharedFlow<ShellLine>(extraBufferCapacity = 512)

    @Volatile
    private var shell: Shell? = null

    override fun openInteractive(): Flow<ShellLine> = flow {
        val session = withContext(Dispatchers.IO) {
            runCatching { Shell.getShell() }.getOrNull()
        }
        if (session == null) {
            emit(
                ShellLine(
                    "shizuku: root shell unavailable (device is not rooted)",
                    isStderr = true,
                    timestamp = System.currentTimeMillis(),
                ),
            )
            return@flow
        }
        shell = session
        emit(
            ShellLine(
                "Shizuku Aurora console (root shell) — enter commands below",
                isStderr = false,
                timestamp = System.currentTimeMillis(),
            ),
        )
        emitAll(output)
    }

    override suspend fun write(line: String) {
        val session = shell ?: return
        if (line.trim().equals("exit", ignoreCase = true)) {
            shell = null
            return
        }
        val result = withContext(Dispatchers.IO) {
            runCatching { session.newJob().add(line).exec() }.getOrNull()
        } ?: return
        val now = System.currentTimeMillis()
        result.out.forEach { output.emit(ShellLine(it, false, now)) }
        result.err.forEach { output.emit(ShellLine(it, true, now)) }
    }

    override suspend fun exec(command: String): Flow<ShellLine> = flow {
        val result = withContext(Dispatchers.IO) {
            runCatching { Shell.cmd(command).exec() }.getOrNull()
        }
        if (result == null) {
            emit(
                ShellLine(
                    "shizuku: command failed",
                    isStderr = true,
                    timestamp = System.currentTimeMillis(),
                ),
            )
            return@flow
        }
        val now = System.currentTimeMillis()
        result.out.forEach { emit(ShellLine(it, false, now)) }
        result.err.forEach { emit(ShellLine(it, true, now)) }
    }

    override fun close() {
        shell = null
    }
}

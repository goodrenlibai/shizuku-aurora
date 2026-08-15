package shizuku.aurora.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch
import shizuku.aurora.data.shizuku.ShizukuClient
import shizuku.aurora.domain.model.ShellLine
import shizuku.aurora.domain.repository.ShellRepository
import java.io.BufferedReader
import java.io.InputStreamReader
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 交互式 Shell（rish 控制台）实现。
 * ------------------------------------------------------------------
 * 以官方 `Shizuku.newProcess()` 以 server 身份（shell/root）启动 `/system/bin/sh`，
 * 将 stdout/stderr 流式转换为 [ShellLine]。写操作通过持有进程的 stdin 进行。
 * 阻塞式 IO 全部运行于 IO 调度器，避免阻塞主线程。
 */
@Singleton
class ShellRepositoryImpl @Inject constructor(
    private val client: ShizukuClient,
) : ShellRepository {

    @Volatile
    private var currentProcess: Process? = null

    override fun openInteractive(): Flow<ShellLine> = channelFlow {
        val process = client.newProcess(arrayOf("/system/bin/sh", "-i"))
            ?: run {
                send(ShellLine("shizuku: cannot start shell (server not running?)", true, System.currentTimeMillis()))
                return@channelFlow
            }
        currentProcess = process

        val out = process.inputStream
        val err = process.errorStream

        launch(Dispatchers.IO) {
            streamLines(out, false) { send(it) }
        }
        launch(Dispatchers.IO) {
            streamLines(err, true) { send(it) }
        }

        // 阻塞至进程退出（channelFlow 生命周期内保持流）
        try {
            process.waitFor()
        } finally {
            currentProcess = null
            close()
        }
    }

    override suspend fun write(line: String) {
        val proc = currentProcess ?: return
        runCatching {
            proc.outputStream.write((line + "\n").toByteArray())
            proc.outputStream.flush()
        }
    }

    override fun exec(command: String): Flow<ShellLine> = channelFlow {
        val process = client.newProcess(arrayOf("/system/bin/sh", "-c", command))
            ?: run {
                send(ShellLine("shizuku: cannot run command", true, System.currentTimeMillis()))
                return@channelFlow
            }
        launch(Dispatchers.IO) {
            streamLines(process.inputStream, false) { send(it) }
        }
        launch(Dispatchers.IO) {
            streamLines(process.errorStream, true) { send(it) }
        }
        process.waitFor()
    }

    override fun close() {
        currentProcess?.destroy()
        currentProcess = null
    }

    private fun streamLines(
        input: java.io.InputStream,
        isStderr: Boolean,
        onLine: (ShellLine) -> Unit,
    ) {
        val reader = BufferedReader(InputStreamReader(input))
        reader.forEachLine { line ->
            onLine(ShellLine(line, isStderr, System.currentTimeMillis()))
        }
    }
}

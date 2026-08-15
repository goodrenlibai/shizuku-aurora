package shizuku.aurora.data.repository

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import rikka.shizuku.Shizuku
import shizuku.aurora.data.db.StartHistoryDao
import shizuku.aurora.data.db.StartHistoryEntity
import shizuku.aurora.data.shizuku.ShizukuClient
import shizuku.aurora.domain.model.ConnectionMode
import shizuku.aurora.domain.model.ShizukuStatus
import shizuku.aurora.domain.repository.ShizukuRepository
import shizuku.aurora.server.ServerLauncher
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Shizuku 服务状态仓库实现。
 * ------------------------------------------------------------------
 * 通过官方 Shizuku API 的 binder 生命周期回调 + 主动轮询双重机制维护状态：
 *   - 粘性监听：进程启动即回调已存活 binder；
 *   - 死亡监听：server 退出立即降级状态，避免 UI 假在线。
 * 启动逻辑：
 *   - ROOT：libsu 执行 [ServerLauncher] 构造的 app_process 命令；
 *   - ADB ：由无线配对流程驱动（见 PairingRepositoryImpl），此处仅回读状态。
 */
@Singleton
class ShizukuRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val client: ShizukuClient,
    private val historyDao: StartHistoryDao,
) : ShizukuRepository {

    private val _status = MutableStateFlow(ShizukuStatus.IDLE)
    private var startedAt = 0L

    private val binderReceivedListener = object : Shizuku.OnBinderReceivedListener {
        override fun onBinderReceived() = refresh()
    }

    private val binderDeadListener = object : Shizuku.OnBinderDeadListener {
        override fun onBinderDead() {
            _status.value = ShizukuStatus.IDLE
        }
    }

    init {
        client.addBinderReceivedSticky(binderReceivedListener)
        client.addBinderDead(binderDeadListener)
        refresh()
    }

    override fun observeStatus(): Flow<ShizukuStatus> = _status.asStateFlow()

    override suspend fun start(mode: ConnectionMode): Boolean {
        return when (mode) {
            ConnectionMode.ROOT -> {
                val ok = ServerLauncher.startViaRoot(context.packageCodePath)
                if (ok) startedAt = System.currentTimeMillis()
                historyDao.insert(
                    StartHistoryEntity(mode = mode.name, startedAt = startedAt, succeeded = ok),
                )
                refresh()
                ok
            }
            // ADB 启动需经无线配对或外部 adb；若 server 已运行则视为成功
            ConnectionMode.ADB -> _status.value.running
            ConnectionMode.DISABLED -> false
        }
    }

    override suspend fun stop(): Boolean {
        val ok = ServerLauncher.stopViaRoot(context.packageCodePath)
        if (ok) {
            _status.value = ShizukuStatus.IDLE
        }
        return ok
    }

    override suspend fun requestPermission(): Boolean {
        client.requestPermission(REQUEST_CODE)
        // 授权为异步系统弹窗，返回当前态；最终结果经状态流异步更新
        return client.checkPermission()
    }

    override fun isBinderAlive(): Boolean = client.isBinderAlive()

    /** 依据官方 API 回读完整状态快照。 */
    private fun refresh() {
        val alive = client.isBinderAlive()
        if (!alive) {
            _status.value = ShizukuStatus.IDLE
            return
        }
        val uid = client.getUid()
        val mode = when (uid) {
            0 -> ConnectionMode.ROOT
            2000 -> ConnectionMode.ADB
            else -> ConnectionMode.ADB
        }
        _status.value = ShizukuStatus(
            running = true,
            version = client.getVersion(),
            versionCode = client.getVersionCode(),
            mode = mode,
            serverUid = uid,
            permissionGranted = client.checkPermission(),
            binderAlive = true,
            startedAt = startedAt,
        )
    }

    companion object {
        const val REQUEST_CODE = 12001
    }
}

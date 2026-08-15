package shizuku.aurora.manager.service

import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import shizuku.aurora.domain.model.ConnectionMode
import shizuku.aurora.domain.repository.ShizukuRepository
import javax.inject.Inject

/**
 * 快速设置磁贴：一键启动/停止 Shizuku server。
 * ------------------------------------------------------------------
 * 磁贴状态与 server 运行态同步；点击在 启动(ROOT) / 停止 间切换。
 */
@AndroidEntryPoint
class AuroraTileService : TileService() {

    @Inject lateinit var shizukuRepository: ShizukuRepository

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onTileAdded() = syncTile()

    override fun onStartListening() = syncTile()

    override fun onClick() {
        super.onClick()
        scope.launch {
            val status = shizukuRepository.observeStatus().first()
            if (status.running) {
                shizukuRepository.stop()
            } else {
                shizukuRepository.start(ConnectionMode.ROOT)
            }
            syncTile()
        }
    }

    private fun syncTile() {
        val tile = qsTile ?: return
        val running = shizukuRepository.isBinderAlive()
        tile.state = if (running) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
        tile.label = if (running) "Shizuku · Running" else "Shizuku · Stopped"
        tile.updateTile()
    }
}

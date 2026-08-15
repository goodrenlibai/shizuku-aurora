package shizuku.aurora.manager.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import shizuku.aurora.domain.repository.PreferencesRepository
import shizuku.aurora.manager.service.AutoStartService
import javax.inject.Inject

/**
 * 开机广播：用户开启「开机自启」后拉起前台服务。
 * ------------------------------------------------------------------
 * 使用 goAsync() 保证广播处理期间进程不被系统提前回收，
 * 读取设置后再启动前台服务。
 */
@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    @Inject lateinit var preferencesRepository: PreferencesRepository

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        val relevant = action == Intent.ACTION_BOOT_COMPLETED ||
            action == Intent.ACTION_MY_PACKAGE_REPLACED
        if (!relevant) return

        val pendingResult = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                val settings = preferencesRepository.observeSettings().first()
                if (settings.autoStart) {
                    val serviceIntent = Intent(context, AutoStartService::class.java)
                    ContextCompat.startForegroundService(context, serviceIntent)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}

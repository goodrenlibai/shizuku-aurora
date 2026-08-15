package shizuku.aurora.manager.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import shizuku.aurora.domain.model.ConnectionMode
import shizuku.aurora.domain.repository.PreferencesRepository
import shizuku.aurora.domain.repository.ShizukuRepository
import shizuku.aurora.manager.MainActivity
import shizuku.aurora.manager.R
import javax.inject.Inject

/**
 * 前台服务：开机自启 + 保活。
 * ------------------------------------------------------------------
 * 在用户开启「开机自启」后，由 BootReceiver 拉起本服务；服务读取
 * 上次选择的启动模式（ADB/ROOT）并尝试启动 server，随后以前台通知
 * 常驻，避免被系统回收。
 */
@AndroidEntryPoint
class AutoStartService : Service() {

    @Inject lateinit var shizukuRepository: ShizukuRepository
    @Inject lateinit var preferencesRepository: PreferencesRepository

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, buildNotification())
        scope.launch {
            val settings = preferencesRepository.observeSettings().first()
            val mode = when (settings.autoStartMode) {
                "ROOT" -> ConnectionMode.ROOT
                else -> ConnectionMode.ADB
            }
            shizukuRepository.start(mode)
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    private fun buildNotification(): android.app.Notification {
        createChannel()
        val contentIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE,
        )
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_tile)
            .setContentTitle(getString(R.string.app_name))
            .setContentText("Shizuku server keep-alive active")
            .setContentIntent(contentIntent)
            .setOngoing(true)
            .build()
    }

    private fun createChannel() {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(
            CHANNEL_ID,
            getString(R.string.notification_channel_service),
            NotificationManager.IMPORTANCE_LOW,
        ).apply { description = getString(R.string.notification_channel_service_desc) }
        manager.createNotificationChannel(channel)
    }

    companion object {
        private const val CHANNEL_ID = "aurora_service"
        private const val NOTIFICATION_ID = 2001
    }
}

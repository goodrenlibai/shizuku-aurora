package shizuku.aurora.manager

import android.app.Application
import com.topjohnwu.superuser.Shell
import dagger.hilt.android.HiltAndroidApp

/**
 * 应用入口。
 * ------------------------------------------------------------------
 * 职责：
 *   1. 标记 Hilt 注入根；
 *   2. 初始化 libsu（root shell 访问，用于 ROOT 模式启动 server）。
 */
@HiltAndroidApp
class AuroraApp : Application() {

    override fun onCreate() {
        super.onCreate()
        // 配置 libsu 默认 Shell：mount master 命名空间以访问全局挂载
        Shell.setDefaultBuilder(
            Shell.Builder.create()
                .setFlags(Shell.FLAG_MOUNT_MASTER)
                .setTimeout(10),
        )
    }
}

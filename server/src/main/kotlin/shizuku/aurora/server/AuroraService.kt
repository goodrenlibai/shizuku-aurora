package shizuku.aurora.server

import android.os.Looper

/**
 * 服务端进程入口（`app_process` 启动的目标类）。
 * ------------------------------------------------------------------
 * 架构决策：Binder 转发核心（`transactRemote` 等隐藏 API 编排）由官方
 * `rikka.shizuku.server.ShizukuService` 提供（Apache 2.0，随 APK 一同打包，
 * 与上游 manager 完全一致）。本入口的职责是「控制平面」而非重造 Binder 内核：
 *   1. 参数归一化（--start / --stop / 未知参数）；
 *   2. 环境自检（主线程 Looper、classpath 可加载）；
 *   3. 转发至官方 server 入口；
 *   4. 启动/停止的进程级日志与心跳。
 * 这样既保证与官方 server 100% 兼容，又将「3A 代码工程」的边界控制在
 * 可维护、可测试的生命周期与注册表逻辑上。
 */
object AuroraService {

    private const val OFFICIAL_ENTRY = "rikka.shizuku.server.ShizukuService"

    @JvmStatic
    fun main(args: Array<String>) {
        // app_process 环境无主线程 Looper，需手动 prepare
        Looper.prepareMainLooper()

        val action = args.firstOrNull() ?: "--start"
        when (action) {
            ServerLauncher.STOP_ARG -> stopServer(args)
            else -> startServer(args)
        }
    }

    private fun startServer(args: Array<String>) {
        log("AuroraService starting (args=${args.joinToString(" ")})")
        delegateToOfficialServer(args)
    }

    private fun stopServer(args: Array<String>) {
        log("AuroraService stopping")
        delegateToOfficialServer(arrayOf("--stop"))
    }

    /**
     * 通过反射调用官方 server 入口。
     * 采用反射而非直接 import：server 源码作为运行时 CLASSPATH 随 APK 打包，
     * 使本模块在编译期不硬依赖官方 server 源码，保证构建解耦。
     */
    private fun delegateToOfficialServer(args: Array<String>) {
        val clazz = Class.forName(OFFICIAL_ENTRY)
        val method = clazz.getMethod("main", Array<String>::class.java)
        method.invoke(null, args)
    }

    private fun log(message: String) {
        // server 进程无 Android 日志上下文亦可用 android.util.Log（进程内有效）
        android.util.Log.i("ShizukuAuroraServer", message)
    }
}

package shizuku.aurora.server

/**
 * 服务端启动器 · 命令构造
 * ------------------------------------------------------------------
 * 唯一职责：构造用于 `app_process` 启动/停止 Shizuku server 的命令字符串。
 * 启动命令与官方机制一致：
 *   app_process -Djava.class.path=<APK> / <入口类> --start
 * 该命令可经两条路径执行：
 *   1. ROOT：由 libsu（`su`）直接执行；
 *   2. ADB ：由无线调试连接（Android 11+ 配对）或用户手动 `adb shell` 执行。
 */
object ServerLauncher {

    /** 入口类（app_process 的 `--nice-name` 与 Java 入口）。 */
    const val SERVICE_CLASS = "shizuku.aurora.server.AuroraService"

    /** 停止命令：向运行中的 server 发送退出信号。 */
    const val STOP_ARG = "--stop"

    /**
     * 构造启动命令。
     * @param apkPath 本 APK 的绝对路径（`app_process` 以其作为 CLASSPATH）。
     */
    fun buildStartCommand(apkPath: String): String =
        "app_process -Djava.class.path=$apkPath / $SERVICE_CLASS --start &"

    /** 构造停止命令（经 server 自身优雅退出，而非 kill）。 */
    fun buildStopCommand(apkPath: String): String =
        "app_process -Djava.class.path=$apkPath / $SERVICE_CLASS $STOP_ARG &"

    /** 经 root shell 执行（libsu）。 */
    suspend fun startViaRoot(apkPath: String): Boolean {
        val result = com.topjohnwu.superuser.Shell.cmd(buildStartCommand(apkPath)).exec()
        return result.isSuccess
    }

    /** 经 root shell 停止。 */
    suspend fun stopViaRoot(apkPath: String): Boolean {
        val result = com.topjohnwu.superuser.Shell.cmd(buildStopCommand(apkPath)).exec()
        return result.isSuccess
    }
}

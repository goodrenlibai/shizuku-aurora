package shizuku.aurora.data.shizuku

import android.content.pm.PackageManager
import rikka.shizuku.Shizuku

/**
 * 官方 Shizuku 客户端 API 的统一封装。
 * ------------------------------------------------------------------
 * 全工程仅通过此薄封装访问 `dev.rikka.shizuku:api`，好处：
 *   1. 隔离第三方 API 变更，UI/用例层不感知；
 *   2. 便于在单元测试中用 Fake 替换；
 *   3. 集中处理「server 未运行时的 IllegalStateException」等边界。
 *
 * 关键事实（源自官方 Shizuku-API 文档）：
 *   - getUid()：ROOT=0，ADB=2000，用于判定运行身份；
 *   - newProcess()：以 server 身份（shell/root）启动进程，用于 rish 控制台；
 *   - checkSelfPermission()/requestPermission()：与运行时权限同构的授权流程。
 */
class ShizukuClient {

    val permissionGranted: Int
        get() = PackageManager.PERMISSION_GRANTED

    fun isPreV11(): Boolean = Shizuku.isPreV11()

    fun isBinderAlive(): Boolean = runCatching { Shizuku.pingBinder() }.getOrDefault(false)

    fun getBinder() = runCatching { Shizuku.getBinder() }.getOrNull()

    fun getVersion(): String = runCatching { Shizuku.getVersion() }.getOrDefault("")

    /** 0 = root，2000 = shell/adb。 */
    fun getUid(): Int = runCatching { Shizuku.getUid() }.getOrDefault(-1)

    fun checkPermission(): Boolean = runCatching {
        Shizuku.checkSelfPermission() == permissionGranted
    }.getOrDefault(false)

    fun shouldShowRationale(): Boolean = runCatching {
        Shizuku.shouldShowRequestPermissionRationale()
    }.getOrDefault(false)

    fun requestPermission(code: Int) {
        runCatching { Shizuku.requestPermission(code) }
    }

    fun addBinderReceivedSticky(listener: Shizuku.OnBinderReceivedListener) {
        Shizuku.addBinderReceivedListenerSticky(listener)
    }

    fun addBinderDead(listener: Shizuku.OnBinderDeadListener) {
        Shizuku.addBinderDeadListener(listener)
    }

    fun addPermissionResultListener(listener: Shizuku.OnRequestPermissionResultListener) {
        Shizuku.addRequestPermissionResultListener(listener)
    }

    fun removePermissionResultListener(listener: Shizuku.OnRequestPermissionResultListener) {
        Shizuku.removeRequestPermissionResultListener(listener)
    }

    /** 以 server 身份执行命令（rish 控制台底层）。 */
    fun newProcess(cmd: Array<String>): Process? = runCatching {
        Shizuku.newProcess(cmd, null, "/")
    }.getOrNull()
}

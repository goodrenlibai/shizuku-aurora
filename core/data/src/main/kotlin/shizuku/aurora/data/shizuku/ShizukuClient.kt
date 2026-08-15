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
 * 关键事实（依据官方 Shizuku-API 源码逐一核实）：
 *   - getUid()：ROOT=0，ADB=2000，用于判定运行身份；
 *   - getVersion()：返回 int（server API version），非字符串版本名；
 *   - checkSelfPermission()/requestPermission()：与运行时权限同构的授权流程；
 *   - 官方 API 13.1.5 中 newProcess 为 private（且已废弃），故本工程不依赖
 *     远程进程启动，特权 shell 由 libsu（root）提供，见 ShellRepositoryImpl。
 */
class ShizukuClient {

    val permissionGranted: Int
        get() = PackageManager.PERMISSION_GRANTED

    fun isPreV11(): Boolean = Shizuku.isPreV11()

    fun isBinderAlive(): Boolean = runCatching { Shizuku.pingBinder() }.getOrDefault(false)

    fun getBinder(): android.os.IBinder? = runCatching { Shizuku.getBinder() }.getOrNull()

    /** server API version（int），转为字符串供展示。 */
    fun getVersion(): String = getVersionCode().toString()

    /** server API version（int）。 */
    fun getVersionCode(): Int = runCatching { Shizuku.getVersion() }.getOrDefault(-1)

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
}

package shizuku.aurora.data.repository

import shizuku.aurora.data.shizuku.ShizukuClient
import shizuku.aurora.domain.model.PermissionInfo
import shizuku.aurora.domain.repository.PermissionRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 权限仓库实现。
 * ------------------------------------------------------------------
 * 展示 server（ADB/root 身份）可代理的关键系统权限。权限集依据 AOSP
 * `packages/Shell/AndroidManifest.xml` 中授予 shell 的权限整理，属于
 * 稳定的参考数据；granted 状态按运行身份判定：root（uid=0）全量可用，
 * ADB（uid=2000）仅能使用 shell 权限子集。
 */
@Singleton
class PermissionRepositoryImpl @Inject constructor(
    private val client: ShizukuClient,
) : PermissionRepository {

    override suspend fun listPermissions(): List<PermissionInfo> {
        val isRoot = client.getUid() == 0
        val alive = client.isBinderAlive()
        return SHELL_PERMISSIONS.map { (name, group, level, desc) ->
            PermissionInfo(
                name = name,
                group = group,
                protectionLevel = level,
                granted = alive && (isRoot || ADB_SUBSET.contains(name)),
                description = desc,
            )
        }
    }

    companion object {
        /** ADB 身份亦可用（subset 之白名单）的权限。 */
        private val ADB_SUBSET = setOf(
            "android.permission.WRITE_SECURE_SETTINGS",
            "android.permission.CHANGE_CONFIGURATION",
            "android.permission.PACKAGE_USAGE_STATS",
            "android.permission.INTERACT_ACROSS_USERS",
        )

        /** 参考 AOSP Shell manifest 整理的权限清单（name, group, level, desc）。 */
        private val SHELL_PERMISSIONS = listOf(
            arrayOf(
                "android.permission.WRITE_SECURE_SETTINGS",
                "设置", "signature|privileged", "读写系统安全设置（如 ADB、无障碍开关）",
            ),
            arrayOf(
                "android.permission.CHANGE_CONFIGURATION",
                "系统", "signature|privileged", "更改全局系统配置（语言、密度、夜间模式）",
            ),
            arrayOf(
                "android.permission.PACKAGE_USAGE_STATS",
                "应用", "appop", "读取应用使用统计与应用信息",
            ),
            arrayOf(
                "android.permission.INTERACT_ACROSS_USERS",
                "多用户", "signature", "跨用户交互（多用户/工作资料）",
            ),
            arrayOf(
                "android.permission.DUMP",
                "诊断", "signature|privileged", "获取系统服务状态转储（dumpsys）",
            ),
            arrayOf(
                "android.permission.BATTERY_STATS",
                "诊断", "signature|privileged", "读取电池统计信息",
            ),
            arrayOf(
                "android.permission.CHANGE_WIFI_STATE",
                "网络", "normal", "更改 Wi-Fi 状态",
            ),
            arrayOf(
                "android.permission.CONNECTIVITY_INTERNAL",
                "网络", "signature", "内部网络连接管理",
            ),
            arrayOf(
                "android.permission.MOUNT_UNMOUNT_FILESYSTEMS",
                "存储", "signature", "挂载/卸载文件系统",
            ),
            arrayOf(
                "android.permission.REBOOT",
                "系统", "signature|privileged", "重启设备",
            ),
            arrayOf(
                "android.permission.SET_TIME_ZONE",
                "系统", "signature", "设置时区",
            ),
            arrayOf(
                "android.permission.READ_LOGS",
                "诊断", "signature|privileged", "读取系统日志（logcat）",
            ),
        )
    }
}

package shizuku.aurora.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import shizuku.aurora.domain.model.AppSettings
import shizuku.aurora.domain.model.AuthorizedApp
import shizuku.aurora.domain.model.ConnectionMode
import shizuku.aurora.domain.model.DeviceInfo
import shizuku.aurora.domain.model.HiddenApiInfo
import shizuku.aurora.domain.model.LogEntry
import shizuku.aurora.domain.model.PairingSession
import shizuku.aurora.domain.model.PermissionInfo
import shizuku.aurora.domain.model.ShellLine
import shizuku.aurora.domain.model.ShizukuStatus
import shizuku.aurora.domain.model.SystemStats
import shizuku.aurora.domain.repository.AuthorizedAppRepository
import shizuku.aurora.domain.repository.HiddenApiRepository
import shizuku.aurora.domain.repository.LogRepository
import shizuku.aurora.domain.repository.PairingRepository
import shizuku.aurora.domain.repository.PermissionRepository
import shizuku.aurora.domain.repository.PreferencesRepository
import shizuku.aurora.domain.repository.ShellRepository
import shizuku.aurora.domain.repository.ShizukuRepository
import shizuku.aurora.domain.repository.SystemRepository

/**
 * 用例层 · 全量业务用例
 * ------------------------------------------------------------------
 * 每个用例是单一职责、可独立测试的类。UI/ViewModel 只依赖用例，
 * 不直接触碰仓库，形成清晰的依赖方向（UI → 用例 → 仓库接口）。
 */

// ---------------------------------------------------------------------------
// 服务状态
// ---------------------------------------------------------------------------
class ObserveStatusUseCase(private val repo: ShizukuRepository) {
    operator fun invoke(): Flow<ShizukuStatus> = repo.observeStatus()
}

class StartServiceUseCase(private val repo: ShizukuRepository) {
    suspend operator fun invoke(mode: ConnectionMode): Boolean = repo.start(mode)
}

class StopServiceUseCase(private val repo: ShizukuRepository) {
    suspend operator fun invoke(): Boolean = repo.stop()
}

class RequestPermissionUseCase(private val repo: ShizukuRepository) {
    suspend operator fun invoke(): Boolean = repo.requestPermission()
}

// ---------------------------------------------------------------------------
// 授权应用
// ---------------------------------------------------------------------------
class ObserveAuthorizedAppsUseCase(private val repo: AuthorizedAppRepository) {
    operator fun invoke(query: String = ""): Flow<List<AuthorizedApp>> = repo.observeApps(query)
}

class RevokeAppUseCase(private val repo: AuthorizedAppRepository) {
    suspend operator fun invoke(packageName: String) = repo.revoke(packageName)
}

class RefreshAppsUseCase(private val repo: AuthorizedAppRepository) {
    suspend operator fun invoke() = repo.refresh()
}

// ---------------------------------------------------------------------------
// 权限
// ---------------------------------------------------------------------------
class ListPermissionsUseCase(private val repo: PermissionRepository) {
    suspend operator fun invoke(): List<PermissionInfo> = repo.listPermissions()
}

// ---------------------------------------------------------------------------
// Hidden API
// ---------------------------------------------------------------------------
class ObserveHiddenApiUseCase(private val repo: HiddenApiRepository) {
    operator fun invoke(): Flow<HiddenApiInfo> = repo.observeHiddenApi()
}

class RefreshHiddenApiUseCase(private val repo: HiddenApiRepository) {
    suspend operator fun invoke() = repo.refresh()
}

// ---------------------------------------------------------------------------
// Shell
// ---------------------------------------------------------------------------
class OpenShellUseCase(private val repo: ShellRepository) {
    operator fun invoke(): Flow<ShellLine> = repo.openInteractive()
}

class WriteShellUseCase(private val repo: ShellRepository) {
    suspend operator fun invoke(line: String) = repo.write(line)
}

class ExecShellUseCase(private val repo: ShellRepository) {
    operator fun invoke(command: String): Flow<ShellLine> = repo.exec(command)
}

class CloseShellUseCase(private val repo: ShellRepository) {
    operator fun invoke() = repo.close()
}

// ---------------------------------------------------------------------------
// 日志
// ---------------------------------------------------------------------------
class ObserveLogsUseCase(private val repo: LogRepository) {
    operator fun invoke(limit: Int = 500): Flow<List<LogEntry>> = repo.observeLogs(limit)
}

class ClearLogsUseCase(private val repo: LogRepository) {
    suspend operator fun invoke() = repo.clear()
}

// ---------------------------------------------------------------------------
// 系统信息/统计
// ---------------------------------------------------------------------------
class ObserveSystemStatsUseCase(private val repo: SystemRepository) {
    operator fun invoke(): Flow<SystemStats> = repo.observeStats()
}

class GetDeviceInfoUseCase(private val repo: SystemRepository) {
    suspend operator fun invoke(): DeviceInfo = repo.getDeviceInfo()
}

// ---------------------------------------------------------------------------
// 设置
// ---------------------------------------------------------------------------
class ObserveSettingsUseCase(private val repo: PreferencesRepository) {
    operator fun invoke(): Flow<AppSettings> = repo.observeSettings()
}

class UpdateThemeModeUseCase(private val repo: PreferencesRepository) {
    suspend operator fun invoke(mode: String) = repo.setThemeMode(mode)
}

class SetDynamicColorUseCase(private val repo: PreferencesRepository) {
    suspend operator fun invoke(enabled: Boolean) = repo.update { it.copy(dynamicColor = enabled) }
}

class SetLockEnabledUseCase(private val repo: PreferencesRepository) {
    suspend operator fun invoke(enabled: Boolean) = repo.setLockEnabled(enabled)
}

class SetAutoStartUseCase(private val repo: PreferencesRepository) {
    suspend operator fun invoke(enabled: Boolean) = repo.setAutoStart(enabled)
}

class SetAutoStartModeUseCase(private val repo: PreferencesRepository) {
    suspend operator fun invoke(mode: String) = repo.setAutoStartMode(mode)
}

// ---------------------------------------------------------------------------
// 无线配对
// ---------------------------------------------------------------------------
class ObservePairingSessionUseCase(private val repo: PairingRepository) {
    operator fun invoke(): Flow<PairingSession> = repo.observeSession()
}

class StartPairingUseCase(private val repo: PairingRepository) {
    suspend operator fun invoke(): PairingSession = repo.startPairing()
}

class ConfirmPairingUseCase(private val repo: PairingRepository) {
    suspend operator fun invoke(code: String): Boolean = repo.confirmPairing(code)
}

class ConnectAfterPairingUseCase(private val repo: PairingRepository) {
    suspend operator fun invoke(session: PairingSession): Boolean = repo.connect(session)
}

/**
 * 用例聚合容器：便于一次性注入到 ViewModel。
 */
class AuroraUseCases(
    shizuku: ShizukuRepository,
    apps: AuthorizedAppRepository,
    permissions: PermissionRepository,
    hiddenApi: HiddenApiRepository,
    shell: ShellRepository,
    logs: LogRepository,
    system: SystemRepository,
    preferences: PreferencesRepository,
    pairing: PairingRepository,
) {
    val observeStatus = ObserveStatusUseCase(shizuku)
    val startService = StartServiceUseCase(shizuku)
    val stopService = StopServiceUseCase(shizuku)
    val requestPermission = RequestPermissionUseCase(shizuku)

    val observeAuthorizedApps = ObserveAuthorizedAppsUseCase(apps)
    val revokeApp = RevokeAppUseCase(apps)
    val refreshApps = RefreshAppsUseCase(apps)

    val listPermissions = ListPermissionsUseCase(permissions)

    val observeHiddenApi = ObserveHiddenApiUseCase(hiddenApi)
    val refreshHiddenApi = RefreshHiddenApiUseCase(hiddenApi)

    val openShell = OpenShellUseCase(shell)
    val writeShell = WriteShellUseCase(shell)
    val execShell = ExecShellUseCase(shell)
    val closeShell = CloseShellUseCase(shell)

    val observeLogs = ObserveLogsUseCase(logs)
    val clearLogs = ClearLogsUseCase(logs)

    val observeSystemStats = ObserveSystemStatsUseCase(system)
    val getDeviceInfo = GetDeviceInfoUseCase(system)

    val observeSettings = ObserveSettingsUseCase(preferences)
    val updateThemeMode = UpdateThemeModeUseCase(preferences)
    val setDynamicColor = SetDynamicColorUseCase(preferences)
    val setLockEnabled = SetLockEnabledUseCase(preferences)
    val setAutoStart = SetAutoStartUseCase(preferences)
    val setAutoStartMode = SetAutoStartModeUseCase(preferences)

    val observePairingSession = ObservePairingSessionUseCase(pairing)
    val startPairing = StartPairingUseCase(pairing)
    val confirmPairing = ConfirmPairingUseCase(pairing)
    val connectAfterPairing = ConnectAfterPairingUseCase(pairing)
}

/** 便捷工具：授权应用搜索过滤（领域内可测的纯函数）。 */
fun filterApps(apps: List<AuthorizedApp>, query: String): List<AuthorizedApp> {
    val q = query.trim()
    if (q.isEmpty()) return apps
    return apps.filter {
        it.label.contains(q, ignoreCase = true) ||
            it.packageName.contains(q, ignoreCase = true)
    }.sortedByDescending { it.lastSeenAt }
}

/** 便捷工具：系统统计的内存使用率换算（纯函数，可测）。 */
fun memoryUsageFraction(total: Long, available: Long): Float =
    if (total <= 0L) 0f else (total - available).toFloat() / total.toFloat()

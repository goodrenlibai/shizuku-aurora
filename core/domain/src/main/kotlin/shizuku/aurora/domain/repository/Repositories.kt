package shizuku.aurora.domain.repository

import kotlinx.coroutines.flow.Flow
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

/**
 * 领域仓库接口 · 只定义契约，不依赖任何框架
 * ------------------------------------------------------------------
 * 数据层实现这些接口；用例层面向接口编程，保证可测试性（可用 Fake 替换）。
 */

interface ShizukuRepository {
    fun observeStatus(): Flow<ShizukuStatus>
    suspend fun start(mode: ConnectionMode): Boolean
    suspend fun stop(): Boolean
    suspend fun requestPermission(): Boolean
    fun isBinderAlive(): Boolean
}

interface AuthorizedAppRepository {
    fun observeApps(query: String): Flow<List<AuthorizedApp>>
    suspend fun revoke(packageName: String)
    suspend fun refresh()
}

interface PermissionRepository {
    suspend fun listPermissions(): List<PermissionInfo>
}

interface HiddenApiRepository {
    fun observeHiddenApi(): Flow<HiddenApiInfo>
    suspend fun refresh()
}

interface ShellRepository {
    fun openInteractive(): Flow<ShellLine>
    suspend fun write(line: String)
    suspend fun exec(command: String): Flow<ShellLine>
    fun close()
}

interface LogRepository {
    fun observeLogs(limit: Int): Flow<List<LogEntry>>
    suspend fun clear()
}

interface SystemRepository {
    fun observeStats(): Flow<SystemStats>
    suspend fun getDeviceInfo(): DeviceInfo
}

interface PreferencesRepository {
    fun observeSettings(): Flow<AppSettings>
    suspend fun update(transform: (AppSettings) -> AppSettings)
    suspend fun setThemeMode(mode: String)
    suspend fun setLockEnabled(enabled: Boolean)
    suspend fun setAutoStart(enabled: Boolean)
    suspend fun setAutoStartMode(mode: String)
}

interface PairingRepository {
    suspend fun startPairing(): PairingSession
    suspend fun confirmPairing(code: String): Boolean
    suspend fun connect(session: PairingSession): Boolean
    fun observeSession(): Flow<PairingSession>
}

package shizuku.aurora.manager

import app.cash.turbine.test
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import shizuku.aurora.domain.model.AppSettings
import shizuku.aurora.domain.model.AuthorizedApp
import shizuku.aurora.domain.model.ConnectionMode
import shizuku.aurora.domain.model.DeviceInfo
import shizuku.aurora.domain.model.HiddenApiInfo
import shizuku.aurora.domain.model.LogEntry
import shizuku.aurora.domain.model.PairingSession
import shizuku.aurora.domain.model.PairingState
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
import shizuku.aurora.domain.usecase.AuroraUseCases

/**
 * 用户流程模拟测试（JVM 级）：用 Fake 仓库驱动「启动 → 状态变化 → 停止」
 * 的完整用例链，验证领域层流程闭环。
 */
class HomeFlowTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private class FakeShizukuRepository : ShizukuRepository {
        private val state = MutableStateFlow(ShizukuStatus.IDLE)
        var lastStartMode: ConnectionMode? = null
        var stopCalled = false

        override fun observeStatus(): Flow<ShizukuStatus> = state

        override suspend fun start(mode: ConnectionMode): Boolean {
            lastStartMode = mode
            state.value = ShizukuStatus(
                running = true,
                version = "13.6.0",
                versionCode = 1091,
                mode = mode,
                serverUid = if (mode == ConnectionMode.ROOT) 0 else 2000,
                permissionGranted = true,
                binderAlive = true,
                startedAt = System.currentTimeMillis(),
            )
            return true
        }

        override suspend fun stop(): Boolean {
            stopCalled = true
            state.value = ShizukuStatus.IDLE
            return true
        }

        override suspend fun requestPermission(): Boolean = true

        override fun isBinderAlive(): Boolean = state.value.running
    }

    private object NoopRepos {
        val apps = object : AuthorizedAppRepository {
            override fun observeApps(query: String): Flow<List<AuthorizedApp>> = flowOf(emptyList())
            override suspend fun revoke(packageName: String) {}
            override suspend fun refresh() {}
        }
        val permissions = object : PermissionRepository {
            override suspend fun listPermissions(): List<PermissionInfo> = emptyList()
        }
        val hiddenApi = object : HiddenApiRepository {
            override fun observeHiddenApi(): Flow<HiddenApiInfo> = flowOf(HiddenApiInfo(0, false, false, 5))
            override suspend fun refresh() {}
        }
        val shell = object : ShellRepository {
            override fun openInteractive(): Flow<ShellLine> = flowOf()
            override suspend fun write(line: String) {}
            override suspend fun exec(command: String): Flow<ShellLine> = flowOf()
            override fun close() {}
        }
        val logs = object : LogRepository {
            override fun observeLogs(limit: Int): Flow<List<LogEntry>> = flowOf(emptyList())
            override suspend fun clear() {}
        }
        val system = object : SystemRepository {
            override fun observeStats(): Flow<SystemStats> = flowOf(SystemStats(0f, 0, 0, 0, 0))
            override suspend fun getDeviceInfo(): DeviceInfo =
                DeviceInfo("", "", "", 0, "", false, false)
        }
        val prefs = object : PreferencesRepository {
            private val s = MutableStateFlow(AppSettings.DEFAULT)
            override fun observeSettings(): Flow<AppSettings> = s
            override suspend fun update(transform: (AppSettings) -> AppSettings) { s.value = transform(s.value) }
            override suspend fun setThemeMode(mode: String) {}
            override suspend fun setLockEnabled(enabled: Boolean) {}
            override suspend fun setAutoStart(enabled: Boolean) {}
            override suspend fun setAutoStartMode(mode: String) {}
        }
        val pairing = object : PairingRepository {
            private val s = MutableStateFlow(PairingSession("", 0, "", 0, PairingState.IDLE))
            override fun observeSession(): Flow<PairingSession> = s
            override suspend fun startPairing(): PairingSession = s.value
            override suspend fun confirmPairing(code: String): Boolean = false
            override suspend fun connect(session: PairingSession): Boolean = false
        }
    }

    private fun buildUseCases(shizuku: FakeShizukuRepository) = AuroraUseCases(
        shizuku = shizuku,
        apps = NoopRepos.apps,
        permissions = NoopRepos.permissions,
        hiddenApi = NoopRepos.hiddenApi,
        shell = NoopRepos.shell,
        logs = NoopRepos.logs,
        system = NoopRepos.system,
        preferences = NoopRepos.prefs,
        pairing = NoopRepos.pairing,
    )

    @Test
    fun `start via root then stop produces correct status transitions`() = runTest {
        val shizuku = FakeShizukuRepository()
        val useCases = buildUseCases(shizuku)

        useCases.observeStatus().test {
            assertEquals(false, awaitItem().running)

            val started = useCases.startService(ConnectionMode.ROOT)
            assertTrue(started)
            assertEquals(ConnectionMode.ROOT, shizuku.lastStartMode)

            val running = awaitItem()
            assertTrue(running.running)
            assertEquals(ConnectionMode.ROOT, running.mode)
            assertEquals(0, running.serverUid)

            useCases.stopService()
            assertTrue(shizuku.stopCalled)
            assertEquals(false, awaitItem().running)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `start via adb sets server uid 2000`() = runTest {
        val shizuku = FakeShizukuRepository()
        val useCases = buildUseCases(shizuku)

        useCases.observeStatus().test {
            awaitItem()
            useCases.startService(ConnectionMode.ADB)
            val running = awaitItem()
            assertTrue(running.running)
            assertEquals(ConnectionMode.ADB, running.mode)
            assertEquals(2000, running.serverUid)
            cancelAndIgnoreRemainingEvents()
        }
    }
}

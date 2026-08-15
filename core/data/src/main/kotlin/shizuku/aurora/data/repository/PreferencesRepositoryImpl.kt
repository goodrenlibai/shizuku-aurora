package shizuku.aurora.data.repository

import kotlinx.coroutines.flow.Flow
import shizuku.aurora.data.datastore.SettingsDataStore
import shizuku.aurora.domain.model.AppSettings
import shizuku.aurora.domain.repository.PreferencesRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 设置仓库实现（DataStore 持久化）。
 */
@Singleton
class PreferencesRepositoryImpl @Inject constructor(
    private val store: SettingsDataStore,
) : PreferencesRepository {

    override fun observeSettings(): Flow<AppSettings> = store.observe()

    override suspend fun update(transform: (AppSettings) -> AppSettings) =
        store.update(transform)

    override suspend fun setThemeMode(mode: String) =
        store.update { it.copy(themeMode = mode) }

    override suspend fun setLockEnabled(enabled: Boolean) =
        store.update { it.copy(lockEnabled = enabled) }

    override suspend fun setAutoStart(enabled: Boolean) =
        store.update { it.copy(autoStart = enabled) }

    override suspend fun setAutoStartMode(mode: String) =
        store.update { it.copy(autoStartMode = mode) }
}

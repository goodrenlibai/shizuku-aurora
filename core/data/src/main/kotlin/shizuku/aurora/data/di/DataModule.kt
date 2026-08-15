package shizuku.aurora.data.di

import android.content.Context
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import shizuku.aurora.data.datastore.SettingsDataStore
import shizuku.aurora.data.db.AppRegistryDao
import shizuku.aurora.data.db.AuroraDatabase
import shizuku.aurora.data.db.LogDao
import shizuku.aurora.data.db.StartHistoryDao
import shizuku.aurora.data.repository.AuthorizedAppRepositoryImpl
import shizuku.aurora.data.repository.HiddenApiRepositoryImpl
import shizuku.aurora.data.repository.LogRepositoryImpl
import shizuku.aurora.data.repository.PairingRepositoryImpl
import shizuku.aurora.data.repository.PermissionRepositoryImpl
import shizuku.aurora.data.repository.PreferencesRepositoryImpl
import shizuku.aurora.data.repository.ShellRepositoryImpl
import shizuku.aurora.data.repository.ShizukuRepositoryImpl
import shizuku.aurora.data.repository.SystemRepositoryImpl
import shizuku.aurora.data.shizuku.ShizukuClient
import shizuku.aurora.domain.repository.AuthorizedAppRepository
import shizuku.aurora.domain.repository.HiddenApiRepository
import shizuku.aurora.domain.repository.LogRepository
import shizuku.aurora.domain.repository.PairingRepository
import shizuku.aurora.domain.repository.PermissionRepository
import shizuku.aurora.domain.repository.PreferencesRepository
import shizuku.aurora.domain.repository.ShellRepository
import shizuku.aurora.domain.repository.ShizukuRepository
import shizuku.aurora.domain.repository.SystemRepository
import javax.inject.Singleton

/**
 * 数据层依赖注入模块。
 * ------------------------------------------------------------------
 * 分两类：
 *   - [DbModule]：提供数据库、DAO、DataStore、客户端等可构造依赖；
 *   - [RepositoryModule]：把仓库接口绑定到实现（面向接口注入，便于测试替换）。
 */

@Module
@InstallIn(SingletonComponent::class)
object DbModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AuroraDatabase =
        AuroraDatabase.build(context)

    @Provides
    fun provideLogDao(db: AuroraDatabase): LogDao = db.logDao()

    @Provides
    fun provideStartHistoryDao(db: AuroraDatabase): StartHistoryDao = db.startHistoryDao()

    @Provides
    fun provideAppRegistryDao(db: AuroraDatabase): AppRegistryDao = db.appRegistryDao()

    @Provides
    @Singleton
    fun provideSettingsDataStore(@ApplicationContext context: Context): SettingsDataStore =
        SettingsDataStore(context)

    @Provides
    @Singleton
    fun provideShizukuClient(): ShizukuClient = ShizukuClient()
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    abstract fun bindShizukuRepository(impl: ShizukuRepositoryImpl): ShizukuRepository

    @Binds
    abstract fun bindAuthorizedAppRepository(impl: AuthorizedAppRepositoryImpl): AuthorizedAppRepository

    @Binds
    abstract fun bindPermissionRepository(impl: PermissionRepositoryImpl): PermissionRepository

    @Binds
    abstract fun bindHiddenApiRepository(impl: HiddenApiRepositoryImpl): HiddenApiRepository

    @Binds
    abstract fun bindShellRepository(impl: ShellRepositoryImpl): ShellRepository

    @Binds
    abstract fun bindLogRepository(impl: LogRepositoryImpl): LogRepository

    @Binds
    abstract fun bindSystemRepository(impl: SystemRepositoryImpl): SystemRepository

    @Binds
    abstract fun bindPreferencesRepository(impl: PreferencesRepositoryImpl): PreferencesRepository

    @Binds
    abstract fun bindPairingRepository(impl: PairingRepositoryImpl): PairingRepository
}

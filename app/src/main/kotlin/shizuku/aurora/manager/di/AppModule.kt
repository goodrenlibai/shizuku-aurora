package shizuku.aurora.manager.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
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
import javax.inject.Singleton

/**
 * 应用层依赖注入：聚合全部仓库，构造用例容器。
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideUseCases(
        shizuku: ShizukuRepository,
        apps: AuthorizedAppRepository,
        permissions: PermissionRepository,
        hiddenApi: HiddenApiRepository,
        shell: ShellRepository,
        logs: LogRepository,
        system: SystemRepository,
        preferences: PreferencesRepository,
        pairing: PairingRepository,
    ): AuroraUseCases = AuroraUseCases(
        shizuku = shizuku,
        apps = apps,
        permissions = permissions,
        hiddenApi = hiddenApi,
        shell = shell,
        logs = logs,
        system = system,
        preferences = preferences,
        pairing = pairing,
    )
}

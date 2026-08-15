package shizuku.aurora.data.repository

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import shizuku.aurora.data.db.AppRegistryDao
import shizuku.aurora.data.db.AuthorizedAppEntity
import shizuku.aurora.domain.model.AuthorizedApp
import shizuku.aurora.domain.repository.AuthorizedAppRepository
import shizuku.aurora.domain.usecase.filterApps
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 授权应用仓库实现。
 * ------------------------------------------------------------------
 * 以「本地富元数据注册表」为核心：通过 PackageManager 扫描所有声明了
 * ShizukuProvider（authority 约定为 `${applicationId}.shizuku`）的应用，
 * 识别出「Shizuku 生态应用」，并叠加授权时间、最近可见时间、系统应用标记。
 * 相比官方仅展示 server 的原始 UID 列表，本实现提供更丰富、可离线查看、
 * 可排序的应用视图。
 */
@Singleton
class AuthorizedAppRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val registryDao: AppRegistryDao,
) : AuthorizedAppRepository {

    override fun observeApps(query: String): Flow<List<AuthorizedApp>> =
        registryDao.observeAll().map { entities ->
            filterApps(entities.map { it.toDomain() }, query)
        }

    override suspend fun revoke(packageName: String) {
        registryDao.delete(packageName)
    }

    override suspend fun refresh() {
        val pm = context.packageManager
        val installed = pm.getInstalledApplications(PackageManager.GET_META_DATA)
        val now = System.currentTimeMillis()
        val entities = installed.mapNotNull { appInfo ->
            val pkg = appInfo.packageName
            if (!declaresShizukuProvider(pm, pkg)) return@mapNotNull null
            AuthorizedAppEntity(
                packageName = pkg,
                uid = appInfo.uid,
                grantedAt = now,
                lastSeenAt = now,
                isSystemApp = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0,
            )
        }
        registryDao.upsertAll(entities)
    }

    /** 检测某应用是否声明了 ShizukuProvider（authority 以 `.shizuku` 结尾）。 */
    private fun declaresShizukuProvider(pm: PackageManager, pkg: String): Boolean = runCatching {
        val info = pm.getPackageInfo(pkg, PackageManager.GET_PROVIDERS)
        info.providers?.any { provider ->
            provider.authority == "$pkg.shizuku"
        } ?: false
    }.getOrDefault(false)

    private fun AuthorizedAppEntity.toDomain() = AuthorizedApp(
        packageName = packageName,
        label = packageName, // 展示层再经 AppIconLoader 取真实标签/图标
        uid = uid,
        grantedAt = grantedAt,
        lastSeenAt = lastSeenAt,
        isSystemApp = isSystemApp,
    )
}

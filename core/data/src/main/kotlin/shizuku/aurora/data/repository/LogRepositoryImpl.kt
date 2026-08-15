package shizuku.aurora.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import shizuku.aurora.data.db.LogDao
import shizuku.aurora.data.db.LogEntity
import shizuku.aurora.domain.model.LogEntry
import shizuku.aurora.domain.model.LogLevel
import shizuku.aurora.domain.repository.LogRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 日志仓库实现（Room 持久化 + 全局追加入口）。
 * ------------------------------------------------------------------
 * 提供 [append] 供全应用（含 server 控制面回调）统一写入日志；
 * 查询按时间倒序，支持级别过滤与条数上限，避免无限增长。
 */
@Singleton
class LogRepositoryImpl @Inject constructor(
    private val logDao: LogDao,
) : LogRepository {

    override fun observeLogs(limit: Int): Flow<List<LogEntry>> =
        logDao.observeRecent(limit).map { list -> list.map { it.toDomain() } }

    override suspend fun clear() = logDao.clear()

    /** 全局日志追加入口（任何模块均可注入 LogRepository 调用）。 */
    suspend fun append(level: LogLevel, tag: String, message: String) {
        logDao.insert(
            LogEntity(
                timestamp = System.currentTimeMillis(),
                level = level.name,
                tag = tag,
                message = message,
            ),
        )
        // 滚动修剪：仅保留最近 5000 条，防止无界增长
        logDao.prune(System.currentTimeMillis() - RETENTION_MS)
    }

    companion object {
        private const val RETENTION_MS = 7L * 24 * 60 * 60 * 1000 // 7 天
        const val MAX_ROWS = 5000
    }
}

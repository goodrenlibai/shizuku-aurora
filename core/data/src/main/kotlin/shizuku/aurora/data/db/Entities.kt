package shizuku.aurora.data.db

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import shizuku.aurora.domain.model.LogLevel

/**
 * 持久化日志实体。
 * ------------------------------------------------------------------
 * 日志落库而非仅内存，支撑历史回溯与按时间/级别筛选；
 * 对 tag + timestamp 建索引以加速查询。
 */
@Entity(
    tableName = "logs",
    indices = [Index("timestamp"), Index("tag")],
)
data class LogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val level: String,
    val tag: String,
    val message: String,
) {
    fun toDomain() = shizuku.aurora.domain.model.LogEntry(
        id = id,
        timestamp = timestamp,
        level = LogLevel.valueOf(level),
        tag = tag,
        message = message,
    )

    companion object {
        fun fromDomain(entry: shizuku.aurora.domain.model.LogEntry) = LogEntity(
            id = entry.id,
            timestamp = entry.timestamp,
            level = entry.level.name,
            tag = entry.tag,
            message = entry.message,
        )
    }
}

/**
 * 服务启动历史记录：用于「最近一次以何种模式启动」的回溯与统计。
 */
@Entity(tableName = "start_history")
data class StartHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val mode: String,
    val startedAt: Long,
    val succeeded: Boolean,
)

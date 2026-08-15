package shizuku.aurora.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * 应用数据库。
 * ------------------------------------------------------------------
 * 单例由 Hilt 提供；仅持久化日志与启动历史两类低频写入数据，
 * 高频实时数据（服务状态、授权列表）走内存 Flow，避免无谓的 DB 压力。
 */
@Database(
    entities = [LogEntity::class, StartHistoryEntity::class, AuthorizedAppEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class AuroraDatabase : RoomDatabase() {
    abstract fun logDao(): LogDao
    abstract fun startHistoryDao(): StartHistoryDao
    abstract fun appRegistryDao(): AppRegistryDao

    companion object {
        const val NAME = "shizuku_aurora.db"

        fun build(context: Context): AuroraDatabase =
            Room.databaseBuilder(context, AuroraDatabase::class.java, NAME)
                .fallbackToDestructiveMigration(destructiveMigrationDropTables = true)
                .build()
    }
}

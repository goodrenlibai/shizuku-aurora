package shizuku.aurora.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface LogDao {
    @Insert
    suspend fun insert(entity: LogEntity)

    @Insert
    suspend fun insertAll(entities: List<LogEntity>)

    @Query("SELECT * FROM logs ORDER BY timestamp DESC LIMIT :limit")
    fun observeRecent(limit: Int): Flow<List<LogEntity>>

    @Query("SELECT * FROM logs WHERE level IN (:levels) ORDER BY timestamp DESC LIMIT :limit")
    fun observeByLevel(levels: List<String>, limit: Int): Flow<List<LogEntity>>

    @Query("DELETE FROM logs")
    suspend fun clear()

    @Query("DELETE FROM logs WHERE timestamp < :before")
    suspend fun prune(before: Long)

    @Query("SELECT COUNT(*) FROM logs")
    suspend fun count(): Int
}

@Dao
interface StartHistoryDao {
    @Insert
    suspend fun insert(entity: StartHistoryEntity)

    @Query("SELECT * FROM start_history ORDER BY startedAt DESC LIMIT 1")
    suspend fun latest(): StartHistoryEntity?

    @Query("DELETE FROM start_history WHERE startedAt < :before")
    suspend fun prune(before: Long)
}

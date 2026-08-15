package shizuku.aurora.data.db

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * 授权应用本地注册表。
 * ------------------------------------------------------------------
 * 官方 server 内部维护授权 UID 列表；本工程在其之上叠加一层富元数据的
 * 本地注册表（授权时间、最近可见时间、系统应用标记），用于：
 *   1. 更丰富的列表展示与排序；
 *   2. 离线可用（server 未运行时仍可查看历史授权记录）；
 *   3. 一键撤销时同步删除本地记录。
 */
@Entity(tableName = "authorized_apps")
data class AuthorizedAppEntity(
    @PrimaryKey val packageName: String,
    val uid: Int,
    val grantedAt: Long,
    val lastSeenAt: Long,
    val isSystemApp: Boolean,
)

@Dao
interface AppRegistryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: AuthorizedAppEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(entities: List<AuthorizedAppEntity>)

    @Query("SELECT * FROM authorized_apps ORDER BY lastSeenAt DESC")
    fun observeAll(): Flow<List<AuthorizedAppEntity>>

    @Query("DELETE FROM authorized_apps WHERE packageName = :packageName")
    suspend fun delete(packageName: String)

    @Query("DELETE FROM authorized_apps")
    suspend fun clear()
}

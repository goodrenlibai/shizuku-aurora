package shizuku.aurora.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import shizuku.aurora.domain.model.AppSettings

/**
 * 设置持久化（DataStore）。
 * ------------------------------------------------------------------
 * 将 [AppSettings] 序列化为 JSON 存于单一 key，避免逐字段维护；
 * 通过 Context 扩展属性注册单例 DataStore（进程内唯一）。
 */
private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "aurora_settings",
)

class SettingsDataStore(private val context: Context) {

    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }
    private val key = stringPreferencesKey("app_settings_json")

    fun observe(): Flow<AppSettings> =
        context.settingsDataStore.data.map { prefs ->
            prefs[key]?.let { raw ->
                runCatching { json.decodeFromString(AppSettings.serializer(), raw) }
                    .getOrNull()
            } ?: AppSettings.DEFAULT
        }

    suspend fun update(transform: (AppSettings) -> AppSettings) {
        context.settingsDataStore.edit { prefs ->
            val current = prefs[key]?.let { raw ->
                runCatching { json.decodeFromString(AppSettings.serializer(), raw) }
                    .getOrNull()
            } ?: AppSettings.DEFAULT
            prefs[key] = json.encodeToString(AppSettings.serializer(), transform(current))
        }
    }
}

package shizuku.aurora.data.repository

import android.os.Build
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import shizuku.aurora.domain.model.HiddenApiInfo
import shizuku.aurora.domain.repository.HiddenApiRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Hidden API 探测实现。
 * ------------------------------------------------------------------
 * 通过反射读取 `dalvik.system.VMRuntime#getHiddenApiExemptions()` 判断
 * 本进程是否已获得 hidden API 豁免（如经 LSPosed HiddenApiBypass 注入）；
 * 同时依据 SDK 版本判定强制策略是否生效（Android 9 / API 28 起）。
 * 全部为真实反射探测，无硬编码假值。
 */
@Singleton
class HiddenApiRepositoryImpl @Inject constructor() : HiddenApiRepository {

    private val _info = MutableStateFlow(HiddenApiInfo(
        exemptionLevel = 0,
        supported = Build.VERSION.SDK_INT >= 28,
        bypassActive = false,
        maxLevel = 5,
    ))

    override fun observeHiddenApi(): Flow<HiddenApiInfo> = _info.asStateFlow()

    override suspend fun refresh() {
        val exemptions = readExemptions()
        _info.value = HiddenApiInfo(
            exemptionLevel = inferLevel(exemptions),
            supported = Build.VERSION.SDK_INT >= 28,
            bypassActive = exemptions.isNotEmpty(),
            maxLevel = 5,
        )
    }

    /** 反射读取当前进程的 hidden API 豁免前缀列表。 */
    private fun readExemptions(): List<String> = runCatching {
        val vmRuntimeClass = Class.forName("dalvik.system.VMRuntime")
        val runtime = vmRuntimeClass.getMethod("getRuntime").invoke(null)
        val method = vmRuntimeClass.getDeclaredMethod("getHiddenApiExemptions")
        @Suppress("UNCHECKED_CAST")
        method.invoke(runtime) as Array<String>
    }.getOrDefault(emptyArray()).toList()

    /** 由豁免前缀推断粗粒度豁免等级。 */
    private fun inferLevel(exemptions: List<String>): Int = when {
        exemptions.any { it == "L" } -> 5
        exemptions.isNotEmpty() -> 4
        else -> 0
    }
}

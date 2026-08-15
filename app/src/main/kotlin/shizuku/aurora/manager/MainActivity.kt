package shizuku.aurora.manager

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.fragment.app.FragmentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import shizuku.aurora.design.theme.AuroraTheme
import shizuku.aurora.design.theme.AuroraThemeMode
import shizuku.aurora.domain.model.AppSettings
import shizuku.aurora.domain.usecase.AuroraUseCases
import shizuku.aurora.manager.navigation.AuroraNavHost
import shizuku.aurora.manager.ui.security.LockScreen
import javax.inject.Inject

/**
 * 主 Activity。
 * ------------------------------------------------------------------
 * 单 Activity 架构：所有页面经 Compose Navigation 呈现。
 * 顶层职责：读取设置 → 应用主题 → 门禁（生物识别锁）→ 导航。
 */
@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { AuroraRoot() }
    }
}

/** 顶层根：主题 + 安全门禁 + 导航。 */
@Composable
fun AuroraRoot() {
    val vm: RootViewModel = hiltViewModel()
    val settings by vm.settings.collectAsStateWithLifecycle()

    var unlocked by remember { mutableStateOf(!settings.lockEnabled) }

    AuroraTheme(
        mode = settings.themeMode.toThemeMode(),
        dynamicColor = settings.dynamicColor,
    ) {
        if (settings.lockEnabled && !unlocked) {
            LockScreen(onUnlocked = { unlocked = true })
        } else {
            AuroraNavHost()
        }
    }
}

/** 设置字符串 → 主题模式枚举映射。 */
fun String.toThemeMode(): AuroraThemeMode = when (this) {
    "LIGHT" -> AuroraThemeMode.LIGHT
    "DARK" -> AuroraThemeMode.DARK
    "AMOLED" -> AuroraThemeMode.AMOLED
    else -> AuroraThemeMode.SYSTEM
}

/** 顶层 ViewModel：仅暴露设置流，供主题与门禁消费。 */
@HiltViewModel
class RootViewModel @Inject constructor(
    useCases: AuroraUseCases,
) : ViewModel() {

    val settings = useCases.observeSettings()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppSettings.DEFAULT)
}

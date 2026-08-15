package shizuku.aurora.manager.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import shizuku.aurora.design.components.AuroraListItem
import shizuku.aurora.design.components.KeyValueRow
import shizuku.aurora.design.components.SectionCard
import shizuku.aurora.domain.model.AppSettings
import shizuku.aurora.domain.usecase.AuroraUseCases
import shizuku.aurora.manager.ui.common.ScreenContainer
import shizuku.aurora.manager.ui.common.ScreenHeader
import javax.inject.Inject

/**
 * 设置：外观（主题/动态取色）、安全（应用锁）、自动启动、关于。
 */
@Composable
fun SettingsScreen(padding: PaddingValues) {
    val vm: SettingsViewModel = hiltViewModel()
    val settings by vm.settings.collectAsStateWithLifecycle()

    ScreenContainer(padding) {
        ScreenHeader(title = "Settings", subtitle = "Appearance, security & automation")

        SectionCard(title = "Appearance") {
            val modes = listOf(
                "SYSTEM" to "Follow system",
                "LIGHT" to "Light",
                "DARK" to "Dark",
                "AMOLED" to "AMOLED black",
            )
            modes.forEach { (value, label) ->
                AuroraListItem(
                    title = label,
                    onClick = { vm.setThemeMode(value) },
                    trailing = {
                        RadioButton(
                            selected = settings.themeMode == value,
                            onClick = { vm.setThemeMode(value) },
                        )
                    },
                )
            }
            AuroraListItem(
                title = "Dynamic color (Material You)",
                subtitle = "Derive colors from wallpaper on Android 12+",
                trailing = {
                    Switch(
                        checked = settings.dynamicColor,
                        onCheckedChange = { vm.setDynamicColor(it) },
                    )
                },
            )
        }

        SectionCard(title = "Security") {
            AuroraListItem(
                title = "App lock",
                subtitle = "Require biometric authentication to open",
                trailing = {
                    Switch(
                        checked = settings.lockEnabled,
                        onCheckedChange = { vm.setLockEnabled(it) },
                    )
                },
            )
        }

        SectionCard(title = "Automation") {
            AuroraListItem(
                title = "Start on boot",
                subtitle = "Automatically start the server after reboot",
                trailing = {
                    Switch(
                        checked = settings.autoStart,
                        onCheckedChange = { vm.setAutoStart(it) },
                    )
                },
            )
            AuroraListItem(
                title = "Start mode",
                onClick = { vm.setAutoStartMode(if (settings.autoStartMode == "ROOT") "ADB" else "ROOT") },
                trailing = {
                    RadioButton(
                        selected = settings.autoStartMode == "ROOT",
                        onClick = {
                            vm.setAutoStartMode(if (settings.autoStartMode == "ROOT") "ADB" else "ROOT")
                        },
                    )
                },
                subtitle = if (settings.autoStartMode == "ROOT") {
                    "Root identity (uid 0)"
                } else {
                    "ADB identity (uid 2000)"
                },
            )
        }

        SectionCard(title = "About") {
            KeyValueRow("Version", "14.0.0-aurora")
            Spacer(Modifier.height(8.dp))
            KeyValueRow("Engine", "Shizuku server (official)")
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Based on Shizuku by RikkaApps, licensed under Apache 2.0.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val useCases: AuroraUseCases,
) : ViewModel() {

    val settings = useCases.observeSettings()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppSettings.DEFAULT)

    fun setThemeMode(mode: String) = viewModelScope.launch {
        useCases.updateThemeMode(mode)
    }

    fun setDynamicColor(enabled: Boolean) = viewModelScope.launch {
        useCases.setDynamicColor(enabled)
    }

    fun setLockEnabled(enabled: Boolean) = viewModelScope.launch {
        useCases.setLockEnabled(enabled)
    }

    fun setAutoStart(enabled: Boolean) = viewModelScope.launch {
        useCases.setAutoStart(enabled)
    }

    fun setAutoStartMode(mode: String) = viewModelScope.launch {
        useCases.setAutoStartMode(mode)
    }
}

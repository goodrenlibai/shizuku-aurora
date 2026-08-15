package shizuku.aurora.manager.ui.permissions

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import shizuku.aurora.design.components.AuroraListItem
import shizuku.aurora.design.components.StatusPill
import shizuku.aurora.design.theme.StatusTone
import shizuku.aurora.domain.model.PermissionInfo
import shizuku.aurora.domain.usecase.AuroraUseCases
import shizuku.aurora.manager.ui.common.ScreenContainerStatic
import shizuku.aurora.manager.ui.common.ScreenHeader
import javax.inject.Inject

/**
 * 权限探查：展示 server（adb/root 身份）可代理的关键系统权限。
 * ------------------------------------------------------------------
 * granted 状态按运行身份判定（root 全量 / adb 白名单子集），
 * 帮助开发者快速判断当前连接能做什么。
 */
@Composable
fun PermissionsScreen(padding: PaddingValues) {
    val vm: PermissionsViewModel = hiltViewModel()
    val permissions by vm.permissions.collectAsStateWithLifecycle()

    ScreenContainerStatic(padding) {
        ScreenHeader(
            title = "Permissions",
            subtitle = "Capabilities of the current server identity",
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.fillMaxWidth().weight(1f),
        ) {
            items(permissions) { permission ->
                AuroraListItem(
                    title = permission.name.removePrefix("android.permission."),
                    subtitle = "${permission.group} · ${permission.protectionLevel}\n${permission.description}",
                    onClick = null,
                    trailing = {
                        StatusPill(
                            tone = if (permission.granted) StatusTone.SUCCESS else StatusTone.NEUTRAL,
                            label = if (permission.granted) "Granted" else "No",
                        )
                    },
                )
            }
        }
    }
}

@HiltViewModel
class PermissionsViewModel @Inject constructor(
    private val useCases: AuroraUseCases,
) : ViewModel() {

    private val _permissions = MutableStateFlow<List<PermissionInfo>>(emptyList())
    val permissions = _permissions.asStateFlow()

    init {
        viewModelScope.launch {
            _permissions.value = useCases.listPermissions()
        }
    }
}

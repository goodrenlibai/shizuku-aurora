package shizuku.aurora.manager.ui.apps

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import shizuku.aurora.design.components.AuroraListItem
import shizuku.aurora.design.components.EmptyState
import shizuku.aurora.domain.model.AuthorizedApp
import shizuku.aurora.domain.usecase.AuroraUseCases
import shizuku.aurora.manager.ui.common.ScreenContainer
import shizuku.aurora.manager.ui.common.ScreenContainerStatic
import shizuku.aurora.manager.ui.common.ScreenHeader
import javax.inject.Inject

/**
 * 授权应用管理：搜索 + 扫描 + 一键撤销。
 * ------------------------------------------------------------------
 * 展示 Aurora 维护的 Shizuku 生态应用注册表（声明 ShizukuProvider 的应用），
 * 支持搜索、刷新扫描、单项撤销。
 */
@Composable
fun AppsScreen(padding: PaddingValues) {
    val vm: AppsViewModel = hiltViewModel()
    val apps by vm.apps.collectAsStateWithLifecycle()
    var query by remember { mutableStateOf("") }

    vm.updateQuery(query)

    ScreenContainerStatic(padding) {
        ScreenHeader(
            title = "Apps",
            subtitle = "Shizuku-capable applications",
        )

        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            trailingIcon = {
                IconButton(onClick = { vm.refresh() }) {
                    Icon(Icons.Filled.Refresh, contentDescription = "Refresh")
                }
            },
            placeholder = { Text("Search apps") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(8.dp))

        if (apps.isEmpty()) {
            EmptyState(
                icon = Icons.Filled.Search,
                title = "No apps found",
                description = "Tap refresh to scan installed apps that integrate Shizuku.",
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.fillMaxWidth().weight(1f),
            ) {
                items(apps, key = { it.key }) { app ->
                    AuthorizedAppRow(app, onRevoke = { vm.revoke(app.packageName) })
                }
            }
        }
    }
}

@Composable
private fun AuthorizedAppRow(app: AuthorizedApp, onRevoke: () -> Unit) {
    val context = LocalContext.current
    AuroraListItem(
        title = remember(app.packageName) { loadLabel(context, app.packageName) },
        subtitle = app.packageName + if (app.isSystemApp) " · system" else "",
        leadingIcon = null,
        onClick = null,
        trailing = {
            IconButton(onClick = onRevoke) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = "Revoke",
                    tint = MaterialTheme.colorScheme.error,
                )
            }
        },
    )
}

private fun loadLabel(context: Context, packageName: String): String = runCatching {
    val pm = context.packageManager
    val info = pm.getApplicationInfo(packageName, 0)
    pm.getApplicationLabel(info).toString()
}.getOrDefault(packageName)

@HiltViewModel
class AppsViewModel @Inject constructor(
    private val useCases: AuroraUseCases,
) : ViewModel() {

    private val query = MutableStateFlow("")

    val apps = query
        .flatMapLatest { useCases.observeAuthorizedApps(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun updateQuery(q: String) {
        query.value = q
    }

    fun revoke(packageName: String) = viewModelScope.launch {
        useCases.revokeApp(packageName)
    }

    fun refresh() = viewModelScope.launch { useCases.refreshApps() }
}

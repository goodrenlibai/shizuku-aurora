package shizuku.aurora.manager.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.PermDataSetting
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import shizuku.aurora.design.components.AuroraListItem
import shizuku.aurora.design.components.MetricCard
import shizuku.aurora.design.components.SectionCard
import shizuku.aurora.design.components.StatusPill
import shizuku.aurora.design.theme.AuroraDimensions
import shizuku.aurora.design.theme.StatusTone
import shizuku.aurora.domain.model.ConnectionMode
import shizuku.aurora.domain.model.ShizukuStatus
import shizuku.aurora.domain.model.SystemStats
import shizuku.aurora.domain.usecase.AuroraUseCases
import shizuku.aurora.manager.navigation.Routes
import shizuku.aurora.manager.ui.common.ScreenContainer
import shizuku.aurora.manager.ui.common.ScreenHeader
import javax.inject.Inject

/**
 * 首页仪表盘：服务状态总览 + 实时指标 + 快捷入口。
 * ------------------------------------------------------------------
 * 这是应用的门面：一个视口内呈现「连接状态、版本、运行身份、权限、
 * CPU/内存/进程实时指标」以及全部功能模块的入口。
 */
@Composable
fun HomeScreen(padding: PaddingValues, onNavigate: (String) -> Unit) {
    val vm: HomeViewModel = hiltViewModel()
    val status by vm.status.collectAsStateWithLifecycle()
    val stats by vm.stats.collectAsStateWithLifecycle()

    ScreenContainer(padding) {
        ScreenHeader(
            title = "Shizuku Aurora",
            subtitle = "System API bridge · Control center",
        )

        StatusCard(
            status = status,
            onStartRoot = { vm.start(ConnectionMode.ROOT) },
            onStartAdb = { onNavigate(Routes.PAIRING) },
            onStop = { vm.stop() },
            onRequestPermission = { vm.requestPermission() },
        )

        MetricsRow(stats)

        QuickActions(onNavigate)
    }
}

@Composable
private fun StatusCard(
    status: ShizukuStatus,
    onStartRoot: () -> Unit,
    onStartAdb: () -> Unit,
    onStop: () -> Unit,
    onRequestPermission: () -> Unit,
) {
    SectionCard(title = "Service status") {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth(),
        ) {
            StatusPill(
                tone = if (status.running) StatusTone.SUCCESS else StatusTone.NEUTRAL,
                label = if (status.running) "RUNNING" else "STOPPED",
            )
            if (status.running) {
                Text(
                    text = "v${status.version}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Spacer(Modifier.height(16.dp))

        if (status.running) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                InfoLine("Identity", status.mode.name)
                InfoLine("Server UID", status.serverUid.toString())
                InfoLine(
                    "Permission",
                    if (status.permissionGranted) "Granted" else "Not granted",
                )
            }
        } else {
            Text(
                text = "Start the Shizuku server to let apps use system APIs with adb/root privileges.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(Modifier.height(16.dp))

        if (status.running) {
            if (!status.permissionGranted) {
                Button(onClick = onRequestPermission, modifier = Modifier.fillMaxWidth()) {
                    Text("Request Shizuku permission")
                }
            }
            OutlinedButton(onClick = onStop, modifier = Modifier.fillMaxWidth()) {
                Text("Stop server")
            }
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onStartRoot, modifier = Modifier.weight(1f)) {
                    Text("Start (ROOT)")
                }
                OutlinedButton(onClick = onStartAdb, modifier = Modifier.weight(1f)) {
                    Text("Start (ADB)")
                }
            }
        }
    }
}

@Composable
private fun InfoLine(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun MetricsRow(stats: SystemStats) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        MetricCard(
            label = "CPU",
            value = "%.1f".format(stats.cpuUsagePercent),
            unit = "%",
            modifier = Modifier.weight(1f),
        )
        MetricCard(
            label = "Memory",
            value = "${(stats.memTotalBytes - stats.memAvailableBytes) / (1024 * 1024)}",
            unit = "MB used",
            modifier = Modifier.weight(1f),
        )
        MetricCard(
            label = "Processes",
            value = stats.processCount.toString(),
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun QuickActions(onNavigate: (String) -> Unit) {
    SectionCard(title = "Tools") {
        QuickAction("Wireless pairing", "Android 11+ wireless debugging", Icons.Filled.Bolt) {
            onNavigate(Routes.PAIRING)
        }
        QuickAction("Permissions", "Inspect adb/root capabilities", Icons.Filled.PermDataSetting) {
            onNavigate(Routes.PERMISSIONS)
        }
        QuickAction("Hidden API", "Exemption & bypass status", Icons.Filled.Key) {
            onNavigate(Routes.HIDDEN_API)
        }
        QuickAction("System monitor", "Live CPU / memory / processes", Icons.Filled.Dashboard) {
            onNavigate(Routes.MONITOR)
        }
        QuickAction("Log viewer", "Persisted event & error log", Icons.Filled.ReceiptLong) {
            onNavigate(Routes.LOGS)
        }
        QuickAction("Onboarding", "Replay the first-run guide", Icons.Filled.Shield) {
            onNavigate(Routes.ONBOARDING)
        }
    }
}

@Composable
private fun QuickAction(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit,
) {
    AuroraListItem(
        title = title,
        subtitle = subtitle,
        leadingIcon = icon,
        onClick = onClick,
        trailing = {
            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
    )
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    useCases: AuroraUseCases,
) : ViewModel() {

    val status = useCases.observeStatus()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ShizukuStatus.IDLE)

    val stats = useCases.observeSystemStats()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SystemStats(0f, 0, 0, 0, 0))

    fun start(mode: ConnectionMode) = viewModelScope.launch { useCases.startService(mode) }

    fun stop() = viewModelScope.launch { useCases.stopService() }

    fun requestPermission() = viewModelScope.launch { useCases.requestPermission() }
}

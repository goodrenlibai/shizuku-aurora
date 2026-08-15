package shizuku.aurora.manager.ui.monitor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import shizuku.aurora.design.components.KeyValueRow
import shizuku.aurora.design.components.MetricCard
import shizuku.aurora.design.components.SectionCard
import shizuku.aurora.domain.model.DeviceInfo
import shizuku.aurora.domain.model.SystemStats
import shizuku.aurora.domain.usecase.AuroraUseCases
import shizuku.aurora.manager.ui.common.ScreenContainer
import shizuku.aurora.manager.ui.common.ScreenHeader
import javax.inject.Inject

/**
 * 系统监控：实时 CPU/内存/进程 + 设备静态信息。
 */
@Composable
fun MonitorScreen(padding: PaddingValues) {
    val vm: MonitorViewModel = hiltViewModel()
    val stats by vm.stats.collectAsStateWithLifecycle()

    val deviceInfo by produceState<DeviceInfo?>(initialValue = null) {
        value = vm.loadDeviceInfo()
    }

    ScreenContainer(padding) {
        ScreenHeader(
            title = "System monitor",
            subtitle = "Live resource usage",
        )

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
                unit = "MB",
                modifier = Modifier.weight(1f),
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            MetricCard(
                label = "Available",
                value = "${stats.memAvailableBytes / (1024 * 1024)}",
                unit = "MB",
                modifier = Modifier.weight(1f),
            )
            MetricCard(
                label = "Processes",
                value = stats.processCount.toString(),
                modifier = Modifier.weight(1f),
            )
        }

        deviceInfo?.let { info ->
            SectionCard(title = "Device") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    KeyValueRow("Manufacturer", info.manufacturer)
                    KeyValueRow("Model", info.model)
                    KeyValueRow("Android", "${info.androidVersion} (API ${info.apiLevel})")
                    KeyValueRow("ABI", info.abi)
                    KeyValueRow("Root available", if (info.hasRoot) "Yes" else "No")
                    KeyValueRow(
                        "Wireless debugging",
                        if (info.wirelessDebugAvailable) "Available" else "Unavailable",
                    )
                }
            }
        }
    }
}

@HiltViewModel
class MonitorViewModel @Inject constructor(
    private val useCases: AuroraUseCases,
) : ViewModel() {

    val stats = useCases.observeSystemStats()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SystemStats(0f, 0, 0, 0, 0))

    suspend fun loadDeviceInfo(): DeviceInfo = useCases.getDeviceInfo()
}

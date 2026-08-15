package shizuku.aurora.manager.ui.logs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import shizuku.aurora.design.components.EmptyState
import shizuku.aurora.domain.model.LogEntry
import shizuku.aurora.domain.model.LogLevel
import shizuku.aurora.domain.usecase.AuroraUseCases
import shizuku.aurora.manager.ui.common.ScreenContainerStatic
import shizuku.aurora.manager.ui.common.ScreenHeader
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

/**
 * 日志查看器：展示持久化的事件/错误日志，支持一键清空。
 */
@Composable
fun LogsScreen(padding: PaddingValues) {
    val vm: LogsViewModel = hiltViewModel()
    val logs by vm.logs.collectAsStateWithLifecycle()

    ScreenContainerStatic(padding) {
        ScreenHeader(
            title = "Logs",
            subtitle = "Persisted events",
        )

        IconButton(
            onClick = { vm.clear() },
            modifier = Modifier.align(androidx.compose.ui.Alignment.End),
        ) {
            Icon(
                imageVector = Icons.Filled.DeleteSweep,
                contentDescription = "Clear logs",
                tint = MaterialTheme.colorScheme.error,
            )
        }

        if (logs.isEmpty()) {
            EmptyState(
                icon = Icons.Filled.DeleteSweep,
                title = "No logs",
                description = "Events will appear here as you use the app.",
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.fillMaxWidth().weight(1f),
            ) {
                items(logs, key = { it.id }) { entry ->
                    LogRow(entry)
                }
            }
        }
    }
}

@Composable
private fun LogRow(entry: LogEntry) {
    val time = rememberFormatter().format(Date(entry.timestamp))
    Column(Modifier.fillMaxWidth()) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = entry.tag,
                style = MaterialTheme.typography.labelMedium,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = time,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Text(
            text = entry.message,
            style = MaterialTheme.typography.bodySmall,
            fontFamily = FontFamily.Monospace,
            color = when (entry.level) {
                LogLevel.ERROR -> MaterialTheme.colorScheme.error
                LogLevel.WARN -> MaterialTheme.colorScheme.onSurfaceVariant
                else -> MaterialTheme.colorScheme.onSurface
            },
        )
        Spacer(Modifier.height(2.dp))
    }
}

@Composable
private fun rememberFormatter() = remember {
    SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault())
}

@HiltViewModel
class LogsViewModel @Inject constructor(
    private val useCases: AuroraUseCases,
) : ViewModel() {

    val logs = useCases.observeLogs(500)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun clear() = viewModelScope.launch { useCases.clearLogs() }
}

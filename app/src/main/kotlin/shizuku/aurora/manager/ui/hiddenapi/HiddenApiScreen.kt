package shizuku.aurora.manager.ui.hiddenapi

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import shizuku.aurora.design.components.KeyValueRow
import shizuku.aurora.design.components.SectionCard
import shizuku.aurora.design.components.StatusPill
import shizuku.aurora.design.components.ToneBanner
import shizuku.aurora.design.theme.StatusTone
import shizuku.aurora.domain.model.HiddenApiInfo
import shizuku.aurora.domain.usecase.AuroraUseCases
import shizuku.aurora.manager.ui.common.ScreenContainer
import shizuku.aurora.manager.ui.common.ScreenHeader
import javax.inject.Inject

/**
 * Hidden API 探查：反射检测当前进程的 hidden API 豁免状态。
 */
@Composable
fun HiddenApiScreen(padding: PaddingValues) {
    val vm: HiddenApiViewModel = hiltViewModel()
    val info by vm.info.collectAsStateWithLifecycle()

    ScreenContainer(padding) {
        ScreenHeader(
            title = "Hidden API",
            subtitle = "Exemption & bypass status",
        )

        if (!info.supported) {
            ToneBanner(
                tone = StatusTone.INFO,
                message = "Hidden API enforcement only applies on Android 9 (API 28) and later.",
            )
        }

        SectionCard(title = "Status") {
            KeyValueRow("Enforcement active", if (info.supported) "Yes" else "No")
            Spacer(Modifier.height(8.dp))
            KeyValueRow("Exemption level", "Level ${info.exemptionLevel} / ${info.maxLevel}")
            Spacer(Modifier.height(8.dp))
            KeyValueRow(
                "Bypass active",
                if (info.bypassActive) "Yes" else "No",
            )
            Spacer(Modifier.height(12.dp))
            StatusPill(
                tone = if (info.bypassActive) StatusTone.SUCCESS else StatusTone.WARNING,
                label = if (info.bypassActive) "EXEMPT" else "RESTRICTED",
            )
        }

        SectionCard(title = "About") {
            Text(
                text = "Shizuku itself relies on hidden API bypass (e.g. LSPosed AndroidHiddenApiBypass) " +
                    "to bridge system APIs. This panel reflects the current process exemption state " +
                    "detected via reflection.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Button(
            onClick = { vm.refresh() },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Icon(Icons.Filled.Refresh, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Re-detect")
        }
    }
}

@HiltViewModel
class HiddenApiViewModel @Inject constructor(
    private val useCases: AuroraUseCases,
) : ViewModel() {

    val info = useCases.observeHiddenApi()
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            HiddenApiInfo(0, false, false, 5),
        )

    fun refresh() = viewModelScope.launch { useCases.refreshHiddenApi() }
}

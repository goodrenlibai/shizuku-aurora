package shizuku.aurora.manager.ui.pairing

import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import shizuku.aurora.design.components.SectionCard
import shizuku.aurora.design.components.StatusPill
import shizuku.aurora.design.components.ToneBanner
import shizuku.aurora.design.theme.StatusTone
import shizuku.aurora.domain.model.PairingSession
import shizuku.aurora.domain.model.PairingState
import shizuku.aurora.domain.usecase.AuroraUseCases
import shizuku.aurora.manager.ui.common.ScreenContainer
import shizuku.aurora.manager.ui.common.ScreenHeader
import javax.inject.Inject

/**
 * 无线调试配对（Android 11+）。
 * ------------------------------------------------------------------
 * 完整流程：mDNS 发现 `_adb-tls-pairing` 服务 → 输入 6 位配对码 →
 * TLS 配对握手 → 凭据落盘 → 以 adb 身份启动 server。
 * 不支持无线调试的系统则引导走 root 或 USB 手动路径。
 */
@Composable
fun PairingScreen(padding: PaddingValues) {
    val vm: PairingViewModel = hiltViewModel()
    val session by vm.session.collectAsStateWithLifecycle()

    var code by remember { mutableStateOf("") }
    val supportsWireless = Build.VERSION.SDK_INT >= 30

    ScreenContainer(padding) {
        ScreenHeader(
            title = "Wireless pairing",
            subtitle = "Start the server with wireless debugging (adb)",
        )

        if (!supportsWireless) {
            ToneBanner(
                tone = StatusTone.WARNING,
                message = "Wireless debugging requires Android 11 (API 30) or later. Use ROOT or USB instead.",
            )
        }

        SectionCard(title = "Pairing") {
            StatusPill(
                tone = when (session.state) {
                    PairingState.PAIRED, PairingState.CONNECTED -> StatusTone.SUCCESS
                    PairingState.FAILED -> StatusTone.ERROR
                    PairingState.PAIRING -> StatusTone.INFO
                    PairingState.IDLE -> StatusTone.NEUTRAL
                },
                label = session.state.name,
            )
            Spacer(Modifier.height(12.dp))

            when (session.state) {
                PairingState.IDLE -> {
                    Button(
                        onClick = { vm.startPairing() },
                        enabled = supportsWireless,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("Discover device")
                    }
                }
                PairingState.PAIRING -> {
                    Text(
                        text = "Enter the 6-digit pairing code shown in Developer options → Wireless debugging.",
                        style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                        color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = code,
                        onValueChange = { if (it.length <= 6) code = it.filter(Char::isDigit) },
                        label = { Text("Pairing code") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = { vm.confirm(code) },
                        enabled = code.length == 6,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("Pair")
                    }
                }
                PairingState.PAIRED -> {
                    Button(
                        onClick = { vm.connect() },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("Start server via wireless adb")
                    }
                }
                PairingState.CONNECTED -> {
                    ToneBanner(
                        tone = StatusTone.SUCCESS,
                        message = "Paired and connected. Server start initiated.",
                    )
                }
                PairingState.FAILED -> {
                    ToneBanner(
                        tone = StatusTone.ERROR,
                        message = "Pairing failed. Check the code and try again.",
                    )
                    Spacer(Modifier.height(12.dp))
                    OutlinedButton(
                        onClick = { vm.startPairing() },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("Retry")
                    }
                }
            }
        }

        SectionCard(title = "Manual (USB)") {
            Text(
                text = "With USB debugging enabled, run on your computer:\n" +
                    "1. adb shell pm path shizuku.aurora.manager\n" +
                    "2. adb shell app_process -Djava.class.path=<apk_path> " +
                    "/ shizuku.aurora.server.AuroraService --start",
                style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@HiltViewModel
class PairingViewModel @Inject constructor(
    private val useCases: AuroraUseCases,
) : ViewModel() {

    val session = useCases.observePairingSession()
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            PairingSession("", 0, "", 0, PairingState.IDLE),
        )

    fun startPairing() = viewModelScope.launch { useCases.startPairing() }

    fun confirm(code: String) = viewModelScope.launch { useCases.confirmPairing(code) }

    fun connect() = viewModelScope.launch {
        useCases.connectAfterPairing(session.value)
    }
}

package shizuku.aurora.design.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import shizuku.aurora.design.theme.AuroraDimensions
import shizuku.aurora.design.theme.StatusTone
import shizuku.aurora.design.theme.statusToneColors

/**
 * 品牌组件库 · AuroraComponents
 * ------------------------------------------------------------------
 * 全部 UI 复用同一套组件，保证「秩序、克制、精确」的视觉语言：
 *   StatusPill    —— 状态胶囊徽章（连接状态/权限状态）
 *   SectionCard   —— 统一容器卡片（标题 + 内容）
 *   MetricCard    —— 指标卡（大数字 + 标签 + 趋势色）
 *   AuroraListItem—— 标准列表项（图标 + 主副文本 + 尾随组件）
 *   EmptyState    —— 空态占位（图标 + 说明 + 可选动作）
 *   KeyValueRow   —— 键值信息行（详情页通用）
 */

/** 状态胶囊徽章：实心小圆点 + 文案，颜色随语义自动映射。 */
@Composable
fun StatusPill(
    tone: StatusTone,
    label: String,
    modifier: Modifier = Modifier,
) {
    val (container, onContainer) = statusToneColors(tone)
    val dotColor = when (tone) {
        StatusTone.SUCCESS -> Color(0xFF2E7D32)
        StatusTone.WARNING -> Color(0xFF8A5300)
        StatusTone.ERROR -> Color(0xFFBA1A1A)
        StatusTone.INFO -> Color(0xFF00639B)
        StatusTone.NEUTRAL -> Color(0xFF757680)
    }
    Surface(
        color = container,
        contentColor = onContainer,
        shape = RoundedCornerShape(50),
        modifier = modifier,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
        ) {
            Box(
                Modifier
                    .size(8.dp)
                    .background(dotColor, CircleShape),
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

/** 统一容器卡片：带可选标题与内边距。 */
@Composable
fun SectionCard(
    title: String? = null,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = AuroraDimensions.elevationResting,
        ),
    ) {
        Column(
            modifier = Modifier.padding(AuroraDimensions.cardPadding),
        ) {
            if (title != null) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(Modifier.height(12.dp))
            }
            content()
        }
    }
}

/** 指标卡：大数字指标，含标签、单位与可选趋势色块。 */
@Composable
fun MetricCard(
    label: String,
    value: String,
    unit: String = "",
    accent: Color = MaterialTheme.colorScheme.primary,
    modifier: Modifier = Modifier,
) {
    val animated by animateFloatAsState(1f, label = "metricAlpha")
    Card(
        modifier = modifier.alpha(animated),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
        ),
        shape = RoundedCornerShape(AuroraDimensions.grid4),
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(10.dp)
                        .background(accent, CircleShape),
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                if (unit.isNotBlank()) {
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = unit,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 3.dp),
                    )
                }
            }
        }
    }
}

/** 标准列表项：前置图标（可选）+ 主/副文本 + 尾随内容。 */
@Composable
fun AuroraListItem(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    leadingIcon: ImageVector? = null,
    leadingTint: Color = MaterialTheme.colorScheme.primary,
    trailing: @Composable () -> Unit = {},
    onClick: (() -> Unit)? = null,
) {
    val itemModifier = if (onClick != null) {
        modifier.clickable(onClick = onClick)
    } else {
        modifier
    }
    androidx.compose.material3.ListItem(
        headlineContent = {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
        },
        supportingContent = subtitle?.let {
            {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
        leadingContent = leadingIcon?.let {
            {
                Icon(
                    imageVector = it,
                    contentDescription = null,
                    tint = leadingTint,
                )
            }
        },
        trailingContent = trailing,
        modifier = itemModifier,
        colors = androidx.compose.material3.ListItemDefaults.colors(
            containerColor = Color.Transparent,
        ),
    )
}

/** 空态占位：图标 + 标题 + 说明 + 可选动作。 */
@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    action: (@Composable () -> Unit)? = null,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            Modifier
                .size(72.dp)
                .background(
                    MaterialTheme.colorScheme.surfaceContainerHigh,
                    CircleShape,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(32.dp),
            )
        }
        Spacer(Modifier.height(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (action != null) {
            Spacer(Modifier.height(20.dp))
            action()
        }
    }
}

/** 键值信息行：详情页通用，左侧标签 + 右侧取值。 */
@Composable
fun KeyValueRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    monospace: Boolean = false,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
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
            fontWeight = if (monospace) FontWeight.Normal else FontWeight.Medium,
            fontFamily = if (monospace) androidx.compose.ui.text.font.FontFamily.Monospace else null,
        )
    }
}

/** 图标语义封装：便于在语义提示中统一使用（纯函数，非 @Composable）。 */
fun toneIcon(tone: StatusTone): ImageVector = when (tone) {
    StatusTone.SUCCESS -> Icons.Filled.CheckCircle
    StatusTone.WARNING -> Icons.Filled.Warning
    StatusTone.ERROR -> Icons.Filled.Error
    StatusTone.INFO -> Icons.Filled.Info
    StatusTone.NEUTRAL -> Icons.Filled.Info
}

/** 语义提示横幅：带图标 + 文案 + 语义色底。 */
@Composable
fun ToneBanner(
    tone: StatusTone,
    message: String,
    modifier: Modifier = Modifier,
) {
    val (container, onContainer) = statusToneColors(tone)
    val accent by animateColorAsState(
        when (tone) {
            StatusTone.SUCCESS -> Color(0xFF2E7D32)
            StatusTone.WARNING -> Color(0xFF8A5300)
            StatusTone.ERROR -> Color(0xFFBA1A1A)
            StatusTone.INFO -> Color(0xFF00639B)
            StatusTone.NEUTRAL -> Color(0xFF757680)
        },
        label = "bannerAccent",
    )
    Surface(
        color = container,
        contentColor = onContainer,
        shape = RoundedCornerShape(AuroraDimensions.grid3),
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(14.dp),
        ) {
            Icon(
                imageVector = toneIcon(tone),
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(20.dp),
            )
            Spacer(Modifier.width(10.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

/** 分隔线：统一视觉节奏。 */
@Composable
fun AuroraDivider(modifier: Modifier = Modifier, thickness: Dp = 1.dp) {
    Box(
        modifier
            .fillMaxWidth()
            .height(thickness)
            .background(MaterialTheme.colorScheme.outlineVariant),
    )
}

/** 带边框的细分隔容器（用于代码/日志块）。 */
@Composable
fun OutlinedContainer(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = RoundedCornerShape(AuroraDimensions.grid3),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant,
        ),
        modifier = modifier,
    ) {
        Column(Modifier.padding(14.dp), content = content)
    }
}

package com.limb.diagnostics.ui.screens.interactive

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.limb.diagnostics.model.DiagnosticMetric
import com.limb.diagnostics.model.DiagnosticStatus
import com.limb.diagnostics.model.DiagnosticTest
import com.limb.diagnostics.ui.theme.LimbAppTheme
import com.limb.diagnostics.ui.theme.LimbButton
import com.limb.diagnostics.ui.theme.LimbCard
import com.limb.diagnostics.ui.theme.LimbStatusChip
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun TelemetryDetailScreen(
    test: DiagnosticTest,
    onClose: () -> Unit,
    onRunTest: (() -> Unit)? = null,
    onMarkStatus: ((DiagnosticStatus, String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    var isRunningCheck by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(LimbAppTheme.colors.background)
            .padding(20.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                Text(
                    text = test.name,
                    style = MaterialTheme.typography.titleLarge,
                    color = LimbAppTheme.colors.textPrimary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = test.category.title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = LimbAppTheme.colors.textSecondary
                )
            }

            IconButton(onClick = onClose) {
                Icon(
                    imageVector = Icons.Outlined.Close,
                    contentDescription = "Close",
                    tint = LimbAppTheme.colors.textSecondary
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                LimbCard(modifier = Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "DIAGNOSTIC STATUS",
                                style = MaterialTheme.typography.labelMedium,
                                color = LimbAppTheme.colors.textTertiary,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            LimbStatusChip(status = test.status)
                        }

                        Text(
                            text = if (test.resultSummary.isNotEmpty()) test.resultSummary else test.description,
                            style = MaterialTheme.typography.bodyLarge,
                            color = LimbAppTheme.colors.textPrimary
                        )
                    }
                }
            }

            if (test.metrics.isNotEmpty()) {
                item {
                    LimbCard(modifier = Modifier.fillMaxWidth()) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(
                                text = "HARDWARE METRICS",
                                style = MaterialTheme.typography.labelMedium,
                                color = LimbAppTheme.colors.textTertiary,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )

                            test.metrics.forEachIndexed { idx, metric ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = metric.label,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = LimbAppTheme.colors.textSecondary
                                    )
                                    Text(
                                        text = "${metric.value} ${metric.unit}".trim(),
                                        style = MaterialTheme.typography.titleMedium,
                                        color = LimbAppTheme.colors.textPrimary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                if (idx < test.metrics.size - 1) {
                                    HorizontalDivider(color = LimbAppTheme.colors.border.copy(alpha = 0.5f), thickness = 0.5.dp)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Action Buttons
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            if (onRunTest != null) {
                LimbButton(
                    text = if (isRunningCheck) "Running Diagnostic..." else "Run Diagnostic Check",
                    onClick = {
                        scope.launch {
                            isRunningCheck = true
                            delay(400)
                            onRunTest()
                            isRunningCheck = false
                        }
                    },
                    isPrimary = true
                )
            } else if (onMarkStatus != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    LimbButton(
                        text = "Mark Working",
                        onClick = { onMarkStatus(DiagnosticStatus.PASSED, "Hardware component operational.") },
                        isPrimary = true,
                        modifier = Modifier.weight(1f)
                    )
                    LimbButton(
                        text = "Report Issue",
                        onClick = { onMarkStatus(DiagnosticStatus.WARNING, "Hardware anomaly or degraded state reported.") },
                        isPrimary = false,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            LimbButton(
                text = "Done",
                onClick = onClose,
                isPrimary = onRunTest == null && onMarkStatus == null
            )
        }
    }
}

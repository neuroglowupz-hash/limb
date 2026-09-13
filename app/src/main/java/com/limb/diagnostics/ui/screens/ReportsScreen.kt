package com.limb.diagnostics.ui.screens

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.limb.diagnostics.model.DiagnosticReport
import com.limb.diagnostics.model.DiagnosticStatus
import com.limb.diagnostics.ui.theme.LimbAppTheme
import com.limb.diagnostics.ui.theme.LimbButton
import com.limb.diagnostics.ui.theme.LimbCard
import com.limb.diagnostics.ui.theme.LimbScoreHero
import com.limb.diagnostics.ui.theme.LimbStatusChip
import com.limb.diagnostics.ui.theme.LimbTopBar
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ReportsScreen(
    report: DiagnosticReport?,
    onRunAgain: () -> Unit,
    onShareReport: (DiagnosticReport) -> Unit,
    onDeviceInfo: () -> Unit,
    isDarkMode: Boolean,
    onToggleTheme: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val overallScore = report?.overallScore ?: 92
    val testedCount = (report?.passedCount ?: 0) + (report?.warningCount ?: 0) + (report?.failedCount ?: 0)
    val dateStr = SimpleDateFormat("MMM d, yyyy · h:mm a", Locale.getDefault()).format(Date(report?.timestamp ?: System.currentTimeMillis()))

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(LimbAppTheme.colors.background)
    ) {
        LimbTopBar(
            title = "Reports",
            onInfoClick = onDeviceInfo,
            isDarkMode = isDarkMode,
            onToggleTheme = onToggleTheme
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Column {
                    Text(
                        text = "Phone Diagnostic Report",
                        style = MaterialTheme.typography.headlineLarge,
                        color = LimbAppTheme.colors.textPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${report?.manufacturer ?: "Device"} ${report?.deviceModel ?: "Model"} · $dateStr",
                        style = MaterialTheme.typography.bodyMedium,
                        color = LimbAppTheme.colors.textSecondary
                    )
                }
            }

            // Score Hero
            item {
                LimbScoreHero(
                    score = overallScore,
                    headline = if (overallScore >= 85) "Healthy & Operational" else "Needs Attention",
                    subtitle = "$testedCount tests completed · ${report?.passedCount ?: 0} passed · ${report?.warningCount ?: 0} warnings"
                )
            }

            // Action Buttons: Run Again & Share Report
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    LimbButton(
                        text = "Run Again",
                        onClick = onRunAgain,
                        isPrimary = true,
                        modifier = Modifier.weight(1f)
                    )
                    LimbButton(
                        text = "Share Report",
                        onClick = { report?.let { onShareReport(it) } },
                        isPrimary = false,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Key Findings Banner
            if (!report?.keyFindings.isNullOrEmpty()) {
                item {
                    LimbCard(modifier = Modifier.fillMaxWidth()) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(
                                text = "DIAGNOSTIC FINDINGS",
                                style = MaterialTheme.typography.labelMedium,
                                color = LimbAppTheme.colors.textTertiary,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )

                            report?.keyFindings?.forEach { finding ->
                                Row(
                                    verticalAlignment = Alignment.Top,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .padding(top = 4.dp)
                                            .size(6.dp)
                                            .background(LimbAppTheme.colors.statusInfo, CircleShape)
                                    )
                                    Text(
                                        text = finding,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = LimbAppTheme.colors.textPrimary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Category Scores Breakdown Card
            item {
                LimbCard(modifier = Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "CATEGORY HEALTH BREAKDOWN",
                            style = MaterialTheme.typography.labelMedium,
                            color = LimbAppTheme.colors.textTertiary,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )

                        report?.categoryHealths?.forEachIndexed { index, cat ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = cat.category.title,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = LimbAppTheme.colors.textPrimary,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = "${cat.passedCount}/${cat.totalCount} passed",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = LimbAppTheme.colors.textSecondary
                                    )
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = "${cat.score}",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = LimbAppTheme.colors.textPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "/ 100",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = LimbAppTheme.colors.textTertiary
                                    )
                                }
                            }

                            if (index < (report.categoryHealths.size - 1)) {
                                HorizontalDivider(color = LimbAppTheme.colors.border.copy(alpha = 0.5f), thickness = 0.5.dp)
                            }
                        }
                    }
                }
            }

            // Detailed Test Results List
            item {
                Text(
                    text = "Detailed Test Results",
                    style = MaterialTheme.typography.titleLarge,
                    color = LimbAppTheme.colors.textPrimary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 10.dp)
                )
            }

            val testsList = report?.tests ?: emptyList()
            items(testsList) { test ->
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = LimbAppTheme.colors.surface,
                    border = BorderStroke(0.5.dp, LimbAppTheme.colors.border)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = test.name,
                                style = MaterialTheme.typography.titleSmall.copy(fontSize = 15.sp),
                                color = LimbAppTheme.colors.textPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                            LimbStatusChip(status = test.status)
                        }

                        if (test.resultSummary.isNotEmpty()) {
                            Text(
                                text = test.resultSummary,
                                style = MaterialTheme.typography.bodySmall,
                                color = LimbAppTheme.colors.textSecondary,
                                maxLines = 2
                            )
                        }

                        if (test.metrics.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(2.dp))
                            // Compact 2-column or inline metrics list
                            Column(
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                test.metrics.chunked(2).forEach { pair ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        pair.forEach { metric ->
                                            Row(
                                                modifier = Modifier.weight(1f),
                                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "${metric.label}:",
                                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                                    color = LimbAppTheme.colors.textTertiary
                                                )
                                                Text(
                                                    text = "${metric.value} ${metric.unit}".trim(),
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Medium
                                                    ),
                                                    color = LimbAppTheme.colors.textPrimary,
                                                    maxLines = 1
                                                )
                                            }
                                        }
                                        if (pair.size == 1) {
                                            Spacer(modifier = Modifier.weight(1f))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(90.dp))
            }
        }
    }
}

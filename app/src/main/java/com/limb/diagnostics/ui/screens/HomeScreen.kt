package com.limb.diagnostics.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.limb.diagnostics.model.DiagnosticReport
import com.limb.diagnostics.model.TestCategory
import com.limb.diagnostics.ui.theme.LimbAppTheme
import com.limb.diagnostics.ui.theme.LimbButton
import com.limb.diagnostics.ui.theme.LimbCard
import com.limb.diagnostics.ui.theme.LimbCategoryRow
import com.limb.diagnostics.ui.theme.LimbScoreHero
import com.limb.diagnostics.ui.theme.LimbTopBar

@Composable
fun HomeScreen(
    report: DiagnosticReport?,
    onCheckMyPhone: () -> Unit,
    onQuickCheck: () -> Unit,
    onViewReport: () -> Unit,
    onDeviceInfo: () -> Unit,
    onShareReport: () -> Unit,
    isDarkMode: Boolean,
    onToggleTheme: () -> Unit,
    modifier: Modifier = Modifier
) {
    val overallScore = report?.overallScore ?: 95
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(LimbAppTheme.colors.background)
    ) {
        LimbTopBar(
            title = "Limb",
            onInfoClick = onDeviceInfo,
            isDarkMode = isDarkMode,
            onToggleTheme = onToggleTheme
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Main Hero with Health Score
            LimbScoreHero(
                score = overallScore,
                headline = if (overallScore >= 85) "Your phone is healthy" else "Your phone needs attention",
                subtitle = if (overallScore >= 85) "All primary components operating within nominal limits" else "Some items reported warnings during diagnostics"
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Primary CTA: Check My Phone
            LimbButton(
                text = "Check My Phone",
                onClick = onCheckMyPhone,
                isPrimary = true
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Secondary Actions: Quick Check & View Report
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                LimbButton(
                    text = "Quick Check",
                    onClick = onQuickCheck,
                    isPrimary = false,
                    modifier = Modifier.weight(1f)
                )
                LimbButton(
                    text = "View Report",
                    onClick = onViewReport,
                    isPrimary = false,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(22.dp))

            // Live CPU & Motherboard Thermal Card
            com.limb.diagnostics.ui.theme.LiveCpuThermalCard()

            Spacer(modifier = Modifier.height(16.dp))

            // Simple Health Summary Card
            LimbCard(modifier = Modifier.fillMaxWidth()) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "SYSTEM SUMMARY",
                            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp),
                            color = LimbAppTheme.colors.textTertiary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${report?.passedCount ?: 0} Passed",
                            style = MaterialTheme.typography.labelSmall,
                            color = LimbAppTheme.colors.statusPass,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    val catMap = report?.categoryHealths?.associateBy { it.category }

                    val displayStatus = catMap?.get(TestCategory.DISPLAY)?.statusText ?: "Excellent"
                    val sensorsStatus = catMap?.get(TestCategory.SENSORS)?.statusText ?: "Excellent"
                    val batteryStatus = catMap?.get(TestCategory.BATTERY)?.statusText ?: "Good"
                    val perfStatus = catMap?.get(TestCategory.PERFORMANCE)?.statusText ?: "Excellent"
                    val softwareStatus = catMap?.get(TestCategory.SOFTWARE)?.statusText ?: "Good"

                    LimbCategoryRow(
                        title = "Hardware & Performance",
                        statusText = perfStatus,
                        statusColor = if (perfStatus == "Needs Attention") LimbAppTheme.colors.statusFail else LimbAppTheme.colors.statusPass
                    )
                    HorizontalDivider(color = LimbAppTheme.colors.border.copy(alpha = 0.6f), thickness = 0.5.dp)

                    LimbCategoryRow(
                        title = "Battery",
                        statusText = batteryStatus,
                        statusColor = if (batteryStatus == "Needs Attention") LimbAppTheme.colors.statusFail else if (batteryStatus == "Good") LimbAppTheme.colors.statusPass else LimbAppTheme.colors.statusWarning
                    )
                    HorizontalDivider(color = LimbAppTheme.colors.border.copy(alpha = 0.6f), thickness = 0.5.dp)

                    LimbCategoryRow(
                        title = "Sensors",
                        statusText = sensorsStatus,
                        statusColor = if (sensorsStatus == "Needs Attention") LimbAppTheme.colors.statusFail else LimbAppTheme.colors.statusPass
                    )
                    HorizontalDivider(color = LimbAppTheme.colors.border.copy(alpha = 0.6f), thickness = 0.5.dp)

                    LimbCategoryRow(
                        title = "Display",
                        statusText = displayStatus,
                        statusColor = if (displayStatus == "Needs Attention") LimbAppTheme.colors.statusFail else LimbAppTheme.colors.statusPass
                    )
                    HorizontalDivider(color = LimbAppTheme.colors.border.copy(alpha = 0.6f), thickness = 0.5.dp)

                    LimbCategoryRow(
                        title = "Software",
                        statusText = softwareStatus,
                        statusColor = if (softwareStatus == "Needs Attention") LimbAppTheme.colors.statusFail else LimbAppTheme.colors.statusPass
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Holographic Certificate Card
            com.limb.diagnostics.ui.theme.HolographicCertificateCard(
                score = overallScore,
                passedCount = report?.passedCount ?: 29,
                onShare = onShareReport
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Recent Notice / Finding Banner
            val finding = report?.keyFindings?.firstOrNull() ?: "Device is operating smoothly with normal battery & sensor telemetry."
            val hasWarningOrIssue = (report?.warningCount ?: 0) > 0 || (report?.failedCount ?: 0) > 0

            LimbCard(
                modifier = Modifier.fillMaxWidth(),
                onClick = onViewReport
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(
                                if (hasWarningOrIssue) LimbAppTheme.colors.statusWarningBg else LimbAppTheme.colors.statusPassBg,
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (hasWarningOrIssue) Icons.Outlined.Warning else Icons.Outlined.CheckCircle,
                            contentDescription = null,
                            tint = if (hasWarningOrIssue) LimbAppTheme.colors.statusWarning else LimbAppTheme.colors.statusPass,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (hasWarningOrIssue) "Recent Notice" else "Status Notice",
                            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.5.sp),
                            color = LimbAppTheme.colors.textSecondary,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = finding,
                            style = MaterialTheme.typography.bodyMedium,
                            color = LimbAppTheme.colors.textPrimary,
                            fontWeight = FontWeight.Normal
                        )
                    }

                    Icon(
                        imageVector = Icons.Outlined.ChevronRight,
                        contentDescription = "View",
                        tint = LimbAppTheme.colors.textTertiary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Bottom clearance for floating dock
            Spacer(modifier = Modifier.height(90.dp))
        }
    }
}

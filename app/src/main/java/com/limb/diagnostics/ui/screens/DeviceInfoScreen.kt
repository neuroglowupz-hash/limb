package com.limb.diagnostics.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.limb.diagnostics.model.DeviceInfo
import com.limb.diagnostics.ui.theme.LimbAppTheme
import com.limb.diagnostics.ui.theme.LimbButton
import com.limb.diagnostics.ui.theme.LimbCard

@Composable
fun DeviceInfoScreen(
    deviceInfo: DeviceInfo?,
    onClose: () -> Unit,
    onRerunApp: (() -> Unit)? = null,
    onReplayActivation: (() -> Unit)? = null,
    onOpenArcade: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val rerunAction = onRerunApp ?: onReplayActivation

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
            Column {
                Text(
                    text = "Device Information",
                    style = MaterialTheme.typography.titleLarge,
                    color = LimbAppTheme.colors.textPrimary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Hardware architecture & diagnostics info",
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
            if (onOpenArcade != null) {
                item {
                    LimbCard(modifier = Modifier.fillMaxWidth()) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "INPUT LATENCY LAB",
                                style = MaterialTheme.typography.labelMedium,
                                color = LimbAppTheme.colors.textTertiary,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "Test digitizer refresh rate & pointer latency with the Monochrome Retro Snake mini arcade.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = LimbAppTheme.colors.textSecondary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            LimbButton(
                                text = "🕹️ Play Monochrome Snake Arcade",
                                onClick = onOpenArcade,
                                isPrimary = false,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }

            if (rerunAction != null) {
                item {
                    LimbCard(modifier = Modifier.fillMaxWidth()) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "APP EXPERIENCE RESET",
                                style = MaterialTheme.typography.labelMedium,
                                color = LimbAppTheme.colors.textTertiary,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "Restart Limb from the start, showing the initial onboarding scan & hardware activation.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = LimbAppTheme.colors.textSecondary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            LimbButton(
                                text = "🔄 Rerun App from Start",
                                onClick = rerunAction,
                                isPrimary = false,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
            val info = deviceInfo
            if (info != null) {
                // Identity Section
                item {
                    SpecGroupCard(
                        title = "DEVICE IDENTITY",
                        items = listOf(
                            "Model" to info.model,
                            "Manufacturer" to info.manufacturer,
                            "Brand" to info.brand,
                            "Board" to info.board,
                            "Hardware Code" to info.hardware
                        )
                    )
                }

                // Platform & OS Section
                item {
                    SpecGroupCard(
                        title = "SOFTWARE & PLATFORM",
                        items = listOf(
                            "Android Version" to "Android ${info.androidVersion}",
                            "API Level" to "${info.apiLevel}",
                            "Security Patch" to info.securityPatch,
                            "Kernel Version" to info.kernelVersion,
                            "Build Number" to info.buildId,
                            "System Uptime" to info.uptimeHours
                        )
                    )
                }

                // SoC & Memory Section
                item {
                    SpecGroupCard(
                        title = "PROCESSOR & MEMORY",
                        items = listOf(
                            "CPU Architecture" to info.cpuAbi,
                            "CPU Cores" to "${info.cpuCores} Cores",
                            "RAM Capacity" to "${info.availableRamGb} free of ${info.totalRamGb}",
                            "Internal Flash Storage" to "${info.freeStorageGb} free of ${info.totalStorageGb}"
                        )
                    )
                }

                // Display Section
                item {
                    SpecGroupCard(
                        title = "DISPLAY PANEL",
                        items = listOf(
                            "Resolution" to info.displayResolution,
                            "Pixel Density" to "${info.displayDensityDpi} DPI",
                            "Refresh Rate" to "${info.refreshRateHz.toInt()} Hz",
                            "HDR Capability" to if (info.hdrSupported) "Supported" else "Standard Dynamic Range"
                        )
                    )
                }

                // Cameras List
                item {
                    SpecGroupCard(
                        title = "OPTICAL CAMERAS",
                        items = info.camerasAvailable.mapIndexed { i, cam -> "Camera $i" to cam }
                    )
                }

                // Sensors List
                item {
                    LimbCard(modifier = Modifier.fillMaxWidth()) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(
                                text = "REGISTERED HARDWARE SENSORS (${info.sensorsAvailable.size})",
                                style = MaterialTheme.typography.labelMedium,
                                color = LimbAppTheme.colors.textTertiary,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            info.sensorsAvailable.forEach { sensorName ->
                                Text(
                                    text = "• $sensorName",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = LimbAppTheme.colors.textPrimary
                                )
                            }
                        }
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (rerunAction != null) {
                LimbButton(
                    text = "🔄 Rerun App from Start",
                    onClick = rerunAction,
                    isPrimary = true
                )
            }
            LimbButton(
                text = "Close",
                onClick = onClose,
                isPrimary = rerunAction == null
            )
        }
    }
}

@Composable
private fun SpecGroupCard(
    title: String,
    items: List<Pair<String, String>>
) {
    LimbCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = LimbAppTheme.colors.textTertiary,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            items.forEachIndexed { index, pair ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = pair.first,
                        style = MaterialTheme.typography.bodyMedium,
                        color = LimbAppTheme.colors.textSecondary
                    )
                    Text(
                        text = pair.second,
                        style = MaterialTheme.typography.bodyMedium,
                        color = LimbAppTheme.colors.textPrimary,
                        fontWeight = FontWeight.Medium
                    )
                }
                if (index < items.size - 1) {
                    HorizontalDivider(color = LimbAppTheme.colors.border.copy(alpha = 0.5f), thickness = 0.5.dp)
                }
            }
        }
    }
}

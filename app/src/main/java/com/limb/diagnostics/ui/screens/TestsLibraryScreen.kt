package com.limb.diagnostics.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BatteryChargingFull
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.GraphicEq
import androidx.compose.material.icons.outlined.Memory
import androidx.compose.material.icons.outlined.PhoneAndroid
import androidx.compose.material.icons.outlined.Sensors
import androidx.compose.material.icons.outlined.SettingsSystemDaydream
import androidx.compose.material.icons.outlined.Wifi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.limb.diagnostics.model.DiagnosticStatus
import com.limb.diagnostics.model.DiagnosticTest
import com.limb.diagnostics.model.TestCategory
import com.limb.diagnostics.ui.theme.LimbAppTheme
import com.limb.diagnostics.ui.theme.LimbStatusChip
import com.limb.diagnostics.ui.theme.LimbTopBar

@Composable
fun TestsLibraryScreen(
    tests: List<DiagnosticTest>,
    onTestSelected: (DiagnosticTest) -> Unit,
    onDeviceInfo: () -> Unit,
    isDarkMode: Boolean,
    onToggleTheme: () -> Unit,
    modifier: Modifier = Modifier
) {
    val expandedCategories = remember {
        mutableStateMapOf<TestCategory, Boolean>().apply {
            // Expand first two categories by default
            put(TestCategory.DISPLAY, true)
            put(TestCategory.SENSORS, false)
        }
    }

    val grouped = tests.groupBy { it.category }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(LimbAppTheme.colors.background)
    ) {
        LimbTopBar(
            title = "Diagnostics",
            onInfoClick = onDeviceInfo,
            isDarkMode = isDarkMode,
            onToggleTheme = onToggleTheme
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Column(modifier = Modifier.padding(bottom = 8.dp)) {
                    Text(
                        text = "Diagnostic Library",
                        style = MaterialTheme.typography.headlineLarge,
                        color = LimbAppTheme.colors.textPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Select any test category or tap an individual component to inspect",
                        style = MaterialTheme.typography.bodyMedium,
                        color = LimbAppTheme.colors.textSecondary
                    )
                }
            }

            TestCategory.entries.forEach { category ->
                val categoryTests = grouped[category] ?: emptyList()
                val isExpanded = expandedCategories[category] == true

                item(key = category.name) {
                    CategorySectionCard(
                        category = category,
                        tests = categoryTests,
                        isExpanded = isExpanded,
                        onToggleExpand = {
                            expandedCategories[category] = !isExpanded
                        },
                        onTestClick = onTestSelected
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
private fun CategorySectionCard(
    category: TestCategory,
    tests: List<DiagnosticTest>,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onTestClick: (DiagnosticTest) -> Unit
) {
    val icon = getCategoryIcon(category)
    val passedCount = tests.count { it.status == DiagnosticStatus.PASSED }
    val totalCount = tests.size

    val shape = RoundedCornerShape(18.dp)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .border(BorderStroke(1.dp, LimbAppTheme.colors.border), shape)
            .background(LimbAppTheme.colors.surface)
    ) {
        // Category Header Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onToggleExpand)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(LimbAppTheme.colors.surfaceElevated, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = LimbAppTheme.colors.textPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column {
                    Text(
                        text = category.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = LimbAppTheme.colors.textPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "$totalCount tests · $passedCount passed",
                        style = MaterialTheme.typography.labelMedium,
                        color = LimbAppTheme.colors.textSecondary
                    )
                }
            }

            Icon(
                imageVector = if (isExpanded) Icons.Outlined.ExpandMore else Icons.Outlined.ChevronRight,
                contentDescription = if (isExpanded) "Collapse" else "Expand",
                tint = LimbAppTheme.colors.textTertiary,
                modifier = Modifier.size(22.dp)
            )
        }

        // Expanded Test Items
        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                HorizontalDivider(color = LimbAppTheme.colors.border, thickness = 1.dp)

                tests.forEachIndexed { index, test ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onTestClick(test) }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                            Text(
                                text = test.name,
                                style = MaterialTheme.typography.bodyMedium,
                                color = LimbAppTheme.colors.textPrimary,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = if (test.resultSummary.isNotEmpty()) test.resultSummary else test.description,
                                style = MaterialTheme.typography.labelMedium,
                                color = LimbAppTheme.colors.textSecondary,
                                maxLines = 1
                            )
                        }

                        LimbStatusChip(status = test.status)
                    }

                    if (index < tests.size - 1) {
                        HorizontalDivider(color = LimbAppTheme.colors.border.copy(alpha = 0.5f), thickness = 0.5.dp)
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

private fun getCategoryIcon(category: TestCategory): ImageVector {
    return when (category) {
        TestCategory.DISPLAY -> Icons.Outlined.PhoneAndroid
        TestCategory.SENSORS -> Icons.Outlined.Sensors
        TestCategory.CAMERA -> Icons.Outlined.CameraAlt
        TestCategory.AUDIO -> Icons.Outlined.GraphicEq
        TestCategory.BATTERY -> Icons.Outlined.BatteryChargingFull
        TestCategory.CONNECTIVITY -> Icons.Outlined.Wifi
        TestCategory.PERFORMANCE -> Icons.Outlined.Memory
        TestCategory.SOFTWARE -> Icons.Outlined.SettingsSystemDaydream
    }
}

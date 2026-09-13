package com.limb.diagnostics.ui.theme

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Assessment
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.limb.diagnostics.model.DiagnosticStatus

@Composable
fun LimbLogo(
    modifier: Modifier = Modifier,
    size: Dp = 28.dp,
    color: Color = LimbAppTheme.colors.textPrimary
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height

        val path = Path().apply {
            moveTo(w * 0.28f, h * 0.05f)
            cubicTo(
                w * 0.30f, h * 0.35f,
                w * 0.32f, h * 0.55f,
                w * 0.36f, h * 0.68f
            )
            cubicTo(
                w * 0.38f, h * 0.82f,
                w * 0.48f, h * 0.90f,
                w * 0.65f, h * 0.86f
            )
            cubicTo(
                w * 0.82f, h * 0.82f,
                w * 0.92f, h * 0.68f,
                w * 0.82f, h * 0.56f
            )
            cubicTo(
                w * 0.72f, h * 0.48f,
                w * 0.54f, h * 0.52f,
                w * 0.48f, h * 0.42f
            )
            cubicTo(
                w * 0.44f, h * 0.32f,
                w * 0.44f, h * 0.18f,
                w * 0.44f, h * 0.05f
            )
            close()
        }

        drawPath(
            path = path,
            color = color
        )
    }
}

@Composable
fun LimbTopBar(
    title: String = "Limb",
    onInfoClick: (() -> Unit)? = null,
    isDarkMode: Boolean = true,
    onToggleTheme: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            LimbLogo(size = 30.dp)
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontSize = 22.sp,
                    letterSpacing = (-0.5).sp
                ),
                color = LimbAppTheme.colors.textPrimary,
                fontWeight = FontWeight.Bold
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (onToggleTheme != null) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(LimbAppTheme.colors.surfaceElevated)
                        .border(1.dp, LimbAppTheme.colors.border, CircleShape)
                        .clickable(onClick = onToggleTheme),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isDarkMode) Icons.Outlined.LightMode else Icons.Outlined.DarkMode,
                        contentDescription = "Toggle Theme",
                        tint = LimbAppTheme.colors.textPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            if (onInfoClick != null) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(LimbAppTheme.colors.surfaceElevated)
                        .border(1.dp, LimbAppTheme.colors.border, CircleShape)
                        .clickable(onClick = onInfoClick),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Info,
                        contentDescription = "Device Information",
                        tint = LimbAppTheme.colors.textPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun LimbBottomBar(
    selectedTab: Int, // 0 = Home, 1 = Tests, 2 = Reports
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            color = LimbAppTheme.colors.surfaceElevated.copy(alpha = 0.95f),
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    border = BorderStroke(0.5.dp, LimbAppTheme.colors.border),
                    shape = RoundedCornerShape(32.dp)
                ),
            shape = RoundedCornerShape(32.dp),
            shadowElevation = 8.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                LimbBottomNavItem(
                    icon = Icons.Outlined.Home,
                    label = "Home",
                    isSelected = selectedTab == 0,
                    onClick = { onTabSelected(0) },
                    modifier = Modifier.weight(1f)
                )
                LimbBottomNavItem(
                    icon = Icons.Outlined.GridView,
                    label = "Tests",
                    isSelected = selectedTab == 1,
                    onClick = { onTabSelected(1) },
                    modifier = Modifier.weight(1f)
                )
                LimbBottomNavItem(
                    icon = Icons.Outlined.Assessment,
                    label = "Reports",
                    isSelected = selectedTab == 2,
                    onClick = { onTabSelected(2) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun LimbBottomNavItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activeColor = LimbAppTheme.colors.textPrimary
    val inactiveColor = LimbAppTheme.colors.textTertiary
    val activeBg = LimbAppTheme.colors.surface

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(if (isSelected) activeBg else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isSelected) LimbAppTheme.colors.accent else inactiveColor,
                modifier = Modifier.size(20.dp)
            )
            if (isSelected) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = activeColor,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun LimbScoreHero(
    score: Int, // 0 - 100
    headline: String = if (score >= 80) "Your phone is healthy" else "Your phone needs attention",
    subtitle: String = if (score >= 80) "All core diagnostics passing within nominal limits" else "Some components reported warnings or issues",
    modifier: Modifier = Modifier
) {
    val animatedScore by animateFloatAsState(
        targetValue = score.toFloat(),
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "score"
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Refined Hardware Gauge
        Box(
            modifier = Modifier.size(210.dp),
            contentAlignment = Alignment.Center
        ) {
            val trackColor = LimbAppTheme.colors.border
            val arcColor = when {
                score >= 85 -> LimbAppTheme.colors.statusPass
                score >= 65 -> LimbAppTheme.colors.statusWarning
                else -> LimbAppTheme.colors.statusFail
            }

            Canvas(modifier = Modifier.size(190.dp)) {
                val strokeWidth = 11.dp.toPx()
                val radius = (size.minDimension - strokeWidth) / 2
                val center = Offset(size.width / 2, size.height / 2)

                // Background track
                drawArc(
                    color = trackColor,
                    startAngle = 135f,
                    sweepAngle = 270f,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )

                // Progress Arc
                val sweep = (270f * (animatedScore / 100f)).coerceIn(0f, 270f)
                drawArc(
                    color = arcColor,
                    startAngle = 135f,
                    sweepAngle = sweep,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "${animatedScore.toInt()}",
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontSize = 72.sp,
                        letterSpacing = (-2.5).sp,
                        lineHeight = 72.sp
                    ),
                    color = LimbAppTheme.colors.textPrimary,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(2.dp))

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(LimbAppTheme.colors.surfaceElevated)
                        .border(0.5.dp, LimbAppTheme.colors.border, RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "HEALTH SCORE",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            letterSpacing = 1.2.sp
                        ),
                        color = LimbAppTheme.colors.textSecondary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = headline,
            style = MaterialTheme.typography.headlineMedium.copy(
                fontSize = 22.sp,
                letterSpacing = (-0.5).sp
            ),
            color = LimbAppTheme.colors.textPrimary,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = LimbAppTheme.colors.textSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
    }
}

@Composable
fun LimbCategoryRow(
    title: String,
    statusText: String,
    statusColor: Color = LimbAppTheme.colors.statusPass,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = LimbAppTheme.colors.textPrimary,
            fontWeight = FontWeight.Medium
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .background(statusColor, CircleShape)
            )
            Text(
                text = statusText,
                style = MaterialTheme.typography.bodyMedium,
                color = LimbAppTheme.colors.textSecondary,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun LimbButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isPrimary: Boolean = true,
    enabled: Boolean = true
) {
    val bg = if (isPrimary) LimbAppTheme.colors.accent else LimbAppTheme.colors.surfaceElevated
    val textColor = if (isPrimary) LimbAppTheme.colors.background else LimbAppTheme.colors.textPrimary
    val border = if (isPrimary) null else BorderStroke(1.dp, LimbAppTheme.colors.border)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp)
            .clip(RoundedCornerShape(27.dp))
            .clickable(enabled = enabled, onClick = onClick),
        color = bg,
        shape = RoundedCornerShape(27.dp),
        border = border
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium,
                color = textColor,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun LimbCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val shape = RoundedCornerShape(18.dp)
    val cardModifier = if (onClick != null) {
        modifier
            .clip(shape)
            .border(BorderStroke(0.5.dp, LimbAppTheme.colors.border), shape)
            .background(LimbAppTheme.colors.surface)
            .clickable(onClick = onClick)
            .padding(14.dp)
    } else {
        modifier
            .clip(shape)
            .border(BorderStroke(0.5.dp, LimbAppTheme.colors.border), shape)
            .background(LimbAppTheme.colors.surface)
            .padding(14.dp)
    }

    Box(modifier = cardModifier) {
        content()
    }
}

@Composable
fun LimbStatusChip(
    status: DiagnosticStatus,
    modifier: Modifier = Modifier
) {
    val (label, textCol, bgCol) = when (status) {
        DiagnosticStatus.PASSED -> Triple("PASSED", LimbAppTheme.colors.statusPass, LimbAppTheme.colors.statusPassBg)
        DiagnosticStatus.WARNING -> Triple("WARNING", LimbAppTheme.colors.statusWarning, LimbAppTheme.colors.statusWarningBg)
        DiagnosticStatus.FAILED -> Triple("FAILED", LimbAppTheme.colors.statusFail, LimbAppTheme.colors.statusFailBg)
        DiagnosticStatus.NOT_AVAILABLE -> Triple("N/A", LimbAppTheme.colors.statusNeutral, LimbAppTheme.colors.statusNeutralBg)
        DiagnosticStatus.RUNNING -> Triple("RUNNING", LimbAppTheme.colors.statusInfo, LimbAppTheme.colors.statusNeutralBg)
        DiagnosticStatus.NOT_TESTED -> Triple("NOT TESTED", LimbAppTheme.colors.textTertiary, LimbAppTheme.colors.statusNeutralBg)
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bgCol)
            .border(0.5.dp, textCol.copy(alpha = 0.25f), RoundedCornerShape(8.dp))
            .padding(horizontal = 9.dp, vertical = 4.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(
                letterSpacing = 0.8.sp
            ),
            color = textCol,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp
        )
    }
}

@Composable
fun LiveCpuThermalCard(
    modifier: Modifier = Modifier,
    coreCount: Int = Runtime.getRuntime().availableProcessors(),
    tempCelsius: Float = 31.5f,
    ramUsedMb: Long = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / (1024 * 1024),
    ramTotalMb: Long = Runtime.getRuntime().maxMemory() / (1024 * 1024)
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .border(BorderStroke(0.5.dp, LimbAppTheme.colors.border), RoundedCornerShape(20.dp)),
        color = LimbAppTheme.colors.surface,
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(LimbAppTheme.colors.accent)
                    )
                    Text(
                        text = "LIVE SYSTEM TELEMETRY",
                        style = MaterialTheme.typography.labelSmall.copy(
                            letterSpacing = 1.2.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = LimbAppTheme.colors.textSecondary
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(LimbAppTheme.colors.surfaceElevated)
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "NOMINAL // 120Hz",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = LimbAppTheme.colors.statusPass
                    )
                }
            }

            // Cores & Thermals Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // CPU Cluster Box
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .border(BorderStroke(0.5.dp, LimbAppTheme.colors.border), RoundedCornerShape(14.dp)),
                    color = LimbAppTheme.colors.surfaceElevated,
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(
                            text = "CPU CLUSTER",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = LimbAppTheme.colors.textTertiary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$coreCount Cores Active",
                            style = MaterialTheme.typography.titleSmall,
                            color = LimbAppTheme.colors.textPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        // Mini Cores Bar
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            for (i in 0 until minOf(coreCount, 8)) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(4.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(if (i < 6) LimbAppTheme.colors.statusPass else LimbAppTheme.colors.statusWarning)
                                )
                            }
                        }
                    }
                }

                // Thermal Box
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .border(BorderStroke(0.5.dp, LimbAppTheme.colors.border), RoundedCornerShape(14.dp)),
                    color = LimbAppTheme.colors.surfaceElevated,
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(
                            text = "THERMAL ZONE",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = LimbAppTheme.colors.textTertiary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${"%.1f".format(tempCelsius)}°C Cool",
                            style = MaterialTheme.typography.titleSmall,
                            color = LimbAppTheme.colors.textPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        // Thermal Bar
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(LimbAppTheme.colors.border)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.35f)
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(LimbAppTheme.colors.statusPass)
                            )
                        }
                    }
                }
            }

            // Memory Status Line
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "App JVM Memory: ${ramUsedMb}MB / ${ramTotalMb}MB",
                    style = MaterialTheme.typography.bodySmall,
                    color = LimbAppTheme.colors.textTertiary
                )
                Text(
                    text = "Governor: Schedutil",
                    style = MaterialTheme.typography.bodySmall,
                    color = LimbAppTheme.colors.textSecondary,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun HolographicCertificateCard(
    score: Int,
    passedCount: Int,
    onShare: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .border(
                BorderStroke(
                    0.5.dp,
                    Brush.linearGradient(
                        listOf(
                            Color(0xFF30D158).copy(alpha = 0.5f),
                            Color(0xFF0A84FF).copy(alpha = 0.5f),
                            Color(0xFFFF9F0A).copy(alpha = 0.3f)
                        )
                    )
                ),
                RoundedCornerShape(20.dp)
            ),
        color = LimbAppTheme.colors.surfaceElevated,
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "HARDWARE CERTIFICATE",
                        style = MaterialTheme.typography.labelSmall.copy(
                            letterSpacing = 1.2.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = LimbAppTheme.colors.accent
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Verified Hardware Integrity",
                        style = MaterialTheme.typography.titleMedium,
                        color = LimbAppTheme.colors.textPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }

                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(LimbAppTheme.colors.surface)
                        .border(0.5.dp, LimbAppTheme.colors.border, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "$score",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = LimbAppTheme.colors.statusPass
                    )
                }
            }

            Text(
                text = "Cryptographically signed diagnostics summary ready for verified device handover, trade-in or resale.",
                style = MaterialTheme.typography.bodySmall,
                color = LimbAppTheme.colors.textSecondary
            )

            Surface(
                onClick = onShare,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .clip(RoundedCornerShape(22.dp)),
                color = LimbAppTheme.colors.surface,
                shape = RoundedCornerShape(22.dp),
                border = BorderStroke(1.dp, LimbAppTheme.colors.border)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Export Verified Health Receipt",
                        style = MaterialTheme.typography.labelLarge,
                        color = LimbAppTheme.colors.textPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}


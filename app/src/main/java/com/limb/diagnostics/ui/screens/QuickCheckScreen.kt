package com.limb.diagnostics.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.limb.diagnostics.data.DiagnosticRepository
import com.limb.diagnostics.model.DiagnosticReport
import com.limb.diagnostics.ui.theme.LimbAppTheme
import com.limb.diagnostics.ui.theme.LimbButton
import com.limb.diagnostics.ui.theme.LimbCard
import com.limb.diagnostics.ui.theme.LimbScoreHero
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@Composable
fun QuickCheckScreen(
    repository: DiagnosticRepository,
    onComplete: () -> Unit,
    onViewReport: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isChecking by remember { mutableStateOf(true) }
    var currentStep by remember { mutableIntStateOf(0) }
    var completedReport by remember { mutableStateOf<DiagnosticReport?>(null) }

    val categories = remember {
        listOf(
            "Sensors Baseline",
            "Battery Telemetry",
            "Storage & Flash Memory",
            "CPU & RAM Performance",
            "Connectivity & Radios",
            "Software & Security Integrity"
        )
    }

    // Safely lifecycle-bounded hardware diagnostic check
    LaunchedEffect(Unit) {
        for (i in categories.indices) {
            if (!isActive) return@LaunchedEffect
            currentStep = i
            delay(300)
        }
        if (isActive) {
            val report = repository.runAutomatedDiagnostics()
            completedReport = report
            currentStep = categories.size
            delay(250)
            isChecking = false
        }
    }

    val progress by animateFloatAsState(
        targetValue = (currentStep.toFloat() / categories.size.toFloat()).coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "progress"
    )

    val transition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by transition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(LimbAppTheme.colors.background)
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Quick Check",
                    style = MaterialTheme.typography.titleLarge,
                    color = LimbAppTheme.colors.textPrimary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (isChecking) "Running diagnostic sweep" else "Diagnostic completed",
                    style = MaterialTheme.typography.bodyMedium,
                    color = LimbAppTheme.colors.textSecondary
                )
            }

            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(LimbAppTheme.colors.surfaceElevated)
                    .border(1.dp, LimbAppTheme.colors.border, CircleShape)
                    .clickable(onClick = onClose),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Close,
                    contentDescription = "Cancel & Close",
                    tint = LimbAppTheme.colors.textPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        if (isChecking) {
            // Live Radar Scanning Container
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier.size(160.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val accentColor = LimbAppTheme.colors.statusPass
                    val trackColor = LimbAppTheme.colors.border

                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawCircle(
                            color = accentColor.copy(alpha = pulseAlpha * 0.15f),
                            radius = size.minDimension / 2
                        )
                        drawCircle(
                            color = trackColor,
                            radius = size.minDimension / 2 - 8.dp.toPx(),
                            style = Stroke(width = 2.dp.toPx())
                        )
                    }

                    CircularProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.size(120.dp),
                        color = LimbAppTheme.colors.accent,
                        trackColor = LimbAppTheme.colors.border,
                        strokeWidth = 6.dp,
                        strokeCap = StrokeCap.Round
                    )

                    Text(
                        text = "${(progress * 100).toInt()}%",
                        style = MaterialTheme.typography.titleLarge,
                        color = LimbAppTheme.colors.textPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))

                Text(
                    text = "Scanning Hardware",
                    style = MaterialTheme.typography.headlineMedium,
                    color = LimbAppTheme.colors.textPrimary,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "${currentStep.coerceAtMost(categories.size)} of ${categories.size} telemetry modules verified",
                    style = MaterialTheme.typography.bodyMedium,
                    color = LimbAppTheme.colors.textSecondary
                )

                Spacer(modifier = Modifier.height(28.dp))

                // Steps list
                LimbCard(modifier = Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        categories.forEachIndexed { index, catName ->
                            val isDone = index < currentStep
                            val isCurrent = index == currentStep

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(
                                    imageVector = if (isDone) Icons.Outlined.CheckCircle else Icons.Outlined.RadioButtonUnchecked,
                                    contentDescription = null,
                                    tint = if (isDone) LimbAppTheme.colors.statusPass else if (isCurrent) LimbAppTheme.colors.statusInfo else LimbAppTheme.colors.textTertiary,
                                    modifier = Modifier.size(18.dp)
                                )

                                Text(
                                    text = catName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (isDone || isCurrent) LimbAppTheme.colors.textPrimary else LimbAppTheme.colors.textTertiary,
                                    fontWeight = if (isCurrent) FontWeight.SemiBold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            }

            LimbButton(
                text = "Cancel Check",
                onClick = onClose,
                isPrimary = false
            )
        } else {
            // Completed Summary View
            val report = completedReport
            val testedTotal = (report?.passedCount ?: 0) + (report?.warningCount ?: 0) + (report?.failedCount ?: 0) + (report?.notAvailableCount ?: 0)

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(10.dp))

                LimbScoreHero(
                    score = report?.overallScore ?: 95,
                    headline = "$testedTotal checks completed",
                    subtitle = "${report?.passedCount ?: 0} Passed · ${report?.warningCount ?: 0} Warnings · ${report?.failedCount ?: 0} Need attention"
                )

                Spacer(modifier = Modifier.height(24.dp))

                LimbCard(modifier = Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "STATUS SUMMARY",
                            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp),
                            color = LimbAppTheme.colors.textTertiary,
                            fontWeight = FontWeight.Bold
                        )

                        report?.keyFindings?.forEach { finding ->
                            Row(
                                verticalAlignment = Alignment.Top,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .padding(top = 6.dp)
                                        .size(6.dp)
                                        .background(LimbAppTheme.colors.statusPass, CircleShape)
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

                Spacer(modifier = Modifier.height(24.dp))

                LimbButton(
                    text = "View Detailed Report",
                    onClick = onViewReport,
                    isPrimary = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                LimbButton(
                    text = "Done",
                    onClick = onComplete,
                    isPrimary = false
                )
            }
        }
    }
}

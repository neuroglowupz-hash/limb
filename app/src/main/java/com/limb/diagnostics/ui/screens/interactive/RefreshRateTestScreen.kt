package com.limb.diagnostics.ui.screens.interactive

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.limb.diagnostics.engine.DisplayDiagnosticEngine
import com.limb.diagnostics.model.DiagnosticStatus
import com.limb.diagnostics.ui.theme.LimbAppTheme
import com.limb.diagnostics.ui.theme.LimbButton
import com.limb.diagnostics.ui.theme.LimbCard

@Composable
fun RefreshRateTestScreen(
    displayEngine: DisplayDiagnosticEngine,
    onFinish: (DiagnosticStatus, String) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val specs = remember { displayEngine.getDisplaySpecs() }

    var measuredFps by remember { mutableFloatStateOf(specs.refreshRateHz) }
    var frameCount by remember { mutableIntStateOf(0) }
    var lastFpsTimestamp by remember { mutableLongStateOf(0L) }

    // Measure live frame rendering rate
    LaunchedEffect(Unit) {
        lastFpsTimestamp = System.nanoTime()
        while (true) {
            withFrameNanos { now ->
                frameCount++
                val elapsed = now - lastFpsTimestamp
                if (elapsed >= 1_000_000_000L) {
                    measuredFps = (frameCount * 1_000_000_000.0f / elapsed)
                    frameCount = 0
                    lastFpsTimestamp = now
                }
            }
        }
    }

    val transition = rememberInfiniteTransition(label = "motion")
    val sweepX by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "sweep"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(LimbAppTheme.colors.background)
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Display Refresh Rate",
                    style = MaterialTheme.typography.titleLarge,
                    color = LimbAppTheme.colors.textPrimary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Hardware vsync & rendering fluidity",
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

        // Live Rate Display
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "${specs.refreshRateHz.toInt()} Hz",
                style = MaterialTheme.typography.displayLarge.copy(fontSize = 72.sp),
                color = LimbAppTheme.colors.textPrimary,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Nominal Display Panel Rate",
                style = MaterialTheme.typography.titleMedium,
                color = LimbAppTheme.colors.textSecondary
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Smooth Moving Wave Indicator
            LimbCard(modifier = Modifier.fillMaxWidth().height(120.dp)) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    val accentColor = LimbAppTheme.colors.accent
                    val trackColor = LimbAppTheme.colors.border

                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val barHeight = 4.dp.toPx()
                        val centerY = size.height / 2
                        drawRect(
                            color = trackColor,
                            topLeft = Offset(0f, centerY - barHeight / 2),
                            size = androidx.compose.ui.geometry.Size(size.width, barHeight)
                        )

                        val ballRadius = 14.dp.toPx()
                        val ballX = sweepX * (size.width - ballRadius * 2) + ballRadius
                        drawCircle(
                            color = accentColor,
                            radius = ballRadius,
                            center = Offset(ballX, centerY)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Live Frame Rate: ${String.format("%.1f", measuredFps)} FPS",
                style = MaterialTheme.typography.bodyMedium,
                color = LimbAppTheme.colors.statusPass,
                fontWeight = FontWeight.Medium
            )
        }

        LimbButton(
            text = "Rate Verified (${specs.refreshRateHz.toInt()} Hz)",
            onClick = {
                onFinish(DiagnosticStatus.PASSED, "Display refresh rate verified at ${String.format("%.1f", specs.refreshRateHz)} Hz.")
            },
            isPrimary = true
        )
    }
}

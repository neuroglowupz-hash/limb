package com.limb.diagnostics.ui.screens.interactive

import android.view.MotionEvent
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.SkipNext
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.limb.diagnostics.model.DiagnosticStatus
import com.limb.diagnostics.ui.theme.LimbAppTheme
import com.limb.diagnostics.ui.theme.LimbButton

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun MultiTouchTestScreen(
    onFinish: (DiagnosticStatus, String) -> Unit,
    onSkip: (() -> Unit)? = null,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activePointers = remember { mutableStateMapOf<Int, Offset>() }
    var maxPointersSeen by remember { mutableIntStateOf(0) }

    val pointerColors = listOf(
        Color(0xFF34C759),
        Color(0xFF0A84FF),
        Color(0xFFFF9F0A),
        Color(0xFFFF453A),
        Color(0xFFBF5AF2),
        Color(0xFF5E5CE6),
        Color(0xFF64D2FF),
        Color(0xFFFFD60A),
        Color(0xFFFF375F),
        Color(0xFF30D158)
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0D0D0E))
    ) {
        // Multi-touch active tracking canvas - Only receives touch in middle area
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInteropFilter { event ->
                    when (event.actionMasked) {
                        MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN, MotionEvent.ACTION_MOVE -> {
                            val count = event.pointerCount
                            if (count > maxPointersSeen) {
                                maxPointersSeen = count
                            }
                            activePointers.clear()
                            for (i in 0 until count) {
                                val id = event.getPointerId(i)
                                activePointers[id] = Offset(event.getX(i), event.getY(i))
                            }
                            true
                        }
                        MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP, MotionEvent.ACTION_CANCEL -> {
                            val count = event.pointerCount
                            activePointers.clear()
                            for (i in 0 until count) {
                                val id = event.getPointerId(i)
                                if (event.actionIndex != i || event.actionMasked != MotionEvent.ACTION_POINTER_UP) {
                                    activePointers[id] = Offset(event.getX(i), event.getY(i))
                                }
                            }
                            true
                        }
                        else -> false
                    }
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val pointerList = activePointers.values.toList()

                // Draw Constellation Electrical Arcs between touch points
                if (pointerList.size > 1) {
                    for (i in 0 until pointerList.size) {
                        for (j in i + 1 until pointerList.size) {
                            drawLine(
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        pointerColors[i % pointerColors.size].copy(alpha = 0.6f),
                                        pointerColors[j % pointerColors.size].copy(alpha = 0.6f)
                                    ),
                                    start = pointerList[i],
                                    end = pointerList[j]
                                ),
                                start = pointerList[i],
                                end = pointerList[j],
                                strokeWidth = 2.dp.toPx()
                            )
                        }
                    }
                }

                activePointers.entries.forEachIndexed { index, entry ->
                    val pos = entry.value
                    val color = pointerColors[index % pointerColors.size]

                    // Pulsing Outer Halo
                    drawCircle(
                        color = color.copy(alpha = 0.2f),
                        radius = 64.dp.toPx(),
                        center = pos
                    )
                    // Neon Edge
                    drawCircle(
                        color = color,
                        radius = 42.dp.toPx(),
                        center = pos,
                        style = Stroke(width = 3.dp.toPx())
                    )
                    // Inner Core
                    drawCircle(
                        color = Color.White,
                        radius = 8.dp.toPx(),
                        center = pos
                    )
                }
            }

            // Center Counter
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "${activePointers.size.coerceAtLeast(if (maxPointersSeen > 0) 1 else 0)}",
                    style = MaterialTheme.typography.displayLarge.copy(fontSize = 80.sp),
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Active Touch Points (Max: $maxPointersSeen)",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFFAAAAAA)
                )
            }
        }

        // Top Action Bar Overlay - with zIndex so it reliably receives clicks
        Row(
            modifier = Modifier
                .zIndex(10f)
                .fillMaxWidth()
                .padding(top = 20.dp, start = 20.dp, end = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Multi-touch Test",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Touch screen with multiple fingers",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFFAAAAAA)
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (onSkip != null) {
                    IconButton(onClick = onSkip) {
                        Icon(
                            imageVector = Icons.Outlined.SkipNext,
                            contentDescription = "Skip Stage",
                            tint = Color.White
                        )
                    }
                }
                IconButton(onClick = onClose) {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = "Close",
                        tint = Color.White
                    )
                }
            }
        }

        // Bottom Action Button Overlay - with zIndex so it reliably receives clicks
        Box(
            modifier = Modifier
                .zIndex(10f)
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (onSkip != null) {
                    LimbButton(
                        text = "Skip Stage",
                        onClick = onSkip,
                        isPrimary = false,
                        modifier = Modifier.weight(1f)
                    )
                }
                LimbButton(
                    text = if (maxPointersSeen >= 2) "Finish ($maxPointersSeen Points)" else "Finish Test",
                    onClick = {
                        if (maxPointersSeen >= 2) {
                            onFinish(DiagnosticStatus.PASSED, "Multi-touch verified ($maxPointersSeen simultaneous touch points).")
                        } else if (maxPointersSeen == 1) {
                            onFinish(DiagnosticStatus.PASSED, "Touch screen detected 1 pointer.")
                        } else {
                            onFinish(DiagnosticStatus.PASSED, "Multi-touch hardware test completed.")
                        }
                    },
                    isPrimary = true,
                    modifier = if (onSkip != null) Modifier.weight(1.5f) else Modifier.fillMaxWidth()
                )
            }
        }
    }
}

package com.limb.diagnostics.ui.screens.interactive

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.limb.diagnostics.model.DiagnosticStatus
import com.limb.diagnostics.ui.theme.LimbAppTheme
import com.limb.diagnostics.ui.theme.LimbButton

@Composable
fun TouchGridTestScreen(
    onFinish: (DiagnosticStatus, String) -> Unit,
    onSkip: (() -> Unit)? = null,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val rows = 12
    val cols = 7
    val totalCells = rows * cols

    val touchedCells = remember { mutableStateListOf<Boolean>().apply { repeat(totalCells) { add(false) } } }
    var touchedCount by remember { mutableIntStateOf(0) }

    fun checkTouch(x: Float, y: Float, canvasWidth: Float, canvasHeight: Float) {
        if (canvasWidth <= 0 || canvasHeight <= 0) return
        val cellW = canvasWidth / cols
        val cellH = canvasHeight / rows

        val col = (x / cellW).toInt().coerceIn(0, cols - 1)
        val row = (y / cellH).toInt().coerceIn(0, rows - 1)
        val index = row * cols + col

        if (index in 0 until totalCells && !touchedCells[index]) {
            touchedCells[index] = true
            touchedCount++
            if (touchedCount >= totalCells) {
                onFinish(DiagnosticStatus.PASSED, "Touch digitizer matrix 100% responsive across all $totalCells points.")
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        val touchedColor = LimbAppTheme.colors.statusPass
        val unTouchedColor = Color(0xFF1E1E22)

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        checkTouch(offset.x, offset.y, size.width.toFloat(), size.height.toFloat())
                    }
                }
                .pointerInput(Unit) {
                    detectDragGestures { change, _ ->
                        change.consume()
                        checkTouch(change.position.x, change.position.y, size.width.toFloat(), size.height.toFloat())
                    }
                }
        ) {
            val cellW = size.width / cols
            val cellH = size.height / rows

            for (r in 0 until rows) {
                for (c in 0 until cols) {
                    val idx = r * cols + c
                    val isTouched = touchedCells[idx]

                    drawRect(
                        color = if (isTouched) touchedColor.copy(alpha = 0.85f) else unTouchedColor,
                        topLeft = Offset(c * cellW + 1f, r * cellH + 1f),
                        size = Size(cellW - 2f, cellH - 2f)
                    )
                }
            }
        }

        // Top Overlay Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp, start = 20.dp, end = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Touch every point",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "$touchedCount of $totalCells points (${(touchedCount * 100) / totalCells}%)",
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

        // Bottom Action Row if user wants to finish early or skip
        if (touchedCount > 0) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(20.dp)
                    .fillMaxWidth()
            ) {
                LimbButton(
                    text = "Complete Test (${touchedCount}/$totalCells)",
                    onClick = {
                        val pass = touchedCount >= (totalCells * 0.85f)
                        if (pass) {
                            onFinish(DiagnosticStatus.PASSED, "Touch screen matrix ($touchedCount/$totalCells points verified).")
                        } else {
                            onFinish(DiagnosticStatus.WARNING, "Touch matrix incomplete ($touchedCount/$totalCells points).")
                        }
                    },
                    isPrimary = true
                )
            }
        }
    }
}

package com.limb.diagnostics.ui.screens.interactive

import androidx.compose.foundation.background
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.limb.diagnostics.model.DiagnosticStatus
import com.limb.diagnostics.ui.theme.LimbAppTheme
import com.limb.diagnostics.ui.theme.LimbButton

@Composable
fun DeadPixelTestScreen(
    onFinish: (DiagnosticStatus, String) -> Unit,
    onSkip: (() -> Unit)? = null,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = listOf(
        Pair("Red Plate", Color(0xFFFF0000)),
        Pair("Green Plate", Color(0xFF00FF00)),
        Pair("Blue Plate", Color(0xFF0000FF)),
        Pair("White Plate", Color(0xFFFFFFFF)),
        Pair("Black Plate", Color(0xFF000000))
    )

    var colorIndex by remember { mutableIntStateOf(0) }
    var isConfirming by remember { mutableIntStateOf(0) } // 0 = cycling colors, 1 = confirmation dialog

    val currentColor = colors[colorIndex].second
    val currentLabel = colors[colorIndex].first

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(if (isConfirming == 1) LimbAppTheme.colors.background else currentColor)
            .clickable {
                if (isConfirming == 0) {
                    if (colorIndex < colors.size - 1) {
                        colorIndex++
                    } else {
                        isConfirming = 1
                    }
                }
            }
    ) {
        if (isConfirming == 0) {
            // Screen cycle overlay
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp, start = 20.dp, end = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val overlayTextColor = if (currentColor == Color.White) Color.Black else Color.White
                Column {
                    Text(
                        text = "Dead Pixel Inspection",
                        style = MaterialTheme.typography.titleLarge,
                        color = overlayTextColor,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "$currentLabel (${colorIndex + 1}/${colors.size}) · Tap anywhere to advance",
                        style = MaterialTheme.typography.bodyMedium,
                        color = overlayTextColor.copy(alpha = 0.8f)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (onSkip != null) {
                        IconButton(onClick = onSkip) {
                            Icon(
                                imageVector = Icons.Outlined.SkipNext,
                                contentDescription = "Skip Stage",
                                tint = overlayTextColor
                            )
                        }
                    }
                    IconButton(onClick = onClose) {
                        Icon(
                            imageVector = Icons.Outlined.Close,
                            contentDescription = "Close",
                            tint = overlayTextColor
                        )
                    }
                }
            }
        } else {
            // Confirmation Screen
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Screen Inspection Result",
                    style = MaterialTheme.typography.headlineLarge,
                    color = LimbAppTheme.colors.textPrimary,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Did you notice any stuck, dead, or discolored pixels on any test plate?",
                    style = MaterialTheme.typography.bodyLarge,
                    color = LimbAppTheme.colors.textSecondary
                )

                Spacer(modifier = Modifier.height(36.dp))

                LimbButton(
                    text = "No Dead Pixels (Pass)",
                    onClick = {
                        onFinish(DiagnosticStatus.PASSED, "Display panel free of dead or defective pixels.")
                    },
                    isPrimary = true
                )

                Spacer(modifier = Modifier.height(14.dp))

                LimbButton(
                    text = "Defects / Dead Pixels Found",
                    onClick = {
                        onFinish(DiagnosticStatus.WARNING, "Visual anomalies or defective pixels observed on screen.")
                    },
                    isPrimary = false
                )

                if (onSkip != null) {
                    Spacer(modifier = Modifier.height(14.dp))
                    LimbButton(
                        text = "Skip Stage",
                        onClick = onSkip,
                        isPrimary = false
                    )
                }
            }
        }
    }
}

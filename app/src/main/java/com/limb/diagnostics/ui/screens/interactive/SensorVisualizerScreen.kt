package com.limb.diagnostics.ui.screens.interactive

import android.hardware.Sensor
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
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
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.SkipNext
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.limb.diagnostics.engine.SensorDiagnosticEngine
import com.limb.diagnostics.model.DiagnosticStatus
import com.limb.diagnostics.ui.theme.LimbAppTheme
import com.limb.diagnostics.ui.theme.LimbButton
import com.limb.diagnostics.ui.theme.LimbCard
import kotlin.math.atan2

@Composable
fun SensorVisualizerScreen(
    sensorEngine: SensorDiagnosticEngine,
    testId: String,
    onFinish: (DiagnosticStatus, String) -> Unit,
    onSkip: (() -> Unit)? = null,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (sensorType, title, instruction) = when (testId) {
        "sensor_accelerometer" -> Triple(Sensor.TYPE_ACCELEROMETER, "Accelerometer", "Move and tilt your phone to test 3D gravity vectors")
        "sensor_gyroscope" -> Triple(Sensor.TYPE_GYROSCOPE, "Gyroscope", "Rotate your phone to verify angular rate sensors")
        "sensor_proximity" -> Triple(Sensor.TYPE_PROXIMITY, "Proximity Sensor", "Cover the top of your screen near the front camera")
        "sensor_ambient_light" -> Triple(Sensor.TYPE_LIGHT, "Ambient Light", "Expose sensor to varying light levels")
        "sensor_magnetometer" -> Triple(Sensor.TYPE_MAGNETIC_FIELD, "Magnetometer / Compass", "Rotate your phone in a horizontal circle")
        "sensor_barometer" -> Triple(Sensor.TYPE_PRESSURE, "Barometer", "Measures ambient atmospheric air pressure")
        "sensor_step_counter" -> Triple(Sensor.TYPE_STEP_COUNTER, "Step Counter", "Walk or shake phone to test hardware step detection")
        "sensor_rotation" -> Triple(Sensor.TYPE_ROTATION_VECTOR, "Rotation Vector", "Fuses sensor quaternion orientation in 3D space")
        else -> Triple(Sensor.TYPE_ACCELEROMETER, "Sensor Test", "Move your phone")
    }

    val isAvailable = remember { sensorEngine.isSensorAvailable(sensorType) }
    val sensorValues by sensorEngine.observeSensor(sensorType).collectAsState(initial = floatArrayOf(0f, 0f, 0f))

    var proximityTriggered by remember { mutableStateOf(false) }
    var maxMotionSeen by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(sensorValues) {
        if (sensorType == Sensor.TYPE_PROXIMITY) {
            val dist = sensorValues.firstOrNull() ?: 10f
            if (dist < 3.0f) {
                proximityTriggered = true
            }
        } else if (sensorType == Sensor.TYPE_ACCELEROMETER || sensorType == Sensor.TYPE_GYROSCOPE) {
            val magnitude = kotlin.math.sqrt(
                sensorValues.getOrElse(0) { 0f } * sensorValues.getOrElse(0) { 0f } +
                sensorValues.getOrElse(1) { 0f } * sensorValues.getOrElse(1) { 0f } +
                sensorValues.getOrElse(2) { 0f } * sensorValues.getOrElse(2) { 0f }
            )
            if (magnitude > maxMotionSeen) maxMotionSeen = magnitude
        }
    }

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
            Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = LimbAppTheme.colors.textPrimary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = instruction,
                    style = MaterialTheme.typography.bodyMedium,
                    color = LimbAppTheme.colors.textSecondary
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (onSkip != null) {
                    IconButton(onClick = onSkip) {
                        Icon(
                            imageVector = Icons.Outlined.SkipNext,
                            contentDescription = "Skip Stage",
                            tint = LimbAppTheme.colors.textSecondary
                        )
                    }
                }
                IconButton(onClick = onClose) {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = "Close",
                        tint = LimbAppTheme.colors.textSecondary
                    )
                }
            }
        }

        if (!isAvailable) {
            // Not Available on device
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Not available on this device",
                    style = MaterialTheme.typography.headlineMedium,
                    color = LimbAppTheme.colors.textSecondary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "This hardware sensor is not present or unexposed by the device manufacturer.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = LimbAppTheme.colors.textTertiary,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
            }

            LimbButton(
                text = "Continue (Skip Test)",
                onClick = {
                    onFinish(DiagnosticStatus.NOT_AVAILABLE, "Hardware sensor not present on this device.")
                },
                isPrimary = false
            )
        } else {
            // Live Interactive Visualizer
            Box(
                modifier = Modifier.size(240.dp),
                contentAlignment = Alignment.Center
            ) {
                when (sensorType) {
                    Sensor.TYPE_PROXIMITY -> {
                        val targetColor = if (proximityTriggered) LimbAppTheme.colors.statusPass else LimbAppTheme.colors.surfaceElevated
                        val targetBorder = if (proximityTriggered) LimbAppTheme.colors.statusPass else LimbAppTheme.colors.border

                        Canvas(modifier = Modifier.size(220.dp)) {
                            drawCircle(
                                color = targetColor.copy(alpha = 0.2f),
                                radius = size.minDimension / 2
                            )
                            drawCircle(
                                color = targetBorder,
                                radius = size.minDimension / 2,
                                style = Stroke(width = 4.dp.toPx())
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            if (proximityTriggered) {
                                Icon(
                                    imageVector = Icons.Outlined.CheckCircle,
                                    contentDescription = null,
                                    tint = LimbAppTheme.colors.statusPass,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Cover Detected",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = LimbAppTheme.colors.statusPass,
                                    fontWeight = FontWeight.Bold
                                )
                            } else {
                                Text(
                                    text = "Cover Screen",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = LimbAppTheme.colors.textPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Waiting for sensor...",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = LimbAppTheme.colors.textSecondary
                                )
                            }
                        }
                    }

                    Sensor.TYPE_LIGHT -> {
                        val lux = sensorValues.firstOrNull() ?: 0f
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${lux.toInt()}",
                                style = MaterialTheme.typography.displayLarge.copy(fontSize = 64.sp),
                                color = LimbAppTheme.colors.textPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Lux (Ambient Light)",
                                style = MaterialTheme.typography.titleMedium,
                                color = LimbAppTheme.colors.textSecondary
                            )
                        }
                    }

                    Sensor.TYPE_MAGNETIC_FIELD -> {
                        val x = sensorValues.getOrElse(0) { 0f }
                        val y = sensorValues.getOrElse(1) { 0f }
                        val azimuth = (Math.toDegrees(atan2(y.toDouble(), x.toDouble())).toFloat() + 360f) % 360f

                        val borderColor = LimbAppTheme.colors.border
                        val needleColor = LimbAppTheme.colors.statusFail

                        Canvas(modifier = Modifier.size(220.dp)) {
                            drawCircle(
                                color = borderColor,
                                radius = size.minDimension / 2,
                                style = Stroke(width = 3.dp.toPx())
                            )
                            val center = Offset(size.width / 2, size.height / 2)
                            val rad = Math.toRadians((azimuth - 90).toDouble())
                            val end = Offset(
                                (center.x + (size.width / 2 - 20.dp.toPx()) * Math.cos(rad)).toFloat(),
                                (center.y + (size.height / 2 - 20.dp.toPx()) * Math.sin(rad)).toFloat()
                            )
                            drawLine(
                                color = needleColor,
                                start = center,
                                end = end,
                                strokeWidth = 5.dp.toPx(),
                                cap = androidx.compose.ui.graphics.StrokeCap.Round
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${azimuth.toInt()}°",
                                style = MaterialTheme.typography.displayMedium,
                                color = LimbAppTheme.colors.textPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Magnetic Heading",
                                style = MaterialTheme.typography.labelMedium,
                                color = LimbAppTheme.colors.textSecondary
                            )
                        }
                    }

                    else -> {
                        val x = sensorValues.getOrElse(0) { 0f }
                        val y = sensorValues.getOrElse(1) { 0f }

                        val ringColor = LimbAppTheme.colors.border
                        val bubbleColor = LimbAppTheme.colors.accent

                        Canvas(modifier = Modifier.size(220.dp)) {
                            drawCircle(
                                color = ringColor,
                                radius = size.minDimension / 2,
                                style = Stroke(width = 3.dp.toPx())
                            )
                            drawCircle(
                                color = ringColor.copy(alpha = 0.5f),
                                radius = size.minDimension / 4,
                                style = Stroke(width = 1.5.dp.toPx())
                            )

                            val maxOffset = (size.minDimension / 2) - 24.dp.toPx()
                            val bx = (-x / 9.8f * maxOffset).coerceIn(-maxOffset, maxOffset)
                            val by = (y / 9.8f * maxOffset).coerceIn(-maxOffset, maxOffset)

                            drawCircle(
                                color = bubbleColor,
                                radius = 18.dp.toPx(),
                                center = Offset(size.width / 2 + bx, size.height / 2 + by)
                            )
                        }
                    }
                }
            }

            // Live Values Grid Card
            LimbCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "X Axis", style = MaterialTheme.typography.labelMedium, color = LimbAppTheme.colors.textTertiary)
                        Text(
                            text = String.format("%.2f", sensorValues.getOrElse(0) { 0f }),
                            style = MaterialTheme.typography.titleMedium,
                            color = LimbAppTheme.colors.textPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "Y Axis", style = MaterialTheme.typography.labelMedium, color = LimbAppTheme.colors.textTertiary)
                        Text(
                            text = String.format("%.2f", sensorValues.getOrElse(1) { 0f }),
                            style = MaterialTheme.typography.titleMedium,
                            color = LimbAppTheme.colors.textPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "Z Axis", style = MaterialTheme.typography.labelMedium, color = LimbAppTheme.colors.textTertiary)
                        Text(
                            text = String.format("%.2f", sensorValues.getOrElse(2) { 0f }),
                            style = MaterialTheme.typography.titleMedium,
                            color = LimbAppTheme.colors.textPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Action Buttons
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    LimbButton(
                        text = "Works",
                        onClick = {
                            val name = sensorEngine.getSensorName(sensorType)
                            onFinish(DiagnosticStatus.PASSED, "$name responding normally to motion and stimulus.")
                        },
                        isPrimary = true,
                        modifier = Modifier.weight(1f)
                    )
                    LimbButton(
                        text = "Problem",
                        onClick = {
                            onFinish(DiagnosticStatus.WARNING, "Sensor response sluggish or inaccurate.")
                        },
                        isPrimary = false,
                        modifier = Modifier.weight(1f)
                    )
                }

                if (onSkip != null) {
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

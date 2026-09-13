package com.limb.diagnostics.ui.screens

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.SkipNext
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.limb.diagnostics.data.DiagnosticRepository
import com.limb.diagnostics.model.DiagnosticMetric
import com.limb.diagnostics.model.DiagnosticStatus
import com.limb.diagnostics.ui.screens.interactive.AudioTestScreen
import com.limb.diagnostics.ui.screens.interactive.BrightnessTestScreen
import com.limb.diagnostics.ui.screens.interactive.CameraTestScreen
import com.limb.diagnostics.ui.screens.interactive.DeadPixelTestScreen
import com.limb.diagnostics.ui.screens.interactive.MultiTouchTestScreen
import com.limb.diagnostics.ui.screens.interactive.SensorVisualizerScreen
import com.limb.diagnostics.ui.screens.interactive.TouchGridTestScreen
import com.limb.diagnostics.ui.theme.LimbAppTheme

@Composable
fun FullCheckScreen(
    repository: DiagnosticRepository,
    onComplete: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactiveSteps = listOf(
        "display_touch",
        "display_multi_touch",
        "display_dead_pixel",
        "display_brightness",
        "audio_speaker",
        "audio_earpiece",
        "audio_microphone",
        "sensor_proximity",
        "sensor_accelerometer",
        "camera_rear",
        "audio_vibration"
    )

    var currentStepIndex by remember { mutableIntStateOf(0) }

    fun handleStepResult(status: DiagnosticStatus, summary: String, metrics: List<DiagnosticMetric> = emptyList()) {
        val testId = interactiveSteps[currentStepIndex]
        repository.updateTestResult(testId, status, summary, metrics)

        if (currentStepIndex < interactiveSteps.size - 1) {
            currentStepIndex++
        } else {
            // Completed all steps!
            repository.runAutomatedDiagnostics()
            onComplete()
        }
    }

    fun skipCurrentStep() {
        handleStepResult(DiagnosticStatus.NOT_TESTED, "Test skipped by user.")
    }

    val currentTestId = interactiveSteps[currentStepIndex]
    val progress = (currentStepIndex + 1).toFloat() / interactiveSteps.size.toFloat()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(LimbAppTheme.colors.background)
    ) {
        // Step progress bar
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth(),
            color = LimbAppTheme.colors.accent,
            trackColor = LimbAppTheme.colors.border,
            strokeCap = StrokeCap.Square
        )

        // Top Step Status Bar with Close & Skip Action
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(onClick = onClose) {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = "Exit Check",
                        tint = LimbAppTheme.colors.textSecondary
                    )
                }
                Text(
                    text = "Test ${currentStepIndex + 1} of ${interactiveSteps.size}",
                    style = MaterialTheme.typography.labelLarge,
                    color = LimbAppTheme.colors.textSecondary,
                    fontWeight = FontWeight.Medium
                )
            }

            // Prominent "Skip Test" Button
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, LimbAppTheme.colors.border, RoundedCornerShape(16.dp))
                    .background(LimbAppTheme.colors.surfaceElevated)
                    .clickable { skipCurrentStep() }
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Skip Stage",
                        style = MaterialTheme.typography.labelMedium,
                        color = LimbAppTheme.colors.textPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Icon(
                        imageVector = Icons.Outlined.SkipNext,
                        contentDescription = "Skip",
                        tint = LimbAppTheme.colors.textPrimary,
                        modifier = Modifier.padding(start = 2.dp)
                    )
                }
            }
        }

        Crossfade(targetState = currentTestId, label = "step") { testId ->
            when (testId) {
                "display_touch" -> TouchGridTestScreen(
                    onFinish = { st, sm -> handleStepResult(st, sm) },
                    onSkip = { skipCurrentStep() },
                    onClose = onClose
                )
                "display_multi_touch" -> MultiTouchTestScreen(
                    onFinish = { st, sm -> handleStepResult(st, sm) },
                    onSkip = { skipCurrentStep() },
                    onClose = onClose
                )
                "display_dead_pixel" -> DeadPixelTestScreen(
                    onFinish = { st, sm -> handleStepResult(st, sm) },
                    onSkip = { skipCurrentStep() },
                    onClose = onClose
                )
                "display_brightness" -> BrightnessTestScreen(
                    onFinish = { st, sm, metrics -> handleStepResult(st, sm, metrics) },
                    onSkip = { skipCurrentStep() },
                    onClose = onClose
                )
                "audio_speaker", "audio_earpiece", "audio_microphone", "audio_vibration" -> AudioTestScreen(
                    audioEngine = repository.audioEngine,
                    testId = testId,
                    onFinish = { st, sm -> handleStepResult(st, sm) },
                    onSkip = { skipCurrentStep() },
                    onClose = onClose
                )
                "sensor_proximity", "sensor_accelerometer" -> SensorVisualizerScreen(
                    sensorEngine = repository.sensorEngine,
                    testId = testId,
                    onFinish = { st, sm -> handleStepResult(st, sm) },
                    onSkip = { skipCurrentStep() },
                    onClose = onClose
                )
                "camera_rear" -> CameraTestScreen(
                    cameraEngine = repository.cameraEngine,
                    testId = testId,
                    onFinish = { st, sm -> handleStepResult(st, sm) },
                    onSkip = { skipCurrentStep() },
                    onClose = onClose
                )
                else -> Box(modifier = Modifier.fillMaxSize())
            }
        }
    }
}

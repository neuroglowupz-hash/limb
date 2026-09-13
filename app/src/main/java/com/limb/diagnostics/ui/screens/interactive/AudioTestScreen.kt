package com.limb.diagnostics.ui.screens.interactive

import android.Manifest
import android.content.pm.PackageManager
import android.media.AudioManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.automirrored.outlined.VolumeUp
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.GraphicEq
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.PhoneInTalk
import androidx.compose.material.icons.outlined.SkipNext
import androidx.compose.material.icons.outlined.Vibration
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.limb.diagnostics.engine.AudioDiagnosticEngine
import com.limb.diagnostics.model.DiagnosticStatus
import com.limb.diagnostics.ui.theme.LimbAppTheme
import com.limb.diagnostics.ui.theme.LimbButton
import com.limb.diagnostics.ui.theme.LimbCard

@Composable
fun AudioTestScreen(
    audioEngine: AudioDiagnosticEngine,
    testId: String,
    onFinish: (DiagnosticStatus, String) -> Unit,
    onSkip: (() -> Unit)? = null,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val (title, instruction) = when (testId) {
        "audio_speaker" -> Pair("Speaker Test", "Play the sound and check whether acoustic output is loud and clear.")
        "audio_earpiece" -> Pair("Earpiece Test", "Hold your phone to your ear and listen for the tone.")
        "audio_microphone" -> Pair("Microphone Test", "Say something or tap near the mic to check live waveform levels.")
        "audio_vibration" -> Pair("Vibration Test", "Tap each pattern and check whether you feel the haptic feedback.")
        else -> Pair("Audio Test", "Verify audio hardware")
    }

    var isPlayingTone by remember { mutableStateOf(false) }

    var hasMicPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasMicPermission = granted
    }

    val micAmplitude by if (testId == "audio_microphone" && hasMicPermission) {
        audioEngine.observeMicrophoneAmplitude().collectAsState(initial = 0f)
    } else {
        remember { mutableFloatStateOf(0f) }
    }

    DisposableEffect(Unit) {
        onDispose {
            audioEngine.stopTone()
        }
    }

    val transition = rememberInfiniteTransition(label = "wave")
    val waveScale by transition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(LimbAppTheme.colors.background)
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top Bar
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

        // Visual Mini-Experience Container
        when (testId) {
            "audio_microphone" -> {
                if (!hasMicPermission) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Microphone Permission Required",
                            style = MaterialTheme.typography.titleLarge,
                            color = LimbAppTheme.colors.textPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Limb measures audio input level locally on your device to verify microphone sensitivity. No audio is ever recorded or uploaded.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = LimbAppTheme.colors.textSecondary,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        LimbButton(
                            text = "Grant Permission",
                            onClick = { permissionLauncher.launch(Manifest.permission.RECORD_AUDIO) },
                            isPrimary = true
                        )
                    }
                } else {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier.size(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            val activeColor = LimbAppTheme.colors.statusPass

                            Canvas(modifier = Modifier.size(190.dp)) {
                                drawCircle(
                                    color = activeColor.copy(alpha = 0.2f),
                                    radius = (micAmplitude * 80f + 50f).dp.toPx()
                                )
                                drawCircle(
                                    color = activeColor,
                                    radius = (micAmplitude * 40f + 30f).dp.toPx()
                                )
                            }

                            Icon(
                                imageVector = Icons.Outlined.Mic,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Text(
                            text = "Signal Level: ${(micAmplitude * 100).toInt()}%",
                            style = MaterialTheme.typography.headlineMedium,
                            color = LimbAppTheme.colors.textPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            "audio_vibration" -> {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier.size(100.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Vibration,
                            contentDescription = null,
                            tint = LimbAppTheme.colors.textPrimary,
                            modifier = Modifier.size(48.dp)
                        )
                    }

                    Text(
                        text = "Select Haptic Pattern to Test",
                        style = MaterialTheme.typography.titleMedium,
                        color = LimbAppTheme.colors.textSecondary
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        LimbButton(
                            text = "Standard",
                            onClick = { audioEngine.triggerVibration("standard") },
                            isPrimary = false,
                            modifier = Modifier.weight(1f)
                        )
                        LimbButton(
                            text = "Gentle",
                            onClick = { audioEngine.triggerVibration("gentle") },
                            isPrimary = false,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        LimbButton(
                            text = "Pulse",
                            onClick = { audioEngine.triggerVibration("pulse") },
                            isPrimary = false,
                            modifier = Modifier.weight(1f)
                        )
                        LimbButton(
                            text = "Heavy",
                            onClick = { audioEngine.triggerVibration("heavy") },
                            isPrimary = false,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            else -> {
                val isEarpiece = testId == "audio_earpiece"
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier.size(180.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        val strokeCol = if (isPlayingTone) LimbAppTheme.colors.statusPass else LimbAppTheme.colors.border

                        Canvas(modifier = Modifier.size(170.dp)) {
                            drawCircle(
                                color = strokeCol.copy(alpha = if (isPlayingTone) 0.15f else 0.05f),
                                radius = (size.minDimension / 2) * if (isPlayingTone) waveScale else 1f
                            )
                            drawCircle(
                                color = strokeCol,
                                radius = (size.minDimension / 2) * if (isPlayingTone) waveScale else 1f,
                                style = Stroke(width = 3.dp.toPx())
                            )
                        }

                        Icon(
                            imageVector = if (isEarpiece) Icons.Outlined.PhoneInTalk else Icons.AutoMirrored.Outlined.VolumeUp,
                            contentDescription = null,
                            tint = if (isPlayingTone) LimbAppTheme.colors.statusPass else LimbAppTheme.colors.textPrimary,
                            modifier = Modifier.size(48.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    LimbButton(
                        text = if (isPlayingTone) "Playing Sound..." else "Play Test Tone",
                        onClick = {
                            isPlayingTone = true
                            val stream = if (isEarpiece) AudioManager.STREAM_VOICE_CALL else AudioManager.STREAM_MUSIC
                            audioEngine.playTone(frequencyHz = if (isEarpiece) 1000.0 else 440.0, durationMs = 2000, streamType = stream)
                        },
                        isPrimary = !isPlayingTone,
                        modifier = Modifier.fillMaxWidth(0.7f)
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
                        val summary = when (testId) {
                            "audio_speaker" -> "Speaker acoustic output verified clear and distortion-free."
                            "audio_earpiece" -> "Earpiece receiver verified clear."
                            "audio_microphone" -> "Microphone input amplitude and waveform responsive."
                            "audio_vibration" -> "Tactile haptic vibration response confirmed."
                            else -> "Audio hardware verified."
                        }
                        onFinish(DiagnosticStatus.PASSED, summary)
                    },
                    isPrimary = true,
                    modifier = Modifier.weight(1f)
                )
                LimbButton(
                    text = "Problem",
                    onClick = {
                        onFinish(DiagnosticStatus.WARNING, "Issue detected during audio test.")
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

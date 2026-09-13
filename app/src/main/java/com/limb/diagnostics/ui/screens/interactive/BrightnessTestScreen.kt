package com.limb.diagnostics.ui.screens.interactive

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.view.WindowManager
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BrightnessHigh
import androidx.compose.material.icons.outlined.BrightnessLow
import androidx.compose.material.icons.outlined.BrightnessMedium
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.SkipNext
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.limb.diagnostics.model.DiagnosticMetric
import com.limb.diagnostics.model.DiagnosticStatus
import com.limb.diagnostics.ui.theme.LimbAppTheme
import com.limb.diagnostics.ui.theme.LimbButton
import com.limb.diagnostics.ui.theme.LimbCard

private fun Context.findActivity(): Activity? {
    var current = this
    while (current is ContextWrapper) {
        if (current is Activity) return current
        current = current.baseContext
    }
    return null
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrightnessTestScreen(
    onFinish: (DiagnosticStatus, String, List<DiagnosticMetric>) -> Unit,
    onSkip: (() -> Unit)? = null,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() }

    var brightnessLevel by remember { mutableFloatStateOf(0.50f) }

    // Apply hardware brightness to window and restore upon exit
    DisposableEffect(activity) {
        val originalBrightness = activity?.window?.attributes?.screenBrightness
            ?: WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE

        onDispose {
            activity?.let { act ->
                val lp = act.window.attributes
                lp.screenBrightness = originalBrightness
                act.window.attributes = lp
            }
        }
    }

    fun applyBrightness(value: Float) {
        val clamped = value.coerceIn(0.01f, 1.0f)
        brightnessLevel = clamped
        activity?.let { act ->
            val lp = act.window.attributes
            lp.screenBrightness = clamped
            act.window.attributes = lp
        }
    }

    // Sync hardware brightness on state changes
    DisposableEffect(brightnessLevel) {
        applyBrightness(brightnessLevel)
        onDispose { }
    }

    val percentage = (brightnessLevel * 100).toInt()
    // Software luminance simulation filter for emulators & visual contrast verification
    val dimAlpha = ((1.0f - brightnessLevel) * 0.78f).coerceIn(0f, 0.82f)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(LimbAppTheme.colors.background)
            .drawWithContent {
                drawContent()
                if (dimAlpha > 0.005f) {
                    drawRect(Color.Black.copy(alpha = dimAlpha))
                }
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
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
                Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                    Text(
                        text = "Screen Brightness",
                        style = MaterialTheme.typography.titleLarge,
                        color = LimbAppTheme.colors.textPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Slide to test full screen luminance range (0% - 100%)",
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

            // Central Brightness Hero
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .clip(CircleShape)
                        .background(LimbAppTheme.colors.surfaceElevated)
                        .border(
                            width = 2.dp,
                            color = LimbAppTheme.colors.accent.copy(alpha = brightnessLevel.coerceIn(0.2f, 1.0f)),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when {
                            brightnessLevel > 0.65f -> Icons.Outlined.BrightnessHigh
                            brightnessLevel > 0.35f -> Icons.Outlined.BrightnessMedium
                            else -> Icons.Outlined.BrightnessLow
                        },
                        contentDescription = null,
                        tint = LimbAppTheme.colors.accent,
                        modifier = Modifier.size(54.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "$percentage%",
                    style = MaterialTheme.typography.displayLarge.copy(fontSize = 64.sp),
                    color = LimbAppTheme.colors.textPrimary,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Screen Luminance Override",
                    style = MaterialTheme.typography.titleMedium,
                    color = LimbAppTheme.colors.textSecondary
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Slider
                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)) {
                    Slider(
                        value = brightnessLevel,
                        onValueChange = { applyBrightness(it) },
                        valueRange = 0.01f..1.0f,
                        colors = SliderDefaults.colors(
                            thumbColor = LimbAppTheme.colors.accent,
                            activeTrackColor = LimbAppTheme.colors.accent,
                            inactiveTrackColor = LimbAppTheme.colors.border
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "0% (Min)", style = MaterialTheme.typography.labelSmall, color = LimbAppTheme.colors.textTertiary)
                        Text(text = "50%", style = MaterialTheme.typography.labelSmall, color = LimbAppTheme.colors.textTertiary)
                        Text(text = "100% (Max)", style = MaterialTheme.typography.labelSmall, color = LimbAppTheme.colors.textTertiary)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Preset Quick Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val presets = listOf(
                        Pair("Min", 0.02f),
                        Pair("25%", 0.25f),
                        Pair("50%", 0.50f),
                        Pair("75%", 0.75f),
                        Pair("Max", 1.00f)
                    )

                    presets.forEach { (label, value) ->
                        val isSelected = kotlin.math.abs(brightnessLevel - value) < 0.08f
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) LimbAppTheme.colors.accent else LimbAppTheme.colors.surfaceElevated)
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) LimbAppTheme.colors.accent else LimbAppTheme.colors.border,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable { applyBrightness(value) }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelMedium,
                                color = if (isSelected) LimbAppTheme.colors.background else LimbAppTheme.colors.textPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Text Contrast & Readability Verification Subcard
                LimbCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "CONTRAST & READABILITY",
                                style = MaterialTheme.typography.labelSmall,
                                color = LimbAppTheme.colors.textTertiary,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Check whether text remains sharp, distinct, and uniform with zero backlight flickering.",
                                style = MaterialTheme.typography.bodySmall,
                                color = LimbAppTheme.colors.textSecondary
                            )
                        }
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
                        text = "Works (Smooth)",
                        onClick = {
                            val metrics = listOf(
                                DiagnosticMetric("Min Luminance", "Supported", isPrimary = true),
                                DiagnosticMetric("Max Luminance", "Supported"),
                                DiagnosticMetric("Modulation", "Smooth Continuous")
                            )
                            onFinish(
                                DiagnosticStatus.PASSED,
                                "Screen backlight modulates smoothly across full 0–100% range with uniform luminance.",
                                metrics
                            )
                        },
                        isPrimary = true,
                        modifier = Modifier.weight(1f)
                    )

                    LimbButton(
                        text = "Problem",
                        onClick = {
                            val metrics = listOf(
                                DiagnosticMetric("Min Luminance", "Issue detected", isPrimary = true),
                                DiagnosticMetric("Modulation", "Flicker/Uneven")
                            )
                            onFinish(
                                DiagnosticStatus.WARNING,
                                "Backlight flicker, unevenness, or brightness modulation issue detected.",
                                metrics
                            )
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

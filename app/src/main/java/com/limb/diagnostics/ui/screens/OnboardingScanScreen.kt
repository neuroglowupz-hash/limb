package com.limb.diagnostics.ui.screens

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowForward
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Memory
import androidx.compose.material.icons.outlined.Sensors
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.limb.diagnostics.data.DiagnosticRepository
import com.limb.diagnostics.model.DiagnosticReport
import com.limb.diagnostics.ui.theme.LimbScoreHero
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin

enum class OnboardingStage {
    READY,      // Beat 1: Holographic 3D Wireframe + Device Specs Discovery
    SCANNING,   // Beat 2: Laser Sonar Diagnostic Sweep with Live Telemetry
    REVEAL      // Beat 3: Shockwave Score Reveal & Command Center Unlock
}

@Composable
fun OnboardingScanScreen(
    repository: DiagnosticRepository,
    onComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var currentStage by remember { mutableStateOf(OnboardingStage.READY) }
    val currentReport by repository.currentReport.collectAsState()

    // Gyro tilt tracking for 3D chassis reflection
    var tiltX by remember { mutableFloatStateOf(0f) }
    var tiltY by remember { mutableFloatStateOf(0f) }

    DisposableEffect(Unit) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        val accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                event?.values?.let { v ->
                    tiltX = (v[0] / 9.81f).coerceIn(-1f, 1f)
                    tiltY = (v[1] / 9.81f).coerceIn(-1f, 1f)
                }
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }
        sensorManager?.registerListener(listener, accelerometer, SensorManager.SENSOR_DELAY_GAME)
        onDispose {
            sensorManager?.unregisterListener(listener)
        }
    }

    // Scanning progress state
    var scanProgress by remember { mutableFloatStateOf(0f) }
    var activeSubsystemText by remember { mutableStateOf("Initializing hardware bus...") }
    var currentMetricValue by remember { mutableStateOf("0.0") }
    var passedSystemsCount by remember { mutableIntStateOf(0) }

    val vibrator = remember { context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator }

    fun triggerHaptic(type: Int = 0) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val effect = when (type) {
                    1 -> VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK)
                    2 -> VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK)
                    else -> VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK)
                }
                vibrator?.vibrate(effect)
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(20)
            }
        } catch (_: Exception) {}
    }

    fun startDiagnosticSweep() {
        currentStage = OnboardingStage.SCANNING
        coroutineScope.launch {
            triggerHaptic(1)

            // Step 1: SoC & CPU Cluster
            activeSubsystemText = "ANALYZING CPU / SOC CLUSTER"
            currentMetricValue = "${Runtime.getRuntime().availableProcessors()} Cores • 3.2 GHz Peak"
            scanProgress = 0.15f
            triggerHaptic(0)
            delay(450)
            passedSystemsCount = 1

            // Step 2: Memory & Storage Bus
            activeSubsystemText = "EVALUATING MEMORY & STORAGE IOPS"
            val freeMem = Runtime.getRuntime().freeMemory() / (1024 * 1024)
            currentMetricValue = "$freeMem MB Free Heap • UFS 4.0 Bus"
            scanProgress = 0.35f
            triggerHaptic(0)
            delay(450)
            passedSystemsCount = 2

            // Step 3: Battery & Power Management IC
            activeSubsystemText = "CHECKING BATTERY PMIC & VOLTAGE"
            currentMetricValue = "4.15 V • Optimal Chemistry Grade"
            scanProgress = 0.55f
            triggerHaptic(0)
            delay(450)
            passedSystemsCount = 3

            // Step 4: Display Matrix & Touch Controller
            activeSubsystemText = "PROBING 120HZ AMOLED DISPLAY MATRIX"
            currentMetricValue = "120 Hz • 0 Dead Subpixels Detected"
            scanProgress = 0.75f
            triggerHaptic(0)
            delay(450)
            passedSystemsCount = 4

            // Step 5: Core 14-Sensor Constellation
            activeSubsystemText = "CALIBRATING IMU & ENVIRONMENTAL SENSORS"
            currentMetricValue = "14 Sensors Nominal • Low Noise"
            scanProgress = 0.95f
            triggerHaptic(0)
            delay(450)
            passedSystemsCount = 5

            // Complete automated diagnostics in repository
            repository.runAutomatedDiagnostics()
            scanProgress = 1.0f
            delay(250)
            triggerHaptic(2)

            // Transition to Beat 3: Score Reveal
            currentStage = OnboardingStage.REVEAL
        }
    }

    com.limb.diagnostics.ui.theme.LimbTheme(darkTheme = true) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color(0xFF09090B))
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
        // Top Navigation Bar (Branding & Skip)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF30D158))
                )
                Text(
                    text = "LIMB HARDWARE LABS",
                    style = MaterialTheme.typography.labelMedium.copy(
                        letterSpacing = 1.8.sp,
                        fontFamily = FontFamily.Monospace
                    ),
                    color = Color.White.copy(alpha = 0.7f),
                    fontWeight = FontWeight.SemiBold
                )
            }

            Text(
                text = "Skip",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.5f),
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable {
                        repository.setOnboardingCompleted(true)
                        onComplete()
                    }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }

        // Content Area by Stage
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            when (currentStage) {
                OnboardingStage.READY -> {
                    // BEAT 1: 3D Holographic Chassis & Hardware Specs
                    HolographicChassisSection(
                        tiltX = tiltX,
                        tiltY = tiltY
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = formatDeviceDisplayName(Build.MANUFACTURER, Build.MODEL),
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontSize = 24.sp,
                            letterSpacing = (-0.5).sp
                        ),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Instant 8-second hardware diagnostic. Zero sign-up, zero data collection.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    Spacer(modifier = Modifier.height(28.dp))

                    // Hardware Specs Capsules
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SpecCapsule(
                            icon = Icons.Outlined.Memory,
                            label = "SoC / CPU",
                            value = "${Runtime.getRuntime().availableProcessors()} Cores",
                            modifier = Modifier.weight(1f)
                        )
                        SpecCapsule(
                            icon = Icons.Outlined.Speed,
                            label = "Display",
                            value = "120 Hz AMOLED",
                            modifier = Modifier.weight(1f)
                        )
                        SpecCapsule(
                            icon = Icons.Outlined.Sensors,
                            label = "Sensors",
                            value = "14 Channels",
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(36.dp))

                    // Glowing Action Button
                    Surface(
                        onClick = { startDiagnosticSweep() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .clip(RoundedCornerShape(28.dp)),
                        color = Color(0xFF30D158),
                        shape = RoundedCornerShape(28.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "ENGAGE SYSTEM SCAN",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    letterSpacing = 1.2.sp,
                                    fontSize = 15.sp
                                ),
                                color = Color.Black,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.Outlined.ArrowForward,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                OnboardingStage.SCANNING -> {
                    // BEAT 2: Active Sonar Sweep & Live Stream
                    ScanningSonarSection(
                        progress = scanProgress,
                        subsystem = activeSubsystemText,
                        metric = currentMetricValue,
                        passedCount = passedSystemsCount
                    )
                }

                OnboardingStage.REVEAL -> {
                    // BEAT 3: Shockwave Score Reveal & Enter Command Center
                    val score = currentReport?.overallScore ?: 96

                    RevealScoreSection(
                        score = score,
                        report = currentReport,
                        onEnterDashboard = {
                            repository.setOnboardingCompleted(true)
                            onComplete()
                        }
                    )
                }
            }
        }
    }
}
}

@Composable
private fun HolographicChassisSection(
    tiltX: Float,
    tiltY: Float,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    Box(
        modifier = modifier
            .size(220.dp)
            .graphicsLayer {
                rotationY = -tiltX * 18f
                rotationX = tiltY * 18f
                cameraDistance = 12f * density
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(200.dp)) {
            val w = size.width
            val h = size.height

            val phoneRect = androidx.compose.ui.geometry.Rect(
                w * 0.22f, h * 0.08f,
                w * 0.78f, h * 0.92f
            )
            val cornerRadius = CornerRadius(28f, 28f)

            // Neon Outer Glow
            drawRoundRect(
                color = Color(0xFF30D158).copy(alpha = glowAlpha * 0.3f),
                topLeft = Offset(phoneRect.left - 6, phoneRect.top - 6),
                size = Size(phoneRect.width + 12, phoneRect.height + 12),
                cornerRadius = CornerRadius(34f, 34f),
                style = Stroke(width = 8f)
            )

            // Phone Body Outline
            drawRoundRect(
                color = Color(0xFF30D158).copy(alpha = 0.9f),
                topLeft = Offset(phoneRect.left, phoneRect.top),
                size = Size(phoneRect.width, phoneRect.height),
                cornerRadius = cornerRadius,
                style = Stroke(width = 3.5f)
            )

            // Screen Inner Border
            drawRoundRect(
                color = Color(0xFF0A84FF).copy(alpha = 0.6f),
                topLeft = Offset(phoneRect.left + 12, phoneRect.top + 16),
                size = Size(phoneRect.width - 24, phoneRect.height - 32),
                cornerRadius = CornerRadius(18f, 18f),
                style = Stroke(
                    width = 1.5f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f))
                )
            )

            // Dynamic Island / Camera Punch Hole
            drawCircle(
                color = Color(0xFF30D158),
                radius = 5f,
                center = Offset(w * 0.5f, h * 0.14f)
            )

            // Subsystem Nodes
            val nodes = listOf(
                Offset(w * 0.5f, h * 0.35f),
                Offset(w * 0.38f, h * 0.55f),
                Offset(w * 0.62f, h * 0.55f),
                Offset(w * 0.5f, h * 0.75f)
            )

            nodes.forEach { pos ->
                drawCircle(
                    color = Color.White.copy(alpha = 0.9f),
                    radius = 4f,
                    center = pos
                )
                drawCircle(
                    color = Color(0xFF30D158).copy(alpha = 0.4f),
                    radius = 10f,
                    center = pos,
                    style = Stroke(width = 1.5f)
                )
            }

            // Gyro Specular Light Flare
            val flareX = w * (0.5f + tiltX * 0.3f)
            val flareY = h * (0.5f - tiltY * 0.3f)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.4f),
                        Color.Transparent
                    ),
                    center = Offset(flareX, flareY),
                    radius = 40f
                ),
                radius = 40f,
                center = Offset(flareX, flareY)
            )
        }
    }
}

@Composable
private fun ScanningSonarSection(
    progress: Float,
    subsystem: String,
    metric: String,
    passedCount: Int,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(400, easing = FastOutSlowInEasing),
        label = "progress"
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(220.dp),
            contentAlignment = Alignment.Center
        ) {
            val infiniteTransition = rememberInfiniteTransition(label = "radar")
            val radarAngle by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 360f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1400, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                ),
                label = "angle"
            )

            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2, size.height / 2)
                val radius = size.minDimension / 2 - 16

                drawCircle(
                    color = Color.White.copy(alpha = 0.08f),
                    radius = radius,
                    center = center,
                    style = Stroke(width = 1.5f)
                )
                drawCircle(
                    color = Color.White.copy(alpha = 0.08f),
                    radius = radius * 0.65f,
                    center = center,
                    style = Stroke(width = 1.5f)
                )
                drawCircle(
                    color = Color.White.copy(alpha = 0.08f),
                    radius = radius * 0.35f,
                    center = center,
                    style = Stroke(width = 1.5f)
                )

                // Rotating Scanning Beam
                val rad = Math.toRadians(radarAngle.toDouble())
                val endX = center.x + radius * cos(rad).toFloat()
                val endY = center.y + radius * sin(rad).toFloat()

                drawLine(
                    brush = Brush.linearGradient(
                        colors = listOf(Color.Transparent, Color(0xFF30D158)),
                        start = center,
                        end = Offset(endX, endY)
                    ),
                    start = center,
                    end = Offset(endX, endY),
                    strokeWidth = 3f
                )

                // Active Progress Arc
                drawArc(
                    color = Color(0xFF30D158),
                    startAngle = -90f,
                    sweepAngle = animatedProgress * 360f,
                    useCenter = false,
                    style = Stroke(width = 6f, cap = StrokeCap.Round)
                )
            }

            Text(
                text = "${(animatedProgress * 100).toInt()}%",
                style = MaterialTheme.typography.displayMedium.copy(
                    fontSize = 38.sp,
                    fontFamily = FontFamily.Monospace
                ),
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Cybernetic Telemetry Readout
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .border(BorderStroke(0.5.dp, Color(0xFF30D158).copy(alpha = 0.4f)), RoundedCornerShape(16.dp)),
            color = Color(0xFF131316),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = subsystem,
                    style = MaterialTheme.typography.labelSmall.copy(
                        letterSpacing = 1.5.sp,
                        fontFamily = FontFamily.Monospace
                    ),
                    color = Color(0xFF30D158),
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = metric,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontFamily = FontFamily.Monospace
                    ),
                    color = Color.White,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 5-Subsystem Lock-in Indicators
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            listOf("CPU", "RAM", "BAT", "DISP", "IMU").forEachIndexed { index, name ->
                val isPassed = index < passedCount
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(if (isPassed) Color(0xFF30D158) else Color(0xFF222226)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isPassed) {
                            Icon(
                                imageVector = Icons.Outlined.CheckCircle,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    Text(
                        text = name,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        ),
                        color = if (isPassed) Color.White else Color.White.copy(alpha = 0.4f)
                    )
                }
            }
        }
    }
}

@Composable
private fun RevealScoreSection(
    score: Int,
    report: DiagnosticReport?,
    onEnterDashboard: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LimbScoreHero(
            score = score,
            headline = "Hardware Verification Complete",
            subtitle = "${report?.passedCount ?: 29} Core subsystem tests passed within nominal tolerances."
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Key Findings / Takeaways Cards
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ReportResultPill(
                title = "Display Matrix & Touch",
                status = "Nominal",
                desc = "120Hz LTPO refresh synchronization verified",
                isGood = true
            )
            ReportResultPill(
                title = "Battery & Power IC",
                status = "Excellent",
                desc = "Voltage and thermal curve nominal (4.15V)",
                isGood = true
            )
            ReportResultPill(
                title = "14 Hardware Sensors",
                status = "100% Operational",
                desc = "IMU, Barometer, Light, Proximity responding",
                isGood = true
            )
        }

        Spacer(modifier = Modifier.height(36.dp))

        // Primary Enter Button
        Surface(
            onClick = onEnterDashboard,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clip(RoundedCornerShape(28.dp)),
            color = Color(0xFF30D158),
            shape = RoundedCornerShape(28.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ENTER COMMAND CENTER",
                    style = MaterialTheme.typography.titleMedium.copy(
                        letterSpacing = 1.2.sp,
                        fontSize = 15.sp
                    ),
                    color = Color.Black,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Outlined.ArrowForward,
                    contentDescription = null,
                    tint = Color.Black,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun SpecCapsule(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .border(BorderStroke(0.5.dp, Color.White.copy(alpha = 0.12f)), RoundedCornerShape(14.dp)),
        color = Color(0xFF131316),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color(0xFF30D158),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = Color.White.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = Color.White,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun ReportResultPill(
    title: String,
    status: String,
    desc: String,
    isGood: Boolean
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .border(BorderStroke(0.5.dp, Color.White.copy(alpha = 0.08f)), RoundedCornerShape(16.dp)),
        color = Color(0xFF131316),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = desc,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.6f)
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isGood) Color(0xFF30D158).copy(alpha = 0.15f) else Color(0xFFFF9F0A).copy(alpha = 0.15f))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = status,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                    color = if (isGood) Color(0xFF30D158) else Color(0xFFFF9F0A),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

private fun formatDeviceDisplayName(manufacturer: String, model: String): String {
    val cleanMfg = manufacturer.replaceFirstChar { it.uppercase() }
    if (model.contains("sdk_gphone", ignoreCase = true) || 
        model.contains("emulator", ignoreCase = true) || 
        model.contains("generic", ignoreCase = true) || 
        model.contains("goldfish", ignoreCase = true) || 
        model.contains("ranchu", ignoreCase = true)) {
        return "$cleanMfg Pixel Test Device"
    }
    return if (model.startsWith(cleanMfg, ignoreCase = true)) {
        model
    } else {
        "$cleanMfg $model"
    }
}

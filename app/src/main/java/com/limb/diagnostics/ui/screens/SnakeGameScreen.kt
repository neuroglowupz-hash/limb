package com.limb.diagnostics.ui.screens

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.KeyboardArrowLeft
import androidx.compose.material.icons.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.abs
import kotlin.random.Random

enum class Direction {
    UP, DOWN, LEFT, RIGHT
}

data class Point(val x: Int, val y: Int)

private const val GRID_SIZE = 20
private const val INITIAL_DELAY_MS = 140L

@Composable
fun SnakeGameScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("limb_snake_prefs", Context.MODE_PRIVATE) }
    var highScore by remember { mutableIntStateOf(prefs.getInt("high_score", 0)) }

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
                vibrator?.vibrate(15)
            }
        } catch (_: Exception) {}
    }

    val snake = remember {
        mutableStateListOf(
            Point(GRID_SIZE / 2, GRID_SIZE / 2),
            Point(GRID_SIZE / 2, GRID_SIZE / 2 + 1),
            Point(GRID_SIZE / 2, GRID_SIZE / 2 + 2)
        )
    }

    var food by remember {
        mutableStateOf(Point(5, 5))
    }

    var direction by remember { mutableStateOf(Direction.UP) }
    var nextDirection by remember { mutableStateOf(Direction.UP) }
    var isPlaying by remember { mutableStateOf(true) }
    var isGameOver by remember { mutableStateOf(false) }
    var score by remember { mutableIntStateOf(0) }
    var tickDelayMs by remember { mutableLongStateOf(INITIAL_DELAY_MS) }

    fun generateFood(snakeBody: List<Point>): Point {
        val occupied = snakeBody.toSet()
        val available = mutableListOf<Point>()
        for (x in 0 until GRID_SIZE) {
            for (y in 0 until GRID_SIZE) {
                val p = Point(x, y)
                if (p !in occupied) available.add(p)
            }
        }
        return if (available.isNotEmpty()) {
            available[Random.nextInt(available.size)]
        } else {
            Point(0, 0)
        }
    }

    fun restartGame() {
        snake.clear()
        val startX = GRID_SIZE / 2
        val startY = GRID_SIZE / 2
        snake.addAll(listOf(Point(startX, startY), Point(startX, startY + 1), Point(startX, startY + 2)))
        direction = Direction.UP
        nextDirection = Direction.UP
        score = 0
        tickDelayMs = INITIAL_DELAY_MS
        isGameOver = false
        isPlaying = true
        food = generateFood(snake)
        triggerHaptic(1)
    }

    fun handleDirectionChange(newDir: Direction) {
        val isOpposite = when (newDir) {
            Direction.UP -> direction == Direction.DOWN
            Direction.DOWN -> direction == Direction.UP
            Direction.LEFT -> direction == Direction.RIGHT
            Direction.RIGHT -> direction == Direction.LEFT
        }
        if (!isOpposite && isPlaying && !isGameOver) {
            nextDirection = newDir
            triggerHaptic(0)
        }
    }

    // Game loop
    LaunchedEffect(isPlaying, isGameOver, tickDelayMs) {
        while (isPlaying && !isGameOver) {
            delay(tickDelayMs)
            direction = nextDirection
            val head = snake.first()
            val newHead = when (direction) {
                Direction.UP -> Point(head.x, head.y - 1)
                Direction.DOWN -> Point(head.x, head.y + 1)
                Direction.LEFT -> Point(head.x - 1, head.y)
                Direction.RIGHT -> Point(head.x + 1, head.y)
            }

            // Check boundary or self collisions
            val hitWall = newHead.x < 0 || newHead.x >= GRID_SIZE || newHead.y < 0 || newHead.y >= GRID_SIZE
            val hitSelf = snake.contains(newHead)

            if (hitWall || hitSelf) {
                isGameOver = true
                isPlaying = false
                triggerHaptic(2)
                if (score > highScore) {
                    highScore = score
                    prefs.edit().putInt("high_score", score).apply()
                }
            } else {
                snake.add(0, newHead)
                if (newHead == food) {
                    score += 1
                    triggerHaptic(1)
                    if (score > highScore) {
                        highScore = score
                        prefs.edit().putInt("high_score", score).apply()
                    }
                    food = generateFood(snake)
                    // Slightly increase speed as snake grows
                    tickDelayMs = (INITIAL_DELAY_MS - (score * 2L)).coerceAtLeast(60L)
                } else {
                    snake.removeAt(snake.size - 1)
                }
            }
        }
    }

    // Pulse animation for the retro food cell
    val infiniteTransition = rememberInfiniteTransition(label = "food_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF161616))
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "MONOCHROME SNAKE",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 2.sp
                    ),
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "INPUT LATENCY LAB // B&W",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    ),
                    color = Color.White.copy(alpha = 0.5f)
                )
            }

            IconButton(
                onClick = {
                    if (!isGameOver) isPlaying = !isPlaying
                },
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF161616))
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Outlined.Pause else Icons.Outlined.PlayArrow,
                    contentDescription = "Pause/Play",
                    tint = Color.White
                )
            }
        }

        // Score HUD Board
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ScorePill(label = "SCORE", value = "$score")
            ScorePill(label = "BEST", value = "$highScore")
            ScorePill(label = "LENGTH", value = "${snake.size}")
            ScorePill(label = "SPEED", value = "${1000 / tickDelayMs} Hz")
        }

        // Main Snake Canvas with Drag Gesture Detection
        var dragAccumulatorX by remember { mutableStateOf(0f) }
        var dragAccumulatorY by remember { mutableStateOf(0f) }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF0A0A0A))
                .border(1.5.dp, Color.White.copy(alpha = 0.8f), RoundedCornerShape(12.dp))
                .pointerInput(isPlaying, isGameOver) {
                    detectDragGestures(
                        onDragStart = {
                            dragAccumulatorX = 0f
                            dragAccumulatorY = 0f
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            dragAccumulatorX += dragAmount.x
                            dragAccumulatorY += dragAmount.y
                            val threshold = 35f

                            if (abs(dragAccumulatorX) > abs(dragAccumulatorY)) {
                                if (dragAccumulatorX > threshold) {
                                    handleDirectionChange(Direction.RIGHT)
                                    dragAccumulatorX = 0f
                                    dragAccumulatorY = 0f
                                } else if (dragAccumulatorX < -threshold) {
                                    handleDirectionChange(Direction.LEFT)
                                    dragAccumulatorX = 0f
                                    dragAccumulatorY = 0f
                                }
                            } else {
                                if (dragAccumulatorY > threshold) {
                                    handleDirectionChange(Direction.DOWN)
                                    dragAccumulatorX = 0f
                                    dragAccumulatorY = 0f
                                } else if (dragAccumulatorY < -threshold) {
                                    handleDirectionChange(Direction.UP)
                                    dragAccumulatorX = 0f
                                    dragAccumulatorY = 0f
                                }
                            }
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val cellSize = size.width / GRID_SIZE

                // Subtle Grid Background Lines
                for (i in 0..GRID_SIZE) {
                    drawLine(
                        color = Color.White.copy(alpha = 0.04f),
                        start = Offset(i * cellSize, 0f),
                        end = Offset(i * cellSize, size.height),
                        strokeWidth = 1f
                    )
                    drawLine(
                        color = Color.White.copy(alpha = 0.04f),
                        start = Offset(0f, i * cellSize),
                        end = Offset(size.width, i * cellSize),
                        strokeWidth = 1f
                    )
                }

                // Draw Food Cell (Pulsing White Diamond / Rounded Cell)
                val foodCenter = Offset(
                    food.x * cellSize + cellSize / 2f,
                    food.y * cellSize + cellSize / 2f
                )
                val foodRadius = (cellSize / 2.2f) * pulseScale

                drawCircle(
                    color = Color.White.copy(alpha = 0.3f),
                    radius = cellSize / 1.8f,
                    center = foodCenter,
                    style = Stroke(width = 1.5f)
                )

                drawRoundRect(
                    color = Color.White,
                    topLeft = Offset(foodCenter.x - foodRadius, foodCenter.y - foodRadius),
                    size = Size(foodRadius * 2, foodRadius * 2),
                    cornerRadius = CornerRadius(4f, 4f)
                )

                // Draw Snake Body & Head
                snake.forEachIndexed { index, segment ->
                    val isHead = index == 0
                    val segLeft = segment.x * cellSize + 1.5f
                    val segTop = segment.y * cellSize + 1.5f
                    val segSize = cellSize - 3f

                    if (isHead) {
                        // High-contrast pure white head with slight corner radius
                        drawRoundRect(
                            color = Color.White,
                            topLeft = Offset(segLeft, segTop),
                            size = Size(segSize, segSize),
                            cornerRadius = CornerRadius(6f, 6f)
                        )
                        // Inner black pupil / directional dot
                        val eyeOffset = when (direction) {
                            Direction.UP -> Offset(segLeft + segSize / 2, segTop + segSize / 3)
                            Direction.DOWN -> Offset(segLeft + segSize / 2, segTop + segSize * 2 / 3)
                            Direction.LEFT -> Offset(segLeft + segSize / 3, segTop + segSize / 2)
                            Direction.RIGHT -> Offset(segLeft + segSize * 2 / 3, segTop + segSize / 2)
                        }
                        drawCircle(
                            color = Color.Black,
                            radius = segSize / 5f,
                            center = eyeOffset
                        )
                    } else {
                        // Body segment with slight gradient opacity toward the tail
                        val alpha = (1f - (index.toFloat() / snake.size * 0.45f)).coerceIn(0.4f, 0.95f)
                        drawRoundRect(
                            color = Color.White.copy(alpha = alpha),
                            topLeft = Offset(segLeft, segTop),
                            size = Size(segSize, segSize),
                            cornerRadius = CornerRadius(4f, 4f)
                        )
                    }
                }
            }

            // Game Over Overlay
            AnimatedVisibility(
                visible = isGameOver,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.88f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "GAME OVER",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 3.sp
                            ),
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = "FINAL SCORE: $score",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontFamily = FontFamily.Monospace
                            ),
                            color = Color.White.copy(alpha = 0.8f)
                        )

                        if (score >= highScore && score > 0) {
                            Text(
                                text = "★ NEW HIGH SCORE ★",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontFamily = FontFamily.Monospace,
                                    letterSpacing = 1.sp
                                ),
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Surface(
                            onClick = { restartGame() },
                            modifier = Modifier
                                .clip(RoundedCornerShape(24.dp))
                                .border(1.5.dp, Color.White, RoundedCornerShape(24.dp)),
                            color = Color.White,
                            shape = RoundedCornerShape(24.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Refresh,
                                    contentDescription = null,
                                    tint = Color.Black,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "PLAY AGAIN",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontFamily = FontFamily.Monospace,
                                        letterSpacing = 1.5.sp
                                    ),
                                    color = Color.Black,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // Pause Overlay
            AnimatedVisibility(
                visible = !isPlaying && !isGameOver,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.75f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "PAUSED",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 4.sp
                        ),
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Tactile Monochrome D-Pad Controller
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            // Up Button
            DPadButton(
                icon = Icons.Outlined.KeyboardArrowUp,
                onClick = { handleDirectionChange(Direction.UP) }
            )

            // Middle Row: Left, Restart/Center, Right
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                DPadButton(
                    icon = Icons.Outlined.KeyboardArrowLeft,
                    onClick = { handleDirectionChange(Direction.LEFT) }
                )

                // Quick Restart Center Pill
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF141414))
                        .border(1.dp, Color.White.copy(alpha = 0.3f), CircleShape)
                        .clickable { restartGame() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Refresh,
                        contentDescription = "Restart",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }

                DPadButton(
                    icon = Icons.Outlined.KeyboardArrowRight,
                    onClick = { handleDirectionChange(Direction.RIGHT) }
                )
            }

            // Down Button
            DPadButton(
                icon = Icons.Outlined.KeyboardArrowDown,
                onClick = { handleDirectionChange(Direction.DOWN) }
            )
        }
    }
}

@Composable
private fun ScorePill(
    label: String,
    value: String
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF121212))
            .border(0.8.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontFamily = FontFamily.Monospace,
                fontSize = 9.sp,
                letterSpacing = 1.sp
            ),
            color = Color.White.copy(alpha = 0.5f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall.copy(
                fontFamily = FontFamily.Monospace,
                fontSize = 14.sp
            ),
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun DPadButton(
    icon: ImageVector,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier
            .size(54.dp)
            .clip(RoundedCornerShape(14.dp)),
        color = Color(0xFF181818),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.25f))
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

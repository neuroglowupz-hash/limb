package com.limb.diagnostics.ui.navigation

import android.content.Intent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.limb.diagnostics.data.DiagnosticRepository
import com.limb.diagnostics.model.DiagnosticReport
import com.limb.diagnostics.model.DiagnosticStatus
import com.limb.diagnostics.ui.screens.DeviceInfoScreen
import com.limb.diagnostics.ui.screens.FullCheckScreen
import com.limb.diagnostics.ui.screens.HomeScreen
import com.limb.diagnostics.ui.screens.OnboardingScanScreen
import com.limb.diagnostics.ui.screens.QuickCheckScreen
import com.limb.diagnostics.ui.screens.ReportsScreen
import com.limb.diagnostics.ui.screens.SnakeGameScreen
import com.limb.diagnostics.ui.screens.TestsLibraryScreen
import com.limb.diagnostics.ui.screens.interactive.AudioTestScreen
import com.limb.diagnostics.ui.screens.interactive.BrightnessTestScreen
import com.limb.diagnostics.ui.screens.interactive.CameraTestScreen
import com.limb.diagnostics.ui.screens.interactive.DeadPixelTestScreen
import com.limb.diagnostics.ui.screens.interactive.MultiTouchTestScreen
import com.limb.diagnostics.ui.screens.interactive.RefreshRateTestScreen
import com.limb.diagnostics.ui.screens.interactive.SensorVisualizerScreen
import com.limb.diagnostics.ui.screens.interactive.TelemetryDetailScreen
import com.limb.diagnostics.ui.screens.interactive.TouchGridTestScreen
import com.limb.diagnostics.ui.theme.LimbAppTheme
import com.limb.diagnostics.ui.theme.LimbBottomBar
import com.limb.diagnostics.ui.theme.LimbTheme

@Composable
fun LimbApp(repository: DiagnosticRepository) {
    val context = LocalContext.current
    val systemDark = isSystemInDarkTheme()
    var isDarkMode by remember { mutableStateOf(systemDark) }

    LimbTheme(darkTheme = isDarkMode) {
        val navController = rememberNavController()
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        val allTests by repository.allTests.collectAsState()
        val currentReport by repository.currentReport.collectAsState()
        val deviceInfo by repository.deviceInfo.collectAsState()

        val selectedTab = when (currentRoute) {
            Screen.Home.route -> 0
            Screen.Tests.route -> 1
            Screen.Reports.route -> 2
            else -> 0
        }

        val showBottomBar = currentRoute in listOf(
            Screen.Home.route,
            Screen.Tests.route,
            Screen.Reports.route
        )

        fun shareReport(report: DiagnosticReport) {
            val text = repository.generateShareableSummary(report)
            val sendIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, text)
                putExtra(Intent.EXTRA_TITLE, "Limb Phone Health Report")
                type = "text/plain"
            }
            val shareIntent = Intent.createChooser(sendIntent, "Share Diagnostic Report")
            context.startActivity(shareIntent)
        }

        val startDest = remember {
            if (repository.isOnboardingCompleted()) Screen.Home.route else Screen.OnboardingScan.route
        }

        Scaffold(
            bottomBar = {
                if (showBottomBar) {
                    LimbBottomBar(
                        selectedTab = selectedTab,
                        onTabSelected = { tab ->
                            when (tab) {
                                0 -> navController.navigate(Screen.Home.route) {
                                    popUpTo(Screen.Home.route) { inclusive = true }
                                }
                                1 -> navController.navigate(Screen.Tests.route) {
                                    popUpTo(Screen.Home.route)
                                }
                                2 -> navController.navigate(Screen.Reports.route) {
                                    popUpTo(Screen.Home.route)
                                }
                            }
                        }
                    )
                }
            },
            containerColor = LimbAppTheme.colors.background
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = startDest,
                modifier = Modifier.padding(innerPadding),
                enterTransition = { fadeIn(animationSpec = tween(220)) },
                exitTransition = { fadeOut(animationSpec = tween(180)) }
            ) {
                composable(Screen.OnboardingScan.route) {
                    OnboardingScanScreen(
                        repository = repository,
                        onComplete = {
                            navController.navigate(Screen.Home.route) {
                                popUpTo(Screen.OnboardingScan.route) { inclusive = true }
                            }
                        }
                    )
                }

                composable(Screen.Home.route) {
                    HomeScreen(
                        report = currentReport,
                        onCheckMyPhone = { navController.navigate(Screen.FullCheck.route) },
                        onQuickCheck = { navController.navigate(Screen.QuickCheck.route) },
                        onViewReport = { navController.navigate(Screen.Reports.route) },
                        onDeviceInfo = { navController.navigate(Screen.DeviceInfo.route) },
                        onShareReport = { currentReport?.let { shareReport(it) } },
                        isDarkMode = isDarkMode,
                        onToggleTheme = { isDarkMode = !isDarkMode }
                    )
                }

                composable(Screen.Tests.route) {
                    TestsLibraryScreen(
                        tests = allTests,
                        onTestSelected = { test ->
                            navController.navigate(Screen.TestDetail.createRoute(test.id))
                        },
                        onDeviceInfo = { navController.navigate(Screen.DeviceInfo.route) },
                        isDarkMode = isDarkMode,
                        onToggleTheme = { isDarkMode = !isDarkMode }
                    )
                }

                composable(Screen.Reports.route) {
                    ReportsScreen(
                        report = currentReport,
                        onRunAgain = { navController.navigate(Screen.QuickCheck.route) },
                        onShareReport = { rep -> shareReport(rep) },
                        onDeviceInfo = { navController.navigate(Screen.DeviceInfo.route) },
                        isDarkMode = isDarkMode,
                        onToggleTheme = { isDarkMode = !isDarkMode }
                    )
                }

                composable(Screen.QuickCheck.route) {
                    QuickCheckScreen(
                        repository = repository,
                        onComplete = { navController.popBackStack() },
                        onViewReport = {
                            navController.navigate(Screen.Reports.route) {
                                popUpTo(Screen.Home.route)
                            }
                        },
                        onClose = { navController.popBackStack() }
                    )
                }

                composable(Screen.FullCheck.route) {
                    FullCheckScreen(
                        repository = repository,
                        onComplete = {
                            navController.navigate(Screen.Reports.route) {
                                popUpTo(Screen.Home.route)
                            }
                        },
                        onClose = { navController.popBackStack() }
                    )
                }

                composable(Screen.DeviceInfo.route) {
                    DeviceInfoScreen(
                        deviceInfo = deviceInfo,
                        onClose = { navController.popBackStack() },
                        onRerunApp = {
                            repository.resetAppToInitialState()
                            navController.navigate(Screen.OnboardingScan.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        },
                        onOpenArcade = {
                            navController.navigate(Screen.SnakeArcade.route)
                        }
                    )
                }

                composable(Screen.SnakeArcade.route) {
                    SnakeGameScreen(
                        onBack = { navController.popBackStack() }
                    )
                }

                composable(
                    route = Screen.TestDetail.route,
                    arguments = listOf(navArgument("testId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val testId = backStackEntry.arguments?.getString("testId") ?: ""
                    val test = allTests.firstOrNull { it.id == testId }

                    fun handleFinish(status: DiagnosticStatus, summary: String) {
                        repository.updateTestResult(testId, status, summary)
                        navController.popBackStack()
                    }

                    when (testId) {
                        "display_brightness" -> BrightnessTestScreen(
                            onFinish = { st, sm, metrics ->
                                repository.updateTestResult(testId, st, sm, metrics)
                                navController.popBackStack()
                            },
                            onClose = { navController.popBackStack() }
                        )
                        "display_touch" -> TouchGridTestScreen(
                            onFinish = { st, sm -> handleFinish(st, sm) },
                            onClose = { navController.popBackStack() }
                        )
                        "display_multi_touch" -> MultiTouchTestScreen(
                            onFinish = { st, sm -> handleFinish(st, sm) },
                            onClose = { navController.popBackStack() }
                        )
                        "display_dead_pixel", "display_colors" -> DeadPixelTestScreen(
                            onFinish = { st, sm -> handleFinish(st, sm) },
                            onClose = { navController.popBackStack() }
                        )
                        "display_refresh_rate" -> RefreshRateTestScreen(
                            displayEngine = repository.displayEngine,
                            onFinish = { st, sm -> handleFinish(st, sm) },
                            onClose = { navController.popBackStack() }
                        )
                        "sensor_accelerometer", "sensor_gyroscope", "sensor_proximity", "sensor_ambient_light", "sensor_magnetometer", "sensor_barometer", "sensor_step_counter", "sensor_rotation" -> SensorVisualizerScreen(
                            sensorEngine = repository.sensorEngine,
                            testId = testId,
                            onFinish = { st, sm -> handleFinish(st, sm) },
                            onClose = { navController.popBackStack() }
                        )
                        "audio_speaker", "audio_earpiece", "audio_microphone", "audio_vibration" -> AudioTestScreen(
                            audioEngine = repository.audioEngine,
                            testId = testId,
                            onFinish = { st, sm -> handleFinish(st, sm) },
                            onClose = { navController.popBackStack() }
                        )
                        "camera_rear", "camera_front", "camera_flash", "camera_focus" -> CameraTestScreen(
                            cameraEngine = repository.cameraEngine,
                            testId = testId,
                            onFinish = { st, sm -> handleFinish(st, sm) },
                            onClose = { navController.popBackStack() }
                        )
                        else -> {
                            if (test != null) {
                                TelemetryDetailScreen(
                                    test = test,
                                    onClose = { navController.popBackStack() },
                                    onRunTest = if (!test.requiresInteraction) {
                                        { repository.runSingleAutomatedTest(testId) }
                                    } else null,
                                    onMarkStatus = { st, sm -> handleFinish(st, sm) }
                                )
                            } else {
                                Box(modifier = Modifier.fillMaxSize())
                            }
                        }
                    }
                }
            }
        }
    }
}

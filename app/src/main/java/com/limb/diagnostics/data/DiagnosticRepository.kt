package com.limb.diagnostics.data

import android.content.Context
import android.os.Build
import com.limb.diagnostics.engine.AudioDiagnosticEngine
import com.limb.diagnostics.engine.BatteryDiagnosticEngine
import com.limb.diagnostics.engine.CameraDiagnosticEngine
import com.limb.diagnostics.engine.ConnectivityDiagnosticEngine
import com.limb.diagnostics.engine.DisplayDiagnosticEngine
import com.limb.diagnostics.engine.PerformanceDiagnosticEngine
import com.limb.diagnostics.engine.SensorDiagnosticEngine
import com.limb.diagnostics.engine.SoftwareDiagnosticEngine
import com.limb.diagnostics.model.DeviceInfo
import com.limb.diagnostics.model.DiagnosticMetric
import com.limb.diagnostics.model.DiagnosticReport
import com.limb.diagnostics.model.DiagnosticStatus
import com.limb.diagnostics.model.DiagnosticTest
import com.limb.diagnostics.model.HealthScoreCalculator
import com.limb.diagnostics.model.TestCategory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

class DiagnosticRepository(private val context: Context) {

    val batteryEngine = BatteryDiagnosticEngine(context)
    val sensorEngine = SensorDiagnosticEngine(context)
    val displayEngine = DisplayDiagnosticEngine(context)
    val audioEngine = AudioDiagnosticEngine(context)
    val cameraEngine = CameraDiagnosticEngine(context)
    val connectivityEngine = ConnectivityDiagnosticEngine(context)
    val performanceEngine = PerformanceDiagnosticEngine(context)
    val softwareEngine = SoftwareDiagnosticEngine(context)

    private val _allTests = MutableStateFlow<List<DiagnosticTest>>(emptyList())
    val allTests: StateFlow<List<DiagnosticTest>> = _allTests.asStateFlow()

    private val _currentReport = MutableStateFlow<DiagnosticReport?>(null)
    val currentReport: StateFlow<DiagnosticReport?> = _currentReport.asStateFlow()

    private val _deviceInfo = MutableStateFlow<DeviceInfo?>(null)
    val deviceInfo: StateFlow<DeviceInfo?> = _deviceInfo.asStateFlow()

    init {
        loadInitialCatalog()
        refreshDeviceInfo()
        // Run initial background baseline check so the user immediately gets a real health overview
        runAutomatedDiagnostics()
    }

    fun refreshDeviceInfo() {
        val info = softwareEngine.gatherFullDeviceInfo(
            displayEngine = displayEngine,
            sensorEngine = sensorEngine,
            cameraEngine = cameraEngine,
            perfEngine = performanceEngine
        )
        _deviceInfo.value = info
    }

    private fun loadInitialCatalog() {
        val initialList = listOf(
            // Display
            DiagnosticTest("display_dead_pixel", TestCategory.DISPLAY, "Dead Pixel", "Fullscreen color screens to identify defective subpixels", requiresInteraction = true),
            DiagnosticTest("display_touch", TestCategory.DISPLAY, "Touch Responsiveness", "Touch grid matrix to verify digitizer accuracy", requiresInteraction = true),
            DiagnosticTest("display_multi_touch", TestCategory.DISPLAY, "Multi-touch", "Multi-finger simultaneous touch point detection", requiresInteraction = true),
            DiagnosticTest("display_brightness", TestCategory.DISPLAY, "Brightness", "Smooth luminance modulation across full range", requiresInteraction = true),
            DiagnosticTest("display_refresh_rate", TestCategory.DISPLAY, "Refresh Rate", "Verifies frame rate synchronization and display controller", requiresInteraction = false),
            DiagnosticTest("display_colors", TestCategory.DISPLAY, "Color Calibration", "Primary RGB spectrum and gamma gradient uniformity", requiresInteraction = true),
            DiagnosticTest("display_resolution", TestCategory.DISPLAY, "Screen Resolution", "Native display matrix resolution and DPI density", requiresInteraction = false),

            // Sensors
            DiagnosticTest("sensor_accelerometer", TestCategory.SENSORS, "Accelerometer", "Measures 3D acceleration and gravitational orientation", requiresInteraction = true),
            DiagnosticTest("sensor_gyroscope", TestCategory.SENSORS, "Gyroscope", "Detects angular velocity and rotational motion", requiresInteraction = true),
            DiagnosticTest("sensor_proximity", TestCategory.SENSORS, "Proximity", "Detects screen occlusion near ear", requiresInteraction = true),
            DiagnosticTest("sensor_ambient_light", TestCategory.SENSORS, "Ambient Light", "Measures environmental illuminance in lux", requiresInteraction = true),
            DiagnosticTest("sensor_magnetometer", TestCategory.SENSORS, "Magnetometer", "Measures Earth magnetic field for compass", requiresInteraction = true),
            DiagnosticTest("sensor_barometer", TestCategory.SENSORS, "Barometer", "Measures ambient atmospheric pressure", requiresInteraction = false),
            DiagnosticTest("sensor_rotation", TestCategory.SENSORS, "Rotation Vector", "Fuses sensor quaternion orientation", requiresInteraction = false),
            DiagnosticTest("sensor_step_counter", TestCategory.SENSORS, "Step Counter", "Hardware pedometer step detection", requiresInteraction = false),

            // Camera
            DiagnosticTest("camera_rear", TestCategory.CAMERA, "Rear Camera", "Main camera optics, sensor and viewfinder", requiresInteraction = true),
            DiagnosticTest("camera_front", TestCategory.CAMERA, "Front Camera", "Front selfie camera optics and sensor", requiresInteraction = true),
            DiagnosticTest("camera_focus", TestCategory.CAMERA, "Autofocus", "Tests lens voice coil actuator focus response", requiresInteraction = true),
            DiagnosticTest("camera_flash", TestCategory.CAMERA, "Flash / Torch", "High-intensity LED illumination unit", requiresInteraction = true),
            DiagnosticTest("camera_lenses", TestCategory.CAMERA, "Lens Detection", "Enumeration of optical configurations (Wide/Tele)", requiresInteraction = false),

            // Audio
            DiagnosticTest("audio_speaker", TestCategory.AUDIO, "Speaker", "Tests primary loudspeaker acoustic output", requiresInteraction = true),
            DiagnosticTest("audio_earpiece", TestCategory.AUDIO, "Earpiece", "Tests voice call earpiece speaker", requiresInteraction = true),
            DiagnosticTest("audio_microphone", TestCategory.AUDIO, "Microphone", "Measures voice input amplitude & waveform", requiresInteraction = true),
            DiagnosticTest("audio_vibration", TestCategory.AUDIO, "Vibration", "Tests haptic feedback actuator patterns", requiresInteraction = true),
            DiagnosticTest("audio_headphone_detection", TestCategory.AUDIO, "Headphone Detection", "Detects wired & Bluetooth audio endpoints", requiresInteraction = false),

            // Battery
            DiagnosticTest("battery_health", TestCategory.BATTERY, "Battery Health", "Power management IC cell health report", requiresInteraction = false),
            DiagnosticTest("battery_temperature", TestCategory.BATTERY, "Temperature", "Internal battery thermal sensor monitoring", requiresInteraction = false),
            DiagnosticTest("battery_voltage", TestCategory.BATTERY, "Voltage", "Terminal potential difference in millivolts", requiresInteraction = false),
            DiagnosticTest("battery_charging_state", TestCategory.BATTERY, "Charging State", "Power source detection and charging status", requiresInteraction = false),

            // Connectivity
            DiagnosticTest("conn_wifi", TestCategory.CONNECTIVITY, "Wi-Fi", "WLAN transceiver and link speed", requiresInteraction = false),
            DiagnosticTest("conn_bluetooth", TestCategory.CONNECTIVITY, "Bluetooth", "Bluetooth Core and BLE transceiver", requiresInteraction = false),
            DiagnosticTest("conn_cellular", TestCategory.CONNECTIVITY, "Mobile Network", "Cellular modem and SIM card status", requiresInteraction = false),
            DiagnosticTest("conn_gps", TestCategory.CONNECTIVITY, "GPS / GNSS", "Global satellite navigation receiver", requiresInteraction = false),
            DiagnosticTest("conn_nfc", TestCategory.CONNECTIVITY, "NFC", "Near Field Communication transceiver", requiresInteraction = false),

            // Performance
            DiagnosticTest("perf_cpu", TestCategory.PERFORMANCE, "CPU Architecture", "Core topology and instruction set", requiresInteraction = false),
            DiagnosticTest("perf_ram", TestCategory.PERFORMANCE, "Memory (RAM)", "LPDDR memory capacity and headroom", requiresInteraction = false),
            DiagnosticTest("perf_storage", TestCategory.PERFORMANCE, "Storage", "Internal flash storage capacity and usage", requiresInteraction = false),
            DiagnosticTest("perf_thermal", TestCategory.PERFORMANCE, "Thermal State", "SoC package throttling governor", requiresInteraction = false),

            // Software
            DiagnosticTest("soft_android_version", TestCategory.SOFTWARE, "Android Version", "OS platform release and API level", requiresInteraction = false),
            DiagnosticTest("soft_security_patch", TestCategory.SOFTWARE, "Security Patch", "Vulnerability patch level date", requiresInteraction = false),
            DiagnosticTest("soft_system_health", TestCategory.SOFTWARE, "System Health", "Kernel uptime and runtime stability", requiresInteraction = false),
            DiagnosticTest("soft_device_info", TestCategory.SOFTWARE, "Device Information", "Manufacturer hardware signatures", requiresInteraction = false)
        )
        _allTests.value = initialList
    }

    fun runAutomatedDiagnostics(): DiagnosticReport {
        val current = _allTests.value.toMutableList()

        val batteryTests = batteryEngine.runBatteryTests()
        val sensorTests = sensorEngine.runSensorAutomatedChecks()
        val displayTests = displayEngine.runDisplayAutomatedChecks()
        val audioTests = audioEngine.checkAudioHardware()
        val cameraTests = cameraEngine.runCameraAutomatedChecks()
        val connTests = connectivityEngine.runConnectivityAutomatedChecks()
        val perfTests = performanceEngine.runPerformanceAutomatedChecks()
        val softTests = softwareEngine.runSoftwareAutomatedChecks()

        val automatedResults = (batteryTests + sensorTests + displayTests + audioTests + cameraTests + connTests + perfTests + softTests)
            .associateBy { it.id }

        for (i in current.indices) {
            val t = current[i]
            if (automatedResults.containsKey(t.id)) {
                current[i] = automatedResults[t.id]!!
            }
        }

        _allTests.value = current

        val patch = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) Build.VERSION.SECURITY_PATCH else "N/A"
        val report = HealthScoreCalculator.calculateReport(
            tests = current,
            deviceModel = Build.MODEL,
            manufacturer = Build.MANUFACTURER,
            androidVersion = Build.VERSION.RELEASE,
            securityPatch = patch
        )
        _currentReport.value = report
        saveReportToFile(report)
        return report
    }

    fun updateTestResult(
        testId: String,
        status: DiagnosticStatus,
        resultSummary: String,
        metrics: List<DiagnosticMetric> = emptyList()
    ) {
        val current = _allTests.value.toMutableList()
        val index = current.indexOfFirst { it.id == testId }
        if (index >= 0) {
            val old = current[index]
            current[index] = old.copy(
                status = status,
                resultSummary = resultSummary,
                metrics = if (metrics.isNotEmpty()) metrics else old.metrics,
                timestamp = System.currentTimeMillis()
            )
            _allTests.value = current

            val patch = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) Build.VERSION.SECURITY_PATCH else "N/A"
            val report = HealthScoreCalculator.calculateReport(
                tests = current,
                deviceModel = Build.MODEL,
                manufacturer = Build.MANUFACTURER,
                androidVersion = Build.VERSION.RELEASE,
                securityPatch = patch
            )
            _currentReport.value = report
            saveReportToFile(report)
        }
    }

    fun runSingleAutomatedTest(testId: String): DiagnosticTest? {
        val result: DiagnosticTest? = when (testId) {
            "display_resolution", "display_refresh_rate" -> displayEngine.runDisplayAutomatedChecks().firstOrNull { it.id == testId }
            "sensor_barometer", "sensor_rotation", "sensor_step_counter" -> sensorEngine.runSensorAutomatedChecks().firstOrNull { it.id == testId }
            "camera_lenses" -> cameraEngine.runCameraAutomatedChecks().firstOrNull { it.id == testId }
            "audio_headphone_detection" -> audioEngine.checkAudioHardware().firstOrNull { it.id == testId }
            "battery_health", "battery_temperature", "battery_voltage", "battery_charging_state" -> batteryEngine.runBatteryTests().firstOrNull { it.id == testId }
            "conn_wifi", "conn_bluetooth", "conn_cellular", "conn_gps", "conn_nfc" -> connectivityEngine.runConnectivityAutomatedChecks().firstOrNull { it.id == testId }
            "perf_cpu", "perf_ram", "perf_storage", "perf_thermal" -> performanceEngine.runPerformanceAutomatedChecks().firstOrNull { it.id == testId }
            "soft_android_version", "soft_security_patch", "soft_system_health", "soft_device_info" -> softwareEngine.runSoftwareAutomatedChecks().firstOrNull { it.id == testId }
            else -> null
        }
        if (result != null) {
            updateTestResult(result.id, result.status, result.resultSummary, result.metrics)
        }
        return result
    }

    fun getInteractiveTests(): List<DiagnosticTest> {
        return _allTests.value.filter { it.requiresInteraction }
    }

    private fun saveReportToFile(report: DiagnosticReport) {
        try {
            val file = File(context.filesDir, "reports.json")
            val obj = JSONObject().apply {
                put("id", report.id)
                put("timestamp", report.timestamp)
                put("overallScore", report.overallScore)
                put("deviceModel", report.deviceModel)
                put("manufacturer", report.manufacturer)
                put("androidVersion", report.androidVersion)
                put("securityPatch", report.securityPatch)
                put("passedCount", report.passedCount)
                put("warningCount", report.warningCount)
                put("failedCount", report.failedCount)
                put("notAvailableCount", report.notAvailableCount)

                val findingsArr = JSONArray()
                report.keyFindings.forEach { findingsArr.put(it) }
                put("keyFindings", findingsArr)
            }
            file.writeText(obj.toString())
        } catch (_: Exception) {}
    }

    fun generateShareableSummary(report: DiagnosticReport): String {
        val sb = StringBuilder()
        sb.appendLine("━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        sb.appendLine("LIMB PHONE DIAGNOSTIC REPORT")
        sb.appendLine("━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        sb.appendLine("Device: ${report.manufacturer} ${report.deviceModel}")
        sb.appendLine("Android: ${report.androidVersion} | Patch: ${report.securityPatch}")
        sb.appendLine("Overall Health Score: ${report.overallScore} / 100")
        sb.appendLine("Summary: ${report.passedCount} Passed, ${report.warningCount} Warnings, ${report.failedCount} Failed, ${report.notAvailableCount} N/A")
        sb.appendLine()
        sb.appendLine("CATEGORY BREAKDOWN:")
        report.categoryHealths.forEach { cat ->
            sb.appendLine("• ${cat.category.title.padEnd(14)}: ${cat.score}/100 (${cat.statusText})")
        }
        sb.appendLine()
        if (report.keyFindings.isNotEmpty()) {
            sb.appendLine("KEY FINDINGS:")
            report.keyFindings.forEach { finding ->
                sb.appendLine("• $finding")
            }
            sb.appendLine()
        }
        sb.appendLine("Verified via Limb Mobile Diagnostics.")
        sb.appendLine("━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        return sb.toString()
    }

    fun isOnboardingCompleted(): Boolean {
        val prefs = context.getSharedPreferences("limb_preferences", Context.MODE_PRIVATE)
        return prefs.getBoolean("onboarding_completed", false)
    }

    fun setOnboardingCompleted(completed: Boolean) {
        val prefs = context.getSharedPreferences("limb_preferences", Context.MODE_PRIVATE)
        prefs.edit().putBoolean("onboarding_completed", completed).apply()
    }

    fun resetAppToInitialState() {
        setOnboardingCompleted(false)
        loadInitialCatalog()
        _currentReport.value = null
    }
}

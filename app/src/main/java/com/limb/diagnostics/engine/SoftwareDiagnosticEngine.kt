package com.limb.diagnostics.engine

import android.content.Context
import android.os.Build
import android.os.SystemClock
import com.limb.diagnostics.model.DeviceInfo
import com.limb.diagnostics.model.DiagnosticMetric
import com.limb.diagnostics.model.DiagnosticStatus
import com.limb.diagnostics.model.DiagnosticTest
import com.limb.diagnostics.model.TestCategory

class SoftwareDiagnosticEngine(private val context: Context) {

    fun getUptimeHours(): String {
        val millis = SystemClock.elapsedRealtime()
        val hours = millis / (1000 * 60 * 60)
        val minutes = (millis / (1000 * 60)) % 60
        return "${hours}h ${minutes}m"
    }

    fun getKernelVersion(): String {
        return try {
            System.getProperty("os.version") ?: "Linux"
        } catch (_: Exception) {
            "Linux"
        }
    }

    fun runSoftwareAutomatedChecks(): List<DiagnosticTest> {
        val tests = mutableListOf<DiagnosticTest>()

        // 1. Android Version
        tests.add(
            DiagnosticTest(
                id = "soft_android_version",
                category = TestCategory.SOFTWARE,
                name = "Android Version",
                description = "Operating system platform release and API level",
                requiresInteraction = false,
                status = DiagnosticStatus.PASSED,
                resultSummary = "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT}) verified.",
                metrics = listOf(
                    DiagnosticMetric("Android Version", "Android ${Build.VERSION.RELEASE}", isPrimary = true),
                    DiagnosticMetric("API Level", "${Build.VERSION.SDK_INT}"),
                    DiagnosticMetric("Codename", Build.VERSION.CODENAME)
                ),
                timestamp = System.currentTimeMillis()
            )
        )

        // 2. Security Patch
        val patch = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Build.VERSION.SECURITY_PATCH
        } else {
            "N/A"
        }
        tests.add(
            DiagnosticTest(
                id = "soft_security_patch",
                category = TestCategory.SOFTWARE,
                name = "Security Patch",
                description = "Android platform security vulnerability update date",
                requiresInteraction = false,
                status = DiagnosticStatus.PASSED,
                resultSummary = "Security patch date: $patch.",
                metrics = listOf(
                    DiagnosticMetric("Security Patch", patch, isPrimary = true)
                ),
                timestamp = System.currentTimeMillis()
            )
        )

        // 3. System Health & Uptime
        val uptime = getUptimeHours()
        tests.add(
            DiagnosticTest(
                id = "soft_system_health",
                category = TestCategory.SOFTWARE,
                name = "System Health",
                description = "Core runtime stability and kernel uptime",
                requiresInteraction = false,
                status = DiagnosticStatus.PASSED,
                resultSummary = "Kernel active with system uptime of $uptime.",
                metrics = listOf(
                    DiagnosticMetric("Uptime", uptime, isPrimary = true),
                    DiagnosticMetric("Kernel", getKernelVersion())
                ),
                timestamp = System.currentTimeMillis()
            )
        )

        // 4. Device Information
        tests.add(
            DiagnosticTest(
                id = "soft_device_info",
                category = TestCategory.SOFTWARE,
                name = "Device Information",
                description = "Manufacturer hardware identity and board signatures",
                requiresInteraction = false,
                status = DiagnosticStatus.PASSED,
                resultSummary = "${Build.MANUFACTURER} ${Build.MODEL} (${Build.BRAND}).",
                metrics = listOf(
                    DiagnosticMetric("Model", Build.MODEL, isPrimary = true),
                    DiagnosticMetric("Manufacturer", Build.MANUFACTURER),
                    DiagnosticMetric("Board", Build.BOARD)
                ),
                timestamp = System.currentTimeMillis()
            )
        )

        return tests
    }

    fun gatherFullDeviceInfo(
        displayEngine: DisplayDiagnosticEngine,
        sensorEngine: SensorDiagnosticEngine,
        cameraEngine: CameraDiagnosticEngine,
        perfEngine: PerformanceDiagnosticEngine
    ): DeviceInfo {
        val displaySpecs = displayEngine.getDisplaySpecs()
        val memInfo = perfEngine.getMemoryInfo()
        val totalRamGb = String.format("%.1f GB", memInfo.totalMem / (1024.0 * 1024.0 * 1024.0))
        val availRamGb = String.format("%.1f GB", memInfo.availMem / (1024.0 * 1024.0 * 1024.0))

        val (totalStorageBytes, availStorageBytes) = perfEngine.getStorageInfo()
        val totalStorageGb = String.format("%.1f GB", totalStorageBytes / (1024.0 * 1024.0 * 1024.0))
        val freeStorageGb = String.format("%.1f GB", availStorageBytes / (1024.0 * 1024.0 * 1024.0))

        val sensors = sensorEngine.getAllAvailableSensorNames()
        val cameras = cameraEngine.inspectCameras().map {
            "${it.facing} ${String.format("%.1f", it.megapixels)} MP (Flash: ${if (it.hasFlash) "Yes" else "No"})"
        }

        val patch = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Build.VERSION.SECURITY_PATCH
        } else {
            "N/A"
        }

        return DeviceInfo(
            model = Build.MODEL,
            manufacturer = Build.MANUFACTURER,
            brand = Build.BRAND,
            board = Build.BOARD,
            hardware = Build.HARDWARE,
            androidVersion = Build.VERSION.RELEASE,
            apiLevel = Build.VERSION.SDK_INT,
            securityPatch = patch,
            kernelVersion = getKernelVersion(),
            buildId = Build.DISPLAY,
            uptimeHours = getUptimeHours(),
            cpuAbi = Build.SUPPORTED_ABIS.firstOrNull() ?: "arm64-v8a",
            cpuCores = perfEngine.getCpuCores(),
            totalRamGb = totalRamGb,
            availableRamGb = availRamGb,
            totalStorageGb = totalStorageGb,
            freeStorageGb = freeStorageGb,
            displayResolution = "${displaySpecs.widthPixels} × ${displaySpecs.heightPixels}",
            displayDensityDpi = displaySpecs.densityDpi,
            refreshRateHz = displaySpecs.refreshRateHz,
            hdrSupported = displaySpecs.isHdrSupported,
            sensorsAvailable = sensors,
            camerasAvailable = cameras
        )
    }
}

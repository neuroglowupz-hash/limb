package com.limb.diagnostics.engine

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import android.os.Environment
import android.os.PowerManager
import android.os.StatFs
import com.limb.diagnostics.model.DiagnosticMetric
import com.limb.diagnostics.model.DiagnosticStatus
import com.limb.diagnostics.model.DiagnosticTest
import com.limb.diagnostics.model.TestCategory

class PerformanceDiagnosticEngine(private val context: Context) {

    private val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    private val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager

    fun getMemoryInfo(): ActivityManager.MemoryInfo {
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)
        return memoryInfo
    }

    fun getStorageInfo(): Pair<Long, Long> {
        val path = Environment.getDataDirectory()
        val stat = StatFs(path.path)
        val blockSize = stat.blockSizeLong
        val totalBlocks = stat.blockCountLong
        val availableBlocks = stat.availableBlocksLong

        val totalBytes = totalBlocks * blockSize
        val availableBytes = availableBlocks * blockSize
        return Pair(totalBytes, availableBytes)
    }

    fun getCpuCores(): Int {
        return Runtime.getRuntime().availableProcessors()
    }

    fun getThermalStatus(): Pair<Int, String> {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && powerManager != null) {
            val status = powerManager.currentThermalStatus
            val statusStr = when (status) {
                PowerManager.THERMAL_STATUS_NONE -> "Nominal"
                PowerManager.THERMAL_STATUS_LIGHT -> "Light Throttling"
                PowerManager.THERMAL_STATUS_MODERATE -> "Moderate Throttling"
                PowerManager.THERMAL_STATUS_SEVERE -> "Severe Throttling"
                PowerManager.THERMAL_STATUS_CRITICAL -> "Critical Throttling"
                PowerManager.THERMAL_STATUS_EMERGENCY -> "Emergency Shutdown"
                PowerManager.THERMAL_STATUS_SHUTDOWN -> "Thermal Shutdown"
                else -> "Nominal"
            }
            return Pair(status, statusStr)
        }
        return Pair(0, "Nominal")
    }

    fun runPerformanceAutomatedChecks(): List<DiagnosticTest> {
        val tests = mutableListOf<DiagnosticTest>()

        // 1. CPU Check
        val cores = getCpuCores()
        val abi = Build.SUPPORTED_ABIS.firstOrNull() ?: "Unknown"
        tests.add(
            DiagnosticTest(
                id = "perf_cpu",
                category = TestCategory.PERFORMANCE,
                name = "CPU Core Architecture",
                description = "Processor multi-core topology and instruction set",
                requiresInteraction = false,
                status = DiagnosticStatus.PASSED,
                resultSummary = "$cores active cores detected ($abi).",
                metrics = listOf(
                    DiagnosticMetric("Cores", "$cores Cores", isPrimary = true),
                    DiagnosticMetric("Architecture", abi),
                    DiagnosticMetric("Platform", Build.HARDWARE)
                ),
                timestamp = System.currentTimeMillis()
            )
        )

        // 2. RAM Memory Check
        val memInfo = getMemoryInfo()
        val totalRamGb = memInfo.totalMem / (1024.0 * 1024.0 * 1024.0)
        val availRamGb = memInfo.availMem / (1024.0 * 1024.0 * 1024.0)
        val usedRamPercent = (((memInfo.totalMem - memInfo.availMem).toDouble() / memInfo.totalMem) * 100).toInt()

        val ramStatus = if (memInfo.lowMemory || usedRamPercent > 92) DiagnosticStatus.WARNING else DiagnosticStatus.PASSED
        val ramSummary = if (ramStatus == DiagnosticStatus.PASSED) {
            "${String.format("%.1f", availRamGb)} GB free of ${String.format("%.1f", totalRamGb)} GB ($usedRamPercent% in use)."
        } else {
            "System memory is under heavy load ($usedRamPercent% in use)."
        }

        tests.add(
            DiagnosticTest(
                id = "perf_ram",
                category = TestCategory.PERFORMANCE,
                name = "System Memory (RAM)",
                description = "LPDDR memory capacity and operating headroom",
                requiresInteraction = false,
                status = ramStatus,
                resultSummary = ramSummary,
                metrics = listOf(
                    DiagnosticMetric("Available RAM", "${String.format("%.1f", availRamGb)} GB", isPrimary = true),
                    DiagnosticMetric("Total RAM", "${String.format("%.1f", totalRamGb)} GB"),
                    DiagnosticMetric("Used", "$usedRamPercent%")
                ),
                timestamp = System.currentTimeMillis()
            )
        )

        // 3. Storage Check
        val (totalStorageBytes, availStorageBytes) = getStorageInfo()
        val totalStorageGb = totalStorageBytes / (1024.0 * 1024.0 * 1024.0)
        val availStorageGb = availStorageBytes / (1024.0 * 1024.0 * 1024.0)
        val usedStoragePercent = (((totalStorageBytes - availStorageBytes).toDouble() / totalStorageBytes) * 100).toInt()

        val storageStatus = when {
            usedStoragePercent > 95 -> DiagnosticStatus.WARNING
            availStorageGb < 2.0 -> DiagnosticStatus.WARNING
            else -> DiagnosticStatus.PASSED
        }
        val storageSummary = if (storageStatus == DiagnosticStatus.PASSED) {
            "${String.format("%.1f", availStorageGb)} GB available of ${String.format("%.1f", totalStorageGb)} GB ($usedStoragePercent% used)."
        } else {
            "Internal storage is almost full (${String.format("%.1f", availStorageGb)} GB free)."
        }

        tests.add(
            DiagnosticTest(
                id = "perf_storage",
                category = TestCategory.PERFORMANCE,
                name = "Internal Storage",
                description = "UFS / eMMC flash storage capacity and health",
                requiresInteraction = false,
                status = storageStatus,
                resultSummary = storageSummary,
                metrics = listOf(
                    DiagnosticMetric("Free Storage", "${String.format("%.1f", availStorageGb)} GB", isPrimary = true),
                    DiagnosticMetric("Total Storage", "${String.format("%.1f", totalStorageGb)} GB"),
                    DiagnosticMetric("Used", "$usedStoragePercent%")
                ),
                timestamp = System.currentTimeMillis()
            )
        )

        // 4. Thermal State
        val (thermalCode, thermalStr) = getThermalStatus()
        val thermalDiagnosticStatus = if (thermalCode >= 3) DiagnosticStatus.WARNING else DiagnosticStatus.PASSED
        tests.add(
            DiagnosticTest(
                id = "perf_thermal",
                category = TestCategory.PERFORMANCE,
                name = "Thermal State",
                description = "SoC package temperature governor and throttling state",
                requiresInteraction = false,
                status = thermalDiagnosticStatus,
                resultSummary = "SoC thermal governor status: $thermalStr.",
                metrics = listOf(
                    DiagnosticMetric("Thermal State", thermalStr, isPrimary = true)
                ),
                timestamp = System.currentTimeMillis()
            )
        )

        return tests
    }
}

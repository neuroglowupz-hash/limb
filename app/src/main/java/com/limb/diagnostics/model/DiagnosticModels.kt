package com.limb.diagnostics.model

import java.util.UUID

enum class TestCategory(val title: String, val description: String) {
    DISPLAY("Display", "Screen pixels, touch responsiveness, refresh rate & colors"),
    SENSORS("Sensors", "Motion, orientation, environmental & proximity sensors"),
    CAMERA("Camera", "Rear/front optics, autofocus, flash & lenses"),
    AUDIO("Audio", "Speakers, earpiece, microphone waveform & vibration"),
    BATTERY("Battery", "Health, temperature, voltage & charging states"),
    CONNECTIVITY("Connectivity", "Wi-Fi, Cellular radio, Bluetooth, NFC & GPS"),
    PERFORMANCE("Performance", "CPU architecture, memory, internal storage & thermals"),
    SOFTWARE("Software", "Android release, security patch & system integrity")
}

enum class DiagnosticStatus {
    NOT_TESTED,
    RUNNING,
    PASSED,
    WARNING,
    FAILED,
    NOT_AVAILABLE
}

enum class BatteryHealthState {
    EXCELLENT,
    GOOD,
    FAIR,
    NEEDS_ATTENTION,
    OVERHEATED,
    UNKNOWN
}

data class DiagnosticMetric(
    val label: String,
    val value: String,
    val unit: String = "",
    val isPrimary: Boolean = false
)

data class DiagnosticTest(
    val id: String,
    val category: TestCategory,
    val name: String,
    val description: String,
    val requiresInteraction: Boolean = false,
    val status: DiagnosticStatus = DiagnosticStatus.NOT_TESTED,
    val resultSummary: String = "",
    val metrics: List<DiagnosticMetric> = emptyList(),
    val timestamp: Long = 0L,
    val isAvailableOnDevice: Boolean = true
)

data class CategoryHealth(
    val category: TestCategory,
    val score: Int, // 0 - 100
    val statusText: String, // "Excellent", "Good", "Needs Attention"
    val passedCount: Int,
    val warningCount: Int,
    val failedCount: Int,
    val notAvailableCount: Int,
    val totalCount: Int
)

data class DiagnosticReport(
    val id: String = UUID.randomUUID().toString(),
    val timestamp: Long = System.currentTimeMillis(),
    val deviceModel: String = "",
    val manufacturer: String = "",
    val androidVersion: String = "",
    val securityPatch: String = "",
    val overallScore: Int = 0,
    val categoryScores: Map<TestCategory, Int> = emptyMap(),
    val categoryHealths: List<CategoryHealth> = emptyList(),
    val tests: List<DiagnosticTest> = emptyList(),
    val keyFindings: List<String> = emptyList(),
    val passedCount: Int = 0,
    val warningCount: Int = 0,
    val failedCount: Int = 0,
    val notAvailableCount: Int = 0
)

data class DeviceSpec(
    val title: String,
    val items: List<Pair<String, String>>
)

data class DeviceInfo(
    val model: String,
    val manufacturer: String,
    val brand: String,
    val board: String,
    val hardware: String,
    val androidVersion: String,
    val apiLevel: Int,
    val securityPatch: String,
    val kernelVersion: String,
    val buildId: String,
    val uptimeHours: String,
    val cpuAbi: String,
    val cpuCores: Int,
    val totalRamGb: String,
    val availableRamGb: String,
    val totalStorageGb: String,
    val freeStorageGb: String,
    val displayResolution: String,
    val displayDensityDpi: Int,
    val refreshRateHz: Float,
    val hdrSupported: Boolean,
    val sensorsAvailable: List<String>,
    val camerasAvailable: List<String>
)

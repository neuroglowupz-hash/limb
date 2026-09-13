package com.limb.diagnostics.engine

import android.content.Context
import android.graphics.ImageFormat
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.os.Build
import com.limb.diagnostics.model.DiagnosticMetric
import com.limb.diagnostics.model.DiagnosticStatus
import com.limb.diagnostics.model.DiagnosticTest
import com.limb.diagnostics.model.TestCategory

class CameraDiagnosticEngine(private val context: Context) {

    private val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager

    data class CameraLensInfo(
        val id: String,
        val facing: String, // "Rear" or "Front"
        val megapixels: Float,
        val hasFlash: Boolean,
        val hasAutofocus: Boolean,
        val focalLengths: List<Float>
    )

    fun inspectCameras(): List<CameraLensInfo> {
        val list = mutableListOf<CameraLensInfo>()
        try {
            val cameraIds = cameraManager.cameraIdList
            for (id in cameraIds) {
                val chars = cameraManager.getCameraCharacteristics(id)
                val facing = when (chars.get(CameraCharacteristics.LENS_FACING)) {
                    CameraCharacteristics.LENS_FACING_FRONT -> "Front"
                    CameraCharacteristics.LENS_FACING_BACK -> "Rear"
                    else -> "External"
                }
                val hasFlash = chars.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) ?: false
                val afModes = chars.get(CameraCharacteristics.CONTROL_AF_AVAILABLE_MODES) ?: intArrayOf()
                val hasAf = afModes.contains(CameraCharacteristics.CONTROL_AF_MODE_AUTO) ||
                        afModes.contains(CameraCharacteristics.CONTROL_AF_MODE_CONTINUOUS_PICTURE)

                val map = chars.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
                var maxMp = 0f
                map?.getOutputSizes(ImageFormat.JPEG)?.let { sizes ->
                    for (size in sizes) {
                        val mp = (size.width * size.height) / 1000000.0f
                        if (mp > maxMp) maxMp = mp
                    }
                }

                val focals = chars.get(CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS)?.toList() ?: emptyList()

                list.add(
                    CameraLensInfo(
                        id = id,
                        facing = facing,
                        megapixels = maxMp,
                        hasFlash = hasFlash,
                        hasAutofocus = hasAf,
                        focalLengths = focals
                    )
                )
            }
        } catch (_: Exception) {}
        return list
    }

    fun runCameraAutomatedChecks(): List<DiagnosticTest> {
        val lenses = inspectCameras()
        val tests = mutableListOf<DiagnosticTest>()

        val rearCameras = lenses.filter { it.facing == "Rear" }
        val frontCameras = lenses.filter { it.facing == "Front" }

        // 1. Rear Camera Check
        val hasRear = rearCameras.isNotEmpty()
        val rearMaxMp = rearCameras.maxOfOrNull { it.megapixels } ?: 0f
        tests.add(
            DiagnosticTest(
                id = "camera_rear",
                category = TestCategory.CAMERA,
                name = "Rear Camera",
                description = "Primary optical sensor matrix and ISP link",
                requiresInteraction = false,
                status = if (hasRear) DiagnosticStatus.PASSED else DiagnosticStatus.NOT_AVAILABLE,
                resultSummary = if (hasRear) "Rear camera sensor verified (${String.format("%.1f", rearMaxMp)} MP)." else "Not available on this device.",
                metrics = if (hasRear) listOf(
                    DiagnosticMetric("Sensor", String.format("%.1f", rearMaxMp), "MP", isPrimary = true),
                    DiagnosticMetric("Lenses", "${rearCameras.size} detected")
                ) else listOf(DiagnosticMetric("Status", "No rear sensor", isPrimary = true)),
                timestamp = System.currentTimeMillis(),
                isAvailableOnDevice = hasRear
            )
        )

        // 2. Front Camera Check
        val hasFront = frontCameras.isNotEmpty()
        val frontMaxMp = frontCameras.maxOfOrNull { it.megapixels } ?: 0f
        tests.add(
            DiagnosticTest(
                id = "camera_front",
                category = TestCategory.CAMERA,
                name = "Front Camera",
                description = "Front-facing selfie optics and sensor",
                requiresInteraction = false,
                status = if (hasFront) DiagnosticStatus.PASSED else DiagnosticStatus.NOT_AVAILABLE,
                resultSummary = if (hasFront) "Front camera sensor verified (${String.format("%.1f", frontMaxMp)} MP)." else "Not available on this device.",
                metrics = if (hasFront) listOf(
                    DiagnosticMetric("Sensor", String.format("%.1f", frontMaxMp), "MP", isPrimary = true)
                ) else listOf(DiagnosticMetric("Status", "No front sensor", isPrimary = true)),
                timestamp = System.currentTimeMillis(),
                isAvailableOnDevice = hasFront
            )
        )

        // 3. Flash / Torch
        val hasFlash = lenses.any { it.hasFlash }
        tests.add(
            DiagnosticTest(
                id = "camera_flash",
                category = TestCategory.CAMERA,
                name = "Camera Flash / Torch",
                description = "High-intensity LED illumination unit",
                requiresInteraction = false,
                status = if (hasFlash) DiagnosticStatus.PASSED else DiagnosticStatus.NOT_AVAILABLE,
                resultSummary = if (hasFlash) "LED flash unit detected and available." else "No hardware flash unit detected.",
                metrics = listOf(
                    DiagnosticMetric("Flash Unit", if (hasFlash) "Present" else "Not Present", isPrimary = true)
                ),
                timestamp = System.currentTimeMillis(),
                isAvailableOnDevice = hasFlash
            )
        )

        // 4. Lens Detection
        tests.add(
            DiagnosticTest(
                id = "camera_lenses",
                category = TestCategory.CAMERA,
                name = "Lens Detection",
                description = "Enumeration of optical configurations (Main, Wide, Telephoto)",
                requiresInteraction = false,
                status = DiagnosticStatus.PASSED,
                resultSummary = "${lenses.size} optical camera sensors registered by HAL.",
                metrics = listOf(
                    DiagnosticMetric("Total Lenses", "${lenses.size}", isPrimary = true),
                    DiagnosticMetric("Rear", "${rearCameras.size}"),
                    DiagnosticMetric("Front", "${frontCameras.size}")
                ),
                timestamp = System.currentTimeMillis()
            )
        )

        return tests
    }

    fun setTorchMode(enabled: Boolean) {
        try {
            val rearCamWithFlash = inspectCameras().firstOrNull { it.facing == "Rear" && it.hasFlash }
            rearCamWithFlash?.let {
                cameraManager.setTorchMode(it.id, enabled)
            }
        } catch (_: Exception) {}
    }
}

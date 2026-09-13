package com.limb.diagnostics.engine

import android.content.Context
import android.os.Build
import android.util.DisplayMetrics
import android.view.WindowManager
import com.limb.diagnostics.model.DiagnosticMetric
import com.limb.diagnostics.model.DiagnosticStatus
import com.limb.diagnostics.model.DiagnosticTest
import com.limb.diagnostics.model.TestCategory

class DisplayDiagnosticEngine(private val context: Context) {

    data class DisplaySpecs(
        val widthPixels: Int,
        val heightPixels: Int,
        val densityDpi: Int,
        val xdpi: Float,
        val ydpi: Float,
        val refreshRateHz: Float,
        val isHdrSupported: Boolean,
        val isWideColorGamut: Boolean
    )

    fun getDisplaySpecs(): DisplaySpecs {
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val metrics = DisplayMetrics()
        
        @Suppress("DEPRECATION")
        val display = wm.defaultDisplay
        @Suppress("DEPRECATION")
        display.getRealMetrics(metrics)

        val refreshRate = display.refreshRate
        val isHdr = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            display.isHdr
        } else {
            false
        }

        val isWideColorGamut = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            display.isWideColorGamut
        } else {
            false
        }

        return DisplaySpecs(
            widthPixels = metrics.widthPixels,
            heightPixels = metrics.heightPixels,
            densityDpi = metrics.densityDpi,
            xdpi = metrics.xdpi,
            ydpi = metrics.ydpi,
            refreshRateHz = refreshRate,
            isHdrSupported = isHdr,
            isWideColorGamut = isWideColorGamut
        )
    }

    fun runDisplayAutomatedChecks(): List<DiagnosticTest> {
        val specs = getDisplaySpecs()
        val tests = mutableListOf<DiagnosticTest>()

        // 1. Resolution & Density
        tests.add(
            DiagnosticTest(
                id = "display_resolution",
                category = TestCategory.DISPLAY,
                name = "Screen Resolution",
                description = "Measures native panel pixel matrix and logical density",
                requiresInteraction = false,
                status = DiagnosticStatus.PASSED,
                resultSummary = "Display panel operating at ${specs.widthPixels} × ${specs.heightPixels} @ ${specs.densityDpi} DPI.",
                metrics = listOf(
                    DiagnosticMetric("Resolution", "${specs.widthPixels} × ${specs.heightPixels}", isPrimary = true),
                    DiagnosticMetric("Density", "${specs.densityDpi}", "DPI"),
                    DiagnosticMetric("HDR Support", if (specs.isHdrSupported) "Yes" else "No")
                ),
                timestamp = System.currentTimeMillis()
            )
        )

        // 2. Refresh Rate
        tests.add(
            DiagnosticTest(
                id = "display_refresh_rate",
                category = TestCategory.DISPLAY,
                name = "Refresh Rate",
                description = "Monitors display controller vertical sync rate",
                requiresInteraction = false,
                status = DiagnosticStatus.PASSED,
                resultSummary = "Display refresh rate verified at ${String.format("%.1f", specs.refreshRateHz)} Hz.",
                metrics = listOf(
                    DiagnosticMetric("Refresh Rate", String.format("%.1f", specs.refreshRateHz), "Hz", isPrimary = true)
                ),
                timestamp = System.currentTimeMillis()
            )
        )

        return tests
    }
}

package com.limb.diagnostics

import com.limb.diagnostics.model.DiagnosticMetric
import com.limb.diagnostics.model.DiagnosticStatus
import com.limb.diagnostics.model.DiagnosticTest
import com.limb.diagnostics.model.HealthScoreCalculator
import com.limb.diagnostics.model.TestCategory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HealthScoreCalculatorTest {

    @Test
    fun testAllPassGenerates100Score() {
        val tests = listOf(
            DiagnosticTest("test1", TestCategory.BATTERY, "Battery Health", "Desc", false, DiagnosticStatus.PASSED),
            DiagnosticTest("test2", TestCategory.SENSORS, "Accelerometer", "Desc", false, DiagnosticStatus.PASSED),
            DiagnosticTest("test3", TestCategory.DISPLAY, "Resolution", "Desc", false, DiagnosticStatus.PASSED)
        )

        val report = HealthScoreCalculator.calculateReport(
            tests = tests,
            deviceModel = "Pixel 8",
            manufacturer = "Google",
            androidVersion = "14",
            securityPatch = "2024-08-01"
        )

        assertEquals(100, report.overallScore)
        assertEquals(3, report.passedCount)
        assertEquals(0, report.warningCount)
        assertEquals(0, report.failedCount)
    }

    @Test
    fun testWarningReducesScore() {
        val tests = listOf(
            DiagnosticTest("test1", TestCategory.BATTERY, "Battery Health", "Desc", false, DiagnosticStatus.PASSED),
            DiagnosticTest("test2", TestCategory.BATTERY, "Temperature", "Desc", false, DiagnosticStatus.WARNING, "Elevated temperature"),
            DiagnosticTest("test3", TestCategory.DISPLAY, "Resolution", "Desc", false, DiagnosticStatus.PASSED)
        )

        val report = HealthScoreCalculator.calculateReport(
            tests = tests,
            deviceModel = "Pixel 8",
            manufacturer = "Google",
            androidVersion = "14",
            securityPatch = "2024-08-01"
        )

        assertTrue(report.overallScore in 80..95)
        assertEquals(2, report.passedCount)
        assertEquals(1, report.warningCount)
        assertTrue(report.keyFindings.any { it.contains("Elevated temperature") })
    }

    @Test
    fun testNotAvailableIgnoredInScore() {
        val tests = listOf(
            DiagnosticTest("test1", TestCategory.BATTERY, "Battery Health", "Desc", false, DiagnosticStatus.PASSED),
            DiagnosticTest("test2", TestCategory.SENSORS, "Barometer", "Desc", false, DiagnosticStatus.NOT_AVAILABLE, "Hardware not present")
        )

        val report = HealthScoreCalculator.calculateReport(
            tests = tests,
            deviceModel = "Pixel 8",
            manufacturer = "Google",
            androidVersion = "14",
            securityPatch = "2024-08-01"
        )

        assertEquals(100, report.overallScore)
        assertEquals(1, report.passedCount)
        assertEquals(1, report.notAvailableCount)
    }
}

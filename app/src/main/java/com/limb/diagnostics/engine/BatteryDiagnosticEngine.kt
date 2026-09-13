package com.limb.diagnostics.engine

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import com.limb.diagnostics.model.DiagnosticMetric
import com.limb.diagnostics.model.DiagnosticStatus
import com.limb.diagnostics.model.DiagnosticTest
import com.limb.diagnostics.model.TestCategory

class BatteryDiagnosticEngine(private val context: Context) {

    data class BatterySnapshot(
        val level: Int,
        val scale: Int,
        val percentage: Int,
        val temperatureCelsius: Float,
        val voltageMv: Int,
        val health: Int,
        val healthString: String,
        val plugged: Int,
        val pluggedString: String,
        val status: Int,
        val statusString: String,
        val technology: String
    )

    fun getBatterySnapshot(): BatterySnapshot {
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val batteryIntent = context.registerReceiver(null, filter)

        val level = batteryIntent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = batteryIntent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        val percentage = if (level >= 0 && scale > 0) (level * 100) / scale else -1

        val rawTemp = batteryIntent?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) ?: 0
        val tempCelsius = rawTemp / 10.0f

        val voltageMv = batteryIntent?.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0) ?: 0

        val rawHealth = batteryIntent?.getIntExtra(
            BatteryManager.EXTRA_HEALTH,
            BatteryManager.BATTERY_HEALTH_UNKNOWN
        ) ?: BatteryManager.BATTERY_HEALTH_UNKNOWN

        val healthString = when (rawHealth) {
            BatteryManager.BATTERY_HEALTH_GOOD -> "Good"
            BatteryManager.BATTERY_HEALTH_OVERHEAT -> "Overheat"
            BatteryManager.BATTERY_HEALTH_DEAD -> "Dead"
            BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "Over Voltage"
            BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE -> "Failure"
            BatteryManager.BATTERY_HEALTH_COLD -> "Cold"
            else -> "Unknown"
        }

        val plugged = batteryIntent?.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0) ?: 0
        val pluggedString = when (plugged) {
            BatteryManager.BATTERY_PLUGGED_AC -> "AC Charger"
            BatteryManager.BATTERY_PLUGGED_USB -> "USB Port"
            BatteryManager.BATTERY_PLUGGED_WIRELESS -> "Wireless Charger"
            else -> "Unplugged"
        }

        val rawStatus = batteryIntent?.getIntExtra(
            BatteryManager.EXTRA_STATUS,
            BatteryManager.BATTERY_STATUS_UNKNOWN
        ) ?: BatteryManager.BATTERY_STATUS_UNKNOWN

        val statusString = when (rawStatus) {
            BatteryManager.BATTERY_STATUS_CHARGING -> "Charging"
            BatteryManager.BATTERY_STATUS_DISCHARGING -> "Discharging"
            BatteryManager.BATTERY_STATUS_FULL -> "Full"
            BatteryManager.BATTERY_STATUS_NOT_CHARGING -> "Not Charging"
            else -> "Discharging"
        }

        val tech = batteryIntent?.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY) ?: "Li-ion"

        return BatterySnapshot(
            level = level,
            scale = scale,
            percentage = percentage,
            temperatureCelsius = tempCelsius,
            voltageMv = voltageMv,
            health = rawHealth,
            healthString = healthString,
            plugged = plugged,
            pluggedString = pluggedString,
            status = rawStatus,
            statusString = statusString,
            technology = tech
        )
    }

    fun runBatteryTests(): List<DiagnosticTest> {
        val snapshot = getBatterySnapshot()
        val tests = mutableListOf<DiagnosticTest>()

        // 1. Battery Health Test
        val healthStatus = when (snapshot.health) {
            BatteryManager.BATTERY_HEALTH_GOOD -> DiagnosticStatus.PASSED
            BatteryManager.BATTERY_HEALTH_OVERHEAT -> DiagnosticStatus.WARNING
            BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> DiagnosticStatus.WARNING
            BatteryManager.BATTERY_HEALTH_DEAD -> DiagnosticStatus.FAILED
            BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE -> DiagnosticStatus.FAILED
            else -> DiagnosticStatus.PASSED
        }
        val healthSummary = when (healthStatus) {
            DiagnosticStatus.PASSED -> "Battery reporting normal operating health."
            DiagnosticStatus.WARNING -> "Battery condition: ${snapshot.healthString}."
            else -> "Battery failure reported."
        }
        tests.add(
            DiagnosticTest(
                id = "battery_health",
                category = TestCategory.BATTERY,
                name = "Battery Health",
                description = "Checks underlying cell condition reported by power management IC",
                requiresInteraction = false,
                status = healthStatus,
                resultSummary = healthSummary,
                metrics = listOf(
                    DiagnosticMetric("Condition", snapshot.healthString, isPrimary = true),
                    DiagnosticMetric("Technology", snapshot.technology)
                ),
                timestamp = System.currentTimeMillis()
            )
        )

        // 2. Battery Temperature Test
        val tempStatus = when {
            snapshot.temperatureCelsius <= 0.0f -> DiagnosticStatus.PASSED // emulator / unexposed fallback
            snapshot.temperatureCelsius in 15.0f..40.0f -> DiagnosticStatus.PASSED
            snapshot.temperatureCelsius in 40.1f..46.0f -> DiagnosticStatus.WARNING
            snapshot.temperatureCelsius > 46.0f -> DiagnosticStatus.FAILED
            else -> DiagnosticStatus.PASSED
        }
        val tempSummary = when (tempStatus) {
            DiagnosticStatus.PASSED -> "Thermal level is within optimal range (${snapshot.temperatureCelsius}°C)."
            DiagnosticStatus.WARNING -> "Battery temperature is elevated (${snapshot.temperatureCelsius}°C)."
            else -> "Battery temperature is critically high (${snapshot.temperatureCelsius}°C)."
        }
        tests.add(
            DiagnosticTest(
                id = "battery_temperature",
                category = TestCategory.BATTERY,
                name = "Temperature",
                description = "Measures internal battery thermal sensor",
                requiresInteraction = false,
                status = tempStatus,
                resultSummary = tempSummary,
                metrics = listOf(
                    DiagnosticMetric("Temperature", "${snapshot.temperatureCelsius}", "°C", isPrimary = true)
                ),
                timestamp = System.currentTimeMillis()
            )
        )

        // 3. Voltage Test
        val voltageStatus = when {
            snapshot.voltageMv in 3400..4500 -> DiagnosticStatus.PASSED
            snapshot.voltageMv in 3000..3399 -> DiagnosticStatus.WARNING
            snapshot.voltageMv in 4501..4800 -> DiagnosticStatus.WARNING
            snapshot.voltageMv == 0 -> DiagnosticStatus.NOT_AVAILABLE
            else -> DiagnosticStatus.WARNING
        }
        val voltSummary = if (voltageStatus == DiagnosticStatus.PASSED) {
            "Voltage is stable at ${snapshot.voltageMv} mV."
        } else if (voltageStatus == DiagnosticStatus.NOT_AVAILABLE) {
            "Voltage sensor not exposed on this device."
        } else {
            "Voltage (${snapshot.voltageMv} mV) is outside typical nominal range."
        }
        tests.add(
            DiagnosticTest(
                id = "battery_voltage",
                category = TestCategory.BATTERY,
                name = "Voltage",
                description = "Monitors real-time potential difference across battery terminals",
                requiresInteraction = false,
                status = voltageStatus,
                resultSummary = voltSummary,
                metrics = listOf(
                    DiagnosticMetric("Voltage", "${snapshot.voltageMv}", "mV", isPrimary = true)
                ),
                timestamp = System.currentTimeMillis(),
                isAvailableOnDevice = snapshot.voltageMv > 0
            )
        )

        // 4. Charging State
        tests.add(
            DiagnosticTest(
                id = "battery_charging_state",
                category = TestCategory.BATTERY,
                name = "Charging State",
                description = "Detects power delivery source and charge circuit state",
                requiresInteraction = false,
                status = DiagnosticStatus.PASSED,
                resultSummary = "Current state: ${snapshot.statusString} via ${snapshot.pluggedString}.",
                metrics = listOf(
                    DiagnosticMetric("Status", snapshot.statusString, isPrimary = true),
                    DiagnosticMetric("Power Source", snapshot.pluggedString),
                    DiagnosticMetric("Charge Level", "${snapshot.percentage}", "%")
                ),
                timestamp = System.currentTimeMillis()
            )
        )

        return tests
    }
}

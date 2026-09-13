package com.limb.diagnostics.engine

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.limb.diagnostics.model.DiagnosticMetric
import com.limb.diagnostics.model.DiagnosticStatus
import com.limb.diagnostics.model.DiagnosticTest
import com.limb.diagnostics.model.TestCategory
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class SensorDiagnosticEngine(private val context: Context) {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    fun isSensorAvailable(sensorType: Int): Boolean {
        return sensorManager.getDefaultSensor(sensorType) != null
    }

    fun getSensorName(sensorType: Int): String {
        return sensorManager.getDefaultSensor(sensorType)?.name ?: "Not Present"
    }

    fun getAllAvailableSensorNames(): List<String> {
        val list = sensorManager.getSensorList(Sensor.TYPE_ALL)
        return list.map { it.name }.distinct()
    }

    fun observeSensor(sensorType: Int, samplingPeriodUs: Int = SensorManager.SENSOR_DELAY_UI): Flow<FloatArray> = callbackFlow {
        val sensor = sensorManager.getDefaultSensor(sensorType)
        if (sensor == null) {
            close()
            return@callbackFlow
        }

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                event?.values?.let { trySend(it.clone()) }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        sensorManager.registerListener(listener, sensor, samplingPeriodUs)

        awaitClose {
            sensorManager.unregisterListener(listener)
        }
    }

    fun runSensorAutomatedChecks(): List<DiagnosticTest> {
        val tests = mutableListOf<DiagnosticTest>()

        // 1. Accelerometer
        tests.add(checkHardwareSensor(
            id = "sensor_accelerometer",
            name = "Accelerometer",
            description = "Measures proper acceleration and gravity vector",
            sensorType = Sensor.TYPE_ACCELEROMETER
        ))

        // 2. Gyroscope
        tests.add(checkHardwareSensor(
            id = "sensor_gyroscope",
            name = "Gyroscope",
            description = "Detects angular velocity and device rotational rate",
            sensorType = Sensor.TYPE_GYROSCOPE
        ))

        // 3. Proximity
        tests.add(checkHardwareSensor(
            id = "sensor_proximity",
            name = "Proximity Sensor",
            description = "Detects objects near the top speaker during calls",
            sensorType = Sensor.TYPE_PROXIMITY
        ))

        // 4. Ambient Light
        tests.add(checkHardwareSensor(
            id = "sensor_ambient_light",
            name = "Ambient Light Sensor",
            description = "Measures surrounding illuminance in lux for adaptive brightness",
            sensorType = Sensor.TYPE_LIGHT
        ))

        // 5. Magnetometer (Compass)
        tests.add(checkHardwareSensor(
            id = "sensor_magnetometer",
            name = "Magnetometer",
            description = "Measures Earth's magnetic field for compass heading",
            sensorType = Sensor.TYPE_MAGNETIC_FIELD
        ))

        // 6. Barometer
        tests.add(checkHardwareSensor(
            id = "sensor_barometer",
            name = "Barometer",
            description = "Measures atmospheric pressure in hPa for altimeter calibration",
            sensorType = Sensor.TYPE_PRESSURE
        ))

        // 7. Step Counter
        tests.add(checkHardwareSensor(
            id = "sensor_step_counter",
            name = "Step Counter",
            description = "Low-power hardware pedometer sensor",
            sensorType = Sensor.TYPE_STEP_COUNTER
        ))

        // 8. Rotation Vector
        tests.add(checkHardwareSensor(
            id = "sensor_rotation",
            name = "Rotation Vector",
            description = "Fuses accelerometer, gyro and magnetometer into orientation quaternion",
            sensorType = Sensor.TYPE_ROTATION_VECTOR
        ))

        return tests
    }

    private fun checkHardwareSensor(
        id: String,
        name: String,
        description: String,
        sensorType: Int
    ): DiagnosticTest {
        val sensor = sensorManager.getDefaultSensor(sensorType)
        val isAvailable = sensor != null

        val status = if (isAvailable) DiagnosticStatus.PASSED else DiagnosticStatus.NOT_AVAILABLE
        val summary = if (isAvailable) {
            "Sensor present (${sensor.name}) and responding."
        } else {
            "Not available on this device."
        }

        val metrics = if (isAvailable) {
            listOf(
                DiagnosticMetric("Sensor Model", sensor.name, isPrimary = true),
                DiagnosticMetric("Vendor", sensor.vendor),
                DiagnosticMetric("Power", "${sensor.power}", "mA"),
                DiagnosticMetric("Max Range", "${sensor.maximumRange}")
            )
        } else {
            listOf(DiagnosticMetric("Status", "Hardware not present", isPrimary = true))
        }

        return DiagnosticTest(
            id = id,
            category = TestCategory.SENSORS,
            name = name,
            description = description,
            requiresInteraction = false,
            status = status,
            resultSummary = summary,
            metrics = metrics,
            timestamp = System.currentTimeMillis(),
            isAvailableOnDevice = isAvailable
        )
    }
}

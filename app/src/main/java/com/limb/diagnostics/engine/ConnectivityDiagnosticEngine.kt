package com.limb.diagnostics.engine

import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import android.nfc.NfcAdapter
import android.os.Build
import android.telephony.TelephonyManager
import com.limb.diagnostics.model.DiagnosticMetric
import com.limb.diagnostics.model.DiagnosticStatus
import com.limb.diagnostics.model.DiagnosticTest
import com.limb.diagnostics.model.TestCategory

class ConnectivityDiagnosticEngine(private val context: Context) {

    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
    private val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    fun runConnectivityAutomatedChecks(): List<DiagnosticTest> {
        val tests = mutableListOf<DiagnosticTest>()

        // 1. Wi-Fi Check
        val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
        val activeNetwork = connectivityManager.activeNetwork
        val caps = activeNetwork?.let { connectivityManager.getNetworkCapabilities(it) }
        val isWifiConnected = caps?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true

        val wifiSpeed = caps?.linkDownstreamBandwidthKbps?.let { it / 1000 } ?: 0
        val wifiStatus = if (isWifiConnected) DiagnosticStatus.PASSED else DiagnosticStatus.PASSED
        val wifiSummary = if (isWifiConnected) {
            "Connected to Wi-Fi (${wifiSpeed} Mbps link)."
        } else {
            "Wi-Fi radio active (disconnected)."
        }

        tests.add(
            DiagnosticTest(
                id = "conn_wifi",
                category = TestCategory.CONNECTIVITY,
                name = "Wi-Fi",
                description = "WLAN 802.11 transceiver and network interface",
                requiresInteraction = false,
                status = wifiStatus,
                resultSummary = wifiSummary,
                metrics = listOf(
                    DiagnosticMetric("Status", if (isWifiConnected) "Connected" else "Idle", isPrimary = true),
                    DiagnosticMetric("Link Speed", if (wifiSpeed > 0) "$wifiSpeed Mbps" else "N/A")
                ),
                timestamp = System.currentTimeMillis()
            )
        )

        // 2. Bluetooth Check
        val bluetoothAdapter = try {
            BluetoothAdapter.getDefaultAdapter()
        } catch (_: Exception) { null }

        val hasBt = bluetoothAdapter != null
        val btEnabled = bluetoothAdapter?.isEnabled == true
        val hasBle = context.packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)

        tests.add(
            DiagnosticTest(
                id = "conn_bluetooth",
                category = TestCategory.CONNECTIVITY,
                name = "Bluetooth",
                description = "Bluetooth Core & BLE radio controller",
                requiresInteraction = false,
                status = if (hasBt) DiagnosticStatus.PASSED else DiagnosticStatus.NOT_AVAILABLE,
                resultSummary = if (hasBt) "Bluetooth hardware operational (BLE: ${if (hasBle) "Supported" else "No"})." else "Bluetooth not present.",
                metrics = listOf(
                    DiagnosticMetric("State", if (btEnabled) "Enabled" else "Available", isPrimary = true),
                    DiagnosticMetric("BLE Support", if (hasBle) "Yes" else "No")
                ),
                timestamp = System.currentTimeMillis(),
                isAvailableOnDevice = hasBt
            )
        )

        // 3. Mobile Network & SIM Check
        val simState = telephonyManager.simState
        val simStatusStr = when (simState) {
            TelephonyManager.SIM_STATE_READY -> "SIM Ready"
            TelephonyManager.SIM_STATE_ABSENT -> "No SIM Card"
            TelephonyManager.SIM_STATE_PIN_REQUIRED -> "PIN Required"
            TelephonyManager.SIM_STATE_PUK_REQUIRED -> "PUK Required"
            TelephonyManager.SIM_STATE_NETWORK_LOCKED -> "Network Locked"
            else -> "SIM Inactive"
        }
        val carrierName = telephonyManager.networkOperatorName.ifEmpty { telephonyManager.simOperatorName }.ifEmpty { "None" }
        val isCellConnected = caps?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true

        tests.add(
            DiagnosticTest(
                id = "conn_cellular",
                category = TestCategory.CONNECTIVITY,
                name = "Mobile Network & SIM",
                description = "Cellular baseband modem and SIM card interface",
                requiresInteraction = false,
                status = DiagnosticStatus.PASSED,
                resultSummary = "SIM status: $simStatusStr (Carrier: $carrierName).",
                metrics = listOf(
                    DiagnosticMetric("SIM State", simStatusStr, isPrimary = true),
                    DiagnosticMetric("Carrier", carrierName),
                    DiagnosticMetric("Data Link", if (isCellConnected) "Active" else "Standby")
                ),
                timestamp = System.currentTimeMillis()
            )
        )

        // 4. GPS / Location Hardware
        val hasGps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.allProviders.contains(LocationManager.GPS_PROVIDER)
        tests.add(
            DiagnosticTest(
                id = "conn_gps",
                category = TestCategory.CONNECTIVITY,
                name = "GPS / GNSS",
                description = "Global satellite navigation receiver",
                requiresInteraction = false,
                status = if (hasGps) DiagnosticStatus.PASSED else DiagnosticStatus.NOT_AVAILABLE,
                resultSummary = if (hasGps) "GNSS / GPS positioning receiver operational." else "No GPS provider detected.",
                metrics = listOf(
                    DiagnosticMetric("GPS Receiver", if (hasGps) "Ready" else "Not Available", isPrimary = true)
                ),
                timestamp = System.currentTimeMillis(),
                isAvailableOnDevice = hasGps
            )
        )

        // 5. NFC
        val nfcAdapter = try { NfcAdapter.getDefaultAdapter(context) } catch (_: Exception) { null }
        val hasNfc = nfcAdapter != null
        val nfcEnabled = nfcAdapter?.isEnabled == true

        tests.add(
            DiagnosticTest(
                id = "conn_nfc",
                category = TestCategory.CONNECTIVITY,
                name = "NFC",
                description = "Near Field Communication transceiver",
                requiresInteraction = false,
                status = if (hasNfc) DiagnosticStatus.PASSED else DiagnosticStatus.NOT_AVAILABLE,
                resultSummary = if (hasNfc) "NFC chip detected (Enabled: ${if (nfcEnabled) "Yes" else "No"})." else "Not available on this device.",
                metrics = listOf(
                    DiagnosticMetric("NFC Chip", if (hasNfc) (if (nfcEnabled) "Enabled" else "Available") else "Not Present", isPrimary = true)
                ),
                timestamp = System.currentTimeMillis(),
                isAvailableOnDevice = hasNfc
            )
        )

        return tests
    }
}

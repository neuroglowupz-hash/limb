# 📜 Changelog

All notable changes to **Limb** are documented in this file in accordance with [Keep a Changelog](https://keepachangelog.com/en/1.0.0/) and [Semantic Versioning](https://semver.org/).

---

## [1.0.0] - 2026-09-13

### 🚀 Major Highlights
* **Zero-Signup "Hyperspace" Hardware Activation**:
  * 3-Beat onboarding sequence (Instant Spec & Gyro 3D chassis discovery, Laser Sonar radar diagnostic sweep, and Shockwave health score reveal).
  * 100% frictionless local execution with immediate zero-wait navigation.
  * Clean device name sanitation for commercial devices and virtual test environments.
* **Frosted Floating Capsule Dock**:
  * Capsule navigation bar with `32dp` rounded geometry and elevation.
  * Haptic feedback on tab transitions and automatic `90dp` bottom clearance.
* **Live System Telemetry HUD**:
  * Real-time multi-core CPU cluster visualizer with active/idle core status bars.
  * SoC package thermal monitoring and memory headroom gauge.
* **Monochrome Snake Mini Game (Input Latency Lab)**:
  * Full black & white retro CRT arcade game with touch swipe gestures & tactile D-Pad.
  * Evaluates touch digitizer latency and 120Hz frame pacing.

### 🧪 Diagnostic Engine Capabilities
* **Display Engine (`DisplayDiagnosticEngine.kt`)**:
  * Dead Pixel Inspector (Full-screen RGBW test patterns).
  * Touch Grid Matrix Digitizer test with coordinate tile completion.
  * Multi-Touch Constellation tracker with simultaneous multi-pointer arcs.
  * Brightness curve calibration and Native Resolution/DPI reporting.
* **Sensor Engine (`SensorDiagnosticEngine.kt`)**:
  * 3D Accelerometer bubble/gravity vector visualizer.
  * Gyroscope angular velocity ($rad/s$) and rotation vector quaternion.
  * Proximity sensor auto-detection, Ambient Light lux meter, and Magnetometer heading.
  * Barometer atmospheric pressure reporting.
* **Camera Engine (`CameraDiagnosticEngine.kt`)**:
  * CameraX HAL integration for front and rear viewfinders.
  * LED torch/flash hardware toggle and multi-lens enumeration.
* **Audio Engine (`AudioDiagnosticEngine.kt`)**:
  * 440 Hz loudspeaker acoustic tone generation.
  * Voice call earpiece speaker verification.
  * Live microphone decibel level ($dB$) and dynamic waveform canvas.
  * Haptic actuator pattern suite (*Tick, Click, Heavy Click, Pulse Wave*).
* **Power Engine (`BatteryDiagnosticEngine.kt`)**:
  * Power IC cell condition, voltage ($mV$), thermal zone ($^\circ C$), and charging source.
* **Connectivity Engine (`ConnectivityDiagnosticEngine.kt`)**:
  * Wi-Fi transceiver, link speed ($Mbps$), and Bluetooth/BLE radio status.
  * Cellular modem, SIM card status, and GNSS/GPS positioning state.
* **Performance & Software Engines (`PerformanceDiagnosticEngine.kt`, `SoftwareDiagnosticEngine.kt`)**:
  * CPU core topology, RAM capacity, Flash storage metrics.
  * Android release, API level, security patch level, kernel version, and system uptime.

### 🎨 UI & Layout Refinements
* **Compact Test Result Cards (`ReportsScreen.kt`)**:
  * Refactored test metrics into inline 2-column key-value pairs (`test.metrics.chunked(2)`).
  * Reduced card footprint from 400dp to ~70dp with high-density hierarchy.
* **App Experience Reset**:
  * Added dedicated "🔄 Rerun App from Start" action in Device Information sheet.
  * Resets persistent onboarding and diagnostic states with backstack clearing.

---

## [0.1.0] - Initial Prototype
* Base project structure with Kotlin 2.0 and Jetpack Compose BOM.
* Initial 8 diagnostic engines and health score calculation algorithms.

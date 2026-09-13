# 🏗️ Limb Architecture & System Design

This document details the architectural principles, layer separations, reactive data streams, and hardware abstraction layer (HAL) integrations in **Limb**.

---

## 🏛️ Architectural Overview

Limb follows modern Android architecture recommendations, built with **100% Jetpack Compose**, **Kotlin Coroutines**, and **Reactive StateFlow**.

```
┌─────────────────────────────────────────────────────────────┐
│                       UI LAYER (Jetpack Compose)            │
│  • Screens (Home, OnboardingScan, Reports, Tests, Snake)    │
│  • Theme & Frosted Capsule Components (Design System)       │
└──────────────────────────────┬──────────────────────────────┘
                               │ StateFlow / Actions
┌──────────────────────────────▼──────────────────────────────┐
│                    DATA & DOMAIN LAYER                      │
│  • DiagnosticRepository (Single Source of Truth)            │
│  • HealthScoreCalculator (Weighted Evaluation Algorithms)    │
│  • SharedPreferences (Local Preferences & Onboarding Flags) │
└──────────────────────────────┬──────────────────────────────┘
                               │
┌──────────────────────────────▼──────────────────────────────┐
│                  HARDWARE ABSTRACTION LAYER                 │
│  • DisplayEngine      • SensorEngine      • AudioEngine     │
│  • BatteryEngine      • CameraEngine      • SoftwareEngine  │
│  • PerformanceEngine  • ConnectivityEngine                  │
└─────────────────────────────────────────────────────────────┘
```

---

## 📦 Package Organization

```
com.limb.diagnostics
 ├── 📦 data
 │    └── DiagnosticRepository.kt   # Aggregator for all test runs & state management
 ├── 📦 engine
 │    ├── AudioDiagnosticEngine.kt        # AudioTrack & AudioRecord acoustic pipeline
 │    ├── BatteryDiagnosticEngine.kt      # BatteryManager & PMIC readings
 │    ├── CameraDiagnosticEngine.kt       # CameraX & Camera2 API HAL integration
 │    ├── ConnectivityDiagnosticEngine.kt # WifiManager, ConnectivityManager, Telephony
 │    ├── DisplayDiagnosticEngine.kt      # DisplayMetrics & WindowManager controller
 │    ├── PerformanceDiagnosticEngine.kt  # /proc/cpuinfo, ActivityManager, StorageStats
 │    ├── SensorDiagnosticEngine.kt       # SensorManager & SensorEventListener streams
 │    └── SoftwareDiagnosticEngine.kt     # Build & OS reflection
 ├── 📦 model
 │    ├── DiagnosticModels.kt       # DiagnosticTest, TestCategory, DiagnosticReport
 │    └── HealthScoreCalculator.kt  # Mathematical scoring engine with weighted deductions
 ├── 📦 ui
 │    ├── navigation
 │    │    ├── LimbApp.kt           # NavHost container, Scaffold & floating dock
 │    │    └── NavRoutes.kt         # Sealed class route definitions
 │    ├── screens
 │    │    ├── DeviceInfoScreen.kt      # Hardware spec sheet & app reset
 │    │    ├── FullCheckScreen.kt       # Interactive multi-step diagnostic wizard
 │    │    ├── HomeScreen.kt            # Command center hero score & live HUD
 │    │    ├── OnboardingScanScreen.kt  # 3-beat Zero-Signup hardware onboarding
 │    │    ├── QuickCheckScreen.kt      # Rapid automated telemetry scanner
 │    │    ├── ReportsScreen.kt         # Detailed report & compact test items
 │    │    ├── SnakeGameScreen.kt       # Monochrome input latency mini game
 │    │    ├── TestsLibraryScreen.kt    # Expandable category test suites
 │    │    └── interactive/             # Sub-screens for touch, camera, audio, sensors
 │    └── theme
 │         ├── Color.kt             # Dark & Light color palettes
 │         ├── Components.kt        # Frosted dock, gauges, chips, and HUD cards
 │         ├── Theme.kt             # CompositionLocal theme provider
 │         └── Type.kt              # Typography hierarchy
```

---

## 🔒 Local-First & Privacy Contract

1. **Zero External Requests**: No HTTP/HTTPS networking clients, analytics trackers, or crash reporting SDKs.
2. **Volatile In-Memory Buffers**: Audio waveforms and camera viewfinder frames are processed in-memory and discarded upon screen exit.
3. **No Hardware Identifier Persistence**: Device IMEI, MEID, serial numbers, or MAC addresses are never stored.

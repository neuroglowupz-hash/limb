package com.limb.diagnostics.ui.navigation

sealed class Screen(val route: String) {
    data object OnboardingScan : Screen("onboarding_scan")
    data object Home : Screen("home")
    data object Tests : Screen("tests")
    data object Reports : Screen("reports")
    data object QuickCheck : Screen("quick_check")
    data object FullCheck : Screen("full_check")
    data object DeviceInfo : Screen("device_info")
    data object TestDetail : Screen("test_detail/{testId}") {
        fun createRoute(testId: String) = "test_detail/$testId"
    }
}

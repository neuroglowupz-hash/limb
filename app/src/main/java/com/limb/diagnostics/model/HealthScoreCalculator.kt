package com.limb.diagnostics.model

object HealthScoreCalculator {

    fun calculateReport(
        tests: List<DiagnosticTest>,
        deviceModel: String,
        manufacturer: String,
        androidVersion: String,
        securityPatch: String
    ): DiagnosticReport {
        val passedTests = tests.filter { it.status == DiagnosticStatus.PASSED }
        val warningTests = tests.filter { it.status == DiagnosticStatus.WARNING }
        val failedTests = tests.filter { it.status == DiagnosticStatus.FAILED }
        val notAvailableTests = tests.filter { it.status == DiagnosticStatus.NOT_AVAILABLE }

        val categoryGroups = tests.groupBy { it.category }
        val categoryScores = mutableMapOf<TestCategory, Int>()
        val categoryHealths = mutableListOf<CategoryHealth>()

        TestCategory.entries.forEach { category ->
            val catTests = categoryGroups[category] ?: emptyList()
            if (catTests.isEmpty()) {
                categoryScores[category] = 100
                categoryHealths.add(
                    CategoryHealth(
                        category = category,
                        score = 100,
                        statusText = "Excellent",
                        passedCount = 0,
                        warningCount = 0,
                        failedCount = 0,
                        notAvailableCount = 0,
                        totalCount = 0
                    )
                )
            } else {
                val sc = computeCategoryScore(catTests)
                categoryScores[category] = sc
                val statusText = when {
                    sc >= 90 -> "Excellent"
                    sc >= 75 -> "Good"
                    sc >= 50 -> "Fair"
                    else -> "Needs Attention"
                }
                categoryHealths.add(
                    CategoryHealth(
                        category = category,
                        score = sc,
                        statusText = statusText,
                        passedCount = catTests.count { it.status == DiagnosticStatus.PASSED },
                        warningCount = catTests.count { it.status == DiagnosticStatus.WARNING },
                        failedCount = catTests.count { it.status == DiagnosticStatus.FAILED },
                        notAvailableCount = catTests.count { it.status == DiagnosticStatus.NOT_AVAILABLE },
                        totalCount = catTests.size
                    )
                )
            }
        }

        val overallScore: Int = if (tests.isNotEmpty()) {
            val validTests = tests.filter { it.status != DiagnosticStatus.NOT_AVAILABLE && it.status != DiagnosticStatus.NOT_TESTED }
            if (validTests.isEmpty()) {
                95
            } else {
                var sum = 0
                for (test in validTests) {
                    sum += when (test.status) {
                        DiagnosticStatus.PASSED -> 100
                        DiagnosticStatus.WARNING -> 70
                        DiagnosticStatus.FAILED -> 0
                        else -> 100
                    }
                }
                (sum / validTests.size).coerceIn(0, 100)
            }
        } else {
            95
        }

        val keyFindings = mutableListOf<String>()
        failedTests.forEach { test ->
            keyFindings.add(test.resultSummary.ifEmpty { "${test.name} check failed." })
        }
        warningTests.forEach { test ->
            keyFindings.add(test.resultSummary.ifEmpty { "${test.name} needs attention." })
        }
        if (keyFindings.isEmpty() && passedTests.isNotEmpty()) {
            keyFindings.add("All tested hardware and components are healthy.")
        } else if (keyFindings.isEmpty()) {
            keyFindings.add("Ready for diagnostic testing.")
        }

        return DiagnosticReport(
            deviceModel = deviceModel,
            manufacturer = manufacturer,
            androidVersion = androidVersion,
            securityPatch = securityPatch,
            overallScore = overallScore,
            categoryScores = categoryScores,
            categoryHealths = categoryHealths,
            tests = tests,
            keyFindings = keyFindings,
            passedCount = passedTests.size,
            warningCount = warningTests.size,
            failedCount = failedTests.size,
            notAvailableCount = notAvailableTests.size
        )
    }

    private fun computeCategoryScore(tests: List<DiagnosticTest>): Int {
        val evaluated = tests.filter { it.status != DiagnosticStatus.NOT_AVAILABLE && it.status != DiagnosticStatus.NOT_TESTED }
        if (evaluated.isEmpty()) return 100
        var sum = 0
        for (test in evaluated) {
            sum += when (test.status) {
                DiagnosticStatus.PASSED -> 100
                DiagnosticStatus.WARNING -> 70
                DiagnosticStatus.FAILED -> 0
                else -> 100
            }
        }
        return (sum / evaluated.size).coerceIn(0, 100)
    }
}

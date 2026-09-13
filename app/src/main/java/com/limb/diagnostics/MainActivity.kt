package com.limb.diagnostics

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.limb.diagnostics.data.DiagnosticRepository
import com.limb.diagnostics.ui.navigation.LimbApp

class MainActivity : ComponentActivity() {

    private lateinit var repository: DiagnosticRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        repository = DiagnosticRepository(applicationContext)

        setContent {
            LimbApp(repository = repository)
        }
    }
}

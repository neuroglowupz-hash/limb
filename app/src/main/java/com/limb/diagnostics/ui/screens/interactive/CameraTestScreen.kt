package com.limb.diagnostics.ui.screens.interactive

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Cameraswitch
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.FlashOn
import androidx.compose.material.icons.outlined.SkipNext
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.limb.diagnostics.engine.CameraDiagnosticEngine
import com.limb.diagnostics.model.DiagnosticStatus
import com.limb.diagnostics.ui.theme.LimbAppTheme
import com.limb.diagnostics.ui.theme.LimbButton

@Composable
fun CameraTestScreen(
    cameraEngine: CameraDiagnosticEngine,
    testId: String,
    onFinish: (DiagnosticStatus, String) -> Unit,
    onSkip: (() -> Unit)? = null,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
    }

    var lensFacing by remember {
        mutableStateOf(
            if (testId == "camera_front") CameraSelector.LENS_FACING_FRONT else CameraSelector.LENS_FACING_BACK
        )
    }

    var isTorchOn by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(LimbAppTheme.colors.background)
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                Text(
                    text = if (lensFacing == CameraSelector.LENS_FACING_FRONT) "Front Camera" else "Rear Camera",
                    style = MaterialTheme.typography.titleLarge,
                    color = LimbAppTheme.colors.textPrimary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Verify optical focus, clarity and lens switching",
                    style = MaterialTheme.typography.bodyMedium,
                    color = LimbAppTheme.colors.textSecondary
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (onSkip != null) {
                    IconButton(onClick = onSkip) {
                        Icon(
                            imageVector = Icons.Outlined.SkipNext,
                            contentDescription = "Skip Stage",
                            tint = LimbAppTheme.colors.textSecondary
                        )
                    }
                }
                IconButton(onClick = onClose) {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = "Close",
                        tint = LimbAppTheme.colors.textSecondary
                    )
                }
            }
        }

        if (!hasCameraPermission) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Camera Permission Required",
                    style = MaterialTheme.typography.titleLarge,
                    color = LimbAppTheme.colors.textPrimary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Limb requires temporary camera access to test image sensor capture, autofocus, and flash LED. No photos or video are ever stored or transmitted.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = LimbAppTheme.colors.textSecondary,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.height(24.dp))
                LimbButton(
                    text = "Grant Permission",
                    onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                    isPrimary = true
                )
            }
        } else {
            // Camera Preview Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(340.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.Black)
                    .border(1.dp, LimbAppTheme.colors.border, RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center
            ) {
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { ctx ->
                        val previewView = PreviewView(ctx)
                        val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                        cameraProviderFuture.addListener({
                            val cameraProvider = cameraProviderFuture.get()
                            val preview = Preview.Builder().build().also {
                                it.setSurfaceProvider(previewView.surfaceProvider)
                            }
                            val selector = CameraSelector.Builder()
                                .requireLensFacing(lensFacing)
                                .build()
                            try {
                                cameraProvider.unbindAll()
                                cameraProvider.bindToLifecycle(
                                    lifecycleOwner,
                                    selector,
                                    preview
                                )
                            } catch (_: Exception) {}
                        }, ContextCompat.getMainExecutor(ctx))
                        previewView
                    },
                    update = { previewView ->
                        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
                        cameraProviderFuture.addListener({
                            val cameraProvider = cameraProviderFuture.get()
                            val preview = Preview.Builder().build().also {
                                it.setSurfaceProvider(previewView.surfaceProvider)
                            }
                            val selector = CameraSelector.Builder()
                                .requireLensFacing(lensFacing)
                                .build()
                            try {
                                cameraProvider.unbindAll()
                                cameraProvider.bindToLifecycle(
                                    lifecycleOwner,
                                    selector,
                                    preview
                                )
                            } catch (_: Exception) {}
                        }, ContextCompat.getMainExecutor(context))
                    }
                )

                // Viewfinder Controls Overlay
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    IconButton(
                        onClick = {
                            lensFacing = if (lensFacing == CameraSelector.LENS_FACING_FRONT)
                                CameraSelector.LENS_FACING_BACK
                            else
                                CameraSelector.LENS_FACING_FRONT
                        },
                        modifier = Modifier
                            .background(Color(0x80000000), CircleShape)
                            .size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Cameraswitch,
                            contentDescription = "Switch Camera",
                            tint = Color.White
                        )
                    }

                    IconButton(
                        onClick = {
                            isTorchOn = !isTorchOn
                            cameraEngine.setTorchMode(isTorchOn)
                        },
                        modifier = Modifier
                            .background(if (isTorchOn) LimbAppTheme.colors.statusWarning else Color(0x80000000), CircleShape)
                            .size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.FlashOn,
                            contentDescription = "Toggle Flash",
                            tint = if (isTorchOn) Color.Black else Color.White
                        )
                    }
                }
            }
        }

        // Action Buttons
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                LimbButton(
                    text = "Works",
                    onClick = {
                        onFinish(DiagnosticStatus.PASSED, "Camera optics, viewfinder preview, and autofocus verified.")
                    },
                    isPrimary = true,
                    modifier = Modifier.weight(1f)
                )
                LimbButton(
                    text = "Problem",
                    onClick = {
                        onFinish(DiagnosticStatus.WARNING, "Optical artifact, blur, or sensor issue detected.")
                    },
                    isPrimary = false,
                    modifier = Modifier.weight(1f)
                )
            }

            if (onSkip != null) {
                LimbButton(
                    text = "Skip Stage",
                    onClick = onSkip,
                    isPrimary = false
                )
            }
        }
    }
}

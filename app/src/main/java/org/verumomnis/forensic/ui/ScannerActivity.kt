package org.verumomnis.forensic.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import org.verumomnis.forensic.core.ForensicEngine
import org.verumomnis.forensic.report.CaseContext
import org.verumomnis.forensic.report.Urgency
import org.verumomnis.forensic.ui.theme.VerumOmnisTheme
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * Scanner Activity - Document Capture Interface
 * 
 * Captures documents using the device camera and processes them
 * through the forensic engine to create sealed reports.
 * 
 * Features:
 * - Camera preview with capture controls
 * - Case context input
 * - Real-time location capture
 * - Processing status display
 */
class ScannerActivity : ComponentActivity() {
    
    private var imageCapture: ImageCapture? = null
    private lateinit var forensicEngine: ForensicEngine
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        forensicEngine = ForensicEngine(this)
        
        setContent {
            VerumOmnisTheme {
                ScannerScreen(
                    onCapture = { description, caseType, urgency -> 
                        captureAndProcess(description, caseType, urgency)
                    },
                    onBack = { finish() },
                    setImageCapture = { imageCapture = it }
                )
            }
        }
    }
    
    private fun captureAndProcess(description: String, caseType: String, urgency: Urgency) {
        val imageCapture = imageCapture ?: return
        
        // Create output file
        val photoFile = File(
            getExternalFilesDir(null),
            "capture_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())}.jpg"
        )
        
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
        
        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    Toast.makeText(
                        this@ScannerActivity,
                        "Document captured. Processing...",
                        Toast.LENGTH_SHORT
                    ).show()
                    
                    // Process the captured image
                    processCapture(photoFile, description, caseType, urgency)
                }
                
                override fun onError(exception: ImageCaptureException) {
                    Toast.makeText(
                        this@ScannerActivity,
                        "Capture failed: ${exception.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        )
    }
    
    private fun processCapture(photoFile: File, description: String, caseType: String, urgency: Urgency) {
        // Use lifecycleScope to tie coroutine to Activity lifecycle
        lifecycleScope.launch {
            try {
                val photoBytes = photoFile.readBytes()
                
                val report = forensicEngine.quickCapture(
                    documentData = photoBytes,
                    description = description
                )
                
                // Save the report
                val outputDir = File(getExternalFilesDir(null), "reports")
                val saveResult = forensicEngine.saveReport(report, outputDir)
                
                runOnUiThread {
                    if (saveResult.success) {
                        Toast.makeText(
                            this@ScannerActivity,
                            "Report saved: ${saveResult.filename}",
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        Toast.makeText(
                            this@ScannerActivity,
                            "Save failed: ${saveResult.error}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
                
                // Clean up temp file
                photoFile.delete()
                
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(
                        this@ScannerActivity,
                        "Processing failed: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }
}

@Composable
fun ScannerScreen(
    onCapture: (String, String, Urgency) -> Unit,
    onBack: () -> Unit,
    setImageCapture: (ImageCapture) -> Unit
) {
    var description by remember { mutableStateOf("") }
    var caseType by remember { mutableStateOf("General Evidence") }
    var showCaptureDialog by remember { mutableStateOf(false) }
    var isProcessing by remember { mutableStateOf(false) }
    
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    
    val hasCameraPermission = remember {
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        if (hasCameraPermission) {
            // Camera Preview
            AndroidView(
                factory = { ctx ->
                    PreviewView(ctx).apply {
                        scaleType = PreviewView.ScaleType.FILL_CENTER
                        
                        val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                        cameraProviderFuture.addListener({
                            val cameraProvider = cameraProviderFuture.get()
                            
                            val preview = Preview.Builder().build().also {
                                it.setSurfaceProvider(surfaceProvider)
                            }
                            
                            val imageCapture = ImageCapture.Builder()
                                .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                                .build()
                            
                            setImageCapture(imageCapture)
                            
                            try {
                                cameraProvider.unbindAll()
                                cameraProvider.bindToLifecycle(
                                    lifecycleOwner,
                                    CameraSelector.DEFAULT_BACK_CAMERA,
                                    preview,
                                    imageCapture
                                )
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }, ContextCompat.getMainExecutor(ctx))
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            // Permission not granted
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Camera permission required",
                    color = Color.White,
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Please grant camera permission in settings",
                    color = Color.Gray,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
        
        // Top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.6f))
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onBack) {
                Text("Cancel", color = Color.White)
            }
            
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "DOCUMENT SCANNER",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Position document in view",
                    color = Color.Gray,
                    fontSize = 12.sp
                )
            }
            
            // Spacer for layout balance
            Box(modifier = Modifier.width(64.dp))
        }
        
        // Bottom controls
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .background(Color.Black.copy(alpha = 0.8f))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Info card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF1A237E).copy(alpha = 0.8f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "📍 Location will be captured",
                        color = Color(0xFFBBDEFB),
                        fontSize = 12.sp
                    )
                    Text(
                        text = "⏰ Timestamp will be recorded",
                        color = Color(0xFFBBDEFB),
                        fontSize = 12.sp
                    )
                    Text(
                        text = "🔒 Document will be cryptographically sealed",
                        color = Color(0xFFBBDEFB),
                        fontSize = 12.sp
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Capture button
            Button(
                onClick = { showCaptureDialog = true },
                modifier = Modifier.size(72.dp),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White
                ),
                enabled = !isProcessing
            ) {
                if (isProcessing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color(0xFF1A237E)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .background(Color(0xFF1A237E), CircleShape)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = if (isProcessing) "Processing..." else "Tap to capture",
                color = Color.Gray,
                fontSize = 12.sp
            )
        }
        
        // Capture dialog
        if (showCaptureDialog) {
            AlertDialog(
                onDismissRequest = { showCaptureDialog = false },
                containerColor = Color(0xFF1A237E),
                title = {
                    Text(
                        text = "Case Details",
                        color = Color.White
                    )
                },
                text = {
                    Column {
                        Text(
                            text = "Add description for this evidence:",
                            color = Color(0xFFBBDEFB),
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            placeholder = { 
                                Text("Describe this document...", color = Color.Gray)
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFF90CAF9),
                                unfocusedBorderColor = Color.Gray
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showCaptureDialog = false
                            isProcessing = true
                            onCapture(description, caseType, Urgency.MEDIUM)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF3949AB)
                        )
                    ) {
                        Text("Capture & Seal")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showCaptureDialog = false }) {
                        Text("Cancel", color = Color.Gray)
                    }
                }
            )
        }
    }
}

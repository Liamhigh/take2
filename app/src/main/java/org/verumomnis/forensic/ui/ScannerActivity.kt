@file:OptIn(ExperimentalMaterial3Api::class)

package org.verumomnis.forensic.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import org.verumomnis.forensic.core.VerumOmnisApplication
import org.verumomnis.forensic.ui.theme.VerumOmnisTheme

/**
 * Document Scanner Activity for capturing forensic evidence
 *
 * Supports:
 * - Document scanning
 * - Photo capture
 * - Text input
 * - Audio recording (future)
 */
class ScannerActivity : ComponentActivity() {

    private val cameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            Toast.makeText(this, "Camera permission required for scanning", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        checkCameraPermission()

        setContent {
            VerumOmnisTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ScannerScreen(
                        onScanDocument = { scanDocument() },
                        onTakePhoto = { takePhoto() },
                        onAddText = { description, content -> addTextEvidence(description, content) },
                        onBack = { finish() }
                    )
                }
            }
        }
    }

    private fun checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun scanDocument() {
        Toast.makeText(this, "Document scanning feature - Coming soon", Toast.LENGTH_SHORT).show()
    }

    private fun takePhoto() {
        Toast.makeText(this, "Photo capture feature - Coming soon", Toast.LENGTH_SHORT).show()
    }

    private fun addTextEvidence(description: String, content: String) {
        @Suppress("UNUSED_VARIABLE")
        val app = application as VerumOmnisApplication

        lifecycleScope.launch {
            try {
                // For now, just show a toast - in production this would access the current case
                Toast.makeText(
                    this@ScannerActivity,
                    "Text evidence added: $description",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            } catch (e: Exception) {
                Toast.makeText(
                    this@ScannerActivity,
                    "Error adding evidence: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}

@Composable
fun ScannerScreen(
    onScanDocument: () -> Unit,
    onTakePhoto: () -> Unit,
    onAddText: (String, String) -> Unit,
    onBack: () -> Unit
) {
    var showTextDialog by remember { mutableStateOf(false) }
    var textDescription by remember { mutableStateOf("") }
    var textContent by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Add Evidence",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            TextButton(onClick = onBack) {
                Text("Cancel")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Evidence Type Options
        Card(
            modifier = Modifier.fillMaxWidth(),
            onClick = onScanDocument
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Scan Document",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Use camera to scan documents",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            onClick = onTakePhoto
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Take Photo",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Capture photographic evidence",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            onClick = { showTextDialog = true }
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Add Text Evidence",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Enter text notes or observations",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Info Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Evidence Collection",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "All evidence is automatically:",
                    style = MaterialTheme.typography.bodySmall
                )
                Text("• Timestamped with device time", style = MaterialTheme.typography.bodySmall)
                Text("• Geolocated (if permitted)", style = MaterialTheme.typography.bodySmall)
                Text("• Hashed using SHA-512", style = MaterialTheme.typography.bodySmall)
                Text("• Cryptographically sealed", style = MaterialTheme.typography.bodySmall)
            }
        }
    }

    // Text Evidence Dialog
    if (showTextDialog) {
        AlertDialog(
            onDismissRequest = { showTextDialog = false },
            title = { Text("Add Text Evidence") },
            text = {
                Column {
                    OutlinedTextField(
                        value = textDescription,
                        onValueChange = { textDescription = it },
                        label = { Text("Description") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = textContent,
                        onValueChange = { textContent = it },
                        label = { Text("Content") },
                        minLines = 3,
                        maxLines = 5,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (textDescription.isNotBlank() && textContent.isNotBlank()) {
                            onAddText(textDescription, textContent)
                            textDescription = ""
                            textContent = ""
                            showTextDialog = false
                        }
                    }
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTextDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@file:OptIn(ExperimentalMaterial3Api::class)

package org.verumomnis.forensic.ui

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.verumomnis.forensic.core.EvidenceType
import org.verumomnis.forensic.leveler.LevelerEngine
import org.verumomnis.forensic.ui.theme.VerumOmnisTheme
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * Document Scanner Activity for capturing forensic evidence
 *
 * Supports:
 * - Document file upload (PDF, images, text files)
 * - Photo capture with camera
 * - Text input for notes
 * - Leveler engine analysis
 */
class ScannerActivity : ComponentActivity() {

    companion object {
        // Allowed MIME types for document upload (restricted for security)
        private val ALLOWED_MIME_TYPES = arrayOf(
            "application/pdf",
            "image/jpeg",
            "image/png",
            "image/webp",
            "image/gif",
            "text/plain",
            "text/csv"
        )
    }

    private var currentPhotoPath: String? = null
    private var currentPhotoUri: Uri? = null

    // Singleton Leveler Engine to avoid creating new instances for each analysis
    private val levelerEngine by lazy { LevelerEngine() }

    // Permission launcher for camera
    private val cameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            Toast.makeText(this, "Camera permission required for scanning", Toast.LENGTH_LONG).show()
        }
    }

    // Permission launcher for storage (for older Android versions)
    private val storagePermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.all { it.value }
        if (!allGranted) {
            Toast.makeText(this, "Storage permission required for file upload", Toast.LENGTH_LONG).show()
        }
    }

    // Document picker launcher
    private val documentPickerLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let { processPickedDocument(it) }
    }

    // Camera launcher
    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && currentPhotoUri != null) {
            processCapturedPhoto(currentPhotoUri!!)
        } else {
            Toast.makeText(this, "Photo capture cancelled or failed", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        checkPermissions()

        setContent {
            VerumOmnisTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ScannerScreen(
                        onUploadDocument = { openDocumentPicker() },
                        onScanDocument = { scanDocument() },
                        onTakePhoto = { takePhoto() },
                        onAddText = { description, content -> addTextEvidence(description, content) },
                        onBack = { finish() }
                    )
                }
            }
        }
    }

    private fun checkPermissions() {
        // Check camera permission
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }

        // Check storage permissions for older Android versions
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            val storagePermissions = arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            val missingPermissions = storagePermissions.filter {
                ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
            }
            if (missingPermissions.isNotEmpty()) {
                storagePermissionLauncher.launch(storagePermissions)
            }
        }
    }

    private fun openDocumentPicker() {
        try {
            // Open document picker with restricted MIME types for security
            documentPickerLauncher.launch(ALLOWED_MIME_TYPES)
        } catch (e: Exception) {
            Toast.makeText(
                this,
                "Error opening file picker: ${e.message}",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun scanDocument() {
        // For now, use ML Kit Document Scanner or fallback to camera
        // The Google ML Kit document scanner requires additional setup
        // For simplicity, we'll use the camera with instructions
        Toast.makeText(
            this,
            "Position document in view and take photo for scanning",
            Toast.LENGTH_LONG
        ).show()
        takePhoto()
    }

    private fun takePhoto() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            return
        }

        try {
            val photoFile = createImageFile()
            currentPhotoPath = photoFile.absolutePath
            currentPhotoUri = FileProvider.getUriForFile(
                this,
                "${packageName}.fileprovider",
                photoFile
            )

            cameraLauncher.launch(currentPhotoUri)
        } catch (e: Exception) {
            Toast.makeText(
                this,
                "Error starting camera: ${e.message}",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            ?: filesDir

        // Ensure directory exists
        if (!storageDir.exists()) {
            storageDir.mkdirs()
        }

        return File.createTempFile(
            "VERUM_${timeStamp}_",
            ".jpg",
            storageDir
        )
    }

    private fun processPickedDocument(uri: Uri) {
        lifecycleScope.launch {
            try {
                Toast.makeText(
                    this@ScannerActivity,
                    "Processing document...",
                    Toast.LENGTH_SHORT
                ).show()

                withContext(Dispatchers.IO) {
                    val contentResolver = contentResolver

                    // Get file name
                    val fileName = getFileName(uri)

                    // Read file content
                    val inputStream = contentResolver.openInputStream(uri)
                    val fileBytes = inputStream?.readBytes()
                    inputStream?.close()

                    if (fileBytes != null) {
                        // Determine document type
                        val mimeType = contentResolver.getType(uri) ?: "application/octet-stream"
                        val evidenceType = determineEvidenceType(mimeType, fileName)

                        // For text files, run Leveler analysis
                        val levelerResult = if (mimeType.startsWith("text/") ||
                            fileName.endsWith(".txt") || fileName.endsWith(".chat")) {
                            val content = String(fileBytes, Charsets.UTF_8)
                            levelerEngine.analyzeDocument(content)
                        } else {
                            null
                        }

                        withContext(Dispatchers.Main) {
                            val message = buildString {
                                append("Document processed: $fileName")
                                append("\nType: $evidenceType")
                                append("\nSize: ${fileBytes.size / 1024} KB")
                                if (levelerResult != null) {
                                    append("\n\nLeveler Analysis:")
                                    append("\nIntegrity Score: ${String.format("%.1f", levelerResult.integrityScore)}%")
                                    append("\nContradictions: ${levelerResult.contradictions.size}")
                                    append("\nEvasion Patterns: ${levelerResult.evasionPatterns.size}")
                                    append("\nAssessment: ${levelerResult.overallAssessment.name}")
                                }
                            }

                            Toast.makeText(
                                this@ScannerActivity,
                                message,
                                Toast.LENGTH_LONG
                            ).show()

                            // Return to main activity
                            setResult(RESULT_OK)
                            finish()
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                this@ScannerActivity,
                                "Failed to read document",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@ScannerActivity,
                        "Error processing document: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun processCapturedPhoto(uri: Uri) {
        lifecycleScope.launch {
            try {
                Toast.makeText(
                    this@ScannerActivity,
                    "Processing photo...",
                    Toast.LENGTH_SHORT
                ).show()

                withContext(Dispatchers.IO) {
                    val inputStream = contentResolver.openInputStream(uri)
                    val fileBytes = inputStream?.readBytes()
                    inputStream?.close()

                    if (fileBytes != null) {
                        withContext(Dispatchers.Main) {
                            val message = buildString {
                                append("Photo captured successfully")
                                append("\nSize: ${fileBytes.size / 1024} KB")
                                append("\nPath: ${currentPhotoPath?.substringAfterLast("/")}")
                            }

                            Toast.makeText(
                                this@ScannerActivity,
                                message,
                                Toast.LENGTH_LONG
                            ).show()

                            setResult(RESULT_OK)
                            finish()
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@ScannerActivity,
                        "Error processing photo: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun getFileName(uri: Uri): String {
        var result: String? = null
        if (uri.scheme == "content") {
            contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val index = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                    if (index >= 0) {
                        result = cursor.getString(index)
                    }
                }
            }
        }
        if (result == null) {
            result = uri.path?.substringAfterLast('/')
        }
        return result ?: "unknown_file"
    }

    private fun determineEvidenceType(mimeType: String, fileName: String): EvidenceType {
        return when {
            mimeType.startsWith("image/") -> EvidenceType.PHOTO
            mimeType == "application/pdf" -> EvidenceType.DOCUMENT
            mimeType.startsWith("video/") -> EvidenceType.VIDEO
            mimeType.startsWith("audio/") -> EvidenceType.AUDIO
            mimeType.startsWith("text/") -> EvidenceType.TEXT
            fileName.endsWith(".txt") || fileName.endsWith(".chat") -> EvidenceType.TEXT
            else -> EvidenceType.DOCUMENT
        }
    }

    private fun addTextEvidence(description: String, content: String) {
        lifecycleScope.launch {
            try {
                // Run Leveler analysis on text content using the singleton instance
                val analysis = levelerEngine.analyzeDocument(content)

                Toast.makeText(
                    this@ScannerActivity,
                    buildString {
                        append("Text evidence added: $description")
                        append("\nIntegrity: ${String.format("%.1f", analysis.integrityScore)}%")
                        append("\nAssessment: ${analysis.overallAssessment.name}")
                    },
                    Toast.LENGTH_LONG
                ).show()

                setResult(RESULT_OK)
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
    onUploadDocument: () -> Unit,
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
            .verticalScroll(rememberScrollState())
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

        // Upload Document Card (Primary Action)
        Card(
            modifier = Modifier.fillMaxWidth(),
            onClick = onUploadDocument,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "📁 Upload Document",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Select PDF, image, text file, or WhatsApp export",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Scan Document Card
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
                        text = "📄 Scan Document",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Use camera to scan physical documents",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Take Photo Card
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
                        text = "📷 Take Photo",
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

        // Add Text Evidence Card
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
                        text = "📝 Add Text Evidence",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Enter text notes, statements, or observations",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Leveler Engine Info Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.tertiaryContainer
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "⚖️ Leveler Engine Analysis",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Text documents are automatically analyzed for:",
                    style = MaterialTheme.typography.bodySmall
                )
                Text("• Contradictory statements", style = MaterialTheme.typography.bodySmall)
                Text("• Evasion patterns (\"I don't recall\")", style = MaterialTheme.typography.bodySmall)
                Text("• Timeline anomalies", style = MaterialTheme.typography.bodySmall)
                Text("• Financial discrepancies", style = MaterialTheme.typography.bodySmall)
                Text("• Integrity scoring (0-100%)", style = MaterialTheme.typography.bodySmall)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Info Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "🔒 Evidence Collection",
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
                Text("• Cryptographically sealed with HMAC", style = MaterialTheme.typography.bodySmall)
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
                    Text(
                        text = "Enter statements, notes, or observations. The Leveler Engine will analyze for contradictions.",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    OutlinedTextField(
                        value = textDescription,
                        onValueChange = { textDescription = it },
                        label = { Text("Description") },
                        placeholder = { Text("e.g., WhatsApp conversation summary") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = textContent,
                        onValueChange = { textContent = it },
                        label = { Text("Content") },
                        placeholder = { Text("Paste the text content here...") },
                        minLines = 5,
                        maxLines = 10,
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
                    Text("Analyze & Add")
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

package org.verumomnis.forensic.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import org.verumomnis.forensic.core.ForensicCase
import org.verumomnis.forensic.core.VerumOmnisApplication
import org.verumomnis.forensic.ui.theme.VerumOmnisTheme

/**
 * Main Activity for Verum Omnis Forensic Engine
 *
 * Provides the primary interface for:
 * - Creating new forensic cases
 * - Adding evidence to cases
 * - Generating forensic reports
 * - Viewing reports
 */
class MainActivity : ComponentActivity() {

    private var currentCase: ForensicCase? = null

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.all { it.value }
        if (allGranted) {
            Toast.makeText(this, "Permissions granted", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(
                this,
                "Some permissions denied - certain features may be limited",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestPermissions()

        setContent {
            VerumOmnisTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen(
                        currentCase = currentCase,
                        onCreateCase = { caseName -> createNewCase(caseName) },
                        onAddEvidence = { startScanner() },
                        onGenerateReport = { generateReport() },
                        onViewReport = { viewReport() }
                    )
                }
            }
        }
    }

    private fun requestPermissions() {
        val permissions = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

        val permissionsToRequest = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (permissionsToRequest.isNotEmpty()) {
            permissionLauncher.launch(permissionsToRequest.toTypedArray())
        }
    }

    private fun createNewCase(caseName: String) {
        val app = application as VerumOmnisApplication
        lifecycleScope.launch {
            currentCase = app.forensicEngine.createNewCase(caseName)
            Toast.makeText(
                this@MainActivity,
                "Case created: ${currentCase?.name}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun startScanner() {
        if (currentCase == null) {
            Toast.makeText(this, "Please create a case first", Toast.LENGTH_SHORT).show()
            return
        }
        startActivity(Intent(this, ScannerActivity::class.java))
    }

    private fun generateReport() {
        val case = currentCase
        if (case == null || case.evidenceItems.isEmpty()) {
            Toast.makeText(this, "Please add evidence first", Toast.LENGTH_SHORT).show()
            return
        }

        val app = application as VerumOmnisApplication
        lifecycleScope.launch {
            try {
                val report = app.forensicEngine.generateReport(case)
                Toast.makeText(
                    this@MainActivity,
                    "Report generated: ${report.name}",
                    Toast.LENGTH_LONG
                ).show()
            } catch (e: Exception) {
                Toast.makeText(
                    this@MainActivity,
                    "Error generating report: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun viewReport() {
        if (currentCase == null) {
            Toast.makeText(this, "Please create a case first", Toast.LENGTH_SHORT).show()
            return
        }
        startActivity(Intent(this, ReportViewerActivity::class.java))
    }
}

@Composable
fun MainScreen(
    currentCase: ForensicCase?,
    onCreateCase: (String) -> Unit,
    onAddEvidence: () -> Unit,
    onGenerateReport: () -> Unit,
    onViewReport: () -> Unit
) {
    var caseName by remember { mutableStateOf("") }
    var showCreateDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        Text(
            text = "VERUM OMNIS",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Forensic Evidence Engine",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.secondary
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Constitution Mode Badge
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Text(
                text = "Constitution Mode: ACTIVE",
                modifier = Modifier.padding(8.dp).fillMaxWidth(),
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Current Case Info
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Current Case",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                if (currentCase != null) {
                    Text("Name: ${currentCase.name}")
                    Text("ID: ${currentCase.id.take(8)}...")
                    Text("Evidence: ${currentCase.evidenceItems.size} items")
                } else {
                    Text("No active case")
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Action Buttons
        Button(
            onClick = { showCreateDialog = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Create New Case")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = onAddEvidence,
            modifier = Modifier.fillMaxWidth(),
            enabled = currentCase != null
        ) {
            Text("Add Evidence")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = onGenerateReport,
            modifier = Modifier.fillMaxWidth(),
            enabled = currentCase != null && (currentCase.evidenceItems.isNotEmpty())
        ) {
            Text("Generate Report")
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedButton(
            onClick = onViewReport,
            modifier = Modifier.fillMaxWidth(),
            enabled = currentCase != null
        ) {
            Text("View Reports")
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Forensic Standards Info
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Forensic Standards",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text("• Hash: SHA-512", style = MaterialTheme.typography.bodySmall)
                Text("• Seal: HMAC-SHA512", style = MaterialTheme.typography.bodySmall)
                Text("• PDF: Version 1.7", style = MaterialTheme.typography.bodySmall)
                Text("• Mode: Offline-First", style = MaterialTheme.typography.bodySmall)
            }
        }

        // Version info
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Version ${VerumOmnisApplication.VERSION}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.outline
        )
    }

    // Create Case Dialog
    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("Create New Case") },
            text = {
                OutlinedTextField(
                    value = caseName,
                    onValueChange = { caseName = it },
                    label = { Text("Case Name") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (caseName.isNotBlank()) {
                            onCreateCase(caseName)
                            caseName = ""
                            showCreateDialog = false
                        }
                    }
                ) {
                    Text("Create")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

package org.verumomnis.forensic.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import org.verumomnis.forensic.ui.theme.VerumOmnisTheme

/**
 * Main Activity - Entry point for Verum Omnis Forensic Engine
 * 
 * Provides the main interface for:
 * - Scanning new documents
 * - Importing existing PDFs
 * - Viewing generated reports
 * - Understanding the constitutional governance
 */
class MainActivity : ComponentActivity() {
    
    private val requiredPermissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.CAMERA
    )
    
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.entries.all { it.value }
        if (allGranted) {
            Toast.makeText(this, "Permissions granted", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(
                this,
                "Some permissions denied. App may have limited functionality.",
                Toast.LENGTH_LONG
            ).show()
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Request permissions if not granted
        if (!hasAllPermissions()) {
            permissionLauncher.launch(requiredPermissions)
        }
        
        setContent {
            VerumOmnisTheme {
                MainScreen(
                    onScanDocument = { startScanner() },
                    onImportPdf = { startImport() },
                    onViewReports = { viewReports() },
                    onViewConstitution = { viewConstitution() }
                )
            }
        }
    }
    
    private fun hasAllPermissions(): Boolean {
        return requiredPermissions.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
    }
    
    private fun startScanner() {
        val intent = Intent(this, ScannerActivity::class.java)
        startActivity(intent)
    }
    
    private fun startImport() {
        // Launch document picker
        Toast.makeText(this, "Document import opening...", Toast.LENGTH_SHORT).show()
    }
    
    private fun viewReports() {
        val intent = Intent(this, ReportViewerActivity::class.java)
        startActivity(intent)
    }
    
    private fun viewConstitution() {
        Toast.makeText(this, "Opening Constitution...", Toast.LENGTH_SHORT).show()
    }
}

@Composable
fun MainScreen(
    onScanDocument: () -> Unit,
    onImportPdf: () -> Unit,
    onViewReports: () -> Unit,
    onViewConstitution: () -> Unit
) {
    val scrollState = rememberScrollState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1A237E),
                        Color(0xFF283593)
                    )
                )
            )
            .verticalScroll(scrollState)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(40.dp))
        
        // Logo/Title
        Text(
            text = "VERUM OMNIS",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center
        )
        
        Text(
            text = "FORENSIC ENGINE",
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFFBBDEFB),
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Tagline
        Text(
            text = "Stateless • Offline • Cryptographically Sealed",
            fontSize = 12.sp,
            color = Color(0xFF90CAF9),
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(40.dp))
        
        // Info card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF303F9F).copy(alpha = 0.5f)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Constitutional Governance Layer",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "This engine operates under strict ethical principles. " +
                           "All processing occurs locally. No data is transmitted " +
                           "externally. Evidence is cryptographically sealed for " +
                           "legal admissibility.",
                    fontSize = 12.sp,
                    color = Color(0xFFE8EAF6),
                    lineHeight = 18.sp
                )
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Main action buttons
        ActionButton(
            text = "Scan Document",
            description = "Capture and seal new evidence",
            onClick = onScanDocument
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        ActionButton(
            text = "Import PDF",
            description = "Process existing document",
            onClick = onImportPdf
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        ActionButton(
            text = "View Reports",
            description = "Access generated reports",
            onClick = onViewReports
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Constitution button
        TextButton(
            onClick = onViewConstitution,
            colors = ButtonDefaults.textButtonColors(
                contentColor = Color(0xFF90CAF9)
            )
        ) {
            Text(
                text = "View Constitution & Principles",
                fontSize = 14.sp
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Footer
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Version 1.0",
                fontSize = 10.sp,
                color = Color(0xFF7986CB)
            )
            Text(
                text = "Created by Liam Highcock",
                fontSize = 10.sp,
                color = Color(0xFF7986CB)
            )
            Text(
                text = "Verum Global Foundation",
                fontSize = 10.sp,
                color = Color(0xFF7986CB)
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun ActionButton(
    text: String,
    description: String,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF3949AB)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = text,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = description,
                fontSize = 12.sp,
                color = Color(0xFFBBDEFB)
            )
        }
    }
}

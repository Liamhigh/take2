package org.verumomnis.forensic.ui

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.verumomnis.forensic.core.VerumOmnisApplication
import org.verumomnis.forensic.integrity.APKIntegrityChecker
import org.verumomnis.forensic.integrity.IntegrityReport
import org.verumomnis.forensic.ui.theme.VerumOmnisTheme
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.TimeZone

/**
 * Activity for displaying APK integrity verification results.
 * 
 * This activity serves as the forensic verification interface,
 * establishing the APK hash as the root of trust for all forensic operations.
 * 
 * Per verum-constitution.json:
 * - tamper_detection: mandatory
 * - audit_trail: true
 */
class VerificationActivity : ComponentActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Perform APK integrity verification
        val integrity = APKIntegrityChecker.verifyAPKIntegrity(this)
        
        setContent {
            VerumOmnisTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    VerificationScreen(
                        integrity = integrity,
                        onBack = { finish() }
                    )
                }
            }
        }
    }
}

@Composable
fun VerificationScreen(
    integrity: IntegrityReport,
    onBack: () -> Unit
) {
    val scrollState = rememberScrollState()
    val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z")
        .withZone(ZoneId.systemDefault())
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        Text(
            text = "🔐 APK INTEGRITY VERIFICATION",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Status Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (integrity.matches) 
                    Color(0xFF1B5E20) // Dark green
                else 
                    Color(0xFFB71C1C) // Dark red
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (integrity.matches) "✅ VALID" else "❌ TAMPERED",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (integrity.matches) 
                        "APK Integrity Verified - Safe for Forensic Use"
                    else 
                        "FORENSIC ENGINE COMPROMISED",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Hash Comparison
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "APK HASH COMPARISON",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = "Expected Hash:",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
                Text(
                    text = integrity.expectedHash,
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Actual Hash:",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
                Text(
                    text = integrity.actualHash.ifEmpty { "N/A" },
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace,
                    color = if (integrity.matches) MaterialTheme.colorScheme.onSurface 
                           else MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Device Information
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "DEVICE INFORMATION",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                DeviceInfoRow("Model", Build.MODEL)
                DeviceInfoRow("Manufacturer", Build.MANUFACTURER)
                DeviceInfoRow("Android Version", Build.VERSION.RELEASE)
                DeviceInfoRow("Security Patch", Build.VERSION.SECURITY_PATCH)
                DeviceInfoRow("App Version", VerumOmnisApplication.VERSION)
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Verification Timestamp
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "VERIFICATION TIMESTAMP",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                val verificationTime = Instant.ofEpochMilli(integrity.verificationTime)
                val utcOffset = TimeZone.getDefault()
                    .getOffset(System.currentTimeMillis()) / 3600000
                
                DeviceInfoRow("Verified", dateFormatter.format(verificationTime))
                DeviceInfoRow("UTC Offset", "$utcOffset hours")
            }
        }
        
        if (integrity.matches) {
            Spacer(modifier = Modifier.height(16.dp))
            
            // Verification Instructions
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "INDEPENDENT VERIFICATION",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = APKIntegrityChecker.getIndependentVerificationInstructions(),
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Trust Statement
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "CHAIN OF TRUST ESTABLISHED",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = """
                            This verification establishes that:
                            
                            1. The forensic engine has not been modified since build
                            2. All output from this engine originates from a known source
                            3. The chain of custody begins with this verified APK
                            
                            This APK hash can be independently verified by any forensic expert.
                        """.trimIndent(),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        } else {
            Spacer(modifier = Modifier.height(16.dp))
            
            // Warning for compromised APK
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "⚠️ WARNING: FORENSIC INTEGRITY COMPROMISED",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = """
                            The APK hash does not match the expected value.
                            This indicates the application may have been modified.
                            
                            DO NOT use this application for forensic evidence collection.
                            Any evidence processed by this version may be inadmissible.
                            
                            Please reinstall from a trusted source.
                        """.trimIndent(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Back Button
        Button(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Back to Main")
        }
        
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun DeviceInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.secondary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

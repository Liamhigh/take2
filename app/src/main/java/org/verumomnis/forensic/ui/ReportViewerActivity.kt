package org.verumomnis.forensic.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import org.verumomnis.forensic.ui.theme.VerumOmnisTheme
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * Report Viewer Activity
 * 
 * Displays list of generated forensic reports and allows
 * viewing and sharing them.
 */
class ReportViewerActivity : ComponentActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            VerumOmnisTheme {
                ReportViewerScreen(
                    reportsDir = File(getExternalFilesDir(null), "reports"),
                    onShareReport = { file -> shareReport(file) },
                    onBack = { finish() }
                )
            }
        }
    }
    
    private fun shareReport(file: File) {
        val uri = FileProvider.getUriForFile(
            this,
            "${packageName}.fileprovider",
            file
        )
        
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        
        startActivity(Intent.createChooser(shareIntent, "Share Forensic Report"))
    }
}

@Composable
fun ReportViewerScreen(
    reportsDir: File,
    onShareReport: (File) -> Unit,
    onBack: () -> Unit
) {
    var reports by remember { mutableStateOf(listOf<ReportFile>()) }
    
    LaunchedEffect(Unit) {
        reports = loadReports(reportsDir)
    }
    
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
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onBack) {
                Text("← Back", color = Color.White)
            }
            
            Text(
                text = "FORENSIC REPORTS",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            
            Box(modifier = Modifier.width(64.dp))
        }
        
        // Report count
        Text(
            text = "${reports.size} reports found",
            color = Color(0xFFBBDEFB),
            fontSize = 14.sp,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        if (reports.isEmpty()) {
            // Empty state
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "No Reports Yet",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Scan a document to create your first forensic report",
                    color = Color(0xFFBBDEFB),
                    fontSize = 14.sp
                )
            }
        } else {
            // Report list
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(reports) { report ->
                    ReportCard(
                        report = report,
                        onShare = { onShareReport(report.file) }
                    )
                }
            }
        }
    }
}

@Composable
fun ReportCard(
    report: ReportFile,
    onShare: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF303F9F).copy(alpha = 0.7f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = report.evidenceId,
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = report.dateCreated,
                        color = Color(0xFFBBDEFB),
                        fontSize = 12.sp
                    )
                }
                
                IconButton(onClick = onShare) {
                    Text("📤", fontSize = 20.sp)
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "📄 ${report.sizeKb} KB",
                    color = Color(0xFF90CAF9),
                    fontSize = 11.sp
                )
                Text(
                    text = "🔒 Sealed",
                    color = Color(0xFF81C784),
                    fontSize = 11.sp
                )
            }
        }
    }
}

data class ReportFile(
    val file: File,
    val evidenceId: String,
    val dateCreated: String,
    val sizeKb: Long
)

private fun loadReports(reportsDir: File): List<ReportFile> {
    if (!reportsDir.exists()) return emptyList()
    
    val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
    
    return reportsDir.listFiles()
        ?.filter { it.extension == "pdf" }
        ?.map { file ->
            val evidenceId = extractEvidenceId(file.name)
            ReportFile(
                file = file,
                evidenceId = evidenceId,
                dateCreated = dateFormat.format(Date(file.lastModified())),
                sizeKb = file.length() / 1024
            )
        }
        ?.sortedByDescending { it.file.lastModified() }
        ?: emptyList()
}

private fun extractEvidenceId(filename: String): String {
    // Extract evidence ID from filename like "VerumOmnis_EV-123456-ABCD_timestamp.pdf"
    val match = Regex("EV-[0-9]+-[A-F0-9]+").find(filename)
    return match?.value ?: filename.removeSuffix(".pdf")
}

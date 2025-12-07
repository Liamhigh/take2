package org.verumomnis.forensic.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import org.verumomnis.forensic.engine.CaseRepository
import org.verumomnis.forensic.engine.EngineOrchestrator
import org.verumomnis.forensic.model.Case
import org.verumomnis.forensic.model.Evidence
import org.verumomnis.forensic.ui.theme.VerumOmnisTheme

/**
 * Activity for viewing and managing a specific forensic case
 */
class CaseDetailActivity : ComponentActivity() {
    
    private var currentCase: Case? = null
    private lateinit var repository: CaseRepository
    private lateinit var orchestrator: EngineOrchestrator
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        repository = CaseRepository(this)
        orchestrator = EngineOrchestrator(this)
        
        val caseId = intent.getStringExtra("caseId")
        if (caseId == null) {
            Toast.makeText(this, "No case ID provided", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        
        currentCase = repository.loadCase(caseId)
        if (currentCase == null) {
            Toast.makeText(this, "Case not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        
        setContent {
            VerumOmnisTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    CaseDetailScreen(
                        case = currentCase!!,
                        onAddEvidence = { addEvidence(it) },
                        onRunEngine = { runEngine() },
                        onBack = { finish() }
                    )
                }
            }
        }
    }
    
    private fun addEvidence(evidence: Evidence) {
        currentCase?.let { case ->
            case.evidence.add(evidence)
            repository.saveCase(case)
            Toast.makeText(this, "Evidence added", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun runEngine() {
        val case = currentCase ?: return
        
        lifecycleScope.launch {
            try {
                val report = orchestrator.run(case)
                val intent = Intent(this@CaseDetailActivity, ReportViewerActivity::class.java)
                intent.putExtra("reportText", report)
                startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(
                    this@CaseDetailActivity,
                    "Error running engine: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}

@Composable
fun CaseDetailScreen(
    case: Case,
    onAddEvidence: (Evidence) -> Unit,
    onRunEngine: () -> Unit,
    onBack: () -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var evidenceSummary by remember { mutableStateOf("") }
    var evidenceContent by remember { mutableStateOf("") }
    
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
                text = case.caseName,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            TextButton(onClick = onBack) {
                Text("Back")
            }
        }
        
        Text(
            text = "Case ID: ${case.caseId.take(8)}...",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Evidence List
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Evidence (${case.evidence.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                if (case.evidence.isEmpty()) {
                    Text(
                        text = "No evidence added yet",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(case.evidence) { evidence ->
                            EvidenceCard(evidence)
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Action Buttons
        Button(
            onClick = { showAddDialog = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Add Evidence")
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Button(
            onClick = onRunEngine,
            modifier = Modifier.fillMaxWidth(),
            enabled = case.evidence.isNotEmpty()
        ) {
            Text("Run Contradiction Engine")
        }
    }
    
    // Add Evidence Dialog
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Add Evidence") },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    OutlinedTextField(
                        value = evidenceSummary,
                        onValueChange = { evidenceSummary = it },
                        label = { Text("Summary") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    OutlinedTextField(
                        value = evidenceContent,
                        onValueChange = { evidenceContent = it },
                        label = { Text("Content") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 4
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (evidenceSummary.isNotBlank() && evidenceContent.isNotBlank()) {
                            onAddEvidence(
                                Evidence(
                                    type = "text",
                                    summary = evidenceSummary,
                                    content = evidenceContent
                                )
                            )
                            evidenceSummary = ""
                            evidenceContent = ""
                            showAddDialog = false
                        }
                    }
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun EvidenceCard(evidence: Evidence) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = evidence.summary,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "Type: ${evidence.type}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (evidence.content.isNotBlank()) {
                Text(
                    text = evidence.content.take(100) + if (evidence.content.length > 100) "..." else "",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

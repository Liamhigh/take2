package org.verumomnis

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import org.verumomnis.databinding.ActivityCaseDetailBinding
import org.verumomnis.engine.NarrativeEngine
import org.verumomnis.engine.ContradictionEngine
import org.verumomnis.engine.ClassificationEngine
import org.verumomnis.engine.ReportEngine
import org.verumomnis.utils.FileUtils
import org.verumomnis.utils.OcrUtils

/**
 * Case Detail Activity
 * 
 * Allows adding evidence and generating forensic contradiction reports
 * using the four-layer Verum Omnis engine pipeline.
 */
class CaseDetailActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityCaseDetailBinding
    private val allEvidence = StringBuilder()
    private var caseId: String = ""
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCaseDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        caseId = intent.getStringExtra("caseId") ?: ""
        
        binding.btnAddText.setOnClickListener {
            val text = binding.editTextNote.text.toString()
            if (text.isNotEmpty()) {
                allEvidence.append(text).append("\n")
                binding.editTextNote.text.clear()
            }
        }
        
        binding.btnAddImage.setOnClickListener {
            OcrUtils.pickImage(this) { extractedText ->
                allEvidence.append(extractedText).append("\n")
            }
        }
        
        binding.btnGenerateReport.setOnClickListener {
            lifecycleScope.launch {
                // FOUR-LAYER ENGINE PIPELINE
                // This pipeline is deterministic and runs in exactly this order:
                // Narrative → Contradiction → Classification → Report
                
                // LAYER 1: Narrative Engine - Normalize evidence into sentences
                val narrativeEngine = NarrativeEngine()
                narrativeEngine.ingest(allEvidence.toString())
                val sentences = narrativeEngine.normalize()
                
                // LAYER 2: Contradiction Engine - Detect contradictions
                val contradictionEngine = ContradictionEngine()
                val contradictions = contradictionEngine.analyze(sentences)
                
                // LAYER 3: Classification Engine - Map to legal categories
                val classificationEngine = ClassificationEngine()
                val legalFindings = classificationEngine.classify(contradictions)
                
                // LAYER 4: Report Engine - Build final forensic report
                val reportEngine = ReportEngine()
                val report = reportEngine.build(sentences, contradictions, legalFindings)
                
                // Save the report
                FileUtils.saveReport(this@CaseDetailActivity, caseId, report)
                
                // Navigate to report viewer
                val intent = Intent(this@CaseDetailActivity, ReportViewerActivity::class.java)
                intent.putExtra("caseId", caseId)
                startActivity(intent)
            }
        }
    }
}

package com.verumomnis.forensic.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.verumomnis.forensic.BuildConfig
import com.verumomnis.forensic.core.EvidenceMetadata
import com.verumomnis.forensic.core.RawEvidence
import com.verumomnis.forensic.databinding.ActivityMainBinding
import com.verumomnis.forensic.evidence.VerumForensicEngine
import com.verumomnis.forensic.offline.OfflineModeManager
import kotlinx.coroutines.launch
import java.io.File
import java.time.Instant

/**
 * Main Activity for Verum Omnis Forensic App
 * 
 * Stateless UI - no user session data is stored
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var forensicEngine: VerumForensicEngine

    private val selectedFiles = mutableListOf<Uri>()

    private val filePickerLauncher = registerForActivityResult(
        ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        if (uris.isNotEmpty()) {
            selectedFiles.clear()
            selectedFiles.addAll(uris)
            updateSelectedFilesUI()
        }
    }

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (allGranted) {
            openFilePicker()
        } else {
            Toast.makeText(this, "Permissions required for file access", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        forensicEngine = VerumForensicEngine(applicationContext)

        setupUI()
        displayOfflineStatus()
        displayConstitutionalInfo()
    }

    private fun setupUI() {
        binding.buttonSelectFiles.setOnClickListener {
            checkPermissionsAndOpenPicker()
        }

        binding.buttonSealEvidence.setOnClickListener {
            if (selectedFiles.isNotEmpty()) {
                sealEvidence()
            } else {
                Toast.makeText(this, "Please select files first", Toast.LENGTH_SHORT).show()
            }
        }

        binding.buttonVerify.setOnClickListener {
            Toast.makeText(this, "Select a sealed package to verify", Toast.LENGTH_SHORT).show()
        }

        // Display app info
        binding.textVersion.text = "Version: ${BuildConfig.VERSION_NAME}"
        binding.textHashStandard.text = "Hash: ${BuildConfig.HASH_STANDARD}"
    }

    private fun checkPermissionsAndOpenPicker() {
        val permissions = mutableListOf<String>()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.READ_MEDIA_IMAGES)
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_VIDEO)
                != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.READ_MEDIA_VIDEO)
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }

        if (permissions.isNotEmpty()) {
            permissionLauncher.launch(permissions.toTypedArray())
        } else {
            openFilePicker()
        }
    }

    private fun openFilePicker() {
        filePickerLauncher.launch(arrayOf(
            "application/pdf",
            "image/*",
            "video/*",
            "audio/*"
        ))
    }

    private fun updateSelectedFilesUI() {
        binding.textSelectedCount.text = "${selectedFiles.size} files selected"
        binding.buttonSealEvidence.isEnabled = selectedFiles.isNotEmpty()
    }

    private fun sealEvidence() {
        binding.progressBar.visibility = android.view.View.VISIBLE
        binding.buttonSealEvidence.isEnabled = false

        lifecycleScope.launch {
            try {
                val rawEvidence = selectedFiles.mapNotNull { uri ->
                    copyToLocalStorage(uri)
                }

                val result = forensicEngine.processEvidence(
                    evidence = rawEvidence,
                    metadata = EvidenceMetadata(
                        description = "Evidence sealed at ${Instant.now()}"
                    )
                )

                result.onSuccess { pkg ->
                    Toast.makeText(
                        this@MainActivity,
                        "Evidence sealed!\nPackage: ${pkg.packageId}\nHash: ${pkg.seal.combinedSealHash.take(16)}...",
                        Toast.LENGTH_LONG
                    ).show()

                    binding.textLastSeal.text = "Last seal: ${pkg.packageId.take(8)}..."
                    binding.textLastSealHash.text = "Hash: ${pkg.seal.combinedSealHash.take(32)}..."

                    selectedFiles.clear()
                    updateSelectedFilesUI()
                }.onFailure { error ->
                    Toast.makeText(
                        this@MainActivity,
                        "Sealing failed: ${error.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } finally {
                binding.progressBar.visibility = android.view.View.GONE
                binding.buttonSealEvidence.isEnabled = true
            }
        }
    }

    private fun copyToLocalStorage(uri: Uri): RawEvidence? {
        return try {
            val inputStream = contentResolver.openInputStream(uri) ?: return null
            val fileName = getFileName(uri)
            val localFile = File(cacheDir, "evidence_${System.currentTimeMillis()}_$fileName")

            localFile.outputStream().use { output ->
                inputStream.copyTo(output)
            }

            RawEvidence(
                filePath = localFile.absolutePath,
                originalFilename = fileName,
                mimeType = contentResolver.getType(uri) ?: "application/octet-stream"
            )
        } catch (e: Exception) {
            null
        }
    }

    private fun getFileName(uri: Uri): String {
        var name = "evidence"
        contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
            if (cursor.moveToFirst() && nameIndex >= 0) {
                name = cursor.getString(nameIndex)
            }
        }
        return name
    }

    private fun displayOfflineStatus() {
        val status = OfflineModeManager.getOfflineStatus(this)
        binding.textOfflineStatus.text = if (status.deviceOffline) {
            "📴 Device Offline - Full functionality"
        } else {
            "🔒 Offline Mode Active - All data stays local"
        }
    }

    private fun displayConstitutionalInfo() {
        binding.textConstitution.text = buildString {
            append("🛡️ Constitutional Governance Active\n")
            append("• Stateless: ${BuildConfig.STATELESS}\n")
            append("• Offline-first: ${BuildConfig.OFFLINE_FIRST}\n")
            append("• Seal required: ${BuildConfig.SEAL_REQUIRED}\n")
            append("• Hash: ${BuildConfig.HASH_STANDARD}")
        }
    }
}

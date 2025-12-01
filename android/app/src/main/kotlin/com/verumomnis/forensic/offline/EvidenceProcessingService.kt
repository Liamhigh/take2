package com.verumomnis.forensic.offline

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.verumomnis.forensic.R
import com.verumomnis.forensic.core.EvidenceMetadata
import com.verumomnis.forensic.core.RawEvidence
import com.verumomnis.forensic.evidence.VerumForensicEngine
import kotlinx.coroutines.*

/**
 * Evidence Processing Service
 * 
 * Foreground service for processing evidence offline.
 * 
 * Constitutional Compliance:
 * - Offline: All processing done locally without network
 * - Stateless: No user session data stored
 * - No telemetry: Zero data transmission
 */
class EvidenceProcessingService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private lateinit var forensicEngine: VerumForensicEngine

    companion object {
        const val CHANNEL_ID = "evidence_processing"
        const val NOTIFICATION_ID = 1001

        const val ACTION_PROCESS = "com.verumomnis.forensic.PROCESS"
        const val ACTION_VERIFY = "com.verumomnis.forensic.VERIFY"
        const val ACTION_EXPORT = "com.verumomnis.forensic.EXPORT"

        const val EXTRA_EVIDENCE_PATHS = "evidence_paths"
        const val EXTRA_PACKAGE_ID = "package_id"
        const val EXTRA_OUTPUT_PATH = "output_path"
    }

    override fun onCreate() {
        super.onCreate()
        forensicEngine = VerumForensicEngine(applicationContext)
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = createNotification("Processing evidence...")
        startForeground(NOTIFICATION_ID, notification)

        when (intent?.action) {
            ACTION_PROCESS -> handleProcessAction(intent)
            ACTION_VERIFY -> handleVerifyAction(intent)
            ACTION_EXPORT -> handleExportAction(intent)
            else -> stopSelf()
        }

        return START_NOT_STICKY
    }

    private fun handleProcessAction(intent: Intent) {
        val evidencePaths = intent.getStringArrayListExtra(EXTRA_EVIDENCE_PATHS) ?: return

        serviceScope.launch {
            try {
                val rawEvidence = evidencePaths.map { path ->
                    RawEvidence(
                        filePath = path,
                        originalFilename = path.substringAfterLast("/"),
                        mimeType = getMimeType(path)
                    )
                }

                val result = forensicEngine.processEvidence(
                    evidence = rawEvidence,
                    metadata = EvidenceMetadata()
                )

                result.onSuccess { pkg ->
                    updateNotification("Evidence sealed: ${pkg.packageId}")
                    broadcastResult(ACTION_PROCESS, true, pkg.packageId)
                }.onFailure { error ->
                    updateNotification("Processing failed: ${error.message}")
                    broadcastResult(ACTION_PROCESS, false, null)
                }
            } finally {
                stopSelf()
            }
        }
    }

    private fun handleVerifyAction(intent: Intent) {
        // Verification logic
        serviceScope.launch {
            try {
                updateNotification("Verifying evidence integrity...")
                // Verification implementation
            } finally {
                stopSelf()
            }
        }
    }

    private fun handleExportAction(intent: Intent) {
        val outputPath = intent.getStringExtra(EXTRA_OUTPUT_PATH) ?: return

        serviceScope.launch {
            try {
                updateNotification("Exporting to PDF...")
                // Export implementation
            } finally {
                stopSelf()
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Evidence Processing",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows progress of evidence processing"
            }

            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(text: String): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Verum Omnis Forensic")
            .setContentText(text)
            .setSmallIcon(android.R.drawable.ic_menu_upload)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()
    }

    private fun updateNotification(text: String) {
        val notification = createNotification(text)
        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(NOTIFICATION_ID, notification)
    }

    private fun broadcastResult(action: String, success: Boolean, packageId: String?) {
        val resultIntent = Intent("com.verumomnis.forensic.RESULT").apply {
            putExtra("action", action)
            putExtra("success", success)
            putExtra("package_id", packageId)
        }
        sendBroadcast(resultIntent)
    }

    private fun getMimeType(path: String): String {
        return when (path.substringAfterLast(".").lowercase()) {
            "pdf" -> "application/pdf"
            "jpg", "jpeg" -> "image/jpeg"
            "png" -> "image/png"
            "gif" -> "image/gif"
            "mp4" -> "video/mp4"
            "mp3" -> "audio/mpeg"
            "wav" -> "audio/wav"
            else -> "application/octet-stream"
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
    }
}

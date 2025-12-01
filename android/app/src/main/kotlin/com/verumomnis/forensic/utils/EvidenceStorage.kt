package com.verumomnis.forensic.utils

import android.content.Context
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.verumomnis.forensic.core.EvidencePackage
import java.io.File
import java.time.Instant

/**
 * Evidence Storage Utilities
 * 
 * Local-only storage for sealed evidence packages.
 * 
 * Constitutional Compliance:
 * - local_storage: encrypted_case_folder
 * - no_cloud_logging: true
 * - stateless: true (no user session data)
 */
object EvidenceStorage {

    private const val SEALED_DIR = "sealed"
    private const val EXTENSION = ".verum"

    private val gson: Gson = GsonBuilder()
        .setPrettyPrinting()
        .create()

    /**
     * Get the directory for sealed evidence packages
     */
    fun getSealedDirectory(context: Context): File {
        return File(context.filesDir, SEALED_DIR).also {
            if (!it.exists()) it.mkdirs()
        }
    }

    /**
     * Save a sealed evidence package to local storage
     * 
     * @param context Application context
     * @param evidencePackage The package to save
     * @return The saved file
     */
    fun savePackage(context: Context, evidencePackage: EvidencePackage): File {
        val sealedDir = getSealedDirectory(context)
        val filename = "${evidencePackage.packageId}$EXTENSION"
        val file = File(sealedDir, filename)
        
        file.writeText(gson.toJson(evidencePackage))
        
        return file
    }

    /**
     * Load a sealed evidence package from local storage
     * 
     * @param context Application context
     * @param packageId The package ID to load
     * @return The loaded package, or null if not found
     */
    fun loadPackage(context: Context, packageId: String): EvidencePackage? {
        val sealedDir = getSealedDirectory(context)
        val file = File(sealedDir, "$packageId$EXTENSION")
        
        return if (file.exists()) {
            gson.fromJson(file.readText(), EvidencePackage::class.java)
        } else {
            null
        }
    }

    /**
     * List all sealed evidence packages
     * 
     * @param context Application context
     * @return List of package IDs
     */
    fun listPackages(context: Context): List<String> {
        val sealedDir = getSealedDirectory(context)
        return sealedDir.listFiles()
            ?.filter { it.name.endsWith(EXTENSION) }
            ?.map { it.name.removeSuffix(EXTENSION) }
            ?: emptyList()
    }

    /**
     * Delete a sealed evidence package
     * 
     * @param context Application context
     * @param packageId The package ID to delete
     * @return True if deleted successfully
     */
    fun deletePackage(context: Context, packageId: String): Boolean {
        val sealedDir = getSealedDirectory(context)
        val file = File(sealedDir, "$packageId$EXTENSION")
        return file.delete()
    }

    /**
     * Get storage statistics
     */
    fun getStorageStats(context: Context): StorageStats {
        val sealedDir = getSealedDirectory(context)
        val files = sealedDir.listFiles() ?: emptyArray()
        
        return StorageStats(
            totalPackages = files.size,
            totalSizeBytes = files.sumOf { it.length() },
            oldestPackage = files.minByOrNull { it.lastModified() }?.name?.removeSuffix(EXTENSION),
            newestPackage = files.maxByOrNull { it.lastModified() }?.name?.removeSuffix(EXTENSION)
        )
    }
}

/**
 * Storage statistics
 */
data class StorageStats(
    val totalPackages: Int,
    val totalSizeBytes: Long,
    val oldestPackage: String?,
    val newestPackage: String?
)

/**
 * File utilities for evidence processing
 */
object FileUtils {

    /**
     * Get file size in human-readable format
     */
    fun formatFileSize(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> "%.1f KB".format(bytes / 1024.0)
            bytes < 1024 * 1024 * 1024 -> "%.1f MB".format(bytes / (1024.0 * 1024))
            else -> "%.2f GB".format(bytes / (1024.0 * 1024 * 1024))
        }
    }

    /**
     * Get file extension
     */
    fun getExtension(filename: String): String {
        return filename.substringAfterLast('.', "")
    }

    /**
     * Check if file is a supported evidence type
     */
    fun isSupportedEvidenceType(mimeType: String): Boolean {
        return mimeType.startsWith("image/") ||
               mimeType.startsWith("video/") ||
               mimeType.startsWith("audio/") ||
               mimeType == "application/pdf" ||
               mimeType == "text/plain"
    }
}

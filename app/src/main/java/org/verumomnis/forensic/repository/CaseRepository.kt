package org.verumomnis.forensic.repository

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import org.verumomnis.forensic.core.EvidenceType
import org.verumomnis.forensic.core.ForensicCase
import org.verumomnis.forensic.core.ForensicEvidence
import org.verumomnis.forensic.crypto.ForensicTripleHashSeal
import org.verumomnis.forensic.location.ForensicLocation
import org.verumomnis.forensic.jurisdiction.Jurisdiction
import java.io.File
import java.time.Instant

/**
 * Repository for managing forensic case persistence
 *
 * Responsibilities:
 * - Save cases to JSON files
 * - Load cases from JSON files
 * - List all cases
 * - Delete cases
 *
 * Storage Structure:
 * - cases/{caseId}/case.json - Case metadata
 * - cases/{caseId}/*.dat - Evidence files
 * - cases/{caseId}/*.seal - Seal files
 * - cases/{caseId}/report_*.pdf - Generated reports
 */
class CaseRepository(private val context: Context) {

    companion object {
        private const val TAG = "CaseRepository"
        private const val CASES_DIR = "cases"
    }

    private val casesDir: File
        get() = File(context.filesDir, CASES_DIR).also { it.mkdirs() }

    /**
     * Saves a case to disk as JSON
     */
    suspend fun saveCase(case: ForensicCase): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val caseDir = File(casesDir, case.id)
            caseDir.mkdirs()

            val caseJson = JSONObject().apply {
                put("id", case.id)
                put("name", case.name)
                put("createdAt", case.createdAt.toString())
                case.jurisdiction?.let { put("jurisdiction", it.name) }

                val evidenceArray = JSONArray()
                case.evidenceItems.forEach { evidence ->
                    val evidenceJson = JSONObject().apply {
                        put("id", evidence.id)
                        put("type", evidence.type.name)
                        put("description", evidence.description)
                        put("timestamp", evidence.timestamp.toString())
                        put("contentHash", evidence.contentHash)

                        // Save location if present
                        evidence.location?.let { location ->
                            put("location", JSONObject().apply {
                                put("latitude", location.latitude)
                                put("longitude", location.longitude)
                                put("altitude", location.altitude)
                                put("accuracy", location.accuracy)
                                put("timestamp", location.timestamp.toString())
                            })
                        }

                        // Save metadata
                        val metadataJson = JSONObject()
                        evidence.metadata.forEach { (key, value) ->
                            metadataJson.put(key, value)
                        }
                        put("metadata", metadataJson)

                        // Save seal information
                        val sealJson = JSONObject().apply {
                            put("contentHash", evidence.seal.contentHash)
                            put("timestamp", evidence.seal.timestamp.toString())
                            put("hmacSeal", evidence.seal.hmacSeal)
                            put("metadataHash", evidence.seal.metadataHash)
                        }
                        put("seal", sealJson)

                        put("fileName", evidence.file.name)
                    }
                    evidenceArray.put(evidenceJson)
                }
                put("evidence", evidenceArray)
            }

            val caseFile = File(caseDir, "case.json")
            caseFile.writeText(caseJson.toString(2))

            Log.d(TAG, "Case saved: ${case.id} (${case.name})")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error saving case: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Loads a case from disk
     */
    suspend fun loadCase(caseId: String): Result<ForensicCase> = withContext(Dispatchers.IO) {
        try {
            val caseDir = File(casesDir, caseId)
            if (!caseDir.exists()) {
                return@withContext Result.failure(IllegalArgumentException("Case not found: $caseId"))
            }

            val caseFile = File(caseDir, "case.json")
            if (!caseFile.exists()) {
                return@withContext Result.failure(IllegalArgumentException("Case metadata not found: $caseId"))
            }

            val caseJson = JSONObject(caseFile.readText())
            val id = caseJson.getString("id")
            val name = caseJson.getString("name")
            val createdAt = Instant.parse(caseJson.getString("createdAt"))
            val jurisdiction = if (caseJson.has("jurisdiction")) {
                Jurisdiction.valueOf(caseJson.getString("jurisdiction"))
            } else {
                null
            }

            val evidenceItems = mutableListOf<ForensicEvidence>()
            val evidenceArray = caseJson.getJSONArray("evidence")
            for (i in 0 until evidenceArray.length()) {
                val evidenceJson = evidenceArray.getJSONObject(i)
                val evidenceId = evidenceJson.getString("id")
                val type = EvidenceType.valueOf(evidenceJson.getString("type"))
                val description = evidenceJson.getString("description")
                val timestamp = Instant.parse(evidenceJson.getString("timestamp"))
                val contentHash = evidenceJson.getString("contentHash")

                // Load location if present
                val location = if (evidenceJson.has("location")) {
                    val locationJson = evidenceJson.getJSONObject("location")
                    ForensicLocation(
                        latitude = locationJson.getDouble("latitude"),
                        longitude = locationJson.getDouble("longitude"),
                        altitude = locationJson.optDouble("altitude", 0.0),
                        accuracy = locationJson.optDouble("accuracy", 0.0).toFloat(),
                        timestamp = Instant.parse(locationJson.getString("timestamp"))
                    )
                } else {
                    null
                }

                // Load metadata
                val metadata = mutableMapOf<String, String>()
                val metadataJson = evidenceJson.getJSONObject("metadata")
                metadataJson.keys().forEach { key ->
                    metadata[key] = metadataJson.getString(key)
                }

                // Load seal
                val sealJson = evidenceJson.getJSONObject("seal")
                val seal = ForensicTripleHashSeal(
                    contentHash = sealJson.getString("contentHash"),
                    timestamp = Instant.parse(sealJson.getString("timestamp")),
                    hmacSeal = sealJson.getString("hmacSeal"),
                    metadataHash = sealJson.getString("metadataHash"),
                    deviceInfo = emptyMap(), // Not stored in simplified seal
                    location = location
                )

                val fileName = evidenceJson.getString("fileName")
                val file = File(caseDir, fileName)

                val evidence = ForensicEvidence(
                    id = evidenceId,
                    type = type,
                    description = description,
                    timestamp = timestamp,
                    contentHash = contentHash,
                    seal = seal,
                    location = location,
                    metadata = metadata,
                    file = file
                )
                evidenceItems.add(evidence)
            }

            val case = ForensicCase(
                id = id,
                name = name,
                createdAt = createdAt,
                directory = caseDir,
                evidenceItems = evidenceItems,
                jurisdiction = jurisdiction
            )

            Log.d(TAG, "Case loaded: ${case.id} (${case.name}) with ${evidenceItems.size} evidence items")
            Result.success(case)
        } catch (e: Exception) {
            Log.e(TAG, "Error loading case: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Lists all cases
     */
    suspend fun listCases(): Result<List<CaseInfo>> = withContext(Dispatchers.IO) {
        try {
            val caseInfoList = mutableListOf<CaseInfo>()
            casesDir.listFiles()?.forEach { caseDir ->
                if (caseDir.isDirectory) {
                    val caseFile = File(caseDir, "case.json")
                    if (caseFile.exists()) {
                        try {
                            val caseJson = JSONObject(caseFile.readText())
                            caseInfoList.add(
                                CaseInfo(
                                    id = caseJson.getString("id"),
                                    name = caseJson.getString("name"),
                                    createdAt = Instant.parse(caseJson.getString("createdAt")),
                                    evidenceCount = caseJson.getJSONArray("evidence").length()
                                )
                            )
                        } catch (e: Exception) {
                            Log.e(TAG, "Error reading case ${caseDir.name}: ${e.message}")
                        }
                    }
                }
            }
            Result.success(caseInfoList.sortedByDescending { it.createdAt })
        } catch (e: Exception) {
            Log.e(TAG, "Error listing cases: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Deletes a case and all its files
     */
    suspend fun deleteCase(caseId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val caseDir = File(casesDir, caseId)
            if (caseDir.exists()) {
                caseDir.deleteRecursively()
                Log.d(TAG, "Case deleted: $caseId")
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting case: ${e.message}", e)
            Result.failure(e)
        }
    }
}

/**
 * Summary information about a case
 */
data class CaseInfo(
    val id: String,
    val name: String,
    val createdAt: Instant,
    val evidenceCount: Int
)

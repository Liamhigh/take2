package org.verumomnis.forensic.engine

import android.content.Context
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import org.verumomnis.forensic.model.Case
import java.io.File
import java.time.Instant

/**
 * Repository for saving and loading forensic cases
 */
class CaseRepository(private val context: Context) {
    
    private val gson: Gson = GsonBuilder()
        .registerTypeAdapter(Instant::class.java, InstantTypeAdapter())
        .create()
    
    private fun caseDir(): File {
        val dir = File(context.filesDir, "cases")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }
    
    fun saveCase(case: Case) {
        val file = File(caseDir(), "${case.caseId}.json")
        file.writeText(gson.toJson(case))
    }
    
    fun loadCase(id: String): Case? {
        val file = File(caseDir(), "$id.json")
        if (!file.exists()) return null
        return gson.fromJson(file.readText(), Case::class.java)
    }
    
    fun listCases(): List<Case> {
        return caseDir().listFiles()?.mapNotNull { file ->
            try {
                gson.fromJson(file.readText(), Case::class.java)
            } catch (e: Exception) {
                null
            }
        } ?: emptyList()
    }
}

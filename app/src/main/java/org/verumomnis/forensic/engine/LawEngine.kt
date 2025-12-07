package org.verumomnis.forensic.engine

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.io.InputStream

/**
 * Reads rule JSON files and applies legal logic
 */
class LawEngine(private val context: Context) {
    
    fun evaluate(statements: List<String>): List<String> {
        val rules = loadJsonArray("rules/verum_rules.json")
        val findings = mutableListOf<String>()
        
        for (i in 0 until rules.length()) {
            val rule = rules.getJSONObject(i)
            val trigger = rule.optString("trigger")
            val consequence = rule.optString("consequence")
            
            if (statements.any { it.contains(trigger, ignoreCase = true) }) {
                findings.add(consequence)
            }
        }
        
        return findings
    }
    
    private fun loadJsonArray(path: String): JSONArray {
        return try {
            val input: InputStream = context.assets.open(path)
            val text = input.bufferedReader().use { it.readText() }
            JSONArray(text)
        } catch (e: Exception) {
            // Return empty array if file doesn't exist
            JSONArray()
        }
    }
}

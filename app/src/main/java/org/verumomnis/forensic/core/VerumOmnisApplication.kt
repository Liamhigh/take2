package org.verumomnis.forensic.core

import android.app.Application
import android.util.Log
import org.verumomnis.forensic.crypto.CryptographicSealingEngine

/**
 * Verum Omnis Forensic Engine Application
 *
 * This application class initializes the forensic engine in a stateless,
 * offline-first configuration as mandated by the verum-constitution.json.
 *
 * Security principles:
 * - No cloud logging
 * - No telemetry
 * - Stateless operation
 * - Offline-first design
 * - Airgap ready
 *
 * Forensic Standards Compliance:
 * - ISO 27037: Digital evidence handling
 * - PDF/A-3B: Archival PDF format
 * - RFC 3161: Timestamp protocol (emulated offline)
 * - Daubert Standard: Methodology documentation for court
 */
class VerumOmnisApplication : Application() {

    companion object {
        private const val TAG = "VerumOmnisApp"
        const val VERSION = CryptographicSealingEngine.VERSION
        const val ENGINE_NAME = "Verum Omnis Constitutional Governance Layer"
    }

    lateinit var forensicEngine: ForensicEngine
        private set

    override fun onCreate() {
        super.onCreate()

        // Initialize the forensic engine
        forensicEngine = ForensicEngine(this)

        Log.i(TAG, "Verum Omnis Forensic Engine initialized")
        Log.i(TAG, "Version: $VERSION")
        Log.i(TAG, "Mode: Offline-First, Stateless")
        Log.i(TAG, "Standards: ISO 27037, PDF/A-3B, Daubert")
    }
}

package org.verumomnis.forensic.core

import android.app.Application
import android.util.Log

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
 */
class VerumOmnisApplication : Application() {

    companion object {
        private const val TAG = "VerumOmnisApp"
        const val VERSION = "1.0.0"
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
    }
}

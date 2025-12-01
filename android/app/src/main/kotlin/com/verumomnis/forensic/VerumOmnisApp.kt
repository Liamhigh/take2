package com.verumomnis.forensic

import android.app.Application

/**
 * Verum Omnis Forensic Application
 * 
 * Constitutional Governance Layer Implementation
 * - Stateless: No persistent user sessions
 * - Offline-first: All forensic operations work without network
 * - No telemetry: Zero data leaves the device
 * - Airgap ready: Full functionality in isolated environments
 */
class VerumOmnisApp : Application() {

    override fun onCreate() {
        super.onCreate()
        
        // Verify constitutional compliance on startup
        verifyConstitutionalCompliance()
    }

    private fun verifyConstitutionalCompliance() {
        // Ensure stateless operation
        require(BuildConfig.STATELESS) { "Constitutional violation: App must be stateless" }
        
        // Ensure offline-first
        require(BuildConfig.OFFLINE_FIRST) { "Constitutional violation: App must be offline-first" }
        
        // Ensure seal requirement
        require(BuildConfig.SEAL_REQUIRED) { "Constitutional violation: Seal is required" }
        
        // Verify hash standard
        require(BuildConfig.HASH_STANDARD == "SHA-512") { 
            "Constitutional violation: Hash standard must be SHA-512" 
        }
    }

    companion object {
        const val TAG = "VerumOmnis"
        
        // Constitutional version
        const val CONSTITUTION_VERSION = "1.0"
        
        // Engine identifier
        const val ENGINE_NAME = "Verum Omnis Constitutional Governance Layer"
    }
}

package org.verumomnis.utils

import android.app.Activity

/**
 * OCR Utilities (Stub implementation)
 * 
 * TODO: Implement OCR using ML Kit or Tesseract
 */
object OcrUtils {
    
    /**
     * Pick an image and extract text using OCR
     * 
     * @param activity The calling activity
     * @param callback Called with extracted text
     */
    fun pickImage(activity: Activity, callback: (String) -> Unit) {
        // TODO: implement OCR (ML Kit or Tesseract)
        // For now, return empty string as placeholder
        callback("")
    }
}

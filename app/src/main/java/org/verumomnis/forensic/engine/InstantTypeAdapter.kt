package org.verumomnis.forensic.engine

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import java.time.Instant

/**
 * Gson type adapter for Instant serialization
 */
class InstantTypeAdapter : TypeAdapter<Instant>() {
    
    override fun write(out: JsonWriter, value: Instant?) {
        if (value == null) {
            out.nullValue()
        } else {
            out.value(value.toString())
        }
    }
    
    override fun read(`in`: JsonReader): Instant? {
        return try {
            val str = `in`.nextString()
            Instant.parse(str)
        } catch (e: Exception) {
            null
        }
    }
}

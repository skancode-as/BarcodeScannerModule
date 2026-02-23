package dk.skancode.barcodescannermodule.util

import android.os.Bundle
import androidx.core.os.bundleOf
import dk.skancode.barcodescannermodule.gs1.Gs1Object

internal class BundleFactory {
    fun create(vararg pairs: Pair<String, Any?>): Bundle = bundleOf(*pairs)
    fun fromMap(data: Map<String, Any?>): Bundle = create(*data.toList().toTypedArray())
    fun fromGs1Object(data: Gs1Object): Bundle {
        val m: MutableMap<String, String> = HashMap()
        for ((key, v) in data.entries) {
            m.put(key.ai, v)
        }
        return this.fromMap(m)
    }
}
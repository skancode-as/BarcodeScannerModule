package dk.skancode.barcodescannermodule

import android.os.Bundle
import androidx.core.os.bundleOf

class BundleFactory {
    fun create(vararg pairs: Pair<String, Any?>): Bundle = bundleOf(*pairs)
}
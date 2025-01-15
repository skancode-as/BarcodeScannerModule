package dk.skancode.barcodescannermodule

import android.app.Activity
import android.content.Context
import dk.skancode.barcodescannermodule.newlandimpl.NewlandScannerModule
import dk.skancode.barcodescannermodule.unitechimpl.UnitechScannerModule

class ScannerModuleFactory {
    companion object {
        fun fromBrand(brand: String, context: Context, activity: Activity): IScannerModule {
            return when (brand.lowercase()) {
                "newland" -> NewlandScannerModule(context, activity)
                "unitech" -> UnitechScannerModule(context, activity)
                else -> DummyScannerModule()
            }
        }
    }
}
package dk.skancode.barcodescannermodule

import android.app.Activity
import android.content.Context
import dk.skancode.barcodescannermodule.impl.dummy.DummyScannerModule
import dk.skancode.barcodescannermodule.impl.newland.NewlandScannerModule
import dk.skancode.barcodescannermodule.impl.unitech.UnitechScannerModule
import dk.skancode.barcodescannermodule.impl.zebra.ZebraScannerModule

class ScannerModuleFactory {
    companion object {
        fun create(activity: Activity): IScannerModule {
            return create(activity, activity)
        }
        fun create(context: Context, activity: Activity): IScannerModule {
            val brand = android.os.Build.BRAND

            return fromBrand(brand, context, activity)
        }
        fun fromBrand(brand: String, context: Context, activity: Activity): IScannerModule {
            return when (brand.lowercase()) {
                "newland" -> NewlandScannerModule(context, activity)
                "unitech" -> UnitechScannerModule(context, activity)
                "zebra" -> ZebraScannerModule(context, activity)
                else -> DummyScannerModule()
            }
        }
    }
}
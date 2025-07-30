package dk.skancode.barcodescannermodule.zebraimpl

import android.content.Context
import android.content.Intent
import dk.skancode.barcodescannermodule.BarcodeBroadcastListener
import dk.skancode.barcodescannermodule.BaseBroadcastReceiver
import dk.skancode.barcodescannermodule.BundleFactory
import dk.skancode.barcodescannermodule.Logger

internal const val SOURCE_EXTRA: String = "com.symbol.datawedge.source"
internal const val LABEL_TYPE: String = "com.symbol.datawedge.label_type"
internal const val DATA_STRING: String = "com.symbol.datawedge.data_string"

internal class ZebraBarcodeDataReceiver(
    val bundleFactory: BundleFactory = BundleFactory(),
    val logger: Logger,
    val handler: BarcodeBroadcastListener,
): BaseBroadcastReceiver(handler) {
    override fun onReceive(context: Context?, intent: Intent?) {
        logger.debug("BarcodeDataReceiver.onReceive: Intent received: $intent")
        if (intent != null) {
            val extras = intent.extras

            val source = extras?.getString(SOURCE_EXTRA)
            if (source == "scanner") {
                val labelType = extras.getString(LABEL_TYPE)
                val data = extras.getString(DATA_STRING)
                logger.info("BarcodeDataReceiver: label type = $labelType")

                handler.onReceive(bundleFactory.create(
                    "barcode1" to data,
                    "barcode2" to null,
                    "barcodeType" to ZebraScannerModule.internalBarcodeTypeMap[labelType],
                    "ok" to (data != null),
                ))
            }
        }
    }
}
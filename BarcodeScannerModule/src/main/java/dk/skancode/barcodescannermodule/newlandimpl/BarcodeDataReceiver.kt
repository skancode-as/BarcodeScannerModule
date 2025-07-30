package dk.skancode.barcodescannermodule.newlandimpl

import android.content.Context
import android.content.Intent
import dk.skancode.barcodescannermodule.BarcodeBroadcastListener
import dk.skancode.barcodescannermodule.BaseBroadcastReceiver
import dk.skancode.barcodescannermodule.BundleFactory
import dk.skancode.barcodescannermodule.Logger

internal class BarcodeDataReceiver(
    private val bundleFactory: BundleFactory = BundleFactory(),
    private val logger: Logger,
    private val handler: BarcodeBroadcastListener,
) : BaseBroadcastReceiver(handler) {
    override fun onReceive(context: Context?, intent: Intent?) {
        logger.debug("BarcodeDataReceiver.onReceive: Intent received: $intent")
        val result1 = intent?.getStringExtra("SCAN_BARCODE1")
        val result2 = intent?.getStringExtra("SCAN_BARCODE2")
        val barcodeType = intent?.getIntExtra("SCAN_BARCODE_TYPE", -1)
        val okStr = intent?.getStringExtra("SCAN_STATE")

        handler.onReceive(
            bundleFactory.create(
                "barcode1" to result1,
                "barcode2" to result2,
                "barcodeType" to barcodeType,
                "ok" to (okStr == "ok")
            )
        )
    }
}

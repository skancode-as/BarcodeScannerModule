package dk.skancode.barcodescannermodule.newlandimpl

import android.content.Context
import android.content.Intent
import androidx.core.os.bundleOf
import dk.skancode.barcodescannermodule.BaseBroadcastReceiver
import dk.skancode.barcodescannermodule.EventHandler
import dk.skancode.barcodescannermodule.IEventHandler

class BarcodeDataReceiver(handler: IEventHandler) : BaseBroadcastReceiver(handler) {
    override fun onReceive(context: Context?, intent: Intent?) {
        val result1 = intent?.getStringExtra("SCAN_BARCODE1")
        val result2 = intent?.getStringExtra("SCAN_BARCODE2")
        val barcodeType = intent?.getIntExtra("SCAN_BARCODE_TYPE", -1)
        val okStr = intent?.getStringExtra("SCAN_STATE")

        handler.onDataReceived(
            EventHandler.BARCODE_RECEIVED, bundleOf(
            "barcode1" to result1,
            "barcode2" to result2,
            "barcodeType" to barcodeType,
            "ok" to (okStr == "ok")
        ))
    }
}

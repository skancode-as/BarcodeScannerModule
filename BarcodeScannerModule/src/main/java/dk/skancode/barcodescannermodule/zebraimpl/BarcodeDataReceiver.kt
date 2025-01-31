package dk.skancode.barcodescannermodule.zebraimpl

import android.content.Context
import android.content.Intent
import androidx.core.os.bundleOf
import dk.skancode.barcodescannermodule.BaseBroadcastReceiver
import dk.skancode.barcodescannermodule.EventHandler
import dk.skancode.barcodescannermodule.IEventHandler

internal const val SOURCE_EXTRA: String = "com.symbol.datawedge.source"
internal const val LABEL_TYPE: String = "com.symbol.datawedge.label_type"
internal const val DATA_STRING: String = "com.symbol.datawedge.data_string"

class ZebraBarcodeDataReceiver(handler: IEventHandler): BaseBroadcastReceiver(handler) {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent != null) {
            val extras = intent.extras

            val source = extras?.getString(SOURCE_EXTRA)
            if (source == "scanner") {
                val labelType = extras.getString(LABEL_TYPE)
                val data = extras.getString(DATA_STRING)

                handler.onDataReceived(EventHandler.BARCODE_RECEIVED, bundleOf(
                    "barcode1" to data,
                    "barcode2" to null,
                    "barcodeType" to labelType,
                    "ok" to (data != null),
                ))
            }
        }
    }
}
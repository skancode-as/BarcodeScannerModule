package dk.skancode.barcodescannermodule.zebraimpl

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dk.skancode.barcodescannermodule.IEventHandler

private const val SOURCE_EXTRA: String = "com.symbol.datawedge.source"
private const val LABEL_TYPE: String = "com.symbol.datawedge.label_type"
private const val DATA_STRING: String = "com.symbol.datawedge.data_string"

class BarcodeDataReceiver(handler: IEventHandler): BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent != null) {
            val extras = intent.extras

            val source = extras?.getString(SOURCE_EXTRA)
            if (source == "scanner") {
                val labelType = extras.getString(LABEL_TYPE)
                val data = extras.getString(DATA_STRING)

                println("labelType: $labelType, data: $data")
            }
        }
    }
}
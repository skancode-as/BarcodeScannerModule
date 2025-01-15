package dk.skancode.barcodescannermodule.newlandimpl

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Build
import androidx.core.os.bundleOf
import dk.skancode.barcodescannermodule.EventHandler
import dk.skancode.barcodescannermodule.IEventHandler

class BarcodeDataReceiver(private val handler: IEventHandler) : BroadcastReceiver() {
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

class NFCDataReceiver(private val handler: IEventHandler) : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        println(intent)

        val tag: Tag? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent?.getParcelableExtra(NfcAdapter.EXTRA_TAG, Tag::class.java)
        } else {
            intent?.getParcelableExtra(NfcAdapter.EXTRA_TAG)
        }

        println(tag)

        handler.onDataReceived("onNFCDataReceived", bundleOf(
            "tag" to tag
        ))
    }
}

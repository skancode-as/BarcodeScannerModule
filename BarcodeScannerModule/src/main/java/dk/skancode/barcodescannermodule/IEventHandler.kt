package dk.skancode.barcodescannermodule

import android.os.Bundle

fun interface IEventHandler {
    fun onDataReceived(eventName: String, payload: Bundle)
}

class EventHandler {
    companion object {
        val BARCODE_RECEIVED = "onBarcodeDataReceived"
        val NFC_RECEIVED = "onNFCDataReceived"
    }
}
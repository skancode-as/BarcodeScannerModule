package dk.skancode.barcodescannermodule.event

import android.os.Bundle

@Deprecated("Deprecated in favor of the TypedEventHandler")
fun interface IEventHandler {
    fun onDataReceived(eventName: String, payload: Bundle)
}

@Deprecated("Deprecated along with IEventHandler, to encourage usage of the TypedEventHandler")
class EventHandler {
    companion object {
        val BARCODE_RECEIVED = "onBarcodeDataReceived"
        val NFC_RECEIVED = "onNFCDataReceived"
    }
}
package dk.skancode.barcodescannermodule.zebraimpl

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.core.content.ContextCompat
import dk.skancode.barcodescannermodule.BaseScannerModule
import dk.skancode.barcodescannermodule.Enabler
import dk.skancode.barcodescannermodule.IEventHandler
import dk.skancode.barcodescannermodule.ScanMode
import dk.skancode.barcodescannermodule.Symbology

private const val ACTION: String = "com.symbol.datawedge.api.ACTION"
private const val CATEGORY: String = "category.DEFAULT"

class ZebraScannerModule(context: Context, activity: Activity): BaseScannerModule(context, activity) {

    override fun registerReceiver(receiver: BroadcastReceiver) {
        val filter = IntentFilter(ACTION).apply {
            addCategory(CATEGORY)
        }
        val flag = ContextCompat.RECEIVER_EXPORTED

        ContextCompat.registerReceiver(context, receiver, filter, flag)
    }

    override fun setScannerState(enabler: Enabler) {
        val intent = Intent(ACTION).apply {
            putExtra("com.symbol.datawedge.api.ENABLE_DATAWEDGE", enabler == Enabler.ON)
        }

        context.sendBroadcast(intent)
    }

    override fun registerBarcodeReceiver(eventHandler: IEventHandler) {
        val dataReceiver = BarcodeDataReceiver(eventHandler)
        dataReceivers[eventHandler] = dataReceiver

        registerReceiver(dataReceiver)
    }

    override fun setAutoEnter(value: Enabler) {
    }

    override fun setNotificationSound(value: Enabler) {
    }

    override fun setNotificationVibration(value: Enabler) {
    }

    override fun setScanMode(value: ScanMode) {
    }

    override fun setNfcStatus(status: Enabler) {
    }

    override fun canSetSymbology(): Boolean {
        return false
    }

    override fun setSymbology(symbology: Symbology) {
        TODO("Not yet implemented")
    }

}
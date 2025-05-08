package dk.skancode.barcodescannermodule.unitechimpl

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.core.content.ContextCompat
import dk.skancode.barcodescannermodule.BaseBroadcastReceiver
import dk.skancode.barcodescannermodule.BaseScannerModule
import dk.skancode.barcodescannermodule.BundleFactory
import dk.skancode.barcodescannermodule.Enabler
import dk.skancode.barcodescannermodule.EventHandler
import dk.skancode.barcodescannermodule.ScanMode
import dk.skancode.barcodescannermodule.Symbology

internal class UnitechScannerModule(context: Context, activity: Activity, val bundleFactory: BundleFactory = BundleFactory()): BaseScannerModule(context, activity) {
    override fun setScannerState(enabler: Enabler) {
        getPreferences().edit().putString("scannerState", enabler.value).apply()

        val intent = if (enabler == Enabler.ON)
            Intent("unitech.scanservice.start")
        else
            Intent("unitech.scanservice.close").putExtra("close", true)

        context.sendBroadcast(intent)
    }

    override fun init() {
        super.init()

        receiver = UnitechBarcodeDataReceiver(bundleFactory) { payload ->
            barcodeEventHandlers.forEach { handler -> handler.onDataReceived(EventHandler.BARCODE_RECEIVED , payload) }
        }
        startBarcode(receiver)
    }

    override fun startBarcode(receiver: BaseBroadcastReceiver) {
        val filter = IntentFilter(DATA_TYPE_INTENT).apply {
            addAction(DATA_INTENT)
        }
        val flag = ContextCompat.RECEIVER_EXPORTED

        ContextCompat.registerReceiver(context, receiver, filter, flag)
    }

    override fun setAutoEnter(value: Enabler) {
        val terminator = if (value == Enabler.ON) "<CR><LF>" else ""
        val intent = Intent("unitech.scanservice.terminator").putExtra("terminator", terminator)
        context.sendBroadcast(intent)
    }

    override fun setNotificationSound(value: Enabler) {
        context.sendBroadcast(
            Intent("unitech.scanservice.sound").apply {
                putExtra("sound", value == Enabler.ON)
            }
        )

        context.sendBroadcast(
            Intent("unitech.scanservice.frequency").apply {
                putExtra("frequency", "1")
            }
        )
    }

    override fun setNotificationVibration(value: Enabler) {
        val intent = Intent("unitech.scanservice.vibration")
        intent.putExtra("vibration", value == Enabler.ON)
        context.sendBroadcast(intent)
    }

    override fun setScanMode(value: ScanMode) {
        when (value.value) {
            "api" -> {
                val intent = Intent("unitech.scanservice.scan2key_setting")
                intent.putExtra("scan2key", false)
                context.sendBroadcast(intent)
            }
            "direct" -> {
                var intent = Intent("unitech.scanservice.scan2key_setting")
                intent.putExtra("scan2key", true)
                context.sendBroadcast(intent)

                intent = Intent("unitech.scanservice.scan2key_outputmethod")
                intent.putExtra("outputmethod", 1)
                context.sendBroadcast(intent)
            }
            "simulate" -> {
                var intent = Intent("unitech.scanservice.scan2key_setting")
                intent.putExtra("scan2key", true)
                context.sendBroadcast(intent)

                intent = Intent("unitech.scanservice.scan2key_outputmethod")
                intent.putExtra("outputmethod", 0)
                context.sendBroadcast(intent)
            }
        }
    }

    override fun canSetNfcStatus(): Boolean {
        return true
    }

    override fun setNfcStatus(status: Enabler) {
        context.sendBroadcast(Intent("unitech.scanservice.nfcenable").apply {
            putExtra("nfcenable", status == Enabler.ON)
        })
    }

    override fun canSetSymbology(): Boolean {
        return false
    }

    override fun setSymbology(symbology: Symbology) {
        throw RuntimeException("Symbologies not supported. Cannot set symbology on UNITECH systems.")
    }
}
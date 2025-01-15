package dk.skancode.barcodescannermodule.unitechimpl

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import androidx.core.content.ContextCompat
import dk.skancode.barcodescannermodule.Enabler
import dk.skancode.barcodescannermodule.IEventHandler
import dk.skancode.barcodescannermodule.IScannerModule
import dk.skancode.barcodescannermodule.ScanMode
import dk.skancode.barcodescannermodule.Symbology
import dk.skancode.barcodescannermodule.unitechimpl.BarcodeDataReceiver

class UnitechScannerModule(private val context: Context, private val activity: Activity):
    IScannerModule {
    private var dataReceiver: BarcodeDataReceiver? = null
    private fun getPreferences(): SharedPreferences {
        return context.getSharedPreferences(context.packageName + ".barcode", Context.MODE_PRIVATE)
    }

    override fun getScannerState(): String {
        return getPreferences().getString("scannerState", "off") ?: "off"
    }

    override fun setScannerState(enabler: Enabler) {
        getPreferences().edit().putString("scannerState", enabler.value).apply()

        val intent = if (enabler == Enabler.ON)
            Intent("unitech.scanservice.start")
        else
            Intent("unitech.scanservice.close").putExtra("close", true)

        context.sendBroadcast(intent)
    }

    override fun registerBarcodeReceiver(eventHandler: IEventHandler) {
        dataReceiver = BarcodeDataReceiver(eventHandler)
        println("UNITECH: registerReceiver")
        val filter = IntentFilter("unitech.scanservice.datatype")
        filter.addAction("unitech.scanservice.data")
        val flag = ContextCompat.RECEIVER_EXPORTED

        ContextCompat.registerReceiver(context, dataReceiver, filter, flag)
    }

    override fun unregisterBarcodeReceiver(eventHandler: IEventHandler) {
        println("UNITECH: unregisterReceiver")
        context.unregisterReceiver(dataReceiver)
    }

    override fun pauseReceivers() {
        throw RuntimeException("Not yet implemented")
    }

    override fun resumeReceivers() {
        throw RuntimeException("Not yet implemented")
    }

    override fun setAutoEnter(value: Enabler) {
        val terminator = if (value == Enabler.ON) "<CR><LF>" else ""
        val intent = Intent("unitech.scanservice.terminator").putExtra("terminator", terminator)
        context.sendBroadcast(intent)
    }

    override fun setNotificationSound(value: Enabler) {
        val intent = Intent("unitech.scanservice.sound")
        intent.putExtra("sound", value == Enabler.ON)
        context.sendBroadcast(intent)
    }

    override fun setNotificationVibration(value: Enabler) {
        println("UNITECH: setNotificationVibration to '${value.value}'")
        val intent = Intent("unitech.scanservice.vibration")
        intent.putExtra("vibration", value == Enabler.ON)
        context.sendBroadcast(intent)
    }

    override fun setScanMode(value: ScanMode) {
        println("UNITECH: setScanMode to '${value.value}'")
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

    override fun nfcAvailable(): Boolean {
        return false
    }

    override fun setNfcStatus(status: Enabler) {
        throw RuntimeException("NFC not available on UNITECH systems.")
    }

    override fun registerNFCReceiver(eventHandler: IEventHandler) {
        throw RuntimeException("Not yet implemented")
    }

    override fun canSetSymbology(): Boolean {
        return false
    }

    override fun setSymbology(symbology: Symbology) {
        throw RuntimeException("Symbologies not supported. Cannot set symbology on UNITECH systems.")
    }

}
package dk.skancode.barcodescannermodule.unitechimpl

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.nfc.NfcAdapter
import android.nfc.NfcManager
import android.os.Build
import android.os.Build.VERSION_CODES
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import dk.skancode.barcodescannermodule.Enabler
import dk.skancode.barcodescannermodule.EventHandler
import dk.skancode.barcodescannermodule.IEventHandler
import dk.skancode.barcodescannermodule.IScannerModule
import dk.skancode.barcodescannermodule.ScanMode
import dk.skancode.barcodescannermodule.Symbology

class UnitechScannerModule(private val context: Context, private val activity: Activity):
    IScannerModule {
    private val dataReceivers: MutableMap<IEventHandler, BarcodeDataReceiver> = HashMap()

    private val nfcManager: NfcManager? = if (Build.VERSION.SDK_INT >= VERSION_CODES.M) {
        context.getSystemService(NfcManager::class.java)
    } else {
        null
    }

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
        val dataReceiver = BarcodeDataReceiver(eventHandler)
        dataReceivers[eventHandler] = dataReceiver
        val filter = IntentFilter(DATA_TYPE_INTENT).apply {
            addAction(DATA_INTENT)
        }
        val flag = ContextCompat.RECEIVER_EXPORTED

        ContextCompat.registerReceiver(context, dataReceiver, filter, flag)
    }

    override fun unregisterBarcodeReceiver(eventHandler: IEventHandler) {
        val dataReceiver = dataReceivers.remove(eventHandler)
        if (dataReceiver != null) {
            context.unregisterReceiver(dataReceiver)
        }
    }

    override fun pauseReceivers() {
        dataReceivers.forEach { (_, receiver) ->
            context.unregisterReceiver(receiver)
        }
    }

    override fun resumeReceivers() {
        val filter = IntentFilter(DATA_TYPE_INTENT).apply {
            addAction(DATA_INTENT)
        }
        val flag = ContextCompat.RECEIVER_EXPORTED

        dataReceivers.forEach { (_, dataReceiver) ->
            ContextCompat.registerReceiver(context, dataReceiver, filter, flag)
        }
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

    override fun nfcAvailable(): Boolean {
        return if (Build.VERSION.SDK_INT >= VERSION_CODES.M) {
            nfcManager!!.defaultAdapter != null
        } else {
            false
        }
    }

    override fun setNfcStatus(status: Enabler) {
        context.sendBroadcast(Intent("unitech.scanservice.nfcenable").apply {
            putExtra("nfcenable", status == Enabler.ON)
        })
    }

    override fun registerNFCReceiver(eventHandler: IEventHandler) {
        val nfcAdapter = nfcManager!!.defaultAdapter

        nfcAdapter.enableReaderMode(
            activity,
            {tag ->
                eventHandler.onDataReceived(
                    EventHandler.NFC_RECEIVED, bundleOf(
                        "tag" to tag
                    ))
            },
            NfcAdapter.FLAG_READER_NFC_A
                .or(NfcAdapter.FLAG_READER_NFC_B)
                .or(NfcAdapter.FLAG_READER_NFC_F)
                .or(NfcAdapter.FLAG_READER_NFC_V)
                .or(NfcAdapter.FLAG_READER_NFC_BARCODE),
            bundleOf(
                NfcAdapter.EXTRA_READER_PRESENCE_CHECK_DELAY to 250
            ),
        )
    }

    override fun canSetSymbology(): Boolean {
        return false
    }

    override fun setSymbology(symbology: Symbology) {
        throw RuntimeException("Symbologies not supported. Cannot set symbology on UNITECH systems.")
    }

}
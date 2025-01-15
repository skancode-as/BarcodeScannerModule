package dk.skancode.barcodescannermodule.newlandimpl

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
import dk.skancode.barcodescannermodule.NewlandSymbology
import dk.skancode.barcodescannermodule.ScanMode
import dk.skancode.barcodescannermodule.ScannerConfigKey
import dk.skancode.barcodescannermodule.Symbology

class NewlandScannerModule(private val context: Context, private val activity: Activity) :
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
        configureScanner(ScannerConfigKey.SCAN_POWER, enabler.ordinal)
    }

    override fun registerBarcodeReceiver(eventHandler: IEventHandler) {
        val dataReceiver = BarcodeDataReceiver(eventHandler)
        dataReceivers[eventHandler] = dataReceiver
        val filter = IntentFilter("nlscan.action.SCANNER_RESULT")
        val flag = ContextCompat.RECEIVER_EXPORTED

        ContextCompat.registerReceiver(context, dataReceiver, filter, flag)
    }

    override fun unregisterBarcodeReceiver(eventHandler: IEventHandler) {
        val receiver = dataReceivers.remove(eventHandler)

        if (receiver != null) {
            context.unregisterReceiver(receiver)
            nfcManager?.defaultAdapter?.disableReaderMode(activity)
        }
    }

    override fun pauseReceivers() {
        dataReceivers.forEach { (_, receiver) ->
            context.unregisterReceiver(receiver)
        }
    }

    override fun resumeReceivers() {
        val filter = IntentFilter("nlscan.action.SCANNER_RESULT")
        val flag = ContextCompat.RECEIVER_EXPORTED

        dataReceivers.forEach { (_, dataReceiver) ->
            ContextCompat.registerReceiver(context, dataReceiver, filter, flag)
        }
    }

    override fun setAutoEnter(value: Enabler) {
        configureScanner(ScannerConfigKey.AUTO_ENTER, value.ordinal)
    }

    override fun setNotificationSound(value: Enabler) {
        configureScanner(ScannerConfigKey.NOTIFICATION_SOUND, value.ordinal)
    }

    override fun setNotificationVibration(value: Enabler) {
        configureScanner(ScannerConfigKey.NOTIFICATION_VIBRATION, value.ordinal)
    }

    override fun setScanMode(value: ScanMode) {
        configureScanner(ScannerConfigKey.SCAN_MODE, value.ordinal)
    }

    override fun nfcAvailable(): Boolean {
        return if (Build.VERSION.SDK_INT >= VERSION_CODES.M) {
            nfcManager!!.defaultAdapter != null
        } else {
            false
        }
    }

    override fun setNfcStatus(status: Enabler) {
        val json = "{\n" +
                "\t\"quick_setting\": [{\n" +
                "\t\t\"quick_setting\": [{\n" +
                "\t\t\t\"NFC.Enable\": \"${status.ordinal}\"\n" +
                "\t\t}],\n" +
                "\t\t\"set_data_diff_flag\": \"1\"\n" +
                "\t}],\n" +
                "\t\"version\": \"V0.00.001\"\n" +
                "}"

        val intent = Intent("com.nlscan.action.backuprecovery")
        intent.setPackage("com.nlscan.nlsbackuprecovery")
        intent.putExtra("set", json)

        context.sendBroadcast(intent)
    }

    override fun registerNFCReceiver(eventHandler: IEventHandler) {
        val nfcAdapter = nfcManager!!.defaultAdapter

        nfcAdapter.enableReaderMode(
            activity,
            {tag ->
                println(tag)
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
        return true
    }

    override fun setSymbology(symbology: Symbology) {
        if (symbology is NewlandSymbology) {
            val newlandSymbology = symbology as NewlandSymbology
            val intent = Intent("ACTION_BARCODE_CFG")
            intent.putExtra("CODE_ID", newlandSymbology.getCodeID())
            intent.putExtra("PROPERTY", newlandSymbology.getProperty())
            intent.putExtra("VALUE", newlandSymbology.getValue())

            context.sendBroadcast(intent)
        } else {
            println("Attempted to set unsupported newland symbology of type ${symbology::class}")
        }
    }

    private fun configureScanner(key: ScannerConfigKey, value: Int) {
        val intent = Intent("ACTION_BAR_SCANCFG")

        intent.putExtra(key.value, value)
        context.sendBroadcast(intent)
    }
}

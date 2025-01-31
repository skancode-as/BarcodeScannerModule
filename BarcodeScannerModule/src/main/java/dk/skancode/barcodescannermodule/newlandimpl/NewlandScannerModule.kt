package dk.skancode.barcodescannermodule.newlandimpl

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.core.content.ContextCompat
import dk.skancode.barcodescannermodule.BaseBroadcastReceiver
import dk.skancode.barcodescannermodule.BaseScannerModule
import dk.skancode.barcodescannermodule.Enabler
import dk.skancode.barcodescannermodule.IEventHandler
import dk.skancode.barcodescannermodule.NewlandSymbology
import dk.skancode.barcodescannermodule.ScanMode
import dk.skancode.barcodescannermodule.ScannerConfigKey
import dk.skancode.barcodescannermodule.Symbology

class NewlandScannerModule(context: Context, activity: Activity) :
    BaseScannerModule(context, activity) {

    override fun setScannerState(enabler: Enabler) {
        getPreferences().edit().putString("scannerState", enabler.value).apply()
        configureScanner(ScannerConfigKey.SCAN_POWER, enabler.ordinal)
    }

    override fun registerBarcodeReceiver(eventHandler: IEventHandler) {
        val dataReceiver = BarcodeDataReceiver(eventHandler)
        dataReceivers.add(dataReceiver)

        registerReceiver(dataReceiver)
    }

    override fun registerReceiver(receiver: BaseBroadcastReceiver) {
        val filter = IntentFilter("nlscan.action.SCANNER_RESULT")
        val flag = ContextCompat.RECEIVER_EXPORTED

        ContextCompat.registerReceiver(context, receiver, filter, flag)
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

    override fun canSetNfcStatus(): Boolean {
        return true
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

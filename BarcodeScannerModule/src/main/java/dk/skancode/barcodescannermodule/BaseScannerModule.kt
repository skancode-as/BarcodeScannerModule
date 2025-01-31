package dk.skancode.barcodescannermodule

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.nfc.NfcAdapter
import android.nfc.NfcManager
import android.os.Build
import android.os.Build.VERSION_CODES
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf

abstract class BaseScannerModule(protected val context: Context, protected val activity: Activity): IScannerModule {
    protected val dataReceivers: MutableSet<BaseBroadcastReceiver> = HashSet()

    protected val nfcManager: NfcManager? =
        if (
            Build.VERSION.SDK_INT >= VERSION_CODES.M &&
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.NFC
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            context.getSystemService(NfcManager::class.java)
        } else {
            null
        }

    protected fun getPreferences(): SharedPreferences {
        return context.getSharedPreferences(context.packageName + ".barcode", Context.MODE_PRIVATE)
    }

    override fun getScannerState(): String {
        return getPreferences().getString("scannerState", "off") ?: "off"
    }

    override fun nfcAvailable(): Boolean {
        return if (Build.VERSION.SDK_INT >= VERSION_CODES.M && nfcManager != null) {
            nfcManager.defaultAdapter != null
        } else {
            false
        }
    }

    override fun getNfcStatus(): Enabler {
        val adapter = nfcManager?.defaultAdapter

        return if (adapter != null && adapter.isEnabled) Enabler.ON else Enabler.OFF
    }

    protected abstract fun registerReceiver(receiver: BaseBroadcastReceiver)

    override fun unregisterBarcodeReceiver(eventHandler: IEventHandler) {
        val receiver: BaseBroadcastReceiver? = dataReceivers.find { r -> r.handler == eventHandler }

        if (receiver != null) {
            dataReceivers.remove(receiver)
            context.unregisterReceiver(receiver)
            nfcManager?.defaultAdapter?.disableReaderMode(activity)
        }
    }

    override fun resumeReceivers() {
        if (dataReceivers.isNotEmpty()) {
            dataReceivers.forEach { receiver ->
                registerReceiver(receiver)
            }
            registerNFCReceiver(dataReceivers.first().handler)
        }
    }

    override fun pauseReceivers() {
        nfcManager?.defaultAdapter?.disableReaderMode(activity)
        dataReceivers.forEach { receiver ->
            context.unregisterReceiver(receiver)
        }
    }

    override fun registerNFCReceiver(eventHandler: IEventHandler) {
        val nfcAdapter = nfcManager!!.defaultAdapter

        nfcAdapter.enableReaderMode(
            activity,
            { tag ->
                eventHandler.onDataReceived(
                    EventHandler.NFC_RECEIVED, bundleOf(
                        "tag" to tag
                    )
                )
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
}
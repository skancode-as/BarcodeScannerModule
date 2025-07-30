package dk.skancode.barcodescannermodule

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.nfc.NfcAdapter
import android.nfc.NfcManager
import android.nfc.Tag
import android.os.Build
import android.os.Build.VERSION_CODES
import android.os.Bundle
import androidx.annotation.VisibleForTesting
import androidx.annotation.VisibleForTesting.Companion.PROTECTED
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import dk.skancode.barcodescannermodule.event.BarcodeType
import dk.skancode.barcodescannermodule.event.EventHandler
import dk.skancode.barcodescannermodule.event.IEventHandler
import dk.skancode.barcodescannermodule.event.TypedEvent
import dk.skancode.barcodescannermodule.event.TypedEventHandler
import dk.skancode.barcodescannermodule.gs1.Gs1Config
import java.util.concurrent.atomic.AtomicBoolean

internal abstract class BaseScannerModule(
    protected val context: Context,
    protected val activity: Activity,
) : IScannerModule, NfcAdapter.ReaderCallback, BarcodeBroadcastListener {
    @Deprecated("Use Use typedEventHandlers instead")
    protected val barcodeEventHandlers = mutableSetOf<IEventHandler>()
    @Deprecated("Use typedEventHandlers instead")
    protected val nfcEventHandlers = mutableSetOf<IEventHandler>()
    protected val typedEventHandlers = mutableSetOf<TypedEventHandler>()
    private var isPaused = AtomicBoolean(false)
    protected abstract val barcodeTypeMap: Map<Int, BarcodeType>
    @VisibleForTesting(otherwise = PROTECTED)
    lateinit var receiver: BaseBroadcastReceiver
    protected var gs1Config_ = Gs1Config(enabled = Enabler.OFF)

    private val nfcManager: NfcManager? =
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

    override fun init() {
        startNFC()
    }

    override fun onReceive(payload: Bundle) {
        if (barcodeEventHandlers.isNotEmpty()) {
            barcodeEventHandlers.forEach { handler ->
                handler.onDataReceived(
                    EventHandler.BARCODE_RECEIVED,
                    payload
                )
            }
        }

        if (typedEventHandlers.isNotEmpty()) {
            typedEventHandlers.forEach { handler ->
                val typeInt = payload.getInt("barcodeType", -1)
                val barcodeType =
                    if (typeInt == -1) BarcodeType.UNKNOWN
                    else barcodeTypeMap[typeInt] ?: BarcodeType.UNKNOWN

                handler.onEvent(
                    TypedEvent.BarcodeEvent(
                        barcode1 = payload.getString("barcode1"),
                        barcode2 = payload.getString("barcode2"),
                        barcodeType = barcodeType,
                        ok = payload.getBoolean("ok")
                    )
                )
            }
        }
    }

    override fun onTagDiscovered(tag: Tag?) {
        if (nfcEventHandlers.isNotEmpty()) {
            nfcEventHandlers.forEach { eventHandler ->
                eventHandler.onDataReceived(
                    EventHandler.NFC_RECEIVED, bundleOf(
                        "tag" to tag
                    )
                )
            }
        }

        if (typedEventHandlers.isNotEmpty()) {
            typedEventHandlers.forEach { handler ->
                handler.onEvent(TypedEvent.NfcEvent(tag = tag))
            }
        }
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

    protected abstract fun startBarcode(receiver: BaseBroadcastReceiver)

    @Deprecated("Use registerTypedEventHandler and unregisterTypedEventHandler instead")
    override fun registerBarcodeReceiver(eventHandler: IEventHandler) {
        barcodeEventHandlers.add(eventHandler)
    }

    @Deprecated("Use registerTypedEventHandler and unregisterTypedEventHandler instead")
    override fun unregisterBarcodeReceiver(eventHandler: IEventHandler) {
        barcodeEventHandlers.remove(eventHandler)
        nfcEventHandlers.remove(eventHandler)
    }

    override fun registerTypedEventHandler(handler: TypedEventHandler) {
        typedEventHandlers.add(handler)
    }

    override fun unregisterTypedEventHandler(handler: TypedEventHandler) {
        typedEventHandlers.remove(handler)
    }

    override fun resumeReceivers() {
        if (isPaused.compareAndSet(true, false)) {
            startBarcode(receiver)
            startNFC()
        }
    }

    override fun pauseReceivers() {
        if (isPaused.compareAndSet(false, true)) {
            nfcManager?.defaultAdapter?.disableReaderMode(activity)
            context.unregisterReceiver(receiver)
        }
    }

    override fun setGs1Config(config: Gs1Config) {
        this.gs1Config_ = config
    }

    //protected abstract fun handleGs1Barcode(payload: Bundle): Bundle

    private fun startNFC() {
        if (nfcManager != null) {
            val nfcAdapter = nfcManager.defaultAdapter

            nfcAdapter.enableReaderMode(
                activity,
                this,
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

    override fun registerNFCReceiver(eventHandler: IEventHandler) {
        nfcEventHandlers.add(eventHandler)
    }
}
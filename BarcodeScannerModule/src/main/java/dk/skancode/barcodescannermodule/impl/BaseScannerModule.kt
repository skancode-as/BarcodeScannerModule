package dk.skancode.barcodescannermodule.impl

import android.Manifest
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.nfc.NfcAdapter
import android.nfc.NfcManager
import android.nfc.Tag
import android.os.Build
import android.os.Bundle
import androidx.annotation.VisibleForTesting
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import dk.skancode.barcodescannermodule.Enabler
import dk.skancode.barcodescannermodule.IScannerModule
import dk.skancode.barcodescannermodule.event.BarcodeType
import dk.skancode.barcodescannermodule.event.EventHandler
import dk.skancode.barcodescannermodule.event.IEventHandler
import dk.skancode.barcodescannermodule.event.TypedEvent
import dk.skancode.barcodescannermodule.event.TypedEventHandler
import dk.skancode.barcodescannermodule.gs1.Gs1Config
import dk.skancode.barcodescannermodule.gs1.Gs1Parser
import dk.skancode.barcodescannermodule.gs1.emptyGs1Object
import dk.skancode.barcodescannermodule.util.BundleFactory
import java.util.concurrent.atomic.AtomicBoolean

internal abstract class BaseScannerModule(
    protected val context: Context,
    protected val activity: Activity,
    private val bundleFactory: BundleFactory,
    private val gs1Parser: Gs1Parser,
) : IScannerModule, NfcAdapter.ReaderCallback, BarcodeBroadcastListener {
    @Deprecated("Use Use typedEventHandlers instead")
    protected val barcodeEventHandlers = mutableSetOf<IEventHandler>()
    @Deprecated("Use typedEventHandlers instead")
    protected val nfcEventHandlers = mutableSetOf<IEventHandler>()
    protected val typedEventHandlers = mutableSetOf<TypedEventHandler>()
    private var isPaused = AtomicBoolean(false)
    protected abstract val barcodeTypeMap: Map<Int, BarcodeType>
    @VisibleForTesting(otherwise = VisibleForTesting.Companion.PROTECTED)
    lateinit var receiver: BaseBroadcastReceiver
    private var _gs1Config = Gs1Config(enabled = Enabler.OFF)

    private val nfcManager: NfcManager? =
        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
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
            if (_gs1Config.enabled == Enabler.ON && payload.getBoolean("ok", false)) {
                val barcode = payload.getString("barcode1")
                if (barcode != null) {
                    val (res, isGs1) = gs1Parser.parse(barcode)
                    if (!isGs1) {
                        val b = bundleFactory.fromGs1Object(res)
                        payload.putBundle("gs1", b)
                    }
                }
            }

            barcodeEventHandlers.forEach { handler ->
                handler.onDataReceived(
                    EventHandler.Companion.BARCODE_RECEIVED,
                    payload
                )
            }
        }

        if (typedEventHandlers.isNotEmpty()) {
            typedEventHandlers.forEach { handler ->
                val scanOk = payload.getBoolean("ok", false)
                val barcode1 = payload.getString("barcode1")
                val typeInt = payload.getInt("barcodeType", -1)
                val barcodeType =
                    if (typeInt == -1) BarcodeType.Companion.UNKNOWN
                    else barcodeTypeMap[typeInt] ?: BarcodeType.Companion.UNKNOWN

                if (_gs1Config.enabled == Enabler.ON) {
                    if (!scanOk || barcode1 == null) {
                        handler.onEvent(
                            TypedEvent.Gs1Event(
                                ok = scanOk,
                                isGs1 = false,
                                gs1 = emptyGs1Object(),
                                barcode = barcode1,
                                barcodeType = barcodeType,
                            )
                        )
                    } else {
                        val (res, isGs1) = gs1Parser.parse(barcode1)
                        handler.onEvent(
                            TypedEvent.Gs1Event(
                                ok = true,
                                isGs1 = isGs1,
                                gs1 = res,
                                barcode = barcode1,
                                barcodeType = barcodeType,
                            )
                        )
                    }
                } else {
                    handler.onEvent(
                        TypedEvent.BarcodeEvent(
                            barcode1 = barcode1,
                            barcode2 = payload.getString("barcode2"),
                            barcodeType = barcodeType,
                            ok = scanOk,
                        )
                    )
                }
            }
        }
    }

    override fun onTagDiscovered(tag: Tag?) {
        if (nfcEventHandlers.isNotEmpty()) {
            nfcEventHandlers.forEach { eventHandler ->
                eventHandler.onDataReceived(
                    EventHandler.Companion.NFC_RECEIVED, bundleOf(
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
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && nfcManager != null) {
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
        this._gs1Config = config
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

internal fun interface BarcodeBroadcastListener {
    fun onReceive(payload: Bundle)
}

internal abstract class BaseBroadcastReceiver(listener: BarcodeBroadcastListener): BroadcastReceiver()
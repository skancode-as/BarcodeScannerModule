package dk.skancode.barcodescannermodule.impl.unitech

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.core.content.ContextCompat
import dk.skancode.barcodescannermodule.impl.BaseBroadcastReceiver
import dk.skancode.barcodescannermodule.impl.BaseScannerModule
import dk.skancode.barcodescannermodule.util.BundleFactory
import dk.skancode.barcodescannermodule.Enabler
import dk.skancode.barcodescannermodule.util.Logger
import dk.skancode.barcodescannermodule.ScanMode
import dk.skancode.barcodescannermodule.Symbology
import dk.skancode.barcodescannermodule.event.BarcodeType
import dk.skancode.barcodescannermodule.gs1.Gs1Parser
import dk.skancode.barcodescannermodule.gs1.Gs1ParserImpl

internal class UnitechScannerModule(
    context: Context,
    activity: Activity,
    val bundleFactory: BundleFactory = BundleFactory(),
    val logger: Logger = Logger("UnitechScannerModule"),
    gs1Parser: Gs1Parser = Gs1ParserImpl()
): BaseScannerModule(context, activity, bundleFactory, gs1Parser) {
    override val barcodeTypeMap: Map<Int, BarcodeType>
        get() = unitechBarcodeTypeMap

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

        receiver = UnitechBarcodeDataReceiver(bundleFactory, logger, this)
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

internal val unitechBarcodeTypeMap: Map<Int, BarcodeType> = mapOf(
    0x01 to BarcodeType.CODE39,
    0x62 to BarcodeType.CODE39,
    0x08 to BarcodeType.UPCA,
    0x63 to BarcodeType.UPCA,
    0x09 to BarcodeType.UPCE,
    0x45 to BarcodeType.UPCE,
    0x0B to BarcodeType.EAN13,
    0x16 to BarcodeType.EAN13,
    0x64 to BarcodeType.EAN13,
    0x0A to BarcodeType.EAN8,
    0x44 to BarcodeType.EAN8,
    0x06 to BarcodeType.INTERLEAVED2OF5,
    0x65 to BarcodeType.INTERLEAVED2OF5,
    0x02 to BarcodeType.CODABAR,
    0x61 to BarcodeType.CODABAR,
    0x03 to BarcodeType.CODE128,
    0x6A to BarcodeType.CODE128,
    0x07 to BarcodeType.CODE93,
    0x69 to BarcodeType.CODE93,
    0x0C to BarcodeType.CODE11,
    0x68 to BarcodeType.CODE11,
    0x0E to BarcodeType.MSI,
    0x67 to BarcodeType.MSI,
    0x10 to BarcodeType.UPC_E1,
    0x45 to BarcodeType.UPC_E1,
    0x15 to BarcodeType.TRIOPTIC_CODE39,
    0x3D to BarcodeType.TRIOPTIC_CODE39,
    0x0F to BarcodeType.UCCEAN128,
    0x49 to BarcodeType.UCCEAN128,
    0x11 to BarcodeType.PDF417,
    0x72 to BarcodeType.PDF417,
    0x04 to BarcodeType.DISCRETE2OF5,
    0x1E to BarcodeType.USPOSTNET,
    0x50 to BarcodeType.USPOSTNET,
    0x1F to BarcodeType.USPLANET,
    0x4C to BarcodeType.USPLANET,
    0x1A to BarcodeType.MICROPDF,
    0x52 to BarcodeType.MICROPDF,
    0x22 to BarcodeType.JAPANESEPOSTAL,
    0x4A to BarcodeType.JAPANESEPOSTAL,
    0x23 to BarcodeType.AUSTRALIANPOSTAL,
    0x41 to BarcodeType.AUSTRALIANPOSTAL,
    0x1B to BarcodeType.DATAMATRIX,
    0x77 to BarcodeType.DATAMATRIX,
    0x1C to BarcodeType.QRCODE,
    0x73 to BarcodeType.QRCODE,
    0x30 to BarcodeType.GS1DATABAR,
    0x79 to BarcodeType.GS1DATABAR,
    0x31 to BarcodeType.GS1DATABARLIMITED,
    0x7B to BarcodeType.GS1DATABARLIMITED,
    0x32 to BarcodeType.GS1DATABAREXPANDED,
    0x7D to BarcodeType.GS1DATABAREXPANDED,
    0x2C to BarcodeType.MICROQR,
    0x73 to BarcodeType.MICROQR,
    0x7A to BarcodeType.AZTEC,
    0x4A to BarcodeType.AZTEC,
    0x2D to BarcodeType.AZTEC,
    0x73 to BarcodeType.KOREAN3OF5,
    0x3F to BarcodeType.KOREAN3OF5,
    0x39 to BarcodeType.MATRIX2OF5,
    0x6D to BarcodeType.MATRIX2OF5,
    0xB7 to BarcodeType.HANXIN,
    0x48 to BarcodeType.HANXIN,
    0x2E to BarcodeType.DOTCODE ,
)

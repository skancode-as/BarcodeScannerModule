package dk.skancode.barcodescannermodule.newlandimpl

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.core.content.ContextCompat
import dk.skancode.barcodescannermodule.BarcodeBroadcastListener
import dk.skancode.barcodescannermodule.BaseBroadcastReceiver
import dk.skancode.barcodescannermodule.BaseScannerModule
import dk.skancode.barcodescannermodule.BundleFactory
import dk.skancode.barcodescannermodule.Enabler
import dk.skancode.barcodescannermodule.Logger
import dk.skancode.barcodescannermodule.NewlandSymbology
import dk.skancode.barcodescannermodule.ScanMode
import dk.skancode.barcodescannermodule.ScannerConfigKey
import dk.skancode.barcodescannermodule.Symbology
import dk.skancode.barcodescannermodule.event.BarcodeType

internal class NewlandScannerModule(
    context: Context,
    activity: Activity,
    val bundleFactory: BundleFactory = BundleFactory(),
    val logger: Logger = Logger("NewlandScannerModule")
) : BaseScannerModule(context, activity), BarcodeBroadcastListener {
    override val barcodeTypeMap: Map<Int, BarcodeType>
        get() = newlandBarcodeTypeMap

    override fun init() {
        super.init()

        receiver = BarcodeDataReceiver(bundleFactory, logger, this)
        startBarcode(receiver)
    }

    override fun startBarcode(receiver: BaseBroadcastReceiver) {
        val filter = IntentFilter("nlscan.action.SCANNER_RESULT")
        val flag = ContextCompat.RECEIVER_EXPORTED

        ContextCompat.registerReceiver(context, receiver, filter, flag)
    }

    override fun setScannerState(enabler: Enabler) {
        getPreferences().edit().putString("scannerState", enabler.value).apply()
        configureScanner(ScannerConfigKey.SCAN_POWER, enabler.ordinal)
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

internal val newlandBarcodeTypeMap = mapOf(
    2 to BarcodeType.CODE128,
    3 to BarcodeType.UCCEAN128,
    4 to BarcodeType.AIM128,
    5 to BarcodeType.GS1_128,
    6 to BarcodeType.ISBT128,
    7 to BarcodeType.EAN8,
    8 to BarcodeType.EAN13,
    9 to BarcodeType.UPCE,
    10 to BarcodeType.UPCA,
    11 to BarcodeType.ISBN,
    12 to BarcodeType.ISSN,
    13 to BarcodeType.CODE39,
    14 to BarcodeType.CODE93,
    15 to BarcodeType.CODE93I,
    16 to BarcodeType.CODABAR,
    17 to BarcodeType.ITF,
    18 to BarcodeType.ITF6,
    19 to BarcodeType.ITF14,
    20 to BarcodeType.DPLEITCODE,
    21 to BarcodeType.DPIDENTCODE,
    22 to BarcodeType.CHNPOST25,
    23 to BarcodeType.STANDARD25,
    23 to BarcodeType.IATA25,
    24 to BarcodeType.MATRIX25,
    25 to BarcodeType.INDUSTRIAL25,
    26 to BarcodeType.COOP25,
    27 to BarcodeType.CODE11,
    28 to BarcodeType.MSIPLESSEY,
    29 to BarcodeType.PLESSEY,
    30 to BarcodeType.RSS14,
    31 to BarcodeType.RSSLIMITED,
    32 to BarcodeType.RSSEXPANDED,
    33 to BarcodeType.TELEPEN,
    34 to BarcodeType.CHANNELCODE,
    35 to BarcodeType.CODE32,
    36 to BarcodeType.CODEZ,
    37 to BarcodeType.CODABLOCKF,
    38 to BarcodeType.CODABLOCKA,
    39 to BarcodeType.CODE49,
    40 to BarcodeType.CODE16K,
    41 to BarcodeType.HIBC128,
    42 to BarcodeType.HIBC39,
    43 to BarcodeType.RSSFAMILY,
    44 to BarcodeType.TRIOPTIC_CODE39,
    45 to BarcodeType.UPC_E1,
    256 to BarcodeType.PDF417,
    257 to BarcodeType.MICROPDF,
    258 to BarcodeType.QRCODE,
    259 to BarcodeType.MICROQR,
    260 to BarcodeType.AZTEC,
    261 to BarcodeType.DATAMATRIX,
    262 to BarcodeType.MAXICODE,
    263 to BarcodeType.CSCODE,
    264 to BarcodeType.GRIDMATRIX,
    265 to BarcodeType.EARMARK,
    266 to BarcodeType.VERICODE,
    267 to BarcodeType.CCA,
    268 to BarcodeType.CCB,
    269 to BarcodeType.CCC,
    270 to BarcodeType.COMPOSITE,
    271 to BarcodeType.HIBCAZT,
    272 to BarcodeType.HIBCDM,
    273 to BarcodeType.HIBCMICROPDF,
    274 to BarcodeType.HIBCQR,
    275 to BarcodeType.DOTCODE,
    512 to BarcodeType.POSTNET,
    513 to BarcodeType.ONECODE,
    514 to BarcodeType.RM4SCC,
    515 to BarcodeType.PLANET,
    516 to BarcodeType.KIX,
    517 to BarcodeType.APCUSTOM,
    518 to BarcodeType.APREDIRECT,
    519 to BarcodeType.APREPLYPAID,
    520 to BarcodeType.APROUTING,
    768 to BarcodeType.NUMOCRB,
    769 to BarcodeType.PASSPORT,
    770 to BarcodeType.TD1,
    2048 to BarcodeType.PRIVATE,
    2049 to BarcodeType.ZZCODE,
    65535 to BarcodeType.UNKNOWN,
)
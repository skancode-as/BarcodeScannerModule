package dk.skancode.barcodescannermodule.impl.zebra

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
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

internal class ZebraScannerModule(
    context: Context,
    activity: Activity,
    val bundleFactory: BundleFactory = BundleFactory(),
    val logger: Logger = Logger("ZebraScannerModule"),
    gs1Parser: Gs1Parser = Gs1ParserImpl(),
): BaseScannerModule(context, activity, bundleFactory, gs1Parser) {
    override val barcodeTypeMap: Map<Int, BarcodeType>
        get() = zebraBarcodeTypeMap

    companion object {
        const val RECEIVER_ACTION: String = "dk.skancode.barcode.module.ACTION"
        const val ACTION: String = "com.symbol.datawedge.api.ACTION"
        const val RECEIVER_CATEGORY: String = Intent.CATEGORY_DEFAULT
        const val ENABLE_SCANNER_EXTRA: String = "com.symbol.datawedge.api.ENABLE_DATAWEDGE"
        const val SET_CONFIG_EXTRA: String = "com.symbol.datawedge.api.SET_CONFIG"
        val internalBarcodeTypeMap = mapOf(
            "" to -1,
            "LABEL-TYPE-AUSPOSTAL" to 0,
            "LABEL-TYPE-AZTEC" to 1,
            "LABEL-TYPE-CANPOSTAL" to 2,
            "LABEL-TYPE-CHINESE-2OF5" to 3,
            "LABEL-TYPE-CODABAR" to 4,
            "LABEL-TYPE-CODE11" to 5,
            "LABEL-TYPE-CODE32" to 6, // Not tested
            "LABEL-TYPE-CODE39" to 7,
            "LABEL-TYPE-CODE93" to 8,
            "LABEL-TYPE-CODE128" to 9,
            "LABEL-TYPE-COMPOSITE-AB" to 10,
            "LABEL-TYPE-COMPOSITE-C" to 11,
            "LABEL-TYPE-DATAMATRIX" to 12,
            "LABEL-TYPE-DECODER-SIGNATURE" to 13, // not tested
            "LABEL-TYPE-D2OF5" to 14,
            "LABEL-TYPE-DOTCODE" to 15,
            "LABEL-TYPE-DUTCH-POSTAL" to 16, // not tested
            "LABEL-TYPE-EAN8" to 17,
            "LABEL-TYPE-EAN13" to 18,
            "LABEL-TYPE-FINNISH-POSTAL-4S" to 19, // not tested
            "LABEL-TYPE-GS1-DATABAR" to 20,
            "LABEL-TYPE-GRIDMATRIX" to 21,
            "LABEL-TYPE-GS1-DATABAR-LIM" to 22,
            "LABEL-TYPE-GS1-DATABAR-EXP" to 23,
            "LABEL-TYPE-GS1-DATAMATRIX" to 24,
            "LABEL-TYPE-GS1-QRCODE" to 25,
            "LABEL-TYPE-HANXIN" to 26,
            "LABEL-TYPE-I2OF5" to 27,
            "LABEL-TYPE-JAPPOSTAL" to 28,
            "LABEL-TYPE-KOREAN-3OF5" to 29, // not tested
            "LABEL-TYPE-MACROPDF" to 30, // not tested
            "LABEL-TYPE-MAILMARK" to 31, // not tested
            "LABEL-TYPE-MATRIX-2OF5" to 32, // not tested
            "LABEL-TYPE-MAXICODE" to 33,
            "LABEL-TYPE-MICR-E13B" to 34, // not tested
            "LABEL-TYPE-MICROPDF" to 35,
            "LABEL-TYPE-MICROQR" to 36,
            "LABEL-TYPE-MACROMICROPDF" to 37, // not tested
            "LABEL-TYPE-MSI" to 38,
            "LABEL-TYPE-OCR-A" to 39, // not tested
            "LABEL-TYPE-OCR-B" to 40, // not tested
            "LABEL-TYPE-PDF417" to 41,
            "LABEL-TYPE-QRCODE" to 42,
            "LABEL-TYPE-TLC39" to 43,
            "LABEL-TYPE-TRIOPTIC39" to 44, // not tested
            "LABEL-TYPE-USCURRENCY" to 45, // not tested
            "LABEL-TYPE-USPOSTNET" to 46,
            "LABEL-TYPE-USPLANET" to 47,
            "LABEL-TYPE-USPOSTAL" to 48, // not tested
            "LABEL-TYPE-UPCA" to 49,
            "LABEL-TYPE-UPCE0" to 50,
            "LABEL-TYPE-US4STATE-FICS" to 51,
            "LABEL-TYPE-UPCE1" to 52,
            "LABEL-TYPE-US4STATE" to 53,
            "LABEL-TYPE-EAN128" to 54,
            "LABEL-TYPE-UKPOSTAL" to 55,
        )
    }

    override fun init() {
        super.init()
        receiver = ZebraBarcodeDataReceiver(bundleFactory, logger, this)
        startBarcode(receiver)
    }

    override fun startBarcode(receiver: BaseBroadcastReceiver) {
        val filter = IntentFilter(RECEIVER_ACTION).apply {
            addCategory(RECEIVER_CATEGORY)
        }
        val flag = ContextCompat.RECEIVER_EXPORTED

        ContextCompat.registerReceiver(context, receiver, filter, flag)
    }

    override fun setScannerState(enabler: Enabler) {
        getPreferences().edit().putString("scannerState", enabler.value).apply()
        val intent = Intent(ACTION).apply {
            putExtra(ENABLE_SCANNER_EXTRA, enabler == Enabler.ON)
        }

        context.sendBroadcast(intent)
    }

    override fun setAutoEnter(value: Enabler) {
        val keystrokeParams = bundleOf(
            "keystroke_action_char" to "LF",
        )
        val keystrokeBundle = bundleOf(
            "PLUGIN_NAME" to "KEYSTROKE",
            "RESET_CONFIG" to "false",
            "PARAM_LIST" to keystrokeParams,
        )
        configureProfile(keystrokeBundle)
    }

    override fun setNotificationSound(value: Enabler) {
        logger.debug("SetNotifcationSound currently not functional")
        val paramBundle = bundleOf(
            "configure_all_scanners" to "true",
            "remote_scanner_audio_feedback_mode" to if (value == Enabler.ON) 2 else 0,
        )

        configureProfile(bundleOf(
            "PLUGIN_NAME" to "BARCODE",
            "RESET_CONFIG" to "false",
            "PARAM_LIST" to paramBundle,
        ))
    }

    override fun setNotificationVibration(value: Enabler) {
        logger.debug("SetNotifcationVibration currently not functional")
        val paramBundle = bundleOf(
            "configure_all_scanners" to "true",
            "decode_haptic_feedback" to (value == Enabler.ON),
        )

        configureProfile(bundleOf(
            "PLUGIN_NAME" to "BARCODE",
            "RESET_CONFIG" to "false",
            "PARAM_LIST" to paramBundle,
        ))
    }

    override fun setScanMode(value: ScanMode) {
        when(value) {
            ScanMode.API -> {
                val intentParams = bundleOf(
                    "intent_output_enabled" to "true",
                    "intent_action" to RECEIVER_ACTION,
                    "intent_category" to RECEIVER_CATEGORY,
                    "intent_delivery" to 2,
                )
                val intentBundle = bundleOf(
                    "PLUGIN_NAME" to "INTENT",
                    "RESET_CONFIG" to "false",
                    "PARAM_LIST" to intentParams,
                )

                val keystrokeParams = bundleOf(
                    "keystroke_output_enabled" to "false",
                )
                val keystrokeBundle = bundleOf(
                    "PLUGIN_NAME" to "KEYSTROKE",
                    "RESET_CONFIG" to "false",
                    "PARAM_LIST" to keystrokeParams,
                )

                configureProfile(arrayListOf(intentBundle, keystrokeBundle))
            }
            ScanMode.PADDING -> {}
            ScanMode.DIRECT -> {
                val intentParams = bundleOf(
                    "intent_output_enabled" to "false",
                )
                val intentBundle = bundleOf(
                    "PLUGIN_NAME" to "INTENT",
                    "RESET_CONFIG" to "true",
                    "PARAM_LIST" to intentParams,
                )

                val keystrokeParams = bundleOf(
                    "keystroke_output_enabled" to "true",
                    "keystroke_character_delay" to 1,
                )
                val keystrokeBundle = bundleOf(
                    "PLUGIN_NAME" to "KEYSTROKE",
                    "RESET_CONFIG" to "false",
                    "PARAM_LIST" to keystrokeParams,
                )
                configureProfile(arrayListOf(intentBundle, keystrokeBundle))
            }
            ScanMode.SIMULATE -> {
                val intentParams = bundleOf(
                    "intent_output_enabled" to "false",
                )
                val intentBundle = bundleOf(
                    "PLUGIN_NAME" to "INTENT",
                    "RESET_CONFIG" to "true",
                    "PARAM_LIST" to intentParams,
                )

                val keystrokeParams = bundleOf(
                    "keystroke_output_enabled" to "true",
                    "keystroke_character_delay" to 100,
                )
                val keystrokeBundle = bundleOf(
                    "PLUGIN_NAME" to "KEYSTROKE",
                    "RESET_CONFIG" to "false",
                    "PARAM_LIST" to keystrokeParams,
                )

                configureProfile(arrayListOf(intentBundle, keystrokeBundle))
            }
        }
    }

    private fun configureProfile(configBundle: Bundle) {
        return configureProfile(arrayListOf(configBundle))
    }

    private fun configureProfile(configBundles: ArrayList<Bundle>) {
        val mainBundle = bundleOf(
            "PROFILE_NAME" to "SkanCodeModule",
            "CONFIG_MODE" to "CREATE_IF_NOT_EXIST",
            "PLUGIN_CONFIG" to configBundles,
            "APP_LIST" to arrayOf(
                bundleOf(
                    "PACKAGE_NAME" to activity.packageName,
                    "ACTIVITY_LIST" to arrayOf("*")
                )
            )
        )

        val intent = Intent(ACTION).apply {
            putExtra(SET_CONFIG_EXTRA, mainBundle)
        }

        logger.debug("Intent to configureProfile: $intent, extras: ${intent.extras}")

        context.sendBroadcast(intent)
    }

    override fun canSetNfcStatus(): Boolean {
        return false
    }

    override fun setNfcStatus(status: Enabler) {
        logger.error("Cannot set Nfc status on Zebra devices. You should prompt the user to enable it in Settings.")
    }

    override fun canSetSymbology(): Boolean {
        return false
    }

    override fun setSymbology(symbology: Symbology) {
        TODO("Not yet implemented")
    }

}

internal val zebraBarcodeTypeMap: Map<Int, BarcodeType> = mapOf(
    -1 to BarcodeType.UNKNOWN,
    0 to BarcodeType.AUSTRALIANPOSTAL,
    1 to BarcodeType.AZTEC,
    2 to BarcodeType.CANADIANPOSTAL,
    3 to BarcodeType.CHINESE2OF5,
    4 to BarcodeType.CODABAR,
    5 to BarcodeType.CODE11,
    6 to BarcodeType.CODE32,
    7 to BarcodeType.CODE39,
    8 to BarcodeType.CODE93,
    9 to BarcodeType.CODE128,
    10 to BarcodeType.COMPOSITEAB,
    11 to BarcodeType.COMPOSITEC,
    12 to BarcodeType.DATAMATRIX,
    13 to BarcodeType.DECODERSIGNATURE,
    14 to BarcodeType.DISCRETE2OF5,
    15 to BarcodeType.DOTCODE,
    16 to BarcodeType.DUTCHPOSTAL,
    17 to BarcodeType.EAN8,
    18 to BarcodeType.EAN13,
    19 to BarcodeType.FINNISHPOSTAL4S,
    20 to BarcodeType.GS1DATABAR,
    21 to BarcodeType.GRIDMATRIX,
    22 to BarcodeType.GS1DATABARLIMITED,
    23 to BarcodeType.GS1DATABAREXPANDED,
    24 to BarcodeType.GS1DATAMATRIX,
    25 to BarcodeType.GS1QRCODE,
    26 to BarcodeType.HANXIN,
    27 to BarcodeType.INTERLEAVED2OF5,
    28 to BarcodeType.JAPANESEPOSTAL,
    29 to BarcodeType.KOREAN3OF5,
    30 to BarcodeType.MACROPDF,
    31 to BarcodeType.MAILMARK,
    32 to BarcodeType.MATRIX2OF5,
    33 to BarcodeType.MAXICODE,
    34 to BarcodeType.MICRE13B,
    35 to BarcodeType.MICROPDF,
    36 to BarcodeType.MICROQR,
    37 to BarcodeType.MACROMICROPDF,
    38 to BarcodeType.MSI,
    39 to BarcodeType.OCRA,
    40 to BarcodeType.OCRB,
    41 to BarcodeType.PDF417,
    42 to BarcodeType.QRCODE,
    43 to BarcodeType.TLC39,
    44 to BarcodeType.TRIOPTIC_CODE39,
    45 to BarcodeType.USCURRENCY,
    46 to BarcodeType.USPOSTNET,
    47 to BarcodeType.USPLANET,
    48 to BarcodeType.USPOSTAL,
    49 to BarcodeType.UPCA,
    50 to BarcodeType.UPCE,
    51 to BarcodeType.US4STATEFICS,
    52 to BarcodeType.UPC_E1,
    53 to BarcodeType.US4STATE,
    54 to BarcodeType.UCCEAN128,
    55 to BarcodeType.UKPOSTAL,
)
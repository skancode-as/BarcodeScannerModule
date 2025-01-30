package dk.skancode.barcodescannermodule.zebraimpl

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import dk.skancode.barcodescannermodule.BaseBroadcastReceiver
import dk.skancode.barcodescannermodule.BaseScannerModule
import dk.skancode.barcodescannermodule.Enabler
import dk.skancode.barcodescannermodule.IEventHandler
import dk.skancode.barcodescannermodule.ScanMode
import dk.skancode.barcodescannermodule.Symbology


class ZebraScannerModule(context: Context, activity: Activity): BaseScannerModule(context, activity) {
    companion object {
        const val RECEIVER_ACTION: String = "dk.skancode.barcode.module.ACTION"
        const val ACTION: String = "com.symbol.datawedge.api.ACTION"
        const val RECEIVER_CATEGORY: String = Intent.CATEGORY_DEFAULT
        const val ENABLE_SCANNER_EXTRA: String = "com.symbol.datawedge.api.ENABLE_DATAWEDGE"
        const val SET_CONFIG_EXTRA: String = "com.symbol.datawedge.api.SET_CONFIG"
    }

    override fun registerReceiver(receiver: BaseBroadcastReceiver) {
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

    override fun registerBarcodeReceiver(eventHandler: IEventHandler) {
        Log.d("ZebraScannerModule", "registerBarcodeReceiver called")
        val dataReceiver = ZebraBarcodeDataReceiver(eventHandler)
        dataReceivers.add(dataReceiver)

        registerReceiver(dataReceiver)
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
        val paramBundle = bundleOf(
            "configure_all_scanners" to "true",
            "remote_scanner_audio_feedback_mode" to if (value == Enabler.ON) 2 else 0,
        )

        // decode_haptic_feedback: Boolean
        configureProfile(bundleOf(
            "PLUGIN_NAME" to "BARCODE",
            "RESET_CONFIG" to "false",
            "PARAM_LIST" to paramBundle,
        ))
    }

    override fun setNotificationVibration(value: Enabler) {
        val paramBundle = bundleOf(
            "configure_all_scanners" to "true",
            "decode_haptic_feedback" to (value == Enabler.ON),
        )

        // decode_haptic_feedback: Boolean
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
            "CONFIG_MODE" to "UPDATE",
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

        Log.d("ZebraScannerModule", "Intent to configureProfile: $intent, extras: ${intent.extras}")

        context.sendBroadcast(intent)
    }

    override fun setNfcStatus(status: Enabler) {
    }

    override fun canSetSymbology(): Boolean {
        return false
    }

    override fun setSymbology(symbology: Symbology) {
        TODO("Not yet implemented")
    }

}
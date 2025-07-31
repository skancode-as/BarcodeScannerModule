package dk.skancode.barcodescannermodule.unitechimpl

import android.content.Context
import android.content.Intent
import dk.skancode.barcodescannermodule.BarcodeBroadcastListener
import dk.skancode.barcodescannermodule.BaseBroadcastReceiver
import dk.skancode.barcodescannermodule.BundleFactory
import dk.skancode.barcodescannermodule.Logger

internal const val DATA_INTENT = "unitech.scanservice.data"
internal const val DATA_TYPE_INTENT = "unitech.scanservice.datatype"

internal class UnitechBarcodeDataReceiver(
    private val bundleFactory: BundleFactory = BundleFactory(),
    private val logger: Logger,
    private val handler: BarcodeBroadcastListener,
) : BaseBroadcastReceiver(handler) {
    private var data: String? = null
    private var dataType: Int? = null

    override fun onReceive(context: Context?, intent: Intent?) {
        logger.info("$intent")
        if (intent?.action == DATA_INTENT) {
            val bundle = intent.extras
            if (bundle != null) {
                data = bundle.getString("text")
                if (data == null) {
                    handler.onReceive(bundleFactory.create(
                        "barcode1" to null,
                        "barcode2" to null,
                        "barcodeType" to null,
                        "ok" to false
                    ))
                } else if (dataType != null) {
                    handler.onReceive(bundleFactory.create(
                        "barcode1" to data,
                        "barcode2" to null,
                        "barcodeType" to dataType,
                        "ok" to true
                    ))
                    data = null
                    dataType = null
                }
            } else {
                handler.onReceive(bundleFactory.create(
                    "barcode1" to null,
                    "barcode2" to null,
                    "barcodeType" to null,
                    "ok" to false
                ))
                data = null
                dataType = null
            }
        }

        if (intent?.action == DATA_TYPE_INTENT) {
            val bundle = intent.extras
            if (bundle != null) {
                dataType = bundle.getInt("text", -1)
                logger.info("UnitechBarcodeDataReceiver: Datatype received = $dataType")
                if (dataType == -1) {
                    dataType = null
                    handler.onReceive(bundleFactory.create(
                        "barcode1" to null,
                        "barcode2" to null,
                        "barcodeType" to null,
                        "ok" to false
                    ))
                } else if (data != null) {
                    handler.onReceive(bundleFactory.create(
                        "barcode1" to data,
                        "barcode2" to null,
                        "barcodeType" to dataType,
                        "ok" to true
                    ))
                    data = null
                    dataType = null
                }
            } else {
                handler.onReceive(bundleFactory.create(
                    "barcode1" to null,
                    "barcode2" to null,
                    "barcodeType" to null,
                    "ok" to false
                ))
                data = null
                dataType = null
            }
        }
    }
}
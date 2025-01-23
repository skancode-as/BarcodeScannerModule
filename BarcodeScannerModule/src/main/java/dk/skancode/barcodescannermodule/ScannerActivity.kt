package dk.skancode.barcodescannermodule

import android.os.Bundle
import androidx.activity.ComponentActivity

open class ScannerActivity: ComponentActivity() {
    private lateinit var _scannerModule: IScannerModule
    val scannerModule: IScannerModule
        get() = _scannerModule

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _scannerModule = ScannerModuleFactory.create(this)
        if (_scannerModule.nfcAvailable()) {
            _scannerModule.setNfcStatus(Enabler.ON)
        }
    }

    override fun onResume() {
        super.onResume()

        _scannerModule.resumeReceivers()
    }

    override fun onPause() {
        super.onPause()

        _scannerModule.pauseReceivers()
    }

}


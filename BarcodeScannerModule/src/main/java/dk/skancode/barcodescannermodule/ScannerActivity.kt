package dk.skancode.barcodescannermodule

import android.os.Bundle
import android.os.PersistableBundle
import androidx.activity.ComponentActivity

abstract class ScannerActivity: ComponentActivity() {
    private lateinit var _scannerModule: IScannerModule
    val scannerModule: IScannerModule
        get() = _scannerModule

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupModule()
    }

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)

        setupModule()
    }

    override fun onResume() {
        super.onResume()

        _scannerModule.resumeReceivers()
    }

    override fun onPause() {
        super.onPause()

        _scannerModule.pauseReceivers()
    }

    private fun setupModule() {
        _scannerModule = ScannerModuleFactory.create(this)
        _scannerModule.init()
        if (_scannerModule.nfcAvailable() && _scannerModule.getNfcStatus() == Enabler.OFF) {
            _scannerModule.setNfcStatus(Enabler.ON)
        }
    }
}


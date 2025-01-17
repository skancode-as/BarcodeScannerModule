package dk.skancode.barcodescannermodule

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.ComposeView

open class ScannerActivity: ComponentActivity() {
    private lateinit var _scannerModule: IScannerModule
    val scannerModule: IScannerModule
        get() = _scannerModule

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _scannerModule = ScannerModuleFactory.create(this)
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


/**
 * Wraps the content provided in [content] parameter, with a [CompositionLocalProvider] of [IScannerModule],
 * so the scannerModule is available through [LocalScannerModule].
 *
 * Then calls [ScannerActivity.setContentView] with a [ComposeView] with the [LocalScannerModule] wrapped content
 *
 * @param content Your app content that depends on LocalScannerModule
 */
@Composable
fun ScannerActivity.ScannerModuleProvider(content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalScannerModule provides scannerModule) {
        content()
    }
}

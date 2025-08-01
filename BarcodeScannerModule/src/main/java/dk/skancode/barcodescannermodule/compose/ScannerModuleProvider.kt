package dk.skancode.barcodescannermodule.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import dk.skancode.barcodescannermodule.IScannerModule
import dk.skancode.barcodescannermodule.ScannerActivity


/**
 * Wraps the content provided in [content] parameter, with a [CompositionLocalProvider] of [IScannerModule],
 * so the scannerModule is available through [LocalScannerModule].
 *
 * @param content Your app content that depends on LocalScannerModule
 */
@Composable
fun ScannerActivity.ScannerModuleProvider(content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalScannerModule provides scannerModule) {
        content()
    }
}


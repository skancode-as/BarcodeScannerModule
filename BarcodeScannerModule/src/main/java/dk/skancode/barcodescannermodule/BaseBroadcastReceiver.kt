package dk.skancode.barcodescannermodule

import android.content.BroadcastReceiver
import android.os.Bundle

internal fun interface BarcodeBroadcastListener {
    fun onReceive(payload: Bundle)
}

internal abstract class BaseBroadcastReceiver(listener: BarcodeBroadcastListener): BroadcastReceiver()
package dk.skancode.barcodescannermodule

import android.content.BroadcastReceiver

abstract class BaseBroadcastReceiver(val handler: IEventHandler): BroadcastReceiver()
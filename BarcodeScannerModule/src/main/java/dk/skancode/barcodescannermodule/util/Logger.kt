package dk.skancode.barcodescannermodule.util

import android.util.Log

internal class Logger(val tag: String) {
    fun info(msg: String, throwable: Throwable? = null) {
        Log.i(tag, msg, throwable)
    }

    fun debug(msg: String, throwable: Throwable? = null) {
        Log.d(tag, msg, throwable)
    }

    fun error(msg: String, throwable: Throwable? = null) {
        Log.e(tag, msg, throwable)
    }
}
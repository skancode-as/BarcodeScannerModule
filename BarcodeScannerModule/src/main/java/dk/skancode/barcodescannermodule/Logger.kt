package dk.skancode.barcodescannermodule

import android.util.Log

class Logger(val tag: String) {
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
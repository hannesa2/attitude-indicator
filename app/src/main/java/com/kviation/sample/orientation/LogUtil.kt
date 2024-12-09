package com.kviation.sample.orientation

import android.util.Log

object LogUtil {
    private const val TAG = "AttitudeIndicator"

    fun v(message: String, vararg args: Any?) {
        var msg = message
        if (args.isNotEmpty()) {
            msg = String.format(msg, *args)
        }
        Log.v(TAG, msg)
    }

    fun i(message: String, vararg args: Any?) {
        var msg = message
        if (args.isNotEmpty()) {
            msg = String.format(msg, *args)
        }
        Log.i(TAG, msg)
    }

    fun w(message: String, vararg args: Any?) {
        var msg = message
        if (args.isNotEmpty()) {
            msg = String.format(msg, *args)
        }
        Log.w(TAG, msg)
    }

    fun e(message: String, vararg args: Any?) {
        var msg = message
        if (args.isNotEmpty()) {
            msg = String.format(msg, *args)
        }
        Log.e(TAG, msg)
    }

    fun e(t: Throwable?, message: String, vararg args: Any?) {
        var msg = message
        if (args.isNotEmpty()) {
            msg = String.format(msg, *args)
        }
        Log.e(TAG, msg, t)
    }
}

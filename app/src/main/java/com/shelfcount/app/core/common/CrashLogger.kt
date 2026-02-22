package com.shelfcount.app.core.common

import android.content.Context
import android.util.Log
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter

object CrashLogger {
    private const val TAG = "ShelfCountCrash"
    private const val CRASH_LOG_FILE = "last_crash.log"

    fun install(
        context: Context,
        enabled: Boolean,
    ) {
        if (!enabled) return

        val previousHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            runCatching {
                val stackTrace =
                    StringWriter().also { writer ->
                        throwable.printStackTrace(PrintWriter(writer))
                    }.toString()
                Log.e(TAG, "Uncaught exception on thread=${thread.name}", throwable)
                File(context.cacheDir, CRASH_LOG_FILE).writeText(stackTrace)
            }
            previousHandler?.uncaughtException(thread, throwable)
        }
    }
}

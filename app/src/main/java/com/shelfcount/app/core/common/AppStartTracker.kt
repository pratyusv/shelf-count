package com.shelfcount.app.core.common

import android.os.SystemClock

object AppStartTracker {
    @Volatile
    private var processStartElapsedRealtimeMs: Long = SystemClock.elapsedRealtime()

    fun markApplicationCreated() {
        processStartElapsedRealtimeMs = processStartElapsedRealtimeMs.coerceAtMost(SystemClock.elapsedRealtime())
    }

    fun coldStartDurationMs(): Long = SystemClock.elapsedRealtime() - processStartElapsedRealtimeMs
}

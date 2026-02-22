package com.shelfcount.app.data.remote

interface SyncOrchestrator {
    suspend fun syncNow()
}

package com.shelfcount.app.data.remote

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NoOpSyncOrchestrator
    @Inject
    constructor(
        private val remoteDataSource: RemoteDataSource,
    ) : SyncOrchestrator {
        override suspend fun syncNow() {
            // Conflict strategy (initial): last-write-wins using updatedAtEpochMillis.
            // In MVP this orchestrator is intentionally no-op until remote sync is enabled.
            remoteDataSource.pullItems()
        }
    }

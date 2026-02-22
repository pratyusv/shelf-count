package com.shelfcount.app.data.remote

import com.shelfcount.app.domain.model.Item
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NoOpRemoteDataSource
    @Inject
    constructor() : RemoteDataSource {
        override suspend fun pushItems(items: List<Item>) {
            // No-op in MVP: remote sync backend is not enabled yet.
        }

        override suspend fun pullItems(): List<Item> = emptyList()
    }

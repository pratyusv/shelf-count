package com.shelfcount.app.data.remote

import com.shelfcount.app.domain.model.Item

interface RemoteDataSource {
    suspend fun pushItems(items: List<Item>)

    suspend fun pullItems(): List<Item>
}

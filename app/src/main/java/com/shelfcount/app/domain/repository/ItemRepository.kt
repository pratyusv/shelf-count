package com.shelfcount.app.domain.repository

import com.shelfcount.app.domain.model.Item
import kotlinx.coroutines.flow.Flow

interface ItemRepository {
    fun observeActiveItems(): Flow<List<Item>>

    fun observeLowStockItems(): Flow<List<Item>>

    fun observeItemById(itemId: Long): Flow<Item?>

    suspend fun addItem(item: Item): Long

    suspend fun updateItem(item: Item)

    suspend fun deleteItem(itemId: Long)

    suspend fun setArchived(
        itemId: Long,
        archived: Boolean,
        updatedAtEpochMillis: Long,
    )

    suspend fun adjustQuantity(
        itemId: Long,
        delta: Double,
        updatedAtEpochMillis: Long,
    )

    suspend fun searchItems(query: String): List<Item>

    suspend fun getItemsByCategory(categoryId: Int): List<Item>

    suspend fun getItemsSortedByName(): List<Item>

    suspend fun getItemsSortedByRecentlyUpdated(): List<Item>

    suspend fun getLowStockItemsSortedByName(): List<Item>
}

package com.shelfcount.app.data.repository

import androidx.room.withTransaction
import com.shelfcount.app.data.local.ShelfCountDatabase
import com.shelfcount.app.data.local.dao.ItemDao
import com.shelfcount.app.data.local.dao.StockTransactionDao
import com.shelfcount.app.data.local.entity.StockTransactionEntity
import com.shelfcount.app.data.mapper.toDomain
import com.shelfcount.app.data.mapper.toEntity
import com.shelfcount.app.domain.model.Item
import com.shelfcount.app.domain.model.TransactionReason
import com.shelfcount.app.domain.model.UnitType
import com.shelfcount.app.domain.repository.ItemRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ItemRepositoryImpl
    @Inject
    constructor(
        private val database: ShelfCountDatabase,
        private val itemDao: ItemDao,
        private val stockTransactionDao: StockTransactionDao,
    ) : ItemRepository {
        override fun observeActiveItems(): Flow<List<Item>> =
            itemDao.observeActiveItems().map { list -> list.map { it.toDomain() } }

        override fun observeLowStockItems(): Flow<List<Item>> =
            itemDao.observeLowStockItems().map { list -> list.map { it.toDomain() } }

        override fun observeItemById(itemId: Long): Flow<Item?> =
            itemDao.observeById(itemId).map { entity -> entity?.toDomain() }

        override suspend fun addItem(item: Item): Long {
            validateItem(item)
            return itemDao.insert(item.toEntity())
        }

        override suspend fun updateItem(item: Item) {
            validateItem(item)
            itemDao.update(item.toEntity())
        }

        override suspend fun deleteItem(itemId: Long) {
            itemDao.deleteById(itemId)
        }

        override suspend fun setArchived(
            itemId: Long,
            archived: Boolean,
            updatedAtEpochMillis: Long,
        ) {
            itemDao.setArchived(itemId, archived, updatedAtEpochMillis)
        }

        override suspend fun adjustQuantity(
            itemId: Long,
            delta: Double,
            updatedAtEpochMillis: Long,
        ) {
            if (delta == 0.0) return
            database.withTransaction {
                itemDao.adjustQuantity(itemId, delta, updatedAtEpochMillis)
                stockTransactionDao.insert(
                    StockTransactionEntity(
                        itemId = itemId,
                        delta = delta,
                        reason = if (delta > 0) TransactionReason.PURCHASE else TransactionReason.CONSUME,
                        createdAtEpochMillis = updatedAtEpochMillis,
                    ),
                )
            }
        }

        override suspend fun searchItems(query: String): List<Item> =
            itemDao.searchByName(query.trim()).map { it.toDomain() }

        override suspend fun getItemsByCategory(categoryId: Int): List<Item> =
            itemDao.getByCategory(categoryId).map { it.toDomain() }

        override suspend fun getItemsSortedByName(): List<Item> = itemDao.getAllByName().map { it.toDomain() }

        override suspend fun getItemsSortedByRecentlyUpdated(): List<Item> =
            itemDao.getAllByRecentlyUpdated().map { it.toDomain() }

        override suspend fun getLowStockItemsSortedByName(): List<Item> =
            itemDao.getLowStockByName().map { it.toDomain() }

        private fun validateItem(item: Item) {
            if (item.name.isBlank()) throw IllegalArgumentException("Item name cannot be blank")
            if (!item.quantity.isFinite() || item.quantity < 0.0) {
                throw IllegalArgumentException("Quantity must be a finite non-negative number")
            }
            if (!item.lowStockThreshold.isFinite() || item.lowStockThreshold < 0.0) {
                throw IllegalArgumentException("Low stock threshold must be a finite non-negative number")
            }

            val requiresWholeNumber = item.unit == UnitType.PIECE || item.unit == UnitType.PACK
            if (requiresWholeNumber && item.quantity % 1.0 != 0.0) {
                throw IllegalArgumentException("Quantity for ${item.unit.name.lowercase()} must be a whole number")
            }
            if (requiresWholeNumber && item.lowStockThreshold % 1.0 != 0.0) {
                throw IllegalArgumentException("Threshold for ${item.unit.name.lowercase()} must be a whole number")
            }
        }
    }

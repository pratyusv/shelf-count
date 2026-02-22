package com.shelfcount.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.shelfcount.app.data.local.entity.ItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ItemDao {
    @Query("SELECT * FROM items WHERE is_archived = 0 ORDER BY updated_at_epoch_millis DESC")
    fun observeActiveItems(): Flow<List<ItemEntity>>

    @Query("SELECT * FROM items WHERE id = :itemId LIMIT 1")
    fun observeById(itemId: Long): Flow<ItemEntity?>

    @Query(
        "SELECT * FROM items " +
            "WHERE is_archived = 0 AND quantity <= low_stock_threshold " +
            "ORDER BY updated_at_epoch_millis DESC",
    )
    fun observeLowStockItems(): Flow<List<ItemEntity>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(item: ItemEntity): Long

    @Update
    suspend fun update(item: ItemEntity)

    @Query("DELETE FROM items WHERE id = :itemId")
    suspend fun deleteById(itemId: Long)

    @Query("UPDATE items SET is_archived = :archived, updated_at_epoch_millis = :updatedAt WHERE id = :itemId")
    suspend fun setArchived(
        itemId: Long,
        archived: Boolean,
        updatedAt: Long,
    )

    @Query(
        "UPDATE items SET quantity = MAX(0, quantity + :delta), updated_at_epoch_millis = :updatedAt WHERE id = :itemId",
    )
    suspend fun adjustQuantity(
        itemId: Long,
        delta: Double,
        updatedAt: Long,
    )

    @Query("SELECT * FROM items WHERE is_archived = 0 AND name LIKE '%' || :query || '%' ORDER BY name ASC")
    suspend fun searchByName(query: String): List<ItemEntity>

    @Query("SELECT * FROM items WHERE is_archived = 0 AND category_id = :categoryId ORDER BY name ASC")
    suspend fun getByCategory(categoryId: Int): List<ItemEntity>

    @Query("SELECT * FROM items WHERE is_archived = 0 ORDER BY name ASC")
    suspend fun getAllByName(): List<ItemEntity>

    @Query("SELECT * FROM items WHERE is_archived = 0 ORDER BY updated_at_epoch_millis DESC")
    suspend fun getAllByRecentlyUpdated(): List<ItemEntity>

    @Query("SELECT * FROM items WHERE is_archived = 0 AND quantity <= low_stock_threshold ORDER BY name ASC")
    suspend fun getLowStockByName(): List<ItemEntity>
}

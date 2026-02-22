package com.shelfcount.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.shelfcount.app.data.local.entity.StockTransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StockTransactionDao {
    @Query("SELECT * FROM stock_transactions WHERE item_id = :itemId ORDER BY created_at_epoch_millis DESC")
    fun observeByItem(itemId: Long): Flow<List<StockTransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(transaction: StockTransactionEntity): Long
}

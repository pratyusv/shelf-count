package com.shelfcount.app.domain.repository

import com.shelfcount.app.domain.model.StockTransaction
import kotlinx.coroutines.flow.Flow

interface StockTransactionRepository {
    fun observeByItem(itemId: Long): Flow<List<StockTransaction>>

    suspend fun addTransaction(transaction: StockTransaction): Long
}

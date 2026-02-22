package com.shelfcount.app.data.repository

import com.shelfcount.app.data.local.dao.StockTransactionDao
import com.shelfcount.app.data.mapper.toDomain
import com.shelfcount.app.data.mapper.toEntity
import com.shelfcount.app.domain.model.StockTransaction
import com.shelfcount.app.domain.repository.StockTransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class StockTransactionRepositoryImpl
    @Inject
    constructor(
        private val stockTransactionDao: StockTransactionDao,
    ) : StockTransactionRepository {
        override fun observeByItem(itemId: Long): Flow<List<StockTransaction>> =
            stockTransactionDao.observeByItem(itemId).map { list -> list.map { it.toDomain() } }

        override suspend fun addTransaction(transaction: StockTransaction): Long =
            stockTransactionDao.insert(transaction.toEntity())
    }

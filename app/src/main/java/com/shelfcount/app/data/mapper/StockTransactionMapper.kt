package com.shelfcount.app.data.mapper

import com.shelfcount.app.data.local.entity.StockTransactionEntity
import com.shelfcount.app.domain.model.StockTransaction

fun StockTransactionEntity.toDomain(): StockTransaction =
    StockTransaction(
        id = id,
        itemId = itemId,
        delta = delta,
        reason = reason,
        createdAtEpochMillis = createdAtEpochMillis,
    )

fun StockTransaction.toEntity(): StockTransactionEntity =
    StockTransactionEntity(
        id = id,
        itemId = itemId,
        delta = delta,
        reason = reason,
        createdAtEpochMillis = createdAtEpochMillis,
    )

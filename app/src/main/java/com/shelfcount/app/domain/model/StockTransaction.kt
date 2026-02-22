package com.shelfcount.app.domain.model

import androidx.compose.runtime.Immutable

enum class TransactionReason {
    PURCHASE,
    CONSUME,
    ADJUSTMENT,
}

@Immutable
data class StockTransaction(
    val id: Long = 0L,
    val itemId: Long,
    val delta: Double,
    val reason: TransactionReason,
    val createdAtEpochMillis: Long,
)

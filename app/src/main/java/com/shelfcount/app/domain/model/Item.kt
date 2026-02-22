package com.shelfcount.app.domain.model

import androidx.compose.runtime.Immutable

@Immutable
data class Item(
    val id: Long = 0L,
    val name: String,
    val categoryId: Int,
    val quantity: Double,
    val unit: UnitType,
    val lowStockThreshold: Double,
    val notes: String? = null,
    val isArchived: Boolean = false,
    val remoteId: String? = null,
    val syncStatus: SyncStatus = SyncStatus.PENDING,
    val lastSyncedAtEpochMillis: Long? = null,
    val createdAtEpochMillis: Long,
    val updatedAtEpochMillis: Long,
)

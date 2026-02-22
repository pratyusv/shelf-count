package com.shelfcount.app.data.mapper

import com.shelfcount.app.core.common.normalizeItemNameKey
import com.shelfcount.app.data.local.entity.ItemEntity
import com.shelfcount.app.domain.model.Item

fun ItemEntity.toDomain(): Item =
    Item(
        id = id,
        name = name,
        categoryId = categoryId,
        quantity = quantity,
        unit = unit,
        lowStockThreshold = lowStockThreshold,
        notes = notes,
        isArchived = isArchived,
        remoteId = remoteId,
        syncStatus = syncStatus,
        lastSyncedAtEpochMillis = lastSyncedAtEpochMillis,
        createdAtEpochMillis = createdAtEpochMillis,
        updatedAtEpochMillis = updatedAtEpochMillis,
    )

fun Item.toEntity(): ItemEntity =
    ItemEntity(
        id = id,
        name = name,
        normalizedName = normalizeItemNameKey(name),
        categoryId = categoryId,
        quantity = quantity,
        unit = unit,
        lowStockThreshold = lowStockThreshold,
        notes = notes,
        isArchived = isArchived,
        remoteId = remoteId,
        syncStatus = syncStatus,
        lastSyncedAtEpochMillis = lastSyncedAtEpochMillis,
        createdAtEpochMillis = createdAtEpochMillis,
        updatedAtEpochMillis = updatedAtEpochMillis,
    )

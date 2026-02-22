package com.shelfcount.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.shelfcount.app.domain.model.SyncStatus
import com.shelfcount.app.domain.model.UnitType

@Entity(
    tableName = "items",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["category_id"],
            onDelete = ForeignKey.RESTRICT,
        ),
    ],
    indices = [
        Index(value = ["normalized_name", "category_id"], unique = true),
        Index(value = ["is_archived", "name"]),
        Index(value = ["is_archived", "updated_at_epoch_millis"]),
        Index(value = ["category_id", "is_archived"]),
        Index(value = ["category_id"]),
        Index(value = ["updated_at_epoch_millis"]),
    ],
)
data class ItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    @ColumnInfo(name = "name")
    val name: String,
    @ColumnInfo(name = "normalized_name")
    val normalizedName: String,
    @ColumnInfo(name = "category_id")
    val categoryId: Int,
    @ColumnInfo(name = "quantity")
    val quantity: Double,
    @ColumnInfo(name = "unit")
    val unit: UnitType,
    @ColumnInfo(name = "low_stock_threshold")
    val lowStockThreshold: Double,
    @ColumnInfo(name = "notes")
    val notes: String?,
    @ColumnInfo(name = "is_archived")
    val isArchived: Boolean,
    @ColumnInfo(name = "remote_id")
    val remoteId: String? = null,
    @ColumnInfo(name = "sync_status")
    val syncStatus: SyncStatus = SyncStatus.PENDING,
    @ColumnInfo(name = "last_synced_at_epoch_millis")
    val lastSyncedAtEpochMillis: Long? = null,
    @ColumnInfo(name = "created_at_epoch_millis")
    val createdAtEpochMillis: Long,
    @ColumnInfo(name = "updated_at_epoch_millis")
    val updatedAtEpochMillis: Long,
)

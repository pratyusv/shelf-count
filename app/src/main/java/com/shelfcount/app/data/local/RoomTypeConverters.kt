package com.shelfcount.app.data.local

import androidx.room.TypeConverter
import com.shelfcount.app.domain.model.SyncStatus
import com.shelfcount.app.domain.model.TransactionReason
import com.shelfcount.app.domain.model.UnitType

class RoomTypeConverters {
    @TypeConverter
    fun fromUnitType(value: UnitType): String = value.name

    @TypeConverter
    fun toUnitType(value: String): UnitType = UnitType.valueOf(value)

    @TypeConverter
    fun fromTransactionReason(value: TransactionReason): String = value.name

    @TypeConverter
    fun toTransactionReason(value: String): TransactionReason = TransactionReason.valueOf(value)

    @TypeConverter
    fun fromSyncStatus(value: SyncStatus): String = value.name

    @TypeConverter
    fun toSyncStatus(value: String): SyncStatus = SyncStatus.valueOf(value)
}

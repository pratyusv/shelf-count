package com.shelfcount.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.shelfcount.app.data.local.dao.CategoryDao
import com.shelfcount.app.data.local.dao.ItemDao
import com.shelfcount.app.data.local.dao.StockTransactionDao
import com.shelfcount.app.data.local.entity.CategoryEntity
import com.shelfcount.app.data.local.entity.ItemEntity
import com.shelfcount.app.data.local.entity.StockTransactionEntity

@Database(
    entities = [CategoryEntity::class, ItemEntity::class, StockTransactionEntity::class],
    version = 1,
    exportSchema = true,
)
@TypeConverters(RoomTypeConverters::class)
abstract class ShelfCountDatabase : RoomDatabase() {
    abstract fun categoryDao(): CategoryDao

    abstract fun itemDao(): ItemDao

    abstract fun stockTransactionDao(): StockTransactionDao

    companion object {
        const val DATABASE_NAME: String = "shelfcount.db"
    }
}

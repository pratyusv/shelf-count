package com.shelfcount.app.di

import android.content.Context
import androidx.room.Room
import com.shelfcount.app.data.local.DatabaseMigrations
import com.shelfcount.app.data.local.ShelfCountDatabase
import com.shelfcount.app.data.local.dao.CategoryDao
import com.shelfcount.app.data.local.dao.ItemDao
import com.shelfcount.app.data.local.dao.StockTransactionDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
    ): ShelfCountDatabase {
        return Room.databaseBuilder(
            context,
            ShelfCountDatabase::class.java,
            ShelfCountDatabase.DATABASE_NAME,
        )
            .addMigrations(*DatabaseMigrations.all)
            .fallbackToDestructiveMigrationOnDowngrade(dropAllTables = true)
            .build()
    }

    @Provides
    fun provideCategoryDao(database: ShelfCountDatabase): CategoryDao = database.categoryDao()

    @Provides
    fun provideItemDao(database: ShelfCountDatabase): ItemDao = database.itemDao()

    @Provides
    fun provideStockTransactionDao(database: ShelfCountDatabase): StockTransactionDao = database.stockTransactionDao()
}

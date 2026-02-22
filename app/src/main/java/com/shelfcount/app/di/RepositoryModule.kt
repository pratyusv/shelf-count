package com.shelfcount.app.di

import com.shelfcount.app.data.repository.CategoryRepositoryImpl
import com.shelfcount.app.data.repository.ItemRepositoryImpl
import com.shelfcount.app.data.repository.StockTransactionRepositoryImpl
import com.shelfcount.app.domain.repository.CategoryRepository
import com.shelfcount.app.domain.repository.ItemRepository
import com.shelfcount.app.domain.repository.StockTransactionRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindCategoryRepository(impl: CategoryRepositoryImpl): CategoryRepository

    @Binds
    @Singleton
    abstract fun bindItemRepository(impl: ItemRepositoryImpl): ItemRepository

    @Binds
    @Singleton
    abstract fun bindStockTransactionRepository(impl: StockTransactionRepositoryImpl): StockTransactionRepository
}

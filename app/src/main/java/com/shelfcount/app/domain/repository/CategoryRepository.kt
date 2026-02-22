package com.shelfcount.app.domain.repository

import com.shelfcount.app.domain.model.Category
import kotlinx.coroutines.flow.Flow

interface CategoryRepository {
    fun observeCategories(): Flow<List<Category>>

    suspend fun seedDefaultCategories(categories: List<Category>)

    suspend fun createCustomCategory(name: String): Long

    suspend fun upsertCategory(category: Category): Long

    suspend fun deleteCategory(categoryId: Int)

    suspend fun existsByName(name: String): Boolean
}

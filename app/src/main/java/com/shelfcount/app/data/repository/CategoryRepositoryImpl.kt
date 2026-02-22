package com.shelfcount.app.data.repository

import com.shelfcount.app.data.local.dao.CategoryDao
import com.shelfcount.app.data.mapper.toDomain
import com.shelfcount.app.data.mapper.toEntity
import com.shelfcount.app.domain.model.Category
import com.shelfcount.app.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class CategoryRepositoryImpl
    @Inject
    constructor(
        private val categoryDao: CategoryDao,
    ) : CategoryRepository {
        override fun observeCategories(): Flow<List<Category>> =
            categoryDao.observeAll().map { list -> list.map { it.toDomain() } }

        override suspend fun seedDefaultCategories(categories: List<Category>) {
            if (categoryDao.count() == 0) {
                categoryDao.insertAll(categories.map { it.toEntity() })
            }
        }

        override suspend fun createCustomCategory(name: String): Long {
            val normalized = normalizeAndValidateName(name)
            if (categoryDao.existsByName(normalized)) {
                throw IllegalArgumentException("Category already exists: $normalized")
            }

            return categoryDao.upsert(
                Category(
                    id = 0,
                    name = normalized,
                    isCustom = true,
                ).toEntity(),
            )
        }

        override suspend fun upsertCategory(category: Category): Long {
            val normalized = normalizeAndValidateName(category.name)
            val alreadyExists =
                if (category.id == 0) {
                    categoryDao.existsByName(normalized)
                } else {
                    categoryDao.existsByNameExcludingId(normalized, category.id)
                }
            if (alreadyExists) {
                throw IllegalArgumentException("Category already exists: $normalized")
            }

            return categoryDao.upsert(
                category.copy(name = normalized).toEntity(),
            )
        }

        override suspend fun deleteCategory(categoryId: Int) {
            categoryDao.deleteById(categoryId)
        }

        override suspend fun existsByName(name: String): Boolean {
            return categoryDao.existsByName(name.trim())
        }

        private fun normalizeAndValidateName(rawName: String): String {
            val normalized =
                rawName
                    .trim()
                    .replace("\\s+".toRegex(), " ")

            if (normalized.isBlank()) {
                throw IllegalArgumentException("Category name cannot be blank")
            }

            return normalized
        }
    }

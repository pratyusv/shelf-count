package com.shelfcount.app.data.repository

import com.shelfcount.app.data.local.dao.CategoryDao
import com.shelfcount.app.data.local.entity.CategoryEntity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CategoryRepositoryImplTest {
    @Test
    fun createCustomCategory_rejectsBlankAndDuplicateNames() =
        runTest {
            val dao = FakeCategoryDao()
            val repository = CategoryRepositoryImpl(dao)

            runCatching { repository.createCustomCategory("   ") }
                .onSuccess { throw AssertionError("Expected blank category to fail") }
                .onFailure { assertTrue(it.message?.contains("blank", ignoreCase = true) == true) }

            repository.createCustomCategory("Laundry")

            runCatching { repository.createCustomCategory("  laundry ") }
                .onSuccess { throw AssertionError("Expected duplicate category to fail") }
                .onFailure { assertTrue(it.message?.contains("exists", ignoreCase = true) == true) }
        }

    @Test
    fun seedDefaultCategories_onlyInsertsOnEmptyDb() =
        runTest {
            val dao = FakeCategoryDao()
            val repository = CategoryRepositoryImpl(dao)

            repository.seedDefaultCategories(
                listOf(
                    com.shelfcount.app.domain.model.Category(1, "Grocery", false),
                    com.shelfcount.app.domain.model.Category(2, "Bathroom", false),
                ),
            )
            repository.seedDefaultCategories(
                listOf(com.shelfcount.app.domain.model.Category(3, "ShouldNotBeInserted", false)),
            )

            assertEquals(2, dao.rows.size)
            assertEquals("Grocery", dao.rows.first().name)
        }
}

private class FakeCategoryDao : CategoryDao {
    val rows: MutableList<CategoryEntity> = mutableListOf()

    override suspend fun count(): Int = rows.size

    override fun observeAll(): Flow<List<CategoryEntity>> = flowOf(rows.toList())

    override suspend fun insertAll(categories: List<CategoryEntity>): List<Long> {
        val ids = mutableListOf<Long>()
        categories.forEach { category ->
            if (rows.none { it.name.equals(category.name, ignoreCase = true) }) {
                val nextId = (rows.maxOfOrNull { it.id } ?: 0) + 1
                rows += category.copy(id = nextId)
                ids += nextId.toLong()
            } else {
                ids += -1L
            }
        }
        return ids
    }

    override suspend fun upsert(category: CategoryEntity): Long {
        val id = if (category.id == 0) (rows.maxOfOrNull { it.id } ?: 0) + 1 else category.id
        val index = rows.indexOfFirst { it.id == id }
        val normalized = category.copy(id = id)
        if (index >= 0) {
            rows[index] = normalized
        } else {
            rows += normalized
        }
        return id.toLong()
    }

    override suspend fun update(category: CategoryEntity) {
        val index = rows.indexOfFirst { it.id == category.id }
        if (index >= 0) rows[index] = category
    }

    override suspend fun deleteById(categoryId: Int) {
        rows.removeAll { it.id == categoryId }
    }

    override suspend fun existsByName(name: String): Boolean = rows.any { it.name.equals(name, ignoreCase = true) }

    override suspend fun existsByNameExcludingId(
        name: String,
        excludedId: Int,
    ): Boolean = rows.any { it.id != excludedId && it.name.equals(name, ignoreCase = true) }
}

package com.shelfcount.app.presentation.inventory

import androidx.lifecycle.SavedStateHandle
import com.shelfcount.app.domain.model.Category
import com.shelfcount.app.domain.model.Item
import com.shelfcount.app.domain.model.UnitType
import com.shelfcount.app.domain.repository.CategoryRepository
import com.shelfcount.app.domain.repository.ItemRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class InventoryViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun openAddItem_updatesDestinationState() =
        runTest {
            val itemRepo = FakeItemRepository()
            val categoryRepo = FakeCategoryRepository()
            val viewModel = InventoryViewModel(itemRepo, categoryRepo, SavedStateHandle())

            viewModel.openAddItem()

            assertEquals(
                com.shelfcount.app.presentation.navigation.AppDestination.ADD_EDIT_ITEM,
                viewModel.destination.value,
            )
        }

    @Test
    fun searchAndFilter_updatesVisibleItems() =
        runTest {
            val itemRepo = FakeItemRepository()
            val categoryRepo = FakeCategoryRepository()
            val now = System.currentTimeMillis()
            itemRepo.items.value =
                listOf(
                    Item(
                        id = 1,
                        name = "Milk",
                        categoryId = 1,
                        quantity = 1.0,
                        unit = UnitType.LITER,
                        lowStockThreshold = 1.0,
                        notes = null,
                        isArchived = false,
                        createdAtEpochMillis = now,
                        updatedAtEpochMillis = now,
                    ),
                    Item(
                        id = 2,
                        name = "Soap",
                        categoryId = 2,
                        quantity = 5.0,
                        unit = UnitType.PIECE,
                        lowStockThreshold = 1.0,
                        notes = null,
                        isArchived = false,
                        createdAtEpochMillis = now,
                        updatedAtEpochMillis = now,
                    ),
                )
            categoryRepo.categories.value =
                listOf(
                    Category(1, "Grocery", false),
                    Category(2, "Bathroom", false),
                )

            val viewModel = InventoryViewModel(itemRepo, categoryRepo, SavedStateHandle())
            val job = launch { viewModel.uiState.collect { } }
            viewModel.onSearchChange("milk")
            viewModel.onLowStockOnlyChange(true)
            advanceUntilIdle()

            val success = viewModel.uiState.value as InventoryUiState.Success
            assertEquals(1, success.visibleItems.size)
            assertEquals("Milk", success.visibleItems.first().name)
            assertTrue(success.showLowStockOnly)
            job.cancel()
        }

    @Test
    fun saveItem_addsNewItemAndReturnsToInventory() =
        runTest {
            val itemRepo = FakeItemRepository()
            val categoryRepo = FakeCategoryRepository()
            categoryRepo.categories.value = listOf(Category(1, "Grocery", false))
            val viewModel = InventoryViewModel(itemRepo, categoryRepo, SavedStateHandle())

            viewModel.openAddItem()
            viewModel.saveItem(
                ItemDraft(
                    id = null,
                    name = "Rice",
                    categoryId = 1,
                    quantity = 2.0,
                    unit = UnitType.KILOGRAM,
                    lowStockThreshold = 1.0,
                    notes = null,
                ),
            )
            advanceUntilIdle()

            assertEquals(1, itemRepo.items.value.size)
            assertEquals("Rice", itemRepo.items.value.first().name)
            assertEquals(
                com.shelfcount.app.presentation.navigation.AppDestination.INVENTORY,
                viewModel.destination.value,
            )
        }

    @Test
    fun categories_orderPlacesOtherLastInUiState() =
        runTest {
            val itemRepo = FakeItemRepository()
            val categoryRepo = FakeCategoryRepository()
            categoryRepo.categories.value =
                listOf(
                    Category(1, "Other", false),
                    Category(2, "Spices", false),
                    Category(3, "Bathroom", false),
                )
            val viewModel = InventoryViewModel(itemRepo, categoryRepo, SavedStateHandle())
            val job = launch { viewModel.uiState.collect { } }
            advanceUntilIdle()

            val success = viewModel.uiState.value as InventoryUiState.Success
            assertEquals(
                listOf("Bathroom", "Spices", "Other"),
                success.categories.map { it.name },
            )
            job.cancel()
        }

    @Test
    fun deleteItem_removesItemFromRepository() =
        runTest {
            val itemRepo = FakeItemRepository()
            val categoryRepo = FakeCategoryRepository()
            val now = System.currentTimeMillis()
            itemRepo.items.value =
                listOf(
                    Item(
                        id = 1,
                        name = "Milk",
                        categoryId = 1,
                        quantity = 1.0,
                        unit = UnitType.LITER,
                        lowStockThreshold = 1.0,
                        notes = null,
                        isArchived = false,
                        createdAtEpochMillis = now,
                        updatedAtEpochMillis = now,
                    ),
                )
            val viewModel = InventoryViewModel(itemRepo, categoryRepo, SavedStateHandle())

            viewModel.deleteItem(1L)
            advanceUntilIdle()

            assertTrue(itemRepo.items.value.isEmpty())
        }
}

private class FakeItemRepository : ItemRepository {
    val items = MutableStateFlow<List<Item>>(emptyList())

    override fun observeActiveItems(): Flow<List<Item>> = items

    override fun observeLowStockItems(): Flow<List<Item>> =
        flowOf(
            items.value.filter {
                it.quantity <= it.lowStockThreshold
            },
        )

    override fun observeItemById(itemId: Long): Flow<Item?> = flowOf(items.value.firstOrNull { it.id == itemId })

    override suspend fun addItem(item: Item): Long {
        val newId = (items.value.maxOfOrNull { it.id } ?: 0L) + 1L
        items.value = items.value + item.copy(id = newId)
        return newId
    }

    override suspend fun updateItem(item: Item) {
        items.value = items.value.map { if (it.id == item.id) item else it }
    }

    override suspend fun deleteItem(itemId: Long) {
        items.value = items.value.filterNot { it.id == itemId }
    }

    override suspend fun setArchived(
        itemId: Long,
        archived: Boolean,
        updatedAtEpochMillis: Long,
    ) {
        items.value =
            items.value.map {
                if (it.id == itemId) {
                    it.copy(isArchived = archived, updatedAtEpochMillis = updatedAtEpochMillis)
                } else {
                    it
                }
            }
    }

    override suspend fun adjustQuantity(
        itemId: Long,
        delta: Double,
        updatedAtEpochMillis: Long,
    ) {
        items.value =
            items.value.map {
                if (it.id == itemId) {
                    it.copy(
                        quantity = (it.quantity + delta).coerceAtLeast(0.0),
                        updatedAtEpochMillis = updatedAtEpochMillis,
                    )
                } else {
                    it
                }
            }
    }

    override suspend fun searchItems(query: String): List<Item> =
        items.value.filter { it.name.contains(query, ignoreCase = true) }

    override suspend fun getItemsByCategory(categoryId: Int): List<Item> =
        items.value.filter { it.categoryId == categoryId }

    override suspend fun getItemsSortedByName(): List<Item> = items.value.sortedBy { it.name.lowercase() }

    override suspend fun getItemsSortedByRecentlyUpdated(): List<Item> =
        items.value.sortedByDescending {
            it.updatedAtEpochMillis
        }

    override suspend fun getLowStockItemsSortedByName(): List<Item> =
        items.value
            .filter { it.quantity <= it.lowStockThreshold }
            .sortedBy { it.name.lowercase() }
}

private class FakeCategoryRepository : CategoryRepository {
    val categories = MutableStateFlow<List<Category>>(emptyList())

    override fun observeCategories(): Flow<List<Category>> = categories

    override suspend fun seedDefaultCategories(categories: List<Category>) {
        if (this.categories.value.isEmpty()) this.categories.value = categories
    }

    override suspend fun createCustomCategory(name: String): Long {
        val normalized = name.trim()
        require(normalized.isNotBlank()) { "Category name cannot be blank" }
        require(
            categories.value.none { it.name.equals(normalized, ignoreCase = true) },
        ) { "Category already exists" }
        val id = (categories.value.maxOfOrNull { it.id } ?: 0) + 1
        categories.value = categories.value + Category(id, normalized, true)
        return id.toLong()
    }

    override suspend fun upsertCategory(category: Category): Long {
        val existing = categories.value.firstOrNull { it.id == category.id }
        categories.value =
            if (existing == null) {
                categories.value + category
            } else {
                categories.value.map { if (it.id == category.id) category else it }
            }
        return category.id.toLong()
    }

    override suspend fun deleteCategory(categoryId: Int) {
        categories.value = categories.value.filterNot { it.id == categoryId }
    }

    override suspend fun existsByName(name: String): Boolean =
        categories.value.any { it.name.equals(name, ignoreCase = true) }
}

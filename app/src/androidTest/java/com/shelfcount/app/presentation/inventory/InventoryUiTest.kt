package com.shelfcount.app.presentation.inventory

import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import com.shelfcount.app.domain.model.Category
import com.shelfcount.app.domain.model.Item
import com.shelfcount.app.domain.model.UnitType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class InventoryUiTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun addItemForm_submitsDraft() {
        var savedDraft: ItemDraft? = null
        val categories = listOf(Category(1, "Grocery", false))

        composeRule.setContent {
            AddEditItemScreen(
                categories = categories,
                initialItem = null,
                preferredCategoryId = null,
                externalError = null,
                onBack = {},
                onSave = { savedDraft = it },
            )
        }

        composeRule.onNodeWithText("Item Name").performTextInput("Rice")
        composeRule.onNodeWithText("Quantity").performTextClearance()
        composeRule.onNodeWithText("Quantity").performTextInput("2")
        composeRule.onNodeWithText("Low Stock Threshold").performTextClearance()
        composeRule.onNodeWithText("Low Stock Threshold").performTextInput("1")
        composeRule.onNodeWithContentDescription("Save item").performClick()

        assertEquals("Rice", savedDraft?.name)
        assertEquals(2.0, savedDraft?.quantity ?: 0.0, 0.0)
    }

    @Test
    fun inventoryList_showsLowStock_andSupportsOneTapQuantityUpdate() {
        val categories = listOf(Category(1, "Grocery", false))
        val now = System.currentTimeMillis()
        var latestQuantity = 1.0

        composeRule.setContent {
            var items by mutableStateOf(
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
                ),
            )
            InventoryScreen(
                items = items,
                categories = categories,
                searchQuery = "",
                selectedCategoryId = null,
                showLowStockOnly = false,
                sortOption = SortOption.RECENT,
                onSearchChange = {},
                onCategoryFilterChange = {},
                onLowStockFilterToggle = {},
                onSortChange = {},
                onIncrement = { id ->
                    items = items.map { if (it.id == id) it.copy(quantity = it.quantity + 1) else it }
                    latestQuantity = items.first().quantity
                },
                onDecrement = { id ->
                    items =
                        items.map {
                            if (it.id == id) {
                                it.copy(quantity = (it.quantity - 1).coerceAtLeast(0.0))
                            } else {
                                it
                            }
                        }
                    latestQuantity = items.first().quantity
                },
                onDelete = { id -> items = items.filterNot { it.id == id } },
                onItemClick = {},
                onAddItemClick = {},
                onCategoryManagementClick = {},
            )
        }

        composeRule.onAllNodesWithText("1.0 liter").assertCountEquals(1)
        composeRule.onAllNodesWithText("Low").assertCountEquals(1)

        // One tap from list should update quantity.
        composeRule.onNodeWithContentDescription("Increase quantity").performClick()
        composeRule.waitForIdle()
        assertEquals(2.0, latestQuantity, 0.0)
    }

    @Test
    fun sortIconMenu_changesSortSelection() {
        val categories = listOf(Category(1, "Grocery", false))
        val now = System.currentTimeMillis()
        var selectedSort = SortOption.RECENT

        composeRule.setContent {
            InventoryScreen(
                items =
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
                    ),
                categories = categories,
                searchQuery = "",
                selectedCategoryId = null,
                showLowStockOnly = false,
                sortOption = selectedSort,
                onSearchChange = {},
                onCategoryFilterChange = {},
                onLowStockFilterToggle = {},
                onSortChange = { selectedSort = it },
                onIncrement = {},
                onDecrement = {},
                onDelete = {},
                onItemClick = {},
                onAddItemClick = {},
                onCategoryManagementClick = {},
            )
        }

        composeRule.onNodeWithContentDescription("Sort and filter options").performClick()
        composeRule.onNodeWithText("Name").performClick()
        assertEquals(SortOption.NAME, selectedSort)
    }

    @Test
    fun categoryFilterRow_placesOtherAtEnd() {
        composeRule.setContent {
            CategoryFilterRow(
                categories =
                    listOf(
                        Category(1, "Other", false),
                        Category(2, "Spices", false),
                        Category(3, "Bathroom", false),
                    ),
                selectedCategoryId = null,
                onCategoryFilterChange = {},
            )
        }

        val bathroomX = composeRule.onNodeWithText("Bathroom").fetchSemanticsNode().positionInRoot.x
        val spicesX = composeRule.onNodeWithText("Spices").fetchSemanticsNode().positionInRoot.x
        val otherX = composeRule.onNodeWithText("Other").fetchSemanticsNode().positionInRoot.x

        assertTrue(bathroomX < spicesX)
        assertTrue(spicesX < otherX)
    }

    @Test
    fun inventoryItemCard_showsCategoryTag() {
        val now = System.currentTimeMillis()
        composeRule.setContent {
            InventoryScreen(
                items =
                    listOf(
                        Item(
                            id = 1,
                            name = "Detergent",
                            categoryId = 2,
                            quantity = 1.0,
                            unit = UnitType.PACK,
                            lowStockThreshold = 1.0,
                            notes = null,
                            isArchived = false,
                            createdAtEpochMillis = now,
                            updatedAtEpochMillis = now,
                        ),
                    ),
                categories =
                    listOf(
                        Category(1, "Grocery", false),
                        Category(2, "Laundry", false),
                    ),
                searchQuery = "",
                selectedCategoryId = null,
                showLowStockOnly = false,
                sortOption = SortOption.RECENT,
                onSearchChange = {},
                onCategoryFilterChange = {},
                onLowStockFilterToggle = {},
                onSortChange = {},
                onIncrement = {},
                onDecrement = {},
                onDelete = {},
                onItemClick = {},
                onAddItemClick = {},
                onCategoryManagementClick = {},
            )
        }

        composeRule.onNodeWithContentDescription("Category tag: Laundry").performClick()
    }

    @Test
    fun deleteIcon_removesItemFromList() {
        val categories = listOf(Category(1, "Grocery", false))
        val now = System.currentTimeMillis()
        var deletedId: Long? = null
        var itemCount = 1

        composeRule.setContent {
            var items by mutableStateOf(
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
                ),
            )
            InventoryScreen(
                items = items,
                categories = categories,
                searchQuery = "",
                selectedCategoryId = null,
                showLowStockOnly = false,
                sortOption = SortOption.RECENT,
                onSearchChange = {},
                onCategoryFilterChange = {},
                onLowStockFilterToggle = {},
                onSortChange = {},
                onIncrement = {},
                onDecrement = {},
                onDelete = { id ->
                    deletedId = id
                    items = items.filterNot { it.id == id }
                    itemCount = items.size
                },
                onItemClick = {},
                onAddItemClick = {},
                onCategoryManagementClick = {},
            )
        }

        composeRule.onAllNodesWithText("Milk").assertCountEquals(1)
        composeRule.onNodeWithTag("delete_item_1").performClick()
        composeRule.waitForIdle()
        assertEquals(1L, deletedId)
        assertEquals(0, itemCount)
    }

    @Test
    fun addItemForm_defaultsToPreferredCategory() {
        var savedDraft: ItemDraft? = null
        val categories =
            listOf(
                Category(1, "Bathroom", false),
                Category(2, "Spices", false),
            )

        composeRule.setContent {
            AddEditItemScreen(
                categories = categories,
                initialItem = null,
                preferredCategoryId = 2,
                externalError = null,
                onBack = {},
                onSave = { savedDraft = it },
            )
        }

        composeRule.onNodeWithText("Item Name").performTextInput("Turmeric")
        composeRule.onNodeWithContentDescription("Save item").performClick()
        assertEquals(2, savedDraft?.categoryId)
    }
}

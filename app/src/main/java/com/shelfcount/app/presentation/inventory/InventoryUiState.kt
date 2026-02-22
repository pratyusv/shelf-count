package com.shelfcount.app.presentation.inventory

import com.shelfcount.app.domain.model.Category
import com.shelfcount.app.domain.model.Item

sealed interface InventoryUiState {
    data object Loading : InventoryUiState

    data class Success(
        val allItems: List<Item>,
        val visibleItems: List<Item>,
        val categories: List<Category>,
        val searchQuery: String,
        val selectedCategoryId: Int?,
        val showLowStockOnly: Boolean,
        val sortOption: SortOption,
    ) : InventoryUiState

    data class Error(val message: String) : InventoryUiState
}

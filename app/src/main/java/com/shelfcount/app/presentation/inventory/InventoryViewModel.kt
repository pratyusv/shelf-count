package com.shelfcount.app.presentation.inventory

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shelfcount.app.domain.model.Item
import com.shelfcount.app.domain.model.sortedForDisplay
import com.shelfcount.app.domain.repository.CategoryRepository
import com.shelfcount.app.domain.repository.ItemRepository
import com.shelfcount.app.presentation.navigation.AppDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val KEY_SEARCH_QUERY = "search_query"
private const val KEY_SELECTED_CATEGORY_ID = "selected_category_id"
private const val KEY_LOW_STOCK_ONLY = "low_stock_only"
private const val KEY_SORT_OPTION = "sort_option"

private data class InventoryFilters(
    val searchQuery: String,
    val selectedCategoryId: Int?,
    val showLowStockOnly: Boolean,
    val sortOption: SortOption,
)

@HiltViewModel
class InventoryViewModel
    @Inject
    constructor(
        private val itemRepository: ItemRepository,
        private val categoryRepository: CategoryRepository,
        private val savedStateHandle: SavedStateHandle,
    ) : ViewModel() {
        private val _destination = MutableStateFlow(AppDestination.INVENTORY)
        val destination: StateFlow<AppDestination> = _destination.asStateFlow()

        private val _editingItemId = MutableStateFlow<Long?>(null)
        val editingItemId: StateFlow<Long?> = _editingItemId.asStateFlow()

        private val _formError = MutableStateFlow<String?>(null)
        val formError: StateFlow<String?> = _formError.asStateFlow()

        private val _categoryError = MutableStateFlow<String?>(null)
        val categoryError: StateFlow<String?> = _categoryError.asStateFlow()

        private var latestItems: List<Item> = emptyList()

        private val searchQueryFlow = savedStateHandle.getStateFlow(KEY_SEARCH_QUERY, "")
        private val selectedCategoryIdFlow = savedStateHandle.getStateFlow<Int?>(KEY_SELECTED_CATEGORY_ID, null)
        private val showLowStockOnlyFlow = savedStateHandle.getStateFlow(KEY_LOW_STOCK_ONLY, false)
        private val sortOptionFlow = savedStateHandle.getStateFlow(KEY_SORT_OPTION, SortOption.RECENT.name)

        private val filtersFlow =
            combine(
                searchQueryFlow,
                selectedCategoryIdFlow,
                showLowStockOnlyFlow,
                sortOptionFlow,
            ) { search, selectedCategoryId, lowStockOnly, sortName ->
                InventoryFilters(
                    searchQuery = search,
                    selectedCategoryId = selectedCategoryId,
                    showLowStockOnly = lowStockOnly,
                    sortOption = SortOption.entries.firstOrNull { it.name == sortName } ?: SortOption.RECENT,
                )
            }

        val uiState: StateFlow<InventoryUiState> =
            combine(
                itemRepository.observeActiveItems(),
                categoryRepository.observeCategories(),
                filtersFlow,
            ) { items, categories, filters ->
                latestItems = items
                val visibleItems =
                    items
                        .filter { item ->
                            val matchesSearch =
                                filters.searchQuery.isBlank() ||
                                    item.name.contains(filters.searchQuery.trim(), ignoreCase = true)
                            val matchesCategory = filters.selectedCategoryId == null || item.categoryId == filters.selectedCategoryId
                            val matchesLowStock = !filters.showLowStockOnly || item.quantity <= item.lowStockThreshold
                            matchesSearch && matchesCategory && !item.isArchived && matchesLowStock
                        }
                        .let { filtered ->
                            when (filters.sortOption) {
                                SortOption.NAME -> filtered.sortedBy { it.name.lowercase() }
                                SortOption.RECENT -> filtered.sortedByDescending { it.updatedAtEpochMillis }
                                SortOption.LOW_STOCK_FIRST ->
                                    filtered.sortedWith(
                                        compareBy<Item> { if (it.quantity <= it.lowStockThreshold) 0 else 1 }
                                            .thenBy { it.name.lowercase() },
                                    )
                            }
                        }

                InventoryUiState.Success(
                    allItems = items,
                    visibleItems = visibleItems,
                    categories = categories.sortedForDisplay(),
                    searchQuery = filters.searchQuery,
                    selectedCategoryId = filters.selectedCategoryId,
                    showLowStockOnly = filters.showLowStockOnly,
                    sortOption = filters.sortOption,
                )
            }
                .map<InventoryUiState.Success, InventoryUiState> { it }
                .catch { emit(InventoryUiState.Error(it.message ?: "Unable to load inventory")) }
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5_000),
                    initialValue = InventoryUiState.Loading,
                )

        fun onSearchChange(value: String) {
            savedStateHandle[KEY_SEARCH_QUERY] = value
        }

        fun onCategoryFilterChange(value: Int?) {
            savedStateHandle[KEY_SELECTED_CATEGORY_ID] = value
        }

        fun onLowStockOnlyChange(value: Boolean) {
            savedStateHandle[KEY_LOW_STOCK_ONLY] = value
        }

        fun onSortChange(value: SortOption) {
            savedStateHandle[KEY_SORT_OPTION] = value.name
        }

        fun increment(itemId: Long) {
            viewModelScope.launch {
                itemRepository.adjustQuantity(
                    itemId = itemId,
                    delta = 1.0,
                    updatedAtEpochMillis = System.currentTimeMillis(),
                )
            }
        }

        fun decrement(itemId: Long) {
            viewModelScope.launch {
                itemRepository.adjustQuantity(
                    itemId = itemId,
                    delta = -1.0,
                    updatedAtEpochMillis = System.currentTimeMillis(),
                )
            }
        }

        fun deleteItem(itemId: Long) {
            viewModelScope.launch {
                itemRepository.deleteItem(itemId)
            }
        }

        fun openAddItem() {
            _editingItemId.value = null
            _formError.value = null
            _destination.value = AppDestination.ADD_EDIT_ITEM
        }

        fun openEditItem(itemId: Long) {
            _editingItemId.value = itemId
            _formError.value = null
            _destination.value = AppDestination.ADD_EDIT_ITEM
        }

        fun openCategoryManagement() {
            _categoryError.value = null
            _destination.value = AppDestination.CATEGORY_MANAGEMENT
        }

        fun backToInventory() {
            _destination.value = AppDestination.INVENTORY
        }

        fun saveItem(draft: ItemDraft) {
            viewModelScope.launch {
                val now = System.currentTimeMillis()
                runCatching {
                    if (draft.id == null) {
                        itemRepository.addItem(
                            Item(
                                id = 0,
                                name = draft.name,
                                categoryId = draft.categoryId,
                                quantity = draft.quantity,
                                unit = draft.unit,
                                lowStockThreshold = draft.lowStockThreshold,
                                notes = draft.notes,
                                isArchived = false,
                                createdAtEpochMillis = now,
                                updatedAtEpochMillis = now,
                            ),
                        )
                    } else {
                        val current =
                            latestItems.firstOrNull { it.id == draft.id }
                                ?: throw IllegalArgumentException("Item not found.")
                        itemRepository.updateItem(
                            current.copy(
                                name = draft.name,
                                categoryId = draft.categoryId,
                                quantity = draft.quantity,
                                unit = draft.unit,
                                lowStockThreshold = draft.lowStockThreshold,
                                notes = draft.notes,
                                updatedAtEpochMillis = now,
                            ),
                        )
                    }
                }.onSuccess {
                    _formError.value = null
                    _destination.value = AppDestination.INVENTORY
                }.onFailure {
                    _formError.value = it.message ?: "Unable to save item."
                }
            }
        }

        fun createCategory(name: String) {
            viewModelScope.launch {
                runCatching {
                    categoryRepository.createCustomCategory(name)
                }.onSuccess {
                    _categoryError.value = null
                }.onFailure {
                    _categoryError.value = it.message ?: "Unable to add category."
                }
            }
        }
    }

package com.shelfcount.app

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.view.doOnPreDraw
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shelfcount.app.core.common.AppStartTracker
import com.shelfcount.app.presentation.category.CategoryManagementScreen
import com.shelfcount.app.presentation.inventory.AddEditItemScreen
import com.shelfcount.app.presentation.inventory.InventoryScreen
import com.shelfcount.app.presentation.inventory.InventoryUiState
import com.shelfcount.app.presentation.inventory.InventoryViewModel
import com.shelfcount.app.presentation.navigation.AppDestination
import com.shelfcount.app.ui.theme.ShelfCountTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private var didReportColdStart = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ShelfCountTheme {
                ShelfCountApp()
            }
        }
        window.decorView.doOnPreDraw {
            if (!didReportColdStart) {
                didReportColdStart = true
                val durationMs = AppStartTracker.coldStartDurationMs()
                if (durationMs > 2_000) {
                    Log.w("ShelfCountStartup", "Cold start above target: ${durationMs}ms")
                } else {
                    Log.i("ShelfCountStartup", "Cold start within target: ${durationMs}ms")
                }
                reportFullyDrawn()
            }
        }
    }
}

@Composable
private fun ShelfCountApp(viewModel: InventoryViewModel = hiltViewModel()) {
    val destination = viewModel.destination.collectAsStateWithLifecycle().value
    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value
    val editingItemId = viewModel.editingItemId.collectAsStateWithLifecycle().value
    val formError = viewModel.formError.collectAsStateWithLifecycle().value
    val categoryError = viewModel.categoryError.collectAsStateWithLifecycle().value

    when (uiState) {
        InventoryUiState.Loading -> {
            Surface {
                Text("Loading inventory...")
            }
        }

        is InventoryUiState.Error -> {
            Surface {
                Text(uiState.message)
            }
        }

        is InventoryUiState.Success -> {
            val editingItem = uiState.allItems.firstOrNull { it.id == editingItemId }
            when (destination) {
                AppDestination.INVENTORY -> {
                    InventoryScreen(
                        items = uiState.visibleItems,
                        categories = uiState.categories,
                        searchQuery = uiState.searchQuery,
                        selectedCategoryId = uiState.selectedCategoryId,
                        showLowStockOnly = uiState.showLowStockOnly,
                        sortOption = uiState.sortOption,
                        onSearchChange = viewModel::onSearchChange,
                        onCategoryFilterChange = viewModel::onCategoryFilterChange,
                        onLowStockFilterToggle = viewModel::onLowStockOnlyChange,
                        onSortChange = viewModel::onSortChange,
                        onIncrement = viewModel::increment,
                        onDecrement = viewModel::decrement,
                        onDelete = viewModel::deleteItem,
                        onItemClick = viewModel::openEditItem,
                        onAddItemClick = viewModel::openAddItem,
                        onCategoryManagementClick = viewModel::openCategoryManagement,
                    )
                }

                AppDestination.ADD_EDIT_ITEM -> {
                    AddEditItemScreen(
                        categories = uiState.categories,
                        initialItem = editingItem,
                        preferredCategoryId = uiState.selectedCategoryId,
                        externalError = formError,
                        onBack = viewModel::backToInventory,
                        onSave = viewModel::saveItem,
                    )
                }

                AppDestination.CATEGORY_MANAGEMENT -> {
                    CategoryManagementScreen(
                        categories = uiState.categories,
                        errorMessage = categoryError,
                        onBack = viewModel::backToInventory,
                        onCreateCategory = viewModel::createCategory,
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ShelfCountAppPreview() {
    ShelfCountTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Text("ShelfCount preview")
        }
    }
}

package com.shelfcount.app.presentation.inventory

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Sort
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.shelfcount.app.domain.model.Category
import com.shelfcount.app.domain.model.Item
import com.shelfcount.app.ui.theme.AppSpacing

enum class SortOption(val label: String) {
    NAME("Name"),
    RECENT("Recently Updated"),
    LOW_STOCK_FIRST("Low Stock First"),
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(
    items: List<Item>,
    categories: List<Category>,
    searchQuery: String,
    selectedCategoryId: Int?,
    showLowStockOnly: Boolean,
    sortOption: SortOption,
    onSearchChange: (String) -> Unit,
    onCategoryFilterChange: (Int?) -> Unit,
    onLowStockFilterToggle: (Boolean) -> Unit,
    onSortChange: (SortOption) -> Unit,
    onIncrement: (Long) -> Unit,
    onDecrement: (Long) -> Unit,
    onDelete: (Long) -> Unit,
    onItemClick: (Long) -> Unit,
    onAddItemClick: () -> Unit,
    onCategoryManagementClick: () -> Unit,
) {
    var sortExpanded by remember { mutableStateOf(false) }
    val categoryNamesById = categories.associate { it.id to it.name }

    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors =
                                listOf(
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.42f),
                                    MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.24f),
                                    MaterialTheme.colorScheme.background,
                                ),
                        ),
                    ),
        )

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text("ShelfCount", style = MaterialTheme.typography.headlineSmall)
                            Text(
                                text = "Clean tracking for daily essentials",
                                style = MaterialTheme.typography.labelSmall,
                            )
                        }
                    },
                    actions = {
                        TextButton(onClick = onCategoryManagementClick) {
                            Icon(Icons.Outlined.Category, contentDescription = null)
                            Text("Categories")
                        }
                    },
                )
            },
            floatingActionButton = {
                ExtendedFloatingActionButton(
                    onClick = onAddItemClick,
                    icon = { Icon(Icons.Outlined.Add, contentDescription = null) },
                    text = { Text("New Item") },
                )
            },
        ) { padding ->
            Column(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(horizontal = AppSpacing.lg, vertical = AppSpacing.md),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = AppSpacing.xs),
                ) {
                    Column(
                        modifier = Modifier.padding(AppSpacing.md),
                        verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                        ) {
                            Icon(Icons.Outlined.Inventory2, contentDescription = null)
                            Text(
                                text = "Inventory (${items.size})",
                                style = MaterialTheme.typography.titleMedium,
                            )
                        }
                        SearchField(query = searchQuery, onQueryChange = onSearchChange)
                        CategoryFilterRow(
                            categories = categories,
                            selectedCategoryId = selectedCategoryId,
                            onCategoryFilterChange = onCategoryFilterChange,
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            FilterChip(
                                selected = showLowStockOnly,
                                onClick = { onLowStockFilterToggle(!showLowStockOnly) },
                                label = { Text("Low stock only") },
                            )
                            Box {
                                IconButton(onClick = { sortExpanded = !sortExpanded }) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Outlined.Sort,
                                        contentDescription = "Sort options",
                                    )
                                }
                                DropdownMenu(
                                    expanded = sortExpanded,
                                    onDismissRequest = { sortExpanded = false },
                                ) {
                                    SortOption.entries.forEach { option ->
                                        DropdownMenuItem(
                                            text = {
                                                val label =
                                                    if (option == sortOption) {
                                                        "Selected: ${option.label}"
                                                    } else {
                                                        option.label
                                                    }
                                                Text(label)
                                            },
                                            onClick = {
                                                onSortChange(option)
                                                sortExpanded = false
                                            },
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                if (items.isEmpty()) {
                    EmptyStatePanel(
                        title = "No items yet",
                        subtitle = "Add your first household item to start tracking stock.",
                        actionLabel = "Add Item",
                        onActionClick = onAddItemClick,
                    )
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                        items(
                            items = items,
                            key = { it.id },
                            contentType = { "inventory_item" },
                        ) { item ->
                            InventoryItemCard(
                                item = item,
                                categoryName = categoryNamesById[item.categoryId] ?: "Uncategorized",
                                onIncrement = { onIncrement(item.id) },
                                onDecrement = { onDecrement(item.id) },
                                onDelete = { onDelete(item.id) },
                                onClick = { onItemClick(item.id) },
                            )
                        }
                    }
                }
            }
        }
    }
}

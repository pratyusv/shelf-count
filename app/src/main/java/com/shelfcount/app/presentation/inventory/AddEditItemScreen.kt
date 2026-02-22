package com.shelfcount.app.presentation.inventory

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Notes
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Scale
import androidx.compose.material.icons.outlined.Straighten
import androidx.compose.material.icons.outlined.Widgets
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.shelfcount.app.domain.model.Category
import com.shelfcount.app.domain.model.Item
import com.shelfcount.app.domain.model.UnitType
import com.shelfcount.app.domain.model.sortedForDisplay
import com.shelfcount.app.ui.theme.AppSpacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditItemScreen(
    categories: List<Category>,
    initialItem: Item?,
    preferredCategoryId: Int?,
    externalError: String?,
    onBack: () -> Unit,
    onSave: (ItemDraft) -> Unit,
) {
    val orderedCategories = categories.sortedForDisplay()
    var name by rememberSaveable(initialItem?.id) { mutableStateOf(initialItem?.name.orEmpty()) }
    var quantityText by rememberSaveable(initialItem?.id) { mutableStateOf(initialItem?.quantity?.toString() ?: "1") }
    var thresholdText by rememberSaveable(initialItem?.id) {
        mutableStateOf(initialItem?.lowStockThreshold?.toString() ?: "1")
    }
    var notes by rememberSaveable(initialItem?.id) { mutableStateOf(initialItem?.notes.orEmpty()) }
    var selectedUnit by rememberSaveable(initialItem?.id) { mutableStateOf(initialItem?.unit ?: UnitType.PIECE) }
    var selectedCategoryId by rememberSaveable(initialItem?.id) {
        val defaultCategoryId =
            preferredCategoryId
                ?.takeIf { preferredId -> orderedCategories.any { it.id == preferredId } }
                ?: orderedCategories.firstOrNull()?.id
                ?: 0
        mutableStateOf(initialItem?.categoryId ?: defaultCategoryId)
    }
    var localValidationError by remember { mutableStateOf<String?>(null) }

    var unitExpanded by remember { mutableStateOf(false) }
    var categoryExpanded by remember { mutableStateOf(false) }

    val isCreate = initialItem == null
    val screenTitle = if (isCreate) "Create item" else "Edit item"
    val screenSubtitle = if (isCreate) "Add stock details" else "Update existing entry"

    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors =
                                listOf(
                                    MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.26f),
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
                            Text(text = screenTitle, style = MaterialTheme.typography.headlineSmall)
                            Text(text = screenSubtitle, style = MaterialTheme.typography.labelSmall)
                        }
                    },
                    navigationIcon = {
                        FilledTonalButton(onClick = onBack) {
                            Text("Back")
                        }
                    },
                )
            },
        ) { padding ->
            Card(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(horizontal = AppSpacing.lg, vertical = AppSpacing.md),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = AppSpacing.xs),
            ) {
                Column(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(AppSpacing.lg)
                            .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Inventory2,
                            contentDescription = null,
                        )
                        Text(
                            text = if (isCreate) "New item details" else "Edit item details",
                            style = MaterialTheme.typography.titleMedium,
                        )
                    }

                    LabeledInput(
                        value = name,
                        onValueChange = { name = it },
                        label = "Item Name",
                        icon = { Icon(Icons.Outlined.Inventory2, contentDescription = null) },
                    )

                    ExposedDropdownMenuBox(
                        expanded = categoryExpanded,
                        onExpandedChange = { categoryExpanded = !categoryExpanded },
                    ) {
                        val categoryLabel =
                            orderedCategories.firstOrNull { it.id == selectedCategoryId }?.name
                                ?: "Select category"
                        OutlinedTextField(
                            value = categoryLabel,
                            onValueChange = {},
                            readOnly = true,
                            modifier =
                                Modifier
                                    .menuAnchor()
                                    .fillMaxWidth(),
                            label = { Text("Category") },
                            leadingIcon = { Icon(Icons.Outlined.Category, contentDescription = null) },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded)
                            },
                        )
                        DropdownMenu(
                            expanded = categoryExpanded,
                            onDismissRequest = { categoryExpanded = false },
                        ) {
                            orderedCategories.forEach { category ->
                                DropdownMenuItem(
                                    text = { Text(category.name) },
                                    onClick = {
                                        selectedCategoryId = category.id
                                        categoryExpanded = false
                                    },
                                )
                            }
                        }
                    }

                    LabeledInput(
                        value = quantityText,
                        onValueChange = { quantityText = it },
                        label = "Quantity",
                        icon = { Icon(Icons.Outlined.Scale, contentDescription = null) },
                    )

                    ExposedDropdownMenuBox(
                        expanded = unitExpanded,
                        onExpandedChange = { unitExpanded = !unitExpanded },
                    ) {
                        OutlinedTextField(
                            value = selectedUnit.name,
                            onValueChange = {},
                            readOnly = true,
                            modifier =
                                Modifier
                                    .menuAnchor()
                                    .fillMaxWidth(),
                            label = { Text("Unit") },
                            leadingIcon = { Icon(Icons.Outlined.Widgets, contentDescription = null) },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = unitExpanded)
                            },
                        )
                        DropdownMenu(
                            expanded = unitExpanded,
                            onDismissRequest = { unitExpanded = false },
                        ) {
                            UnitType.entries.forEach { unit ->
                                DropdownMenuItem(
                                    text = { Text(unit.name) },
                                    onClick = {
                                        selectedUnit = unit
                                        unitExpanded = false
                                    },
                                )
                            }
                        }
                    }

                    LabeledInput(
                        value = thresholdText,
                        onValueChange = { thresholdText = it },
                        label = "Low Stock Threshold",
                        icon = { Icon(Icons.Outlined.Straighten, contentDescription = null) },
                    )

                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Notes (optional)") },
                        leadingIcon = { Icon(Icons.AutoMirrored.Outlined.Notes, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                    )

                    val shownError = localValidationError ?: externalError
                    if (shownError != null) {
                        Text(
                            text = shownError,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }

                    Button(
                        onClick = {
                            val quantity = quantityText.toDoubleOrNull()
                            val threshold = thresholdText.toDoubleOrNull()
                            when {
                                name.isBlank() -> localValidationError = "Item name is required."
                                quantity == null || quantity < 0 -> {
                                    localValidationError = "Quantity must be a valid non-negative number."
                                }
                                threshold == null || threshold < 0 -> {
                                    localValidationError = "Threshold must be a valid non-negative number."
                                }
                                selectedCategoryId == 0 -> localValidationError = "Category is required."
                                else -> {
                                    localValidationError = null
                                    onSave(
                                        ItemDraft(
                                            id = initialItem?.id,
                                            name = name.trim(),
                                            categoryId = selectedCategoryId,
                                            quantity = quantity,
                                            unit = selectedUnit,
                                            lowStockThreshold = threshold,
                                            notes = notes.trim().ifBlank { null },
                                        ),
                                    )
                                }
                            }
                        },
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .semantics { contentDescription = "Save item" },
                    ) {
                        Text(if (isCreate) "Save Item" else "Save Changes")
                    }
                }
            }
        }
    }
}

@Composable
private fun LabeledInput(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: @Composable () -> Unit,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = icon,
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
    )
}

data class ItemDraft(
    val id: Long?,
    val name: String,
    val categoryId: Int,
    val quantity: Double,
    val unit: UnitType,
    val lowStockThreshold: Double,
    val notes: String?,
)

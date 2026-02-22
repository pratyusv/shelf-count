package com.shelfcount.app.core.model

import com.shelfcount.app.domain.model.Category

object DefaultCategories {
    val all: List<Category> =
        listOf(
            Category(id = 1, name = "Grocery", isCustom = false),
            Category(id = 2, name = "Bathroom", isCustom = false),
            Category(id = 3, name = "Laundry", isCustom = false),
            Category(id = 4, name = "Spices", isCustom = false),
            Category(id = 5, name = "Cleaning", isCustom = false),
            Category(id = 6, name = "Pantry", isCustom = false),
            Category(id = 7, name = "Other", isCustom = false),
        )
}

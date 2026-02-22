package com.shelfcount.app.domain.model

private const val OTHER_CATEGORY_NAME = "Other"

fun List<Category>.sortedForDisplay(): List<Category> =
    sortedWith(
        compareBy<Category> {
            if (it.name.equals(OTHER_CATEGORY_NAME, ignoreCase = true)) 1 else 0
        }.thenBy { it.name.lowercase() },
    )

package com.shelfcount.app.data.mapper

import com.shelfcount.app.data.local.entity.CategoryEntity
import com.shelfcount.app.domain.model.Category

fun CategoryEntity.toDomain(): Category =
    Category(
        id = id,
        name = name,
        isCustom = isCustom,
    )

fun Category.toEntity(): CategoryEntity =
    CategoryEntity(
        id = id,
        name = name,
        isCustom = isCustom,
    )

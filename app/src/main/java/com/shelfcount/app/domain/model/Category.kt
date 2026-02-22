package com.shelfcount.app.domain.model

import androidx.compose.runtime.Immutable

@Immutable
data class Category(
    val id: Int,
    val name: String,
    val isCustom: Boolean,
)

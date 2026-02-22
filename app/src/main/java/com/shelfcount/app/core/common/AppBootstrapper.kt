package com.shelfcount.app.core.common

import com.shelfcount.app.core.model.DefaultCategories
import com.shelfcount.app.domain.repository.CategoryRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppBootstrapper
    @Inject
    constructor(
        private val categoryRepository: CategoryRepository,
    ) {
        suspend fun run() {
            categoryRepository.seedDefaultCategories(DefaultCategories.all)
        }
    }

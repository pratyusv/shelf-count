package com.shelfcount.app.data.local.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.shelfcount.app.data.local.ShelfCountDatabase
import com.shelfcount.app.data.local.entity.CategoryEntity
import com.shelfcount.app.data.local.entity.ItemEntity
import com.shelfcount.app.domain.model.UnitType
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ItemDaoTest {
    private lateinit var database: ShelfCountDatabase
    private lateinit var categoryDao: CategoryDao
    private lateinit var itemDao: ItemDao

    @Before
    fun setup() {
        runBlocking {
            database =
                Room.inMemoryDatabaseBuilder(
                    ApplicationProvider.getApplicationContext(),
                    ShelfCountDatabase::class.java,
                ).allowMainThreadQueries().build()
            categoryDao = database.categoryDao()
            itemDao = database.itemDao()
            categoryDao.upsert(CategoryEntity(id = 1, name = "Grocery", isCustom = false))
            categoryDao.upsert(CategoryEntity(id = 2, name = "Bathroom", isCustom = false))
        }
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun crudAndSearchFilterQueries_workAsExpected() {
        runBlocking {
            val milkId =
                itemDao.insert(
                    ItemEntity(
                        name = "Milk",
                        categoryId = 1,
                        quantity = 1.0,
                        unit = UnitType.LITER,
                        lowStockThreshold = 2.0,
                        notes = null,
                        isArchived = false,
                        createdAtEpochMillis = 1L,
                        updatedAtEpochMillis = 1L,
                    ),
                )
            val soapId =
                itemDao.insert(
                    ItemEntity(
                        name = "Soap",
                        categoryId = 2,
                        quantity = 5.0,
                        unit = UnitType.PIECE,
                        lowStockThreshold = 1.0,
                        notes = null,
                        isArchived = false,
                        createdAtEpochMillis = 2L,
                        updatedAtEpochMillis = 2L,
                    ),
                )

            val searchResult = itemDao.searchByName("Mil")
            assertEquals(1, searchResult.size)
            assertEquals("Milk", searchResult.first().name)

            val groceryItems = itemDao.getByCategory(1)
            assertEquals(1, groceryItems.size)
            assertEquals(milkId, groceryItems.first().id)

            val lowStock = itemDao.getLowStockByName()
            assertEquals(1, lowStock.size)
            assertEquals(milkId, lowStock.first().id)

            itemDao.adjustQuantity(milkId, -1.0, 3L)
            val updatedMilk = itemDao.searchByName("Milk").first()
            assertEquals(0.0, updatedMilk.quantity, 0.0)

            itemDao.setArchived(soapId, archived = true, updatedAt = 4L)
            val activeByName = itemDao.getAllByName()
            assertTrue(activeByName.none { it.id == soapId })
        }
    }
}

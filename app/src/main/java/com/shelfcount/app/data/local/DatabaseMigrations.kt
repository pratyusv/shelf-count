package com.shelfcount.app.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.shelfcount.app.core.common.normalizeItemNameKey

object DatabaseMigrations {
    val migration1To2 =
        object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE items ADD COLUMN normalized_name TEXT NOT NULL DEFAULT ''",
                )

                val keepers = mutableMapOf<String, ItemKeeper>()
                db.query(
                    "SELECT id, category_id, name, quantity, unit FROM items ORDER BY category_id ASC, id ASC",
                ).use { cursor ->
                    val idIndex = cursor.getColumnIndexOrThrow("id")
                    val categoryIdIndex = cursor.getColumnIndexOrThrow("category_id")
                    val nameIndex = cursor.getColumnIndexOrThrow("name")
                    val quantityIndex = cursor.getColumnIndexOrThrow("quantity")
                    val unitIndex = cursor.getColumnIndexOrThrow("unit")

                    while (cursor.moveToNext()) {
                        val id = cursor.getLong(idIndex)
                        val categoryId = cursor.getInt(categoryIdIndex)
                        val name = cursor.getString(nameIndex)
                        val quantity = cursor.getDouble(quantityIndex)
                        val unit = cursor.getString(unitIndex)
                        val normalizedName = normalizeItemNameKey(name).ifBlank { "item-$id" }
                        val key = "$categoryId|$normalizedName"
                        val existingKeeper = keepers[key]

                        if (existingKeeper == null) {
                            db.execSQL(
                                "UPDATE items SET normalized_name = ? WHERE id = ?",
                                arrayOf<Any>(normalizedName, id),
                            )
                            keepers[key] = ItemKeeper(id = id, unit = unit)
                        } else {
                            if (existingKeeper.unit == unit) {
                                db.execSQL(
                                    "UPDATE items SET quantity = quantity + ? WHERE id = ?",
                                    arrayOf<Any>(quantity, existingKeeper.id),
                                )
                            }
                            db.execSQL("DELETE FROM items WHERE id = ?", arrayOf(id))
                        }
                    }
                }

                db.execSQL(
                    "CREATE UNIQUE INDEX IF NOT EXISTS index_items_normalized_name_category_id " +
                        "ON items(normalized_name, category_id)",
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_items_name_category_id " +
                        "ON items(name, category_id)",
                )
            }
        }

    private data class ItemKeeper(
        val id: Long,
        val unit: String,
    )

    // Keep this list as the single migration source for Room builder wiring.
    val all: Array<Migration> = arrayOf(migration1To2)
}

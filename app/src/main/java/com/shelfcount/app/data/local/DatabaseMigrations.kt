package com.shelfcount.app.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object DatabaseMigrations {
    // Keep this list as the single migration source for Room builder wiring.
    val all: Array<Migration> = emptyArray()

    // Example placeholder for future schema changes once DB version is bumped.
    @Suppress("unused")
    val migration1To2 =
        object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Add ALTER TABLE or data migration statements here.
            }
        }
}

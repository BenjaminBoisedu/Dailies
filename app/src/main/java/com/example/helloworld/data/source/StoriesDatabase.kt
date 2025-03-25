package com.example.helloworld.data.source

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.helloworld.domain.model.Story

@Database(entities = [Story::class], version = 4)
abstract class StoriesDatabase : RoomDatabase() {
    abstract val dao: StoriesDao

    companion object {
        const val DATABASE_NAME = "stories.db"

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                val cursor = db.query("PRAGMA table_info(stories)")
                val columnNames = mutableListOf<String>()
                while (cursor.moveToNext()) {
                    columnNames.add(cursor.getString(1))
                }
                cursor.close()

                if (!columnNames.contains("priority")) {
                    db.execSQL("ALTER TABLE stories ADD COLUMN priority INTEGER NOT NULL DEFAULT 0")
                }
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE stories ADD COLUMN latitude REAL")
                db.execSQL("ALTER TABLE stories ADD COLUMN longitude REAL")
                db.execSQL("ALTER TABLE stories ADD COLUMN locationName TEXT")
                db.execSQL("ALTER TABLE stories ADD COLUMN isRecurring INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE stories ADD COLUMN recurringType TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE stories ADD COLUMN recurringInterval INTEGER NOT NULL DEFAULT 0")
            }
        }
    }
}
package com.example.helloworld.data.source

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.helloworld.domain.model.Story

@Database(entities = [Story::class], version = 2)
abstract class StoriesDatabase : RoomDatabase() {
    abstract val dao: StoriesDao

    companion object {
        const val DATABASE_NAME = "stories.db"

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Remove nested migrate function - this was causing the issue
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
    }
}

package com.example.helloworld.data.source

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.helloworld.domain.model.Story

@Database(entities = [Story::class], version = 5, exportSchema = false)
abstract class StoriesDatabase : RoomDatabase() {
    abstract val dao: StoriesDao

    companion object {
        const val DATABASE_NAME = "stories.db"

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE stories ADD COLUMN priority INTEGER NOT NULL DEFAULT 0")
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE stories ADD COLUMN latitude REAL")
                db.execSQL("ALTER TABLE stories ADD COLUMN longitude REAL")
                db.execSQL("ALTER TABLE stories ADD COLUMN locationName TEXT")
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE stories ADD COLUMN recurringType TEXT")
                db.execSQL("ALTER TABLE stories ADD COLUMN recurringInterval INTEGER")
            }
        }

        val MIGRATION_3_5 = object : Migration(3, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Create a temporary table with the correct schema
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS stories_new (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        title TEXT NOT NULL,
                        description TEXT NOT NULL,
                        date TEXT NOT NULL,
                        time TEXT NOT NULL,
                        done INTEGER NOT NULL,
                        priority INTEGER NOT NULL,
                        latitude REAL,
                        longitude REAL,
                        locationName TEXT,
                        recurringType TEXT NOT NULL DEFAULT '',
                        recurringInterval INTEGER NOT NULL DEFAULT 0,
                        isRecurring INTEGER NOT NULL DEFAULT 0
                    )
                """)

                // Copy data from old table to new table
                db.execSQL("""
                    INSERT INTO stories_new (
                        id, title, description, date, time, done, priority,
                        latitude, longitude, locationName
                    )
                    SELECT
                        id, title, description, date, time, done, priority,
                        latitude, longitude, locationName
                    FROM stories
                """)

                // Replace tables
                db.execSQL("DROP TABLE stories")
                db.execSQL("ALTER TABLE stories_new RENAME TO stories")
            }
        }
    }
}
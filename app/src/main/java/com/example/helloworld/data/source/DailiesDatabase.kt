package com.example.helloworld.data.source

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.helloworld.domain.model.Daily

@Database(entities = [Daily::class], version = 6, exportSchema = false)
abstract class DailiesDatabase : RoomDatabase() {
    abstract val dao: DailiesDao

    companion object {
        const val DATABASE_NAME = "dailies.db"

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE dailies ADD COLUMN priority INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE dailies ADD COLUMN latitude REAL")
                db.execSQL("ALTER TABLE dailies ADD COLUMN longitude REAL")
                db.execSQL("ALTER TABLE dailies ADD COLUMN locationName TEXT")
                db.execSQL("ALTER TABLE dailies ADD COLUMN recurringType TEXT")
                db.execSQL("ALTER TABLE dailies ADD COLUMN recurringInterval INTEGER")
                db.execSQL("ALTER TABLE dailies ADD COLUMN isRecurring INTEGER NOT NULL DEFAULT 0")
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE dailies ADD COLUMN recurringType TEXT")
                db.execSQL("ALTER TABLE dailies ADD COLUMN recurringInterval INTEGER")
                db.execSQL("ALTER TABLE dailies ADD COLUMN isRecurring INTEGER NOT NULL DEFAULT 0")
            }
        }
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE dailies ADD COLUMN recurringType TEXT")
                db.execSQL("ALTER TABLE dailies ADD COLUMN recurringInterval INTEGER")
                db.execSQL("ALTER TABLE dailies ADD COLUMN isRecurring INTEGER NOT NULL DEFAULT 0")
            }
        }
        val MIGRATION_3_5 = object : Migration(3, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // For migrating from v3 to v5, we need to add the recurring columns if they don't exist
                db.execSQL("ALTER TABLE dailies ADD COLUMN recurringType TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE dailies ADD COLUMN recurringInterval INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE dailies ADD COLUMN isRecurring INTEGER NOT NULL DEFAULT 0")
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE dailies ADD COLUMN recurringType TEXT")
                db.execSQL("ALTER TABLE dailies ADD COLUMN recurringInterval INTEGER")
                db.execSQL("ALTER TABLE dailies ADD COLUMN isRecurring INTEGER NOT NULL DEFAULT 0")
            }
        }

        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Add the notificationTime column with default value "30"
                db.execSQL("ALTER TABLE dailies ADD COLUMN notificationTime TEXT NOT NULL DEFAULT '30'")
            }
        }
    }
}
package com.nallanudi.nallanudi

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class AppDatabase private constructor(context: Context) {
    private val helper = DatabaseHelper(context.applicationContext)
    private val dao = WordDao(helper)

    fun wordDao(): WordDao = dao

    private class DatabaseHelper(context: Context) : SQLiteOpenHelper(
        context,
        DATABASE_NAME,
        null,
        DATABASE_VERSION
    ) {
        override fun onCreate(db: SQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS words (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    englishWord TEXT NOT NULL,
                    kannadaWord TEXT NOT NULL,
                    kannadaExplanation TEXT NOT NULL,
                    subject TEXT NOT NULL,
                    isSaved INTEGER NOT NULL DEFAULT 0,
                    isLearned INTEGER NOT NULL DEFAULT 0,
                    isViewed INTEGER NOT NULL DEFAULT 0,
                    viewedTime INTEGER NOT NULL DEFAULT 0
                )
                """.trimIndent()
            )
        }

        override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
            db.execSQL("DROP TABLE IF EXISTS words")
            onCreate(db)
        }
    }

    companion object {
        private const val DATABASE_NAME = "nallanudi_database"
        private const val DATABASE_VERSION = 3

        @Volatile
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: AppDatabase(context).also { instance = it }
            }
    }
}

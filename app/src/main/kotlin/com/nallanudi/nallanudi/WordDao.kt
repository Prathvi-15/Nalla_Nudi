package com.nallanudi.nallanudi

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteOpenHelper

class WordDao(private val helper: SQLiteOpenHelper) {
    fun insert(word: Word) {
        val id = helper.writableDatabase.insert("words", null, word.toContentValues(includeId = false))
        if (id >= 0) word.id = id.toInt()
    }

    fun getAllWords(): MutableList<Word> =
        queryWords("SELECT * FROM words ORDER BY englishWord ASC")

    fun getWordsBySubject(subject: String): MutableList<Word> =
        queryWords("SELECT * FROM words WHERE subject = ? ORDER BY englishWord ASC", subject)

    fun searchWords(query: String): MutableList<Word> =
        queryWords("SELECT * FROM words WHERE englishWord LIKE ? OR kannadaWord LIKE ?", query, query)

    fun getSavedWords(): MutableList<Word> =
        queryWords("SELECT * FROM words WHERE isSaved = 1")

    fun getLearnedWords(): MutableList<Word> =
        queryWords("SELECT * FROM words WHERE isLearned = 1")

    fun getWordCount(): Int =
        queryCount("SELECT COUNT(*) FROM words")

    fun getSavedCount(): Int =
        queryCount("SELECT COUNT(*) FROM words WHERE isSaved = 1")

    fun getLearnedCount(): Int =
        queryCount("SELECT COUNT(*) FROM words WHERE isLearned = 1")

    fun getLearnedCountBySubject(subject: String): Int =
        queryCount("SELECT COUNT(*) FROM words WHERE subject = ? AND isLearned = 1", subject)

    fun getViewedWords(): MutableList<Word> =
        queryWords("SELECT * FROM words WHERE isViewed = 1 ORDER BY viewedTime DESC")

    fun markAsViewed(id: Int, time: Long) {
        val values = ContentValues().apply {
            put("isViewed", 1)
            put("viewedTime", time)
        }
        helper.writableDatabase.update("words", values, "id = ?", arrayOf(id.toString()))
    }

    fun update(word: Word) {
        helper.writableDatabase.update(
            "words",
            word.toContentValues(includeId = false),
            "id = ?",
            arrayOf(word.id.toString())
        )
    }

    private fun queryWords(sql: String, vararg args: String): MutableList<Word> {
        val words = mutableListOf<Word>()
        helper.readableDatabase.rawQuery(sql, args).use { cursor ->
            while (cursor.moveToNext()) {
                words.add(cursor.toWord())
            }
        }
        return words
    }

    private fun queryCount(sql: String, vararg args: String): Int =
        helper.readableDatabase.rawQuery(sql, args).use { cursor ->
            if (cursor.moveToFirst()) cursor.getInt(0) else 0
        }

    private fun Word.toContentValues(includeId: Boolean): ContentValues =
        ContentValues().apply {
            if (includeId) put("id", id)
            put("englishWord", englishWord)
            put("kannadaWord", kannadaWord)
            put("kannadaExplanation", kannadaExplanation)
            put("subject", subject)
            put("isSaved", if (isSaved) 1 else 0)
            put("isLearned", if (isLearned) 1 else 0)
            put("isViewed", if (isViewed) 1 else 0)
            put("viewedTime", viewedTime)
        }

    private fun Cursor.toWord(): Word =
        Word(
            englishWord = getString(getColumnIndexOrThrow("englishWord")),
            kannadaWord = getString(getColumnIndexOrThrow("kannadaWord")),
            kannadaExplanation = getString(getColumnIndexOrThrow("kannadaExplanation")),
            subject = getString(getColumnIndexOrThrow("subject")),
            isSaved = getInt(getColumnIndexOrThrow("isSaved")) == 1,
            isLearned = getInt(getColumnIndexOrThrow("isLearned")) == 1,
            isViewed = getInt(getColumnIndexOrThrow("isViewed")) == 1,
            viewedTime = getLong(getColumnIndexOrThrow("viewedTime")),
            id = getInt(getColumnIndexOrThrow("id"))
        )
}

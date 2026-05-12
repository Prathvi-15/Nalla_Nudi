package com.nallanudi.nallanudi

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.cardview.widget.CardView
import org.json.JSONArray
import java.util.Calendar

class MainActivity : AppCompatActivity() {
    private lateinit var tvWordOfDay: TextView
    private lateinit var tvWordOfDayKannada: TextView
    private lateinit var layoutRecentChips: LinearLayout
    private lateinit var db: AppDatabase
    private lateinit var prefs: SharedPreferences
    private var isDarkMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        val savedPrefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val savedDarkMode = savedPrefs.getBoolean(KEY_DARK, false)
        AppCompatDelegate.setDefaultNightMode(
            if (savedDarkMode) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        )

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        db = AppDatabase.getInstance(this)
        prefs = savedPrefs
        isDarkMode = savedDarkMode

        tvWordOfDay = findViewById(R.id.tvWordOfDay)
        tvWordOfDayKannada = findViewById(R.id.tvWordOfDayKannada)
        layoutRecentChips = findViewById(R.id.layoutRecentChips)

        findViewById<TextView>(R.id.btnDarkMode).apply {
            text = if (isDarkMode) "Light" else "Dark"
            setOnClickListener {
                isDarkMode = !isDarkMode
                prefs.edit().putBoolean(KEY_DARK, isDarkMode).apply()
                AppCompatDelegate.setDefaultNightMode(
                    if (isDarkMode) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
                )
            }
        }

        findViewById<TextView>(R.id.btnGoToSearch).setOnClickListener {
            startActivity(Intent(this, SearchActivity::class.java))
        }

        findViewById<TextView>(R.id.btnClearRecentHome).setOnClickListener {
            prefs.edit().remove(KEY_RECENT).apply()
            loadRecentChips()
        }

        findViewById<CardView>(R.id.btnScience).setOnClickListener { openSearch("Science") }
        findViewById<CardView>(R.id.btnMath).setOnClickListener { openSearch("Math") }
        findViewById<CardView>(R.id.btnCommerce).setOnClickListener { openSearch("Commerce") }

        findViewById<CardView>(R.id.btnMyList).setOnClickListener {
            startActivity(Intent(this, MyListActivity::class.java))
        }
        findViewById<CardView>(R.id.btnFlashcard).setOnClickListener {
            startActivity(Intent(this, FlashcardActivity::class.java))
        }
        findViewById<CardView>(R.id.btnQuiz).setOnClickListener {
            startActivity(Intent(this, QuizActivity::class.java))
        }
        findViewById<CardView>(R.id.btnProgress).setOnClickListener {
            startActivity(Intent(this, ProgressActivity::class.java))
        }
        findViewById<CardView>(R.id.btnSpelling).setOnClickListener {
            startActivity(Intent(this, SpellingActivity::class.java))
        }
        findViewById<CardView>(R.id.btnChallenge).setOnClickListener {
            startActivity(Intent(this, ChallengeActivity::class.java))
        }
        findViewById<CardView>(R.id.btnHistory).setOnClickListener {
            startActivity(Intent(this, WordHistoryActivity::class.java))
        }

        setWordOfTheDay()
        loadRecentChips()
    }

    private fun openSearch(filter: String) {
        startActivity(Intent(this, SearchActivity::class.java).putExtra("filter", filter))
    }

    private fun setWordOfTheDay() {
        val all = db.wordDao().getAllWords()
        if (all.isNotEmpty()) {
            val word = all[Calendar.getInstance().get(Calendar.DAY_OF_YEAR) % all.size]
            tvWordOfDay.text = word.englishWord
            tvWordOfDayKannada.text = "${word.kannadaWord} - ${word.kannadaExplanation}"
        }
    }

    private fun loadRecentChips() {
        layoutRecentChips.removeAllViews()

        try {
            val arr = JSONArray(prefs.getString(KEY_RECENT, "[]"))

            if (arr.length() == 0) {
                val empty = TextView(this).apply {
                    text = "No recent searches yet"
                    setTextColor(Color.parseColor("#6B7280"))
                    textSize = 12f
                }
                layoutRecentChips.addView(empty)
                return
            }

            for (i in 0 until arr.length()) {
                val fullTerm = arr.getString(i)
                val display = if (fullTerm.length > 10) {
                    fullTerm.substring(0, 10) + "..."
                } else {
                    fullTerm
                }

                val chip = TextView(this).apply {
                    text = display
                    textSize = 12f
                    setTextColor(Color.parseColor("#4F46E5"))
                    setBackgroundResource(R.drawable.search_bg)
                    setPadding(22, 10, 22, 10)
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        marginEnd = 8
                    }
                    setOnClickListener {
                        startActivity(
                            Intent(this@MainActivity, SearchActivity::class.java)
                                .putExtra("query", fullTerm)
                        )
                    }
                }
                layoutRecentChips.addView(chip)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onResume() {
        super.onResume()
        setWordOfTheDay()
        loadRecentChips()
    }

    companion object {
        private const val PREFS_NAME = "NallaNudiPrefs"
        private const val KEY_RECENT = "recentSearches"
        private const val KEY_DARK = "darkMode"
    }
}

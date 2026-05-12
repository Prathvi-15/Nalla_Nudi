package com.nallanudi.nallanudi

import android.os.Bundle
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class ProgressActivity : AppCompatActivity() {

    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_progress)

        db = AppDatabase.getInstance(this)

        findViewById<TextView>(R.id.btnBack)
            .setOnClickListener {
                finish()
            }

        loadStats()
    }

    private fun loadStats() {

        val total =
            db.wordDao().getWordCount()

        val learned =
            db.wordDao().getLearnedCount()

        val saved =
            db.wordDao().getSavedCount()

        val sciLearned =
            db.wordDao().getLearnedCountBySubject("Science")

        val mathLearned =
            db.wordDao().getLearnedCountBySubject("Math")

        val comLearned =
            db.wordDao().getLearnedCountBySubject("Commerce")

        val prefs =
            getSharedPreferences(
                "NallaNudiPrefs",
                MODE_PRIVATE
            )

        val bestScore =
            prefs.getInt("bestScore", 0)

        val totalQuizzes =
            prefs.getInt("totalQuizzes", 0)

        findViewById<TextView>(R.id.tvTotal).text =
            total.toString()

        findViewById<TextView>(R.id.tvLearned).text =
            learned.toString()

        findViewById<TextView>(R.id.tvSaved).text =
            saved.toString()

        findViewById<TextView>(R.id.tvBest).text =
            "$bestScore/10"

        findViewById<TextView>(R.id.tvQuizzes).text =
            totalQuizzes.toString()

        val overallPct =
            if (total > 0)
                learned * 100 / total
            else
                0

        findViewById<ProgressBar>(R.id.pbOverall)
            .progress = overallPct

        val sciTotal = 50
        val mathTotal = 50
        val comTotal = 50

        val sciPct =
            sciLearned * 100 / sciTotal

        val mathPct =
            mathLearned * 100 / mathTotal

        val comPct =
            comLearned * 100 / comTotal

        findViewById<ProgressBar>(R.id.pbScience)
            .progress = sciPct

        findViewById<ProgressBar>(R.id.pbMath)
            .progress = mathPct

        findViewById<ProgressBar>(R.id.pbCommerce)
            .progress = comPct

        findViewById<TextView>(R.id.tvSciPct).text =
            "$sciLearned/$sciTotal"

        findViewById<TextView>(R.id.tvMathPct).text =
            "$mathLearned/$mathTotal"

        findViewById<TextView>(R.id.tvComPct).text =
            "$comLearned/$comTotal"

        val msg = when {

            learned == 0 ->
                "Start learning to track progress! 💪"

            overallPct < 25 ->
                "Great start! Keep going! 🌱"

            overallPct < 50 ->
                "You're doing well! 🌟"

            overallPct < 75 ->
                "Halfway there! Amazing! 🔥"

            overallPct < 100 ->
                "Almost done! Incredible! 🚀"

            else ->
                "You learned all words! 🏆 Champion!"
        }

        findViewById<TextView>(R.id.tvMotivation).text =
            msg
    }
}
package com.nallanudi.nallanudi

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.util.Collections
import kotlin.math.min

class SpellingActivity : AppCompatActivity() {

    private lateinit var tvQuestion: TextView
    private lateinit var tvProgress: TextView
    private lateinit var tvScore: TextView
    private lateinit var tvStreak: TextView
    private lateinit var tvHint: TextView

    private lateinit var etAnswer: EditText

    private lateinit var btnSubmit: Button
    private lateinit var btnSkip: Button
    private lateinit var btnHint: Button

    private lateinit var progressBar: ProgressBar

    private lateinit var wordList: MutableList<Word>
    private lateinit var db: AppDatabase

    private var currentIndex = 0
    private var score = 0
    private var streak = 0
    private var hintsUsed = 0

    private val total = 10

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_spelling)

        db = AppDatabase.getInstance(this)

        wordList = db.wordDao().getAllWords()

        Collections.shuffle(wordList)

        if (wordList.size > total) {

            wordList =
                ArrayList(
                    wordList.subList(0, total)
                )
        }

        tvQuestion = findViewById(R.id.tvQuestion)
        tvProgress = findViewById(R.id.tvProgress)
        tvScore = findViewById(R.id.tvScore)
        tvStreak = findViewById(R.id.tvStreak)
        tvHint = findViewById(R.id.tvHint)

        etAnswer = findViewById(R.id.etAnswer)

        btnSubmit = findViewById(R.id.btnSubmit)
        btnSkip = findViewById(R.id.btnSkip)
        btnHint = findViewById(R.id.btnHint)

        progressBar = findViewById(R.id.progressBar)

        findViewById<TextView>(R.id.btnBack)
            .setOnClickListener {
                finish()
            }

        loadQuestion()

        btnSubmit.setOnClickListener {

            val answer =
                etAnswer.text.toString().trim()

            if (answer.isEmpty()) {

                Toast.makeText(
                    this,
                    "Please type your answer!",
                    Toast.LENGTH_SHORT
                ).show()

                return@setOnClickListener
            }

            checkAnswer(answer)
        }

        btnSkip.setOnClickListener {

            val word = wordList[currentIndex]

            Toast.makeText(
                this,
                "Answer: ${word.englishWord}",
                Toast.LENGTH_LONG
            ).show()

            streak = 0

            tvStreak.text = ""

            currentIndex++

            Handler(Looper.getMainLooper())
                .postDelayed(
                    { loadQuestion() },
                    1500
                )
        }

        btnHint.setOnClickListener {

            val word = wordList[currentIndex]

            hintsUsed++

            val hint =
                word.englishWord.substring(
                    0,
                    min(
                        3,
                        word.englishWord.length
                    )
                ) + "..."

            tvHint.text =
                "💡 Hint: $hint (${word.subject})"

            tvHint.visibility = View.VISIBLE
        }
    }

    private fun loadQuestion() {

        if (currentIndex >= wordList.size) {

            showResult()

            return
        }

        val word = wordList[currentIndex]

        etAnswer.setText("")

        tvHint.visibility = View.GONE

        hintsUsed = 0

        progressBar.progress =
            currentIndex * 100 / total

        tvProgress.text =
            "Word ${currentIndex + 1} of $total"

        tvScore.text =
            "Score: $score"

        tvStreak.text =
            if (streak > 1)
                "🔥 $streak"
            else
                ""

        tvQuestion.text =
            "Type the English word for:\n\n" +
                    "${word.kannadaWord}\n\n" +
                    "(${word.kannadaExplanation})"

        btnSubmit.backgroundTintList =
            ColorStateList.valueOf(
                Color.parseColor("#4A148C")
            )

        btnSubmit.text = "✅ Submit"

        btnSubmit.isClickable = true
    }

    private fun checkAnswer(answer: String) {

        val word = wordList[currentIndex]

        val correct =
            answer.trim().equals(
                word.englishWord.trim(),
                ignoreCase = true
            )

        if (correct) {

            btnSubmit.backgroundTintList =
                ColorStateList.valueOf(
                    Color.parseColor("#2E7D32")
                )

            btnSubmit.text = "✅ Correct!"

            score++

            streak++

            tvStreak.text =
                if (streak > 1)
                    "🔥 $streak"
                else
                    ""

            Toast.makeText(
                this,
                "🎉 Correct! ${word.englishWord}",
                Toast.LENGTH_SHORT
            ).show()

        } else {

            btnSubmit.backgroundTintList =
                ColorStateList.valueOf(
                    Color.parseColor("#C62828")
                )

            btnSubmit.text = "❌ Wrong!"

            streak = 0

            tvStreak.text = ""

            Toast.makeText(
                this,
                "❌ Answer: ${word.englishWord}",
                Toast.LENGTH_LONG
            ).show()
        }

        btnSubmit.isClickable = false

        currentIndex++

        Handler(Looper.getMainLooper())
            .postDelayed(
                { loadQuestion() },
                2000
            )
    }

    private fun showResult() {

        progressBar.progress = 100

        val prefs =
            getSharedPreferences(
                "NallaNudiPrefs",
                MODE_PRIVATE
            )

        val best =
            prefs.getInt("spellingBest", 0)

        if (score > best) {

            prefs.edit()
                .putInt("spellingBest", score)
                .apply()
        }

        tvQuestion.text =
            "🎉 Spelling Bee Complete!\n\n" +
                    "You scored $score out of $total\n\n" +
                    when {

                        score >= 8 ->
                            "Spelling Champion! 🏆"

                        score >= 5 ->
                            "Good effort! 👍"

                        else ->
                            "Keep practicing! 💪"
                    }

        tvProgress.text = "Finished!"

        etAnswer.visibility = View.GONE

        btnHint.visibility = View.GONE

        btnSkip.visibility = View.GONE

        btnSubmit.text = "🔁 Play Again"

        btnSubmit.isClickable = true

        btnSubmit.backgroundTintList =
            ColorStateList.valueOf(
                Color.parseColor("#4A148C")
            )

        btnSubmit.setOnClickListener {

            score = 0
            streak = 0
            currentIndex = 0

            Collections.shuffle(wordList)

            etAnswer.visibility = View.VISIBLE

            btnHint.visibility = View.VISIBLE

            btnSkip.visibility = View.VISIBLE

            loadQuestion()
        }
    }
}
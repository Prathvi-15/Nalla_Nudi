package com.nallanudi.nallanudi

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.util.Collections

class QuizActivity : AppCompatActivity() {

    private lateinit var tvQuestion: TextView
    private lateinit var tvScore: TextView
    private lateinit var tvProgress: TextView
    private lateinit var tvStreak: TextView

    private lateinit var btnOpt1: Button
    private lateinit var btnOpt2: Button
    private lateinit var btnOpt3: Button
    private lateinit var btnOpt4: Button

    private lateinit var progressBar: ProgressBar

    private lateinit var wordList: MutableList<Word>
    private lateinit var db: AppDatabase

    private var currentIndex = 0
    private var score = 0
    private var streak = 0

    private val total = 10

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_quiz)

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
        tvScore = findViewById(R.id.tvScore)
        tvProgress = findViewById(R.id.tvProgress)
        tvStreak = findViewById(R.id.tvStreak)

        progressBar = findViewById(R.id.progressBar)

        btnOpt1 = findViewById(R.id.btnOpt1)
        btnOpt2 = findViewById(R.id.btnOpt2)
        btnOpt3 = findViewById(R.id.btnOpt3)
        btnOpt4 = findViewById(R.id.btnOpt4)

        findViewById<TextView>(R.id.btnBack)
            .setOnClickListener {
                finish()
            }

        loadQuestion()
    }

    private fun loadQuestion() {

        if (currentIndex >= wordList.size) {
            showResult()
            return
        }

        val correct = wordList[currentIndex]

        progressBar.progress =
            currentIndex * 100 / total

        tvProgress.text =
            "Question ${currentIndex + 1} of $total"

        tvScore.text =
            "Score: $score"

        tvStreak.text =
            if (streak > 1)
                "🔥 $streak"
            else
                ""

        tvQuestion.text =
            "What is the Kannada meaning of:\n\n\"${correct.englishWord}\"?"

        val allWords =
            db.wordDao().getAllWords()

        allWords.remove(correct)

        Collections.shuffle(allWords)

        val options = mutableListOf(
            correct,
            allWords[0],
            allWords[1],
            allWords[2]
        )

        Collections.shuffle(options)

        val buttons = arrayOf(
            btnOpt1,
            btnOpt2,
            btnOpt3,
            btnOpt4
        )

        for (i in buttons.indices) {

            buttons[i].text =
                options[i].kannadaWord

            resetButton(buttons[i])

            val chosen = options[i]

            buttons[i].setOnClickListener {

                checkAnswer(
                    it as Button,
                    chosen,
                    correct,
                    buttons
                )
            }
        }
    }

    private fun checkAnswer(
        clicked: Button,
        chosen: Word,
        correct: Word,
        buttons: Array<Button>
    ) {

        buttons.forEach {
            it.isClickable = false
        }

        if (chosen.id == correct.id) {

            clicked.backgroundTintList =
                ColorStateList.valueOf(
                    Color.parseColor("#388E3C")
                )

            score++
            streak++

            tvStreak.text =
                if (streak > 1)
                    "🔥 $streak"
                else
                    ""

            Toast.makeText(
                this,
                "✅ Correct!",
                Toast.LENGTH_SHORT
            ).show()

        } else {

            clicked.backgroundTintList =
                ColorStateList.valueOf(
                    Color.parseColor("#C62828")
                )

            buttons.filter {
                it.text.toString() == correct.kannadaWord
            }.forEach {

                it.backgroundTintList =
                    ColorStateList.valueOf(
                        Color.parseColor("#388E3C")
                    )
            }

            streak = 0

            Toast.makeText(
                this,
                "❌ ${correct.kannadaWord}",
                Toast.LENGTH_SHORT
            ).show()
        }

        currentIndex++

        Handler(Looper.getMainLooper())
            .postDelayed(
                { loadQuestion() },
                1500
            )
    }

    private fun resetButton(button: Button) {

        button.backgroundTintList =
            ColorStateList.valueOf(
                Color.parseColor("#7B1FA2")
            )

        button.setTextColor(Color.WHITE)

        button.isClickable = true
    }

    private fun showResult() {

        progressBar.progress = 100

        val prefs =
            getSharedPreferences(
                "NallaNudiPrefs",
                MODE_PRIVATE
            )

        val bestScore =
            prefs.getInt("bestScore", 0)

        val totalQuizzes =
            prefs.getInt("totalQuizzes", 0)

        if (score > bestScore) {

            prefs.edit()
                .putInt("bestScore", score)
                .apply()
        }

        prefs.edit()
            .putInt(
                "totalQuizzes",
                totalQuizzes + 1
            )
            .apply()

        tvQuestion.text =
            "🎉 Quiz Complete!\n\n" +
                    "You scored $score out of $total\n\n" +
                    when {

                        score >= 8 ->
                            "Excellent! 🌟"

                        score >= 5 ->
                            "Good job! 👍"

                        else ->
                            "Keep practicing! 💪"
                    }

        tvProgress.text = "Finished!"

        tvStreak.text = ""

        btnOpt1.visibility = View.GONE
        btnOpt2.visibility = View.GONE
        btnOpt3.visibility = View.GONE

        btnOpt4.text = "🔁 Play Again"

        btnOpt4.backgroundTintList =
            ColorStateList.valueOf(
                Color.parseColor("#4A148C")
            )

        btnOpt4.setOnClickListener {

            score = 0
            streak = 0
            currentIndex = 0

            Collections.shuffle(wordList)

            btnOpt1.visibility = View.VISIBLE
            btnOpt2.visibility = View.VISIBLE
            btnOpt3.visibility = View.VISIBLE

            loadQuestion()
        }
    }
}
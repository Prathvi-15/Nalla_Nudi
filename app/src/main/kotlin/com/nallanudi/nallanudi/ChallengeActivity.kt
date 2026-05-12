package com.nallanudi.nallanudi

import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.util.Collections

class ChallengeActivity : AppCompatActivity() {
    private lateinit var tvQuestion: TextView
    private lateinit var tvScore: TextView
    private lateinit var tvHighScore: TextView
    private lateinit var tvTimer: TextView
    private lateinit var tvStreak: TextView
    private lateinit var tvLives: TextView
    private lateinit var btnOpt1: Button
    private lateinit var btnOpt2: Button
    private lateinit var btnOpt3: Button
    private lateinit var btnOpt4: Button
    private lateinit var timerBar: ProgressBar
    private lateinit var wordList: MutableList<Word>
    private lateinit var db: AppDatabase
    private lateinit var prefs: SharedPreferences
    private var currentIndex = 0
    private var score = 0
    private var highScore = 0
    private var streak = 0
    private var lives = 3
    private var countDownTimer: CountDownTimer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_challenge)

        db = AppDatabase.getInstance(this)
        prefs = getSharedPreferences("NallaNudiPrefs", MODE_PRIVATE)
        highScore = prefs.getInt("challengeHigh", 0)
        wordList = db.wordDao().getAllWords()
        Collections.shuffle(wordList)

        tvQuestion = findViewById(R.id.tvQuestion)
        tvScore = findViewById(R.id.tvScore)
        tvHighScore = findViewById(R.id.tvHighScore)
        tvTimer = findViewById(R.id.tvTimer)
        tvStreak = findViewById(R.id.tvStreak)
        tvLives = findViewById(R.id.tvLives)
        timerBar = findViewById(R.id.timerBar)
        btnOpt1 = findViewById(R.id.btnOpt1)
        btnOpt2 = findViewById(R.id.btnOpt2)
        btnOpt3 = findViewById(R.id.btnOpt3)
        btnOpt4 = findViewById(R.id.btnOpt4)

        findViewById<TextView>(R.id.btnBack).setOnClickListener {
            countDownTimer?.cancel()
            finish()
        }

        tvHighScore.text = "Best: $highScore"
        loadQuestion()
    }

    private fun loadQuestion() {
        if (lives <= 0) {
            showGameOver()
            return
        }

        if (currentIndex >= wordList.size) {
            Collections.shuffle(wordList)
            currentIndex = 0
        }

        val correct = wordList[currentIndex]
        tvScore.text = "Score: $score"
        tvLives.text = getLivesText()
        tvStreak.text = if (streak > 1) "x$streak" else ""
        tvQuestion.text = "Kannada meaning of:\n\n${correct.englishWord} ?"

        val all = db.wordDao().getAllWords()
        all.remove(correct)
        Collections.shuffle(all)
        val options = mutableListOf(correct, all[0], all[1], all[2])
        Collections.shuffle(options)

        val buttons = arrayOf(btnOpt1, btnOpt2, btnOpt3, btnOpt4)
        for (i in buttons.indices) {
            buttons[i].text = options[i].kannadaWord
            resetButton(buttons[i])
            val chosen = options[i]
            buttons[i].setOnClickListener { checkAnswer(it as Button, chosen, correct, buttons) }
        }

        startTimer(buttons, correct)
    }

    private fun startTimer(buttons: Array<Button>, correct: Word) {
        countDownTimer?.cancel()
        timerBar.max = 10000
        timerBar.progress = 10000

        countDownTimer = object : CountDownTimer(10000, 100) {
            override fun onTick(ms: Long) {
                timerBar.progress = ms.toInt()
                tvTimer.text = "${ms / 1000 + 1}s"
                tvTimer.setTextColor(Color.parseColor(if (ms < 3000) "#C62828" else "#4A148C"))
            }

            override fun onFinish() {
                tvTimer.text = "0s"
                buttons.forEach { it.isClickable = false }
                buttons.filter { it.text.toString() == correct.kannadaWord }.forEach {
                    it.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#388E3C"))
                }
                lives--
                streak = 0
                tvLives.text = getLivesText()
                Toast.makeText(this@ChallengeActivity, "Time up! -1 Life", Toast.LENGTH_SHORT).show()
                currentIndex++
                Handler(Looper.getMainLooper()).postDelayed({ loadQuestion() }, 1500)
            }
        }.start()
    }

    private fun checkAnswer(clicked: Button, chosen: Word, correct: Word, buttons: Array<Button>) {
        countDownTimer?.cancel()
        buttons.forEach { it.isClickable = false }

        if (chosen.id == correct.id) {
            clicked.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#388E3C"))
            score++
            streak++
            if (streak >= 5) {
                score++
                Toast.makeText(this, "STREAK BONUS! +2", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Correct! +1", Toast.LENGTH_SHORT).show()
            }

            if (score > highScore) {
                highScore = score
                prefs.edit().putInt("challengeHigh", highScore).apply()
                tvHighScore.text = "Best: $highScore"
            }
        } else {
            clicked.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#C62828"))
            buttons.filter { it.text.toString() == correct.kannadaWord }.forEach {
                it.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#388E3C"))
            }
            lives--
            streak = 0
            tvLives.text = getLivesText()
            Toast.makeText(this, "Wrong! -1 Life", Toast.LENGTH_SHORT).show()
        }

        currentIndex++
        Handler(Looper.getMainLooper()).postDelayed({ loadQuestion() }, 1500)
    }

    private fun resetButton(button: Button) {
        button.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#4A148C"))
        button.setTextColor(Color.WHITE)
        button.isClickable = true
    }

    private fun getLivesText(): String {
        val sb = StringBuilder()
        repeat(lives) { sb.append("H ") }
        repeat(3 - lives) { sb.append("X ") }
        return sb.toString().trim()
    }

    private fun showGameOver() {
        countDownTimer?.cancel()
        tvQuestion.text = "Game Over!\n\nFinal Score: $score\nBest Score: $highScore\n\n" +
            when {
                score >= highScore -> "New High Score!"
                score >= 10 -> "Amazing!"
                score >= 5 -> "Good try!"
                else -> "Keep practicing!"
            }
        tvLives.text = "Game Over"
        tvTimer.text = ""

        btnOpt1.visibility = View.GONE
        btnOpt2.visibility = View.GONE
        btnOpt3.visibility = View.GONE
        btnOpt4.visibility = View.VISIBLE
        btnOpt4.text = "Play Again"
        btnOpt4.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#4A148C"))
        btnOpt4.setOnClickListener {
            score = 0
            streak = 0
            lives = 3
            currentIndex = 0
            Collections.shuffle(wordList)
            btnOpt1.visibility = View.VISIBLE
            btnOpt2.visibility = View.VISIBLE
            btnOpt3.visibility = View.VISIBLE
            loadQuestion()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()
    }
}

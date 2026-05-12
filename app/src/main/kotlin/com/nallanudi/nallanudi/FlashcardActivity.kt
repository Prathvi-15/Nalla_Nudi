package com.nallanudi.nallanudi

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.util.Collections

class FlashcardActivity : AppCompatActivity() {

    private lateinit var tvCardFront: TextView
    private lateinit var tvCardBack: TextView
    private lateinit var tvCardSubject: TextView
    private lateinit var tvProgress: TextView
    private lateinit var tvStreak: TextView
    private lateinit var btnFlip: Button
    private lateinit var btnKnow: Button
    private lateinit var btnDontKnow: Button
    private lateinit var cardFront: FrameLayout
    private lateinit var cardBack: FrameLayout
    private lateinit var wordList: MutableList<Word>
    private lateinit var db: AppDatabase

    private var currentIndex = 0
    private var knownCount = 0
    private var streak = 0
    private var isFlipped = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_flashcard)

        db = AppDatabase.getInstance(this)
        wordList = db.wordDao().getAllWords()

        Collections.shuffle(wordList)

        tvCardFront = findViewById(R.id.tvCardFront)
        tvCardBack = findViewById(R.id.tvCardBack)
        tvCardSubject = findViewById(R.id.tvCardSubject)
        tvProgress = findViewById(R.id.tvProgress)
        tvStreak = findViewById(R.id.tvStreak)

        btnFlip = findViewById(R.id.btnFlip)
        btnKnow = findViewById(R.id.btnKnow)
        btnDontKnow = findViewById(R.id.btnDontKnow)

        val btnShuffle = findViewById<Button>(R.id.btnShuffle)

        cardFront = findViewById(R.id.cardFront)
        cardBack = findViewById(R.id.cardBack)

        findViewById<TextView>(R.id.btnBack).setOnClickListener {
            finish()
        }

        showCard()

        btnFlip.setOnClickListener {
            flipCard()
        }

        cardFront.setOnClickListener {
            flipCard()
        }

        cardBack.setOnClickListener {
            flipCard()
        }

        btnKnow.setOnClickListener {
            val word = wordList[currentIndex]

            word.isLearned = true
            db.wordDao().update(word)

            knownCount++
            streak++

            tvStreak.text = if (streak > 1) "🔥 $streak" else ""

            nextCard()
        }

        btnDontKnow.setOnClickListener {
            streak = 0
            tvStreak.text = ""
            nextCard()
        }

        btnShuffle.setOnClickListener {
            Collections.shuffle(wordList)

            currentIndex = 0
            knownCount = 0
            streak = 0
            isFlipped = false

            tvStreak.text = ""

            showCard()
        }
    }

    private fun showCard() {

        if (currentIndex >= wordList.size) {
            showComplete()
            return
        }

        val word = wordList[currentIndex]

        isFlipped = false

        cardFront.visibility = View.VISIBLE
        cardBack.visibility = View.GONE

        btnFlip.text = "👆 Tap to see Kannada meaning"

        tvCardFront.text = word.englishWord

        tvCardBack.text =
            "${word.kannadaWord}\n\n${word.kannadaExplanation}"

        tvCardSubject.text = word.subject

        tvProgress.text =
            "${currentIndex + 1} / ${wordList.size}  |  ✅ $knownCount known"

        when (word.subject) {

            "Science" -> {
                tvCardSubject.setBackgroundColor(
                    Color.parseColor("#7B1FA2")
                )
            }

            "Math" -> {
                tvCardSubject.setBackgroundColor(
                    Color.parseColor("#1565C0")
                )
            }

            "Commerce" -> {
                tvCardSubject.setBackgroundColor(
                    Color.parseColor("#E65100")
                )
            }
        }
    }

    private fun flipCard() {

        if (!isFlipped) {

            cardFront.visibility = View.GONE
            cardBack.visibility = View.VISIBLE

            btnFlip.text = "👆 Tap to see English word"

            isFlipped = true

        } else {

            cardFront.visibility = View.VISIBLE
            cardBack.visibility = View.GONE

            btnFlip.text = "👆 Tap to see Kannada meaning"

            isFlipped = false
        }
    }

    private fun nextCard() {

        currentIndex++
        isFlipped = false

        showCard()
    }

    private fun showComplete() {

        cardFront.visibility = View.VISIBLE
        cardBack.visibility = View.GONE

        tvCardFront.text =
            "🎉 Session Complete!\n\n" +
                    "You knew $knownCount out of ${wordList.size} words!\n\n" +
                    when {

                        knownCount >= wordList.size * 0.8 ->
                            "Excellent! 🌟"

                        knownCount >= wordList.size * 0.5 ->
                            "Good job! 👍"

                        else ->
                            "Keep practicing! 💪"
                    }

        tvProgress.text = "Finished! ✅"

        btnKnow.visibility = View.GONE
        btnDontKnow.visibility = View.GONE

        btnFlip.text = "🔁 Study Again"

        btnFlip.setOnClickListener {

            currentIndex = 0
            knownCount = 0
            streak = 0
            isFlipped = false

            Collections.shuffle(wordList)

            btnKnow.visibility = View.VISIBLE
            btnDontKnow.visibility = View.VISIBLE

            btnFlip.text = "👆 Tap to see Kannada meaning"

            btnFlip.setOnClickListener {
                flipCard()
            }

            showCard()
        }
    }
}
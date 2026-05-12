package com.nallanudi.nallanudi

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class WordHistoryActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_word_history)

        val viewedWords = AppDatabase.getInstance(this).wordDao().getViewedWords()
        findViewById<TextView>(R.id.btnBack).setOnClickListener { finish() }

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerHistory)
        val tvEmpty = findViewById<TextView>(R.id.tvEmpty)
        val tvCount = findViewById<TextView>(R.id.tvCount)

        recyclerView.layoutManager = LinearLayoutManager(this)

        if (viewedWords.isEmpty()) {
            tvEmpty.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
            tvCount.text = "0 words viewed"
        } else {
            tvEmpty.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
            tvCount.text = "${viewedWords.size} words viewed"
            recyclerView.adapter = WordAdapter(this, viewedWords)
        }
    }
}

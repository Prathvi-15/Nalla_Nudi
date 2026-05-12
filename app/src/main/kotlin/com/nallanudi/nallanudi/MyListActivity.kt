package com.nallanudi.nallanudi

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MyListActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_my_list)

        val savedWords =
            AppDatabase.getInstance(this)
                .wordDao()
                .getSavedWords()

        val recyclerView =
            findViewById<RecyclerView>(R.id.recyclerViewMyList)

        val tvEmpty =
            findViewById<TextView>(R.id.tvEmpty)

        recyclerView.layoutManager =
            LinearLayoutManager(this)

        if (savedWords.isEmpty()) {

            tvEmpty.text =
                "No words saved yet!\n\nTap ⭐ Save on any word to add it here."

        } else {

            recyclerView.adapter =
                WordAdapter(this, savedWords)
        }
    }
}
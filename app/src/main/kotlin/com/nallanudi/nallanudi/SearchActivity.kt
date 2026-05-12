package com.nallanudi.nallanudi

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.json.JSONArray
import kotlin.math.min

class SearchActivity : AppCompatActivity() {

    private lateinit var etSearch: EditText
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: WordAdapter
    private lateinit var db: AppDatabase

    private lateinit var tvResultCount: TextView
    private lateinit var tvNoResults: TextView
    private lateinit var btnClearRecent: TextView
    private lateinit var layoutRecentChips: LinearLayout

    private lateinit var prefs: SharedPreferences
    private lateinit var allWords: MutableList<Word>

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_search)

        db = AppDatabase.getInstance(this)

        prefs = getSharedPreferences(
            PREFS_NAME,
            Context.MODE_PRIVATE
        )

        allWords = db.wordDao().getAllWords()

        etSearch = findViewById(R.id.etSearchPage)

        recyclerView = findViewById(R.id.recyclerSearch)

        tvResultCount = findViewById(R.id.tvResultCount)

        tvNoResults = findViewById(R.id.tvNoResults)

        btnClearRecent = findViewById(R.id.btnClearRecent)

        layoutRecentChips =
            findViewById(R.id.layoutRecentChips)

        findViewById<TextView>(R.id.btnBack)
            .setOnClickListener {
                finish()
            }

        recyclerView.layoutManager =
            LinearLayoutManager(this)

        adapter = WordAdapter(this, allWords)

        recyclerView.adapter = adapter

        tvResultCount.text =
            "${allWords.size} words available"

        loadRecentChips()

        btnClearRecent.setOnClickListener {

            prefs.edit()
                .remove(KEY_RECENT)
                .apply()

            loadRecentChips()
        }

        intent.getStringExtra("filter")
            ?.let { filter ->

                val filtered =
                    db.wordDao()
                        .getWordsBySubject(filter)

                adapter.updateList(filtered)

                tvResultCount.text =
                    "${filtered.size} words in $filter"
            }

        intent.getStringExtra("query")
            ?.let { query ->

                etSearch.setText(query)

                etSearch.setSelection(query.length)

                performSearch(query)
            }

        etSearch.addTextChangedListener(
            object : TextWatcher {

                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) = Unit

                override fun afterTextChanged(
                    s: Editable?
                ) = Unit

                override fun onTextChanged(
                    s: CharSequence?,
                    start: Int,
                    before: Int,
                    count: Int
                ) {

                    val q =
                        s.toString().trim()

                    if (q.isEmpty()) {

                        adapter.updateList(allWords)

                        tvResultCount.text =
                            "${allWords.size} words available"

                        tvNoResults.visibility =
                            View.GONE

                        recyclerView.visibility =
                            View.VISIBLE

                        loadRecentChips()

                    } else {

                        performSearch(q)
                    }
                }
            }
        )
    }

    private fun performSearch(query: String) {

        val results =
            db.wordDao()
                .searchWords("%$query%")

        adapter.updateList(results)

        if (results.isEmpty()) {

            tvNoResults.visibility =
                View.VISIBLE

            recyclerView.visibility =
                View.GONE

            tvResultCount.text =
                "No results for \"$query\""

        } else {

            tvNoResults.visibility =
                View.GONE

            recyclerView.visibility =
                View.VISIBLE

            tvResultCount.text =
                "${results.size} result(s) for \"$query\""

            saveRecentSearch(query)

            loadRecentChips()
        }
    }

    private fun saveRecentSearch(query: String) {

        try {

            val arr = getRecentArray()

            for (i in 0 until arr.length()) {

                if (
                    arr.getString(i)
                        .equals(query, ignoreCase = true)
                ) {

                    arr.remove(i)

                    break
                }
            }

            val newArr = JSONArray()

            newArr.put(query)

            for (
            i in 0 until min(
                arr.length(),
                MAX_RECENT - 1
            )
            ) {

                newArr.put(arr.get(i))
            }

            prefs.edit()
                .putString(
                    KEY_RECENT,
                    newArr.toString()
                )
                .apply()

        } catch (e: Exception) {

            e.printStackTrace()
        }
    }

    private fun getRecentArray(): JSONArray =

        try {

            JSONArray(
                prefs.getString(
                    KEY_RECENT,
                    "[]"
                )
            )

        } catch (e: Exception) {

            JSONArray()
        }

    private fun loadRecentChips() {

        layoutRecentChips.removeAllViews()

        try {

            val arr = getRecentArray()

            for (i in 0 until arr.length()) {

                val term = arr.getString(i)

                val chip = TextView(this).apply {

                    text = "🔍 $term"

                    textSize = 13f

                    setTextColor(
                        Color.parseColor("#4A148C")
                    )

                    setBackgroundResource(
                        R.drawable.search_bg
                    )

                    setPadding(
                        24,
                        12,
                        24,
                        12
                    )

                    layoutParams =
                        LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        ).apply {

                            marginEnd = 10
                        }

                    setOnClickListener {

                        etSearch.setText(term)

                        etSearch.setSelection(term.length)

                        performSearch(term)
                    }
                }

                layoutRecentChips.addView(chip)
            }

        } catch (e: Exception) {

            e.printStackTrace()
        }
    }

    companion object {

        private const val PREFS_NAME =
            "NallaNudiPrefs"

        private const val KEY_RECENT =
            "recentSearches"

        private const val MAX_RECENT = 8
    }
}
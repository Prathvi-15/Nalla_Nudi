package com.nallanudi.nallanudi

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.speech.tts.TextToSpeech
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import java.util.Locale

class WordAdapter(
    private val context: Context,
    private var wordList: MutableList<Word>
) : RecyclerView.Adapter<WordAdapter.WordViewHolder>() {

    private val db = AppDatabase.getInstance(context)

    private var kannadaReady = false

    private var ttsEnglish: TextToSpeech

    private var ttsKannada: TextToSpeech

    init {

        ttsEnglish = TextToSpeech(context) { status ->

            if (status == TextToSpeech.SUCCESS) {

                ttsEnglish.language = Locale.ENGLISH
            }
        }

        ttsKannada = TextToSpeech(context) { status ->

            if (status == TextToSpeech.SUCCESS) {

                val result =
                    ttsKannada.setLanguage(
                        Locale("kn", "IN")
                    )

                if (
                    result != TextToSpeech.LANG_NOT_SUPPORTED &&
                    result != TextToSpeech.LANG_MISSING_DATA
                ) {

                    kannadaReady = true
                }
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): WordViewHolder {

        val view =
            LayoutInflater.from(context)
                .inflate(
                    R.layout.item_word,
                    parent,
                    false
                )

        return WordViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: WordViewHolder,
        position: Int
    ) {

        val word = wordList[position]

        holder.tvEnglishWord.text =
            word.englishWord

        holder.tvKannadaWord.text =
            word.kannadaWord

        holder.tvKannadaExplanation.text =
            word.kannadaExplanation

        holder.tvSubjectTag.text =
            word.subject

        when (word.subject) {

            "Science" -> {
                holder.tvSubjectTag.setBackgroundColor(
                    Color.parseColor("#7B1FA2")
                )
            }

            "Math" -> {
                holder.tvSubjectTag.setBackgroundColor(
                    Color.parseColor("#1565C0")
                )
            }

            "Commerce" -> {
                holder.tvSubjectTag.setBackgroundColor(
                    Color.parseColor("#E65100")
                )
            }
        }

        holder.btnSave.text =
            if (word.isSaved)
                "✅ Saved"
            else
                "⭐ Save"

        holder.btnSave.backgroundTintList =
            ColorStateList.valueOf(
                Color.parseColor(
                    if (word.isSaved)
                        "#388E3C"
                    else
                        "#4A148C"
                )
            )

        if (!word.isViewed) {

            val now = System.currentTimeMillis()

            db.wordDao().markAsViewed(
                word.id,
                now
            )

            word.isViewed = true

            word.viewedTime = now
        }

        holder.btnPronounce.setOnClickListener {

            ttsEnglish.stop()

            ttsEnglish.speak(
                word.englishWord,
                TextToSpeech.QUEUE_FLUSH,
                null,
                null
            )
        }

        holder.btnPronounceKannada.setOnClickListener {

            if (kannadaReady) {

                ttsKannada.stop()

                ttsKannada.speak(
                    "${word.kannadaWord}. ${word.kannadaExplanation}",
                    TextToSpeech.QUEUE_FLUSH,
                    null,
                    null
                )

            } else {

                Toast.makeText(
                    context,
                    "Kannada voice not available.\nInstall Kannada TTS in Settings.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        holder.btnSave.setOnClickListener {

            word.isSaved = !word.isSaved

            db.wordDao().update(word)

            notifyItemChanged(position)

            Toast.makeText(
                context,
                if (word.isSaved)
                    "${word.englishWord} saved! ⭐"
                else
                    "${word.englishWord} removed",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun getItemCount(): Int =
        wordList.size

    fun updateList(newList: MutableList<Word>) {

        wordList = newList

        notifyDataSetChanged()
    }

    fun shutdown() {

        ttsEnglish.shutdown()

        ttsKannada.shutdown()
    }

    class WordViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {

        val tvEnglishWord: TextView =
            itemView.findViewById(R.id.tvEnglishWord)

        val tvKannadaWord: TextView =
            itemView.findViewById(R.id.tvKannadaWord)

        val tvKannadaExplanation: TextView =
            itemView.findViewById(R.id.tvKannadaExplanation)

        val tvSubjectTag: TextView =
            itemView.findViewById(R.id.tvSubjectTag)

        val btnPronounce: Button =
            itemView.findViewById(R.id.btnPronounce)

        val btnPronounceKannada: Button =
            itemView.findViewById(R.id.btnPronounceKannada)

        val btnSave: Button =
            itemView.findViewById(R.id.btnSave)
    }
}
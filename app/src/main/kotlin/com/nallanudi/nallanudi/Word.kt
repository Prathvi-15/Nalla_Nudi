package com.nallanudi.nallanudi

data class Word(
    var englishWord: String,
    var kannadaWord: String,
    var kannadaExplanation: String,
    var subject: String,
    var isSaved: Boolean = false,
    var isLearned: Boolean = false,
    var isViewed: Boolean = false,
    var viewedTime: Long = 0L,
    var id: Int = 0
)

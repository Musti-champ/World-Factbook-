package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "dossiers")
data class DossierEntity(
    @PrimaryKey val countryName: String,
    val isBookmarked: Boolean = true,
    val analystNotes: String = "",
    val cachedDetailedFactsJson: String? = null,
    val countryJson: String? = null,
    val cachedBriefingMarkdown: String? = null,
    val lastUpdated: Long = System.currentTimeMillis()
)

@Entity(tableName = "quiz_scores")
data class QuizScoreEntity(
    @PrimaryKey val quizMode: String, // "capital", "flag", "trivia"
    val highScore: Int = 0,
    val totalPlayed: Int = 0
)

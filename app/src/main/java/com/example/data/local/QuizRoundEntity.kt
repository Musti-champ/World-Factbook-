package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "quiz_rounds")
data class QuizRoundEntity(
    @PrimaryKey val id: String,
    val quizMode: String,
    val score: Int,
    val completedAt: Long = System.currentTimeMillis()
)

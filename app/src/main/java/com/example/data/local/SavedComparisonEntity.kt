package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saved_comparisons")
data class SavedComparisonEntity(
    @PrimaryKey val id: String, // e.g., "United States_vs_Canada"
    val countryNameA: String,
    val countryNameB: String,
    val analystNotes: String = "",
    val savedAt: Long = System.currentTimeMillis()
)

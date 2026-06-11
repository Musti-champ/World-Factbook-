package com.example.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface DossierDao {

    @Query("SELECT * FROM dossiers WHERE isBookmarked = 1")
    fun getBookmarkedDossiers(): Flow<List<DossierEntity>>

    @Query("SELECT * FROM dossiers")
    fun getAllDossiersFlow(): Flow<List<DossierEntity>>

    @Query("SELECT * FROM dossiers")
    suspend fun getAllDossiers(): List<DossierEntity>

    @Query("SELECT * FROM dossiers WHERE countryName = :countryName LIMIT 1")
    suspend fun getDossierByCountry(countryName: String): DossierEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveDossier(dossier: DossierEntity)

    @Query("UPDATE dossiers SET analystNotes = :notes, lastUpdated = :timestamp WHERE countryName = :countryName")
    suspend fun updateAnalystNotes(countryName: String, notes: String, timestamp: Long = System.currentTimeMillis())

    @Query("DELETE FROM dossiers WHERE countryName = :countryName")
    suspend fun deleteDossier(countryName: String)

    // Quiz Score queries
    @Query("SELECT * FROM quiz_scores")
    fun getAllHighScores(): Flow<List<QuizScoreEntity>>

    @Query("SELECT * FROM quiz_scores WHERE quizMode = :mode LIMIT 1")
    suspend fun getScoreByMode(mode: String): QuizScoreEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveScore(score: QuizScoreEntity)

    // Quiz History queries
    @Query("SELECT * FROM quiz_rounds ORDER BY completedAt DESC")
    fun getAllQuizRoundsFlow(): Flow<List<QuizRoundEntity>>

    @Query("SELECT * FROM quiz_rounds")
    suspend fun getAllQuizRounds(): List<QuizRoundEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveQuizRound(round: QuizRoundEntity)

    // Saved Comparisons queries
    @Query("SELECT * FROM saved_comparisons ORDER BY savedAt DESC")
    fun getAllSavedComparisonsFlow(): Flow<List<SavedComparisonEntity>>

    @Query("SELECT * FROM saved_comparisons WHERE id = :id LIMIT 1")
    suspend fun getComparisonById(id: String): SavedComparisonEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveComparison(comparison: SavedComparisonEntity)

    @Query("DELETE FROM saved_comparisons WHERE id = :id")
    suspend fun deleteComparison(id: String)
}

package com.example.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.Country
import com.example.data.CountryDatabase
import com.example.data.local.AppDatabase
import com.example.data.local.DossierEntity
import com.example.data.local.QuizScoreEntity
import com.example.data.local.QuizRoundEntity
import com.example.data.local.SavedComparisonEntity
import com.example.network.GeminiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.json.JSONObject
import org.json.JSONArray

sealed interface Screen {
    object Dashboard : Screen
    data class CountryDetail(val country: Country) : Screen
    object Compare : Screen
    object Quiz : Screen
    object Favorites : Screen
}

sealed interface ProfileAiState {
    object Idle : ProfileAiState
    object Loading : ProfileAiState
    data class Success(val briefingMarkdown: String, val isOnlineRecent: Boolean = false) : ProfileAiState
    data class Error(val errorMessage: String, val fallbackMarkdown: String? = null) : ProfileAiState
}

sealed interface CompareAiState {
    object Idle : CompareAiState
    object Loading : CompareAiState
    data class Success(val comparisonMarkdown: String) : CompareAiState
    data class Error(val errorMessage: String) : CompareAiState
}

sealed interface SearchAllState {
    object Idle : SearchAllState
    object Probing : SearchAllState
    data class Success(val countryName: String) : SearchAllState
    data class Error(val errorMessage: String) : SearchAllState
}

data class TriviaQuestion(
    val question: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String
)

data class QuizRound(
    val id: String,
    val quizMode: String,
    val score: Int,
    val completedAt: Long = System.currentTimeMillis()
)

class FactbookViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val dao = db.dossierDao()
    private val geminiService = GeminiService()

    // Screen State
    private val _currentScreen = MutableStateFlow<Screen>(Screen.Dashboard)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    // Navigation Backstack (Simple list of previous screens)
    private val screenStack = mutableListOf<Screen>()

    // Country Lists
    private val _allCountries = MutableStateFlow<List<Country>>(CountryDatabase.countries)
    val allCountries: StateFlow<List<Country>> = _allCountries.asStateFlow()
    
    // Search and Filters
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _selectedRegion = MutableStateFlow("All")
    val selectedRegion = _selectedRegion.asStateFlow()

    private val _searchAllState = MutableStateFlow<SearchAllState>(SearchAllState.Idle)
    val searchAllState = _searchAllState.asStateFlow()

    // Favorited/Bookmarked Dossiers from Room
    private val _bookmarks = MutableStateFlow<Map<String, DossierEntity>>(emptyMap())
    val bookmarks = _bookmarks.asStateFlow()

    // All cached/saved dossiers in DB (bookmarks + search caches)
    private val _cachedDossiers = MutableStateFlow<Map<String, DossierEntity>>(emptyMap())
    val cachedDossiers = _cachedDossiers.asStateFlow()

    // Dossier tags classification from SharedPreferences
    private val _dossierTags = MutableStateFlow<Map<String, String>>(emptyMap())
    val dossierTags = _dossierTags.asStateFlow()

    // Quiz stats from Room
    private val _highScores = MutableStateFlow<Map<String, QuizScoreEntity>>(emptyMap())
    val highScores = _highScores.asStateFlow()

    // Dynamic AI states
    private val _activeProfileDossier = MutableStateFlow<ProfileAiState>(ProfileAiState.Idle)
    val activeProfileDossier = _activeProfileDossier.asStateFlow()

    private val _compareState = MutableStateFlow<CompareAiState>(CompareAiState.Idle)
    val compareState = _compareState.asStateFlow()

    // Selected countries for comparison
    private val _compareCountryA = MutableStateFlow<Country?>(null)
    val compareCountryA = _compareCountryA.asStateFlow()

    private val _compareCountryB = MutableStateFlow<Country?>(null)
    val compareCountryB = _compareCountryB.asStateFlow()

    // Quiz Game variables
    private val _quizQuestionType = MutableStateFlow("capital") // "capital", "flag", "trivia"
    val quizQuestionType = _quizQuestionType.asStateFlow()

    private val _quizQuestion = MutableStateFlow<TriviaQuestion?>(null)
    val quizQuestion = _quizQuestion.asStateFlow()

    private val _quizIsLoading = MutableStateFlow(false)
    val quizIsLoading = _quizIsLoading.asStateFlow()

    private val _quizScore = MutableStateFlow(0)
    val quizScore = _quizScore.asStateFlow()

    private val _quizLives = MutableStateFlow(3)
    val quizLives = _quizLives.asStateFlow()

    private val _answeredCorrectly = MutableStateFlow<Boolean?>(null) // null = unanswered, True, False
    val answeredCorrectly = _answeredCorrectly.asStateFlow()

    private val _selectedOptionIndex = MutableStateFlow<Int?>(null)
    val selectedOptionIndex = _selectedOptionIndex.asStateFlow()

    private val _quizHistory = MutableStateFlow<List<QuizRound>>(emptyList())
    val quizHistory = _quizHistory.asStateFlow()

    // Saved Comparisons StateFlow
    private val _savedComparisons = MutableStateFlow<List<SavedComparisonEntity>>(emptyList())
    val savedComparisons = _savedComparisons.asStateFlow()

    init {
        loadCountriesFromAssets(application)

        // Migrate legacy SharedPrefs quiz history to Room on startup
        viewModelScope.launch {
            try {
                val prefs = application.getSharedPreferences("quiz_prefs", Context.MODE_PRIVATE)
                val hasMigrated = prefs.getBoolean("room_migrated", false)
                if (!hasMigrated) {
                    val jsonStr = prefs.getString("round_history", null)
                    if (!jsonStr.isNullOrBlank()) {
                        val array = JSONArray(jsonStr)
                        for (i in 0 until array.length()) {
                            val obj = array.getJSONObject(i)
                            val id = obj.optString("id", java.util.UUID.randomUUID().toString())
                            val quizMode = obj.getString("quizMode")
                            val score = obj.getInt("score")
                            val completedAt = obj.optLong("completedAt", System.currentTimeMillis())
                            
                            dao.saveQuizRound(
                                QuizRoundEntity(
                                    id = id,
                                    quizMode = quizMode,
                                    score = score,
                                    completedAt = completedAt
                                )
                            )
                        }
                    }
                    prefs.edit().putBoolean("room_migrated", true).apply()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // Observe Quiz history from Room database
        viewModelScope.launch {
            dao.getAllQuizRoundsFlow().collectLatest { entities ->
                val domainHistory = entities.map { entity ->
                    QuizRound(
                        id = entity.id,
                        quizMode = entity.quizMode,
                        score = entity.score,
                        completedAt = entity.completedAt
                    )
                }
                _quizHistory.value = domainHistory
            }
        }

        // Observe all dossiers (bookmarks + caches)
        viewModelScope.launch {
            dao.getAllDossiersFlow().collectLatest { entities ->
                val allMap = entities.associateBy { it.countryName }
                _cachedDossiers.value = allMap
                
                val bookmarkedMap = entities.filter { it.isBookmarked }.associateBy { it.countryName }
                _bookmarks.value = bookmarkedMap
                
                // Dynamically unpack and add any search-cached countries to the main list
                val cachedCountries = entities.mapNotNull { entity ->
                    entity.countryJson?.let { jsonStr ->
                        try {
                            countryFromJsonString(jsonStr)
                        } catch (e: Exception) {
                            null
                        }
                    }
                }
                
                if (cachedCountries.isNotEmpty()) {
                    val currentList = _allCountries.value.toMutableList()
                    var listChanged = false
                    cachedCountries.forEach { cached ->
                        val index = currentList.indexOfFirst { it.name.trim().equals(cached.name.trim(), ignoreCase = true) }
                        if (index >= 0) {
                            if (currentList[index] != cached) {
                                currentList[index] = cached
                                listChanged = true
                            }
                        } else {
                            currentList.add(cached)
                            listChanged = true
                        }
                    }
                    if (listChanged) {
                        _allCountries.value = currentList
                    }
                }
            }
        }

        // Observe High Scores
        viewModelScope.launch {
            dao.getAllHighScores().collectLatest { scores ->
                _highScores.value = scores.associateBy { it.quizMode }
            }
        }

        // Observe Saved Geopolitical Comparisons
        viewModelScope.launch {
            dao.getAllSavedComparisonsFlow().collectLatest { lst ->
                _savedComparisons.value = lst
            }
        }

        // Initialize dossier tags
        loadDossierTags(application)
    }

    private fun loadCountriesFromAssets(application: Application) {
        try {
            val jsonString = application.assets.open("countries.json")
                .bufferedReader()
                .use { it.readText() }
            val jsonArray = JSONArray(jsonString)
            val list = mutableListOf<Country>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                
                val langArray = obj.optJSONArray("languages")
                val languages = mutableListOf<String>()
                if (langArray != null) {
                    for (j in 0 until langArray.length()) {
                        languages.add(langArray.getString(j))
                    }
                }
                
                val curArray = obj.optJSONArray("currencies")
                val currencies = mutableListOf<String>()
                if (curArray != null) {
                    for (j in 0 until curArray.length()) {
                        currencies.add(curArray.getString(j))
                    }
                }

                list.add(
                    Country(
                        name = obj.getString("name"),
                        officialName = obj.getString("officialName"),
                        flagEmoji = obj.optString("flagEmoji", "🏳️"),
                        capital = obj.optString("capital", ""),
                        region = obj.optString("region", ""),
                        subregion = obj.optString("subregion", ""),
                        population = obj.optLong("population", 0L),
                        areaSqKm = obj.optDouble("areaSqKm", 0.0),
                        languages = languages,
                        currencies = currencies,
                        latitude = obj.optDouble("latitude", 0.0),
                        longitude = obj.optDouble("longitude", 0.0),
                        shortOverview = obj.optString("shortOverview", ""),
                        capitalLocation = obj.optString("capitalLocation", ""),
                        climate = obj.optString("climate", ""),
                        governmentType = obj.optString("governmentType", ""),
                        headOfState = obj.optString("headOfState", "")
                    )
                )
            }
            if (list.isNotEmpty()) {
                _allCountries.value = list
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Screen Management
    fun navigateTo(screen: Screen) {
        if (_currentScreen.value != screen) {
            screenStack.add(_currentScreen.value)
            _currentScreen.value = screen
        }
    }

    fun navigateBack(): Boolean {
        if (screenStack.isNotEmpty()) {
            _currentScreen.value = screenStack.removeLast()
            return true
        }
        return false
    }

    // Filter updates
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun updateSelectedRegion(region: String) {
        _selectedRegion.value = region
    }

    // Bookmarking / Dossier local notes logic
    fun toggleBookmark(country: Country) {
        viewModelScope.launch {
            val existing = dao.getDossierByCountry(country.name)
            if (existing != null) {
                if (existing.isBookmarked) {
                    if (existing.analystNotes.isNotBlank() || existing.cachedDetailedFactsJson != null || existing.countryJson != null || existing.cachedBriefingMarkdown != null) {
                        dao.saveDossier(existing.copy(isBookmarked = false, lastUpdated = System.currentTimeMillis()))
                    } else {
                        dao.deleteDossier(country.name)
                    }
                } else {
                    dao.saveDossier(existing.copy(isBookmarked = true, lastUpdated = System.currentTimeMillis()))
                }
            } else {
                dao.saveDossier(DossierEntity(countryName = country.name, isBookmarked = true))
            }
        }
    }

    fun saveAnalystNotes(countryName: String, notes: String) {
        viewModelScope.launch {
            val existing = dao.getDossierByCountry(countryName)
            if (existing != null) {
                dao.saveDossier(existing.copy(analystNotes = notes, lastUpdated = System.currentTimeMillis()))
            } else {
                dao.saveDossier(DossierEntity(countryName = countryName, isBookmarked = false, analystNotes = notes))
            }
        }
    }

    fun loadDossierTags(application: Application) {
        val prefs = application.getSharedPreferences("dossier_tags_prefs", Context.MODE_PRIVATE)
        val allKeys = prefs.all
        val map = allKeys.mapValues { it.value?.toString() ?: "UNCLASSIFIED" }
        _dossierTags.value = map
    }

    fun saveDossierTag(countryName: String, tag: String) {
        val prefs = getApplication<Application>().getSharedPreferences("dossier_tags_prefs", Context.MODE_PRIVATE)
        prefs.edit().putString(countryName, tag).apply()
        val current = _dossierTags.value.toMutableMap()
        current[countryName] = tag
        _dossierTags.value = current
    }

    // Rich AI dossier fetch
    fun loadAiDossier(countryName: String) {
        _activeProfileDossier.value = ProfileAiState.Loading
        viewModelScope.launch {
            val country = com.example.data.CountryDatabase.countries.firstOrNull { it.name.equals(countryName, ignoreCase = true) }
            val fallbackMarkdown = if (country != null) {
                val facts = com.example.data.DetailedCountryFactsProvider.getDetailedFacts(country.name, country)
                com.example.data.DetailedCountryFactsProvider.getOfflineDossierMarkdown(country, facts)
            } else {
                "# $countryName: Strategic Geopolitical Profile\n\nNo offline documentation is currently registered in local database. Establish live connection to initialize records."
            }

            try {
                val markdown = geminiService.fetchCountryDossier(countryName)
                
                var detailedFactsJson: String? = null
                try {
                    detailedFactsJson = geminiService.fetchCountryDetailedFacts(countryName)
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                val existing = dao.getDossierByCountry(countryName)
                val updatedDetailedJson = if (!detailedFactsJson.isNullOrBlank()) detailedFactsJson else existing?.cachedDetailedFactsJson
                
                if (existing != null) {
                    dao.saveDossier(
                        existing.copy(
                            cachedDetailedFactsJson = updatedDetailedJson,
                            cachedBriefingMarkdown = markdown,
                            lastUpdated = System.currentTimeMillis()
                        )
                    )
                } else {
                    dao.saveDossier(
                        DossierEntity(
                            countryName = countryName,
                            isBookmarked = false,
                            cachedDetailedFactsJson = updatedDetailedJson,
                            cachedBriefingMarkdown = markdown
                        )
                    )
                }

                _activeProfileDossier.value = ProfileAiState.Success(markdown, isOnlineRecent = true)
            } catch (e: Exception) {
                val existing = dao.getDossierByCountry(countryName)
                if (existing != null && !existing.cachedBriefingMarkdown.isNullOrBlank()) {
                    _activeProfileDossier.value = ProfileAiState.Success(existing.cachedBriefingMarkdown, isOnlineRecent = true)
                } else {
                    _activeProfileDossier.value = ProfileAiState.Error(
                        "Satellite signal lost: ${e.localizedMessage}. Visualizing secure offline database archives.",
                        fallbackMarkdown = fallbackMarkdown
                    )
                }
            }
        }
    }

    fun loadInitialProfileDossier(countryName: String) {
        viewModelScope.launch {
            val existing = dao.getDossierByCountry(countryName)
            val country = com.example.data.CountryDatabase.countries.firstOrNull { it.name.equals(countryName, ignoreCase = true) }
            val fallbackMarkdown = if (country != null) {
                val facts = com.example.data.DetailedCountryFactsProvider.getDetailedFacts(country.name, country)
                com.example.data.DetailedCountryFactsProvider.getOfflineDossierMarkdown(country, facts)
            } else {
                "# $countryName: Strategic Geopolitical Profile\n\nNo offline documentation is currently registered in local database. Establish live connection to initialize records."
            }

            if (existing != null && !existing.cachedBriefingMarkdown.isNullOrBlank()) {
                _activeProfileDossier.value = ProfileAiState.Success(existing.cachedBriefingMarkdown, isOnlineRecent = true)
            } else {
                _activeProfileDossier.value = ProfileAiState.Success(fallbackMarkdown, isOnlineRecent = false)
            }
        }
    }

    fun clearProfileDossier() {
        _activeProfileDossier.value = ProfileAiState.Idle
    }

    // Dynamic country database lookup for any country in the world
    fun probeSatelliteCountry(countryName: String) {
        if (countryName.isBlank()) return
        _searchAllState.value = SearchAllState.Probing
        viewModelScope.launch {
            try {
                val normalizedQuery = countryName.trim().lowercase()
                
                // 1. Check built-in countries list first
                val builtInMatch = CountryDatabase.countries.firstOrNull { it.name.trim().lowercase() == normalizedQuery }
                if (builtInMatch != null) {
                    _searchAllState.value = SearchAllState.Success(builtInMatch.name)
                    return@launch
                }

                // 2. Check local DB cache
                val existingDossier = dao.getDossierByCountry(countryName)
                var existingCountryStr = existingDossier?.countryJson
                if (existingCountryStr == null) {
                    val allDossiers = dao.getAllDossiers()
                    val match = allDossiers.firstOrNull { it.countryName.trim().lowercase() == normalizedQuery && it.countryJson != null }
                    existingCountryStr = match?.countryJson
                }

                if (existingCountryStr != null) {
                    val cachedCountry = countryFromJsonString(existingCountryStr)
                    val currentList = _allCountries.value.toMutableList()
                    val existingIndex = currentList.indexOfFirst { it.name.trim().equals(cachedCountry.name.trim(), ignoreCase = true) }
                    if (existingIndex >= 0) {
                        currentList[existingIndex] = cachedCountry
                    } else {
                        currentList.add(cachedCountry)
                    }
                    _allCountries.value = currentList
                    _searchAllState.value = SearchAllState.Success(cachedCountry.name)
                    return@launch
                }

                // 3. Telemetry network lookup (Gemini link)
                val jsonText = geminiService.fetchCountryJsonData(countryName)
                if (jsonText.isEmpty()) {
                    _searchAllState.value = SearchAllState.Error("No telemetry response received. Please check satellite links.")
                    return@launch
                }
                
                val json = JSONObject(jsonText)
                val name = json.getString("name")
                val officialName = json.getString("officialName")
                val flagEmoji = json.optString("flagEmoji", "🏳️")
                val capital = json.optString("capital", "Unknown Capital")
                val region = json.optString("region", "All")
                val subregion = json.optString("subregion", "Unknown")
                val population = json.optLong("population", 0L)
                val areaSqKm = json.optDouble("areaSqKm", 0.0)
                
                val langArray = json.optJSONArray("languages")
                val languages = mutableListOf<String>()
                if (langArray != null) {
                    for (i in 0 until langArray.length()) {
                        languages.add(langArray.getString(i))
                    }
                } else if (json.has("languages")) {
                    languages.add(json.getString("languages"))
                }
                if (languages.isEmpty()) languages.add("English")

                val curArray = json.optJSONArray("currencies")
                val currencies = mutableListOf<String>()
                if (curArray != null) {
                    for (i in 0 until curArray.length()) {
                        currencies.add(curArray.getString(i))
                    }
                } else if (json.has("currencies")) {
                    currencies.add(json.getString("currencies"))
                }
                if (currencies.isEmpty()) currencies.add("Local Currency")

                val latitude = json.optDouble("latitude", 0.0)
                val longitude = json.optDouble("longitude", 0.0)
                val shortOverview = json.optString("shortOverview", "No overview compiled yet.")
                val capitalLocation = json.optString("capitalLocation", "Information restricted.")
                val climate = json.optString("climate", "Temperate notes.")
                val governmentType = json.optString("governmentType", "Republic")
                val headOfState = json.optString("headOfState", "President")

                val newCountry = Country(
                    name = name,
                    officialName = officialName,
                    flagEmoji = flagEmoji,
                    capital = capital,
                    region = region,
                    subregion = subregion,
                    population = population,
                    areaSqKm = areaSqKm,
                    languages = languages,
                    currencies = currencies,
                    latitude = latitude,
                    longitude = longitude,
                    shortOverview = shortOverview,
                    capitalLocation = capitalLocation,
                    climate = climate,
                    governmentType = governmentType,
                    headOfState = headOfState
                )

                // Cache in local database
                val countryJsonStr = newCountry.toJsonString()
                val finalExisting = dao.getDossierByCountry(newCountry.name)
                if (finalExisting != null) {
                    dao.saveDossier(finalExisting.copy(countryJson = countryJsonStr, lastUpdated = System.currentTimeMillis()))
                } else {
                    dao.saveDossier(
                        DossierEntity(
                            countryName = newCountry.name,
                            isBookmarked = false,
                            countryJson = countryJsonStr
                        )
                    )
                }

                // Add to list, maintaining uniqueness or updating
                val currentList = _allCountries.value.toMutableList()
                val existingIndex = currentList.indexOfFirst { it.name.trim().equals(newCountry.name.trim(), ignoreCase = true) }
                if (existingIndex >= 0) {
                    currentList[existingIndex] = newCountry
                } else {
                    currentList.add(newCountry)
                }
                _allCountries.value = currentList
                _searchAllState.value = SearchAllState.Success(newCountry.name)
            } catch (e: Exception) {
                _searchAllState.value = SearchAllState.Error("Telemetry decoding failed: ${e.localizedMessage}")
            }
        }
    }

    fun clearSearchAllState() {
        _searchAllState.value = SearchAllState.Idle
    }

    // Compare logic
    fun setCompareCountry(slotKey: String, country: Country?) {
        if (slotKey == "A") {
            _compareCountryA.value = country
        } else {
            _compareCountryB.value = country
        }
        _compareState.value = CompareAiState.Idle
    }

    fun makeAiComparisonReport() {
        val cA = _compareCountryA.value
        val cB = _compareCountryB.value
        if (cA == null || cB == null) return

        _compareState.value = CompareAiState.Loading
        viewModelScope.launch {
            try {
                val report = geminiService.fetchCountryComparison(cA.name, cB.name)
                _compareState.value = CompareAiState.Success(report)
            } catch (e: Exception) {
                _compareState.value = CompareAiState.Error("Satellite report analysis interrupted: ${e.localizedMessage}")
            }
        }
    }

    fun saveGeopoliticalComparison(countryNameA: String, countryNameB: String, analystNotes: String) {
        val id = "${countryNameA}_vs_${countryNameB}"
        viewModelScope.launch {
            dao.saveComparison(
                SavedComparisonEntity(
                    id = id,
                    countryNameA = countryNameA,
                    countryNameB = countryNameB,
                    analystNotes = analystNotes,
                    savedAt = System.currentTimeMillis()
                )
            )
        }
    }

    fun deleteGeopoliticalComparison(id: String) {
        viewModelScope.launch {
            dao.deleteComparison(id)
        }
    }

    // Quiz mechanics
    fun startNewQuizSession(type: String) {
        _quizQuestionType.value = type
        _quizScore.value = 0
        _quizLives.value = 3
        _answeredCorrectly.value = null
        _selectedOptionIndex.value = null
        loadNextQuizQuestion()
    }

    fun loadNextQuizQuestion() {
        _quizIsLoading.value = true
        _answeredCorrectly.value = null
        _selectedOptionIndex.value = null
        _quizQuestion.value = null

        viewModelScope.launch {
            when (_quizQuestionType.value) {
                "capital" -> {
                    generateCapitalQuestion()
                }
                "flag" -> {
                    generateFlagQuestion()
                }
                "trivia" -> {
                    generateAiTriviaQuestion()
                }
                "active_facts" -> {
                    generateActiveFactsQuestion()
                }
            }
            _quizIsLoading.value = false
        }
    }

    private fun generateCapitalQuestion() {
        val currentCountries = allCountries.value
        if (currentCountries.isEmpty()) return
        val correctCountry = currentCountries.random()
        val correctCapital = correctCountry.capital
        
        val wrongCapitals = currentCountries
            .filter { it.name != correctCountry.name }
            .map { it.capital }
            .shuffled()
            .take(3)

        val options = (wrongCapitals + correctCapital).shuffled()
        val correctIndex = options.indexOf(correctCapital)

        _quizQuestion.value = TriviaQuestion(
            question = "What is the capital city of ${correctCountry.name}?",
            options = options,
            correctIndex = correctIndex,
            explanation = "${correctCountry.capital} is indeed the official capital of ${correctCountry.name}."
        )
    }

    private fun generateFlagQuestion() {
        val currentCountries = allCountries.value
        if (currentCountries.isEmpty()) return
        val correctCountry = currentCountries.random()
        val correctName = correctCountry.name
        
        val wrongNames = currentCountries
            .filter { it.name != correctCountry.name }
            .map { it.name }
            .shuffled()
            .take(3)

        val options = (wrongNames + correctName).shuffled()
        val correctIndex = options.indexOf(correctName)

        _quizQuestion.value = TriviaQuestion(
            question = "Which country owns this flag:  ${correctCountry.flagEmoji}",
            options = options,
            correctIndex = correctIndex,
            explanation = "${correctCountry.flagEmoji} belongs to ${correctCountry.name}."
        )
    }

    private suspend fun generateAiTriviaQuestion() {
        // Fetch dynamic trivia from Gemini
        val names = allCountries.value.map { it.name }
        val jsonString = geminiService.fetchTriviaQuestion(names)
        if (jsonString.isNotEmpty()) {
            try {
                val json = JSONObject(jsonString)
                val q = json.getString("question")
                val optArr = json.getJSONArray("options")
                val opts = listOf(
                    optArr.getString(0),
                    optArr.getString(1),
                    optArr.getString(2),
                    optArr.getString(3)
                )
                val cIndex = json.getInt("correctIndex")
                val exp = json.optString("briefingExplanation", "Splendid work!")

                _quizQuestion.value = TriviaQuestion(
                    question = q,
                    options = opts,
                    correctIndex = cIndex,
                    explanation = exp
                )
                return
            } catch (e: Exception) {
                // Parse error, fall back gracefully
            }
        }
        // Fallback to local capital or flag questions silently
        val coinFlip = (0..1).random()
        if (coinFlip == 0) generateCapitalQuestion() else generateFlagQuestion()
    }

    fun submitAnswer(optionIndex: Int) {
        val q = _quizQuestion.value ?: return
        if (_answeredCorrectly.value != null) return // Already answered

        _selectedOptionIndex.value = optionIndex
        val correct = optionIndex == q.correctIndex
        _answeredCorrectly.value = correct

        if (correct) {
            _quizScore.value = _quizScore.value + 10
        } else {
            _quizLives.value = maxOf(0, _quizLives.value - 1)
        }

        // Check and save score records to Room if game over
        if (_quizLives.value == 0) {
            saveQuizStats()
        }
    }

    private fun saveQuizStats() {
        viewModelScope.launch {
            val mode = _quizQuestionType.value
            val currentScore = _quizScore.value
            val existing = dao.getScoreByMode(mode) ?: QuizScoreEntity(quizMode = mode)
            
            val updated = existing.copy(
                highScore = maxOf(existing.highScore, currentScore),
                totalPlayed = existing.totalPlayed + 1
            )
            dao.saveScore(updated)

            // Save to Room DB table quiz_rounds for robust history & leaderboard
            val roundId = java.util.UUID.randomUUID().toString()
            val completedAt = System.currentTimeMillis()
            dao.saveQuizRound(
                QuizRoundEntity(
                    id = roundId,
                    quizMode = mode,
                    score = currentScore,
                    completedAt = completedAt
                )
            )

            // Save to SharedPreferences round history as well for backward compatibility
            try {
                val application = getApplication<Application>()
                saveQuizRoundToHistory(
                    application,
                    QuizRound(
                        id = roundId,
                        quizMode = mode,
                        score = currentScore,
                        completedAt = completedAt
                    )
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun loadQuizHistory(context: Context) {
        try {
            val prefs = context.getSharedPreferences("quiz_prefs", Context.MODE_PRIVATE)
            val jsonStr = prefs.getString("round_history", null) ?: "[]"
            val array = JSONArray(jsonStr)
            val list = mutableListOf<QuizRound>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    QuizRound(
                        id = obj.getString("id"),
                        quizMode = obj.getString("quizMode"),
                        score = obj.getInt("score"),
                        completedAt = obj.getLong("completedAt")
                    )
                )
            }
            _quizHistory.value = list.sortedByDescending { it.completedAt }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun saveQuizRoundToHistory(context: Context, round: QuizRound) {
        try {
            val prefs = context.getSharedPreferences("quiz_prefs", Context.MODE_PRIVATE)
            val currentList = _quizHistory.value.toMutableList()
            currentList.add(0, round) // Inject latest at top
            _quizHistory.value = currentList

            val array = JSONArray()
            for (item in currentList) {
                val obj = JSONObject()
                obj.put("id", item.id)
                obj.put("quizMode", item.quizMode)
                obj.put("score", item.score)
                obj.put("completedAt", item.completedAt)
                array.put(obj)
            }
            prefs.edit().putString("round_history", array.toString()).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun generateActiveFactsQuestion() {
        val currentCountries = allCountries.value
        if (currentCountries.size < 4) {
            generateCapitalQuestion()
            return
        }

        val correctCountry = currentCountries.random()
        
        // Pick dynamic fact category
        val categories = mutableListOf<String>()
        if (correctCountry.population > 0) categories.add("population")
        if (correctCountry.subregion.isNotBlank()) categories.add("subregion")
        if (correctCountry.languages.isNotEmpty()) categories.add("language")
        if (correctCountry.currencies.isNotEmpty()) categories.add("currency")
        if (correctCountry.governmentType.isNotBlank()) categories.add("government")
        if (correctCountry.areaSqKm > 0.0) categories.add("area")

        if (categories.isEmpty()) {
            generateCapitalQuestion()
            return
        }

        val category = categories.random()
        var questionText = ""
        var wrongAnswers = listOf<String>()
        var correctAnswer = ""
        var explanationText = ""

        when (category) {
            "population" -> {
                val pop = correctCountry.population
                val formattedPop = formatPopulationString(pop)
                questionText = "Which country has a population of approximately $formattedPop?"
                correctAnswer = correctCountry.name
                
                wrongAnswers = currentCountries
                    .filter { it.name != correctCountry.name }
                    .shuffled()
                    .take(3)
                    .map { it.name }
                
                explanationText = "${correctCountry.name}'s population is indeed $formattedPop."
            }
            "subregion" -> {
                val subregion = correctCountry.subregion
                questionText = "Which of these countries is located in the global subregion designated as '$subregion'?"
                correctAnswer = correctCountry.name

                wrongAnswers = currentCountries
                    .filter { it.name != correctCountry.name && it.subregion != subregion }
                    .shuffled()
                    .take(3)
                    .map { it.name }

                explanationText = "${correctCountry.name} is officially situated within the $subregion subregion."
            }
            "language" -> {
                val lang = correctCountry.languages.random()
                questionText = "For which of these countries is '$lang' spoken as an official or major language?"
                correctAnswer = correctCountry.name

                wrongAnswers = currentCountries
                    .filter { it.name != correctCountry.name && !it.languages.contains(lang) }
                    .shuffled()
                    .take(3)
                    .map { it.name }

                explanationText = "$lang is a major or official language spoken in the territory of ${correctCountry.name}."
            }
            "currency" -> {
                val currency = correctCountry.currencies.random()
                questionText = "Which of these countries officially utilizes '$currency' as legal tender?"
                correctAnswer = correctCountry.name

                wrongAnswers = currentCountries
                    .filter { it.name != correctCountry.name && !it.currencies.contains(currency) }
                    .shuffled()
                    .take(3)
                    .map { it.name }

                explanationText = "${correctCountry.name} operates with the currency $currency."
            }
            "government" -> {
                val gov = correctCountry.governmentType
                questionText = "Which country's form of government is officially styled as a '$gov'?"
                correctAnswer = correctCountry.name

                wrongAnswers = currentCountries
                    .filter { it.name != correctCountry.name && !it.governmentType.equals(gov, ignoreCase = true) }
                    .shuffled()
                    .take(3)
                    .map { it.name }

                explanationText = "The sovereignty of ${correctCountry.name} is structured as a $gov."
            }
            "area" -> {
                val area = correctCountry.areaSqKm
                val formattedArea = String.format("%,.0f", area)
                questionText = "Which of these countries spans a geographical land area of approximately $formattedArea square kilometers?"
                correctAnswer = correctCountry.name

                wrongAnswers = currentCountries
                    .filter { it.name != correctCountry.name }
                    .shuffled()
                    .take(3)
                    .map { it.name }

                explanationText = "${correctCountry.name} administers a total area of approximately $formattedArea sq km."
            }
        }

        // Handle case where we didn't find enough wrong options
        if (wrongAnswers.size < 3) {
            generateCapitalQuestion()
            return
        }

        val options = (wrongAnswers + correctAnswer).shuffled()
        val correctIndex = options.indexOf(correctAnswer)

        _quizQuestion.value = TriviaQuestion(
            question = questionText,
            options = options,
            correctIndex = correctIndex,
            explanation = explanationText
        )
    }

    private fun formatPopulationString(pop: Long): String {
        return when {
            pop >= 1_000_000_000 -> String.format("%.1f billion", pop.toDouble() / 1_000_000_000)
            pop >= 1_000_000 -> String.format("%.1f million", pop.toDouble() / 1_000_000)
            else -> String.format("%,d", pop)
        }
    }

    private fun Country.toJsonString(): String {
        val json = JSONObject()
        json.put("name", name)
        json.put("officialName", officialName)
        json.put("flagEmoji", flagEmoji)
        json.put("capital", capital)
        json.put("region", region)
        json.put("subregion", subregion)
        json.put("population", population)
        json.put("areaSqKm", areaSqKm)
        
        val langArr = JSONArray()
        languages.forEach { langArr.put(it) }
        json.put("languages", langArr)
        
        val curArr = JSONArray()
        currencies.forEach { curArr.put(it) }
        json.put("currencies", curArr)
        
        json.put("latitude", latitude)
        json.put("longitude", longitude)
        json.put("shortOverview", shortOverview)
        json.put("capitalLocation", capitalLocation)
        json.put("climate", climate)
        json.put("governmentType", governmentType)
        json.put("headOfState", headOfState)
        return json.toString()
    }

    private fun countryFromJsonString(jsonStr: String): Country {
        val json = JSONObject(jsonStr)
        val name = json.getString("name")
        val officialName = json.getString("officialName")
        val flagEmoji = json.optString("flagEmoji", "🏳️")
        val capital = json.optString("capital", "")
        val region = json.optString("region", "")
        val subregion = json.optString("subregion", "")
        val population = json.optLong("population", 0L)
        val areaSqKm = json.optDouble("areaSqKm", 0.0)
        
        val langArr = json.optJSONArray("languages")
        val languages = mutableListOf<String>()
        if (langArr != null) {
            for (i in 0 until langArr.length()) {
                languages.add(langArr.getString(i))
            }
        }
        
        val curArr = json.optJSONArray("currencies")
        val currencies = mutableListOf<String>()
        if (curArr != null) {
            for (i in 0 until curArr.length()) {
                currencies.add(curArr.getString(i))
            }
        }
        
        val latitude = json.optDouble("latitude", 0.0)
        val longitude = json.optDouble("longitude", 0.0)
        val shortOverview = json.optString("shortOverview", "")
        val capitalLocation = json.optString("capitalLocation", "")
        val climate = json.optString("climate", "")
        val governmentType = json.optString("governmentType", "")
        val headOfState = json.optString("headOfState", "")
        
        return Country(
            name = name,
            officialName = officialName,
            flagEmoji = flagEmoji,
            capital = capital,
            region = region,
            subregion = subregion,
            population = population,
            areaSqKm = areaSqKm,
            languages = languages,
            currencies = currencies,
            latitude = latitude,
            longitude = longitude,
            shortOverview = shortOverview,
            capitalLocation = capitalLocation,
            climate = climate,
            governmentType = governmentType,
            headOfState = headOfState
        )
    }
}

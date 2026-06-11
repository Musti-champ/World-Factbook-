package com.example.network

import com.example.BuildConfig
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class GeminiRequest(
    val contents: List<Content>,
    val systemInstruction: Content? = null,
    val generationConfig: GenerationConfig? = null
)

@JsonClass(generateAdapter = true)
data class Content(
    val parts: List<Part>
)

@JsonClass(generateAdapter = true)
data class Part(
    val text: String
)

@JsonClass(generateAdapter = true)
data class GenerationConfig(
    val temperature: Float? = null,
    val responseType: String? = null
)

@JsonClass(generateAdapter = true)
data class GeminiResponse(
    val candidates: List<Candidate>?
)

@JsonClass(generateAdapter = true)
data class Candidate(
    val content: Content?
)

interface GeminiApi {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}

object RetrofitClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    val apiService: GeminiApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(GeminiApi::class.java)
    }
}

class GeminiService {
    suspend fun fetchCountryDossier(countryName: String): String {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return "INTELLIGENCE NOTICE: Gemini AI key not configured in Secrets panel. Please key in your API credentials to run dynamic intelligence briefings. Standard profile files are cached locally."
        }

        val prompt = """
            You are a senior CIA political analyst. Generate a comprehensive Intelligence Dossier on the country '$countryName' matching the CIA World Factbook standard.
            
            Format the briefing document beautifully with clear Markdown headings. Use a formal, objective, professional intelligence assessment tone.
            
            Structure:
            
            # $countryName: Strategic Intelligence Overview
            
            ### 1. Geopolitical Significance
            (Summarize the primary strategic importance of the country, its neighbors, maritime choke points, or resources)
            
            ### 2. Historical Arc & Governance
            (A highly executive summary of key historical pivots, current administration, and political stability)
            
            ### 3. Economic Landscape & Vital Industries
            (Detail GDP, main industries like tech, agriculture, or mineral deposits, trade partners, and fiscal challenges)
            
            ### 4. Transnational Challenges & Social Dynamics
            (List primary boundary disputes, security threats, environmental issues, major immigration patterns, or wealth inequality)
            
            ### 5. CIA Analyst Fun Fact
            (Highlight a fascinating, highly unexpected cultural or geographical fact about this country)
        """.trimIndent()

        val request = GeminiRequest(
            contents = listOf(Content(parts = listOf(Part(text = prompt)))),
            systemInstruction = Content(parts = listOf(Part(text = "You are an elite geopolitical intelligence specialist. Respond with high-signal briefings in elegant markdown formatting.")))
        )

        return try {
            val response = RetrofitClient.apiService.generateContent(apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: "Unable to parse intelligence payload from satellite. Please try again."
        } catch (e: Exception) {
            "satellite network failure: ${e.localizedMessage}. Verify network links or security clearances."
        }
    }

    suspend fun fetchCountryComparison(countryA: String, countryB: String): String {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return "COMPARISON ERROR: Gemini API Key is missing. Side-by-side numerical stats are loaded below, but dynamic comparative intelligence reports require an AI security authorization."
        }

        val prompt = """
            You are a CIA lead diplomatic envoy comparing two nations side-by-side: '$countryA' and '$countryB'.
            
            Provide a professional, objective comparative assessment. Use beautiful markdown formatting.
            
            Structure:
            
            # Deep Geopolitical Comparison: $countryA vs. $countryB
            
            ### Key Geopolitical Friction or Partnership
            (How do these two countries interact diplomatically and trade-wise? Do they share alliances or compete?)
            
            ### Socio-Economic Divergence & Convergence
            (Compare their economic models, standards of living, demographic trends, and resource strategies)
            
            ### Comparative Strategic Challenges
            (What unique geographic or security vulnerabilities does each country possess?)
            
            ### Envoy's Summary
            (A 2-sentence tactical conclusion detailing how an intelligence agency views their current relative global status)
        """.trimIndent()

        val request = GeminiRequest(
            contents = listOf(Content(parts = listOf(Part(text = prompt)))),
            systemInstruction = Content(parts = listOf(Part(text = "You are a senior analyst specializing in diplomatic relations and global macroeconomics.")))
        )

        return try {
            val response = RetrofitClient.apiService.generateContent(apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: "Unable to generate comparisons. Satellite connection timed out."
        } catch (e: Exception) {
            "Network interference: ${e.localizedMessage}."
        }
    }

    suspend fun fetchTriviaQuestion(allCountries: List<String>): String {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return ""
        }

        // We want a JSON response containing an intelligence trivia question and 4 multiple-choice options with index of correct answer
        val prompt = """
            Generate ONE ultra-interesting multiple-choice trivia question about world geography, culture, history, or capital of any nation.
            It must be fun, tricky, and related to countries. Return it ONLY in JSON layout.
            List of valid countries to pick from: ${allCountries.shuffled().take(15).joinToString()}
            
            Example Format:
            {
               "question": "Which country is home to the world's northernmost capital city?",
               "options": ["Iceland", "Norway", "Finland", "Sweden"],
               "correctIndex": 0,
               "briefingExplanation": "Reykjavik, Iceland, is the northernmost capital of a sovereign state."
            }
        """.trimIndent()

        val request = GeminiRequest(
            contents = listOf(Content(parts = listOf(Part(text = prompt)))),
            generationConfig = GenerationConfig(temperature = 0.8f, responseType = "application/json")
        )

        return try {
            val response = RetrofitClient.apiService.generateContent(apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: ""
        } catch (e: Exception) {
            ""
        }
    }

    suspend fun fetchCountryJsonData(countryName: String): String {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return ""
        }

        val prompt = """
            Provide a complete, correct, real, and professional country profile for the sovereign country requested: '$countryName'.
            Your output MUST be a parser-safe JSON object and ONLY that, with no markdown code blocks or wrapping. Try to return accurate values.
            
            JSON format:
            {
               "name": "Short common name (e.g., Ghana)",
               "officialName": "Official name (e.g., Republic of Ghana)",
               "flagEmoji": "Single emoji (e.g. 🇬🇭)",
               "capital": "Capital city (e.g. Accra)",
               "region": "Exactly one of: Africa, Americas, Asia, Europe, Oceania",
               "subregion": "Subregion name (e.g. Western Africa)",
               "population": 32800000,
               "areaSqKm": 238533.0,
               "languages": ["English", "Twi"],
               "currencies": ["Ghanaian Cedi (₵)"],
               "latitude": 7.9465,
               "longitude": -1.0232,
               "shortOverview": "A brief, highly objective 2-sentence tactical summary of geography, governance, and macroeconomic status.",
               "capitalLocation": "Geographic location of the capital",
               "climate": "Description of regional climatology and seasons",
               "governmentType": "Constitutional presidential republic",
               "headOfState": "President"
            }
            Ensure the fields exactly match.
        """.trimIndent()

        val request = GeminiRequest(
            contents = listOf(Content(parts = listOf(Part(text = prompt)))),
            generationConfig = GenerationConfig(temperature = 0.2f, responseType = "application/json")
        )

        return try {
            val response = RetrofitClient.apiService.generateContent(apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: ""
        } catch (e: Exception) {
            ""
        }
    }

    suspend fun fetchCountryDetailedFacts(countryName: String): String {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return ""
        }

        val prompt = """
            Provide a complete, correct, real, and professional analysis of the country '$countryName' focusing on geopolitical, society, and macroeconomic statistics matching the CIA World Factbook standard.
            Your output MUST be a parser-safe JSON object and ONLY that, with no markdown code blocks or wrapping. Try to return highly accurate, real-world current values.
            
            JSON format:
            {
               "background": "A comprehensive background history text block of around 150-250 words describing prehistoric origin, colonization, federation, resources, modern developments.",
               "nationalityNoun": "Noun form of nationality (e.g. Swede(s), Australian(s))",
               "nationalityAdjective": "Adjective form of nationality (e.g. Swedish, Australian)",
               "ethnicGroups": "Detailed percentage breakdown of ethnic groups (e.g. Swedish 80%, ...)",
               "languages": "Major languages spoken with official status denoted (e.g. Swedish (official), ...)",
               "religions": "Religious breakdown with percentages (e.g. Lutheran 87%, other 13%)",
               "ageStructure": "Complete age demographics structure breakdown (e.g. 0-14 years: 17%, ...)",
               "populationGrowthRate": "Population growth rate percentage (e.g. 0.8% or 1.3%)",
               "birthRate": "Birth rate statistic per 1,000 population (e.g. 11.1 births/1,000 population)",
               "deathRate": "Death rate statistic per 1,000 population (e.g. 9.1 deaths/1,000 population)",
               "netMigrationRate": "Net migration rate per 1,000 population (e.g. 5.1 migrants/1,000 population)",
               "maternalMortalityRate": "Maternal mortality rate per 100,000 live births (e.g. 4 deaths/100,000 live births)",
               "infantMortalityRate": "Infant mortality rate per 1,000 live births (e.g. 2.4 deaths/1,000 live births)",
               "lifeExpectancyAtBirth": "Average life expectancy in years (e.g. 83.1 years)",
               "totalFertilityRate": "Average children born per woman (e.g. 1.66 children born/woman)",
               "healthExpenditures": "Total health budget share of GDP (e.g. 10.9% of GDP)",
               "hivAidsAdultPrevalenceRate": "Adult HIV/AIDS prevalence rate percentage (e.g. 0.1%)"
            }
            Ensure the fields exactly match.
        """.trimIndent()

        val request = GeminiRequest(
            contents = listOf(Content(parts = listOf(Part(text = prompt)))),
            generationConfig = GenerationConfig(temperature = 0.2f, responseType = "application/json")
        )

        return try {
            val response = RetrofitClient.apiService.generateContent(apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: ""
        } catch (e: Exception) {
            ""
        }
    }
}

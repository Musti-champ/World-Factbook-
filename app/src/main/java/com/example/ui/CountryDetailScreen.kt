package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Country
import com.example.data.DetailedCountryFacts
import com.example.data.DetailedCountryFactsProvider
import com.example.data.local.DossierEntity
import com.example.ui.theme.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import kotlin.math.*

fun getActiveFacts(country: Country, savedDossier: DossierEntity?): DetailedCountryFacts {
    savedDossier?.cachedDetailedFactsJson?.let { jsonStr ->
        try {
            val obj = org.json.JSONObject(jsonStr)
            return DetailedCountryFacts(
                background = obj.optString("background", country.shortOverview),
                nationalityNoun = obj.optString("nationalityNoun", ""),
                nationalityAdjective = obj.optString("nationalityAdjective", ""),
                ethnicGroups = obj.optString("ethnicGroups", ""),
                languages = obj.optString("languages", country.languages.joinToString(", ")),
                religions = obj.optString("religions", ""),
                ageStructure = obj.optString("ageStructure", ""),
                populationGrowthRate = obj.optString("populationGrowthRate", "0.5%"),
                birthRate = obj.optString("birthRate", "11 births/1,000 population"),
                deathRate = obj.optString("deathRate", "9 deaths/1,000 population"),
                netMigrationRate = obj.optString("netMigrationRate", "3 migrants/1,000 population"),
                maternalMortalityRate = obj.optString("maternalMortalityRate", "8 deaths/100,000 live births"),
                infantMortalityRate = obj.optString("infantMortalityRate", "3 deaths/1,000 live births"),
                lifeExpectancyAtBirth = obj.optString("lifeExpectancyAtBirth", "79 years"),
                totalFertilityRate = obj.optString("totalFertilityRate", "1.7 children born/woman"),
                healthExpenditures = obj.optString("healthExpenditures", "8.5% of GDP"),
                hivAidsAdultPrevalenceRate = obj.optString("hivAidsAdultPrevalenceRate", "0.1%")
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    return DetailedCountryFactsProvider.getDetailedFacts(country.name, country)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CountryDetailScreen(
    country: Country,
    viewModel: FactbookViewModel,
    modifier: Modifier = Modifier
) {
    val bookmarks by viewModel.bookmarks.collectAsState()
    val cachedDossiers by viewModel.cachedDossiers.collectAsState()
    val aiState by viewModel.activeProfileDossier.collectAsState()
    val isBookmarked = bookmarks.containsKey(country.name)
    val savedDossier = cachedDossiers[country.name]
    val dossierTags by viewModel.dossierTags.collectAsState()
    val currentTag = dossierTags[country.name] ?: "UNCLASSIFIED"

    var userNotes by remember(country.name, savedDossier) {
        mutableStateOf(savedDossier?.analystNotes ?: "")
    }

    val focusManager = LocalFocusManager.current
    var selectedTab by remember { mutableStateOf(0) }

    // Retrieve rich merged facts
    val facts = remember(country.name, savedDossier) {
        getActiveFacts(country, savedDossier)
    }

    LaunchedEffect(country.name) {
        viewModel.loadInitialProfileDossier(country.name)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile: ${country.name}", fontWeight = FontWeight.Bold, color = IntelOnSurface) },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.navigateBack() },
                        modifier = Modifier.testTag("back_button")
                    ) {
                        Icon(Icons.AutoMirrored.Default.ArrowBack, contentDescription = "Back to list", tint = IntelGold)
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.toggleBookmark(country) },
                        modifier = Modifier.testTag("bookmark_detail_btn")
                    ) {
                        Icon(
                            imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = "Bookmark",
                            tint = if (isBookmarked) IntelGold else IntelOnBg
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = IntelDarkBg)
            )
        },
        containerColor = IntelDarkBg,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            // Static Top Identity Banner (Clean & Non-scrolling top focus)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = IntelSurface),
                border = BorderStroke(2.dp, IntelOnSurface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(PastelPinkBg, IntelSurface)
                            )
                        )
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = country.flagEmoji,
                        fontSize = 58.sp,
                        modifier = Modifier.padding(end = 16.dp)
                    )
                    Column {
                        val currentTagColor = when (currentTag) {
                            "RESTRICTED" -> Color(0xFF00639B)
                            "CONFIDENTIAL" -> Color(0xFF0A5C22)
                            "TOP SECRET" -> Color(0xFFBA1A1A)
                            else -> Color(0xFF64748B)
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(currentTagColor.copy(alpha = 0.12f))
                                .border(BorderStroke(1.dp, currentTagColor.copy(alpha = 0.8f)), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "CLASSIFICATION: $currentTag",
                                fontSize = 8.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Black,
                                color = currentTagColor,
                                letterSpacing = 0.5.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = country.officialName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = IntelOnSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "GEOGRAPHIC COORDINATES: [lat: ${country.latitude}, lng: ${country.longitude}]",
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            color = IntelOnSurface,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Material 3 Segmented/Tab Row
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = IntelDarkBg,
                contentColor = IntelGold,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = IntelGold
                    )
                },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)
            ) {
                val tabs = listOf("INTRODUCTION", "SOCIETY", "STATISTICS", "MILITARY (GFP)", "JOURNAL & AI")
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTab == index) IntelGold else IntelOnSurface.copy(alpha = 0.6f)
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Scrollable dynamic view area based on selected tab
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                when (selectedTab) {
                    0 -> {
                        // INTRODUCTION TAB
                        Text(
                            text = "INTRODUCTION",
                            style = MaterialTheme.typography.labelLarge,
                            color = IntelGold,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.5.sp,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = IntelSurface),
                            border = BorderStroke(1.dp, IntelOnSurface)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Background",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Black,
                                    color = IntelOnSurface
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = facts.background,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = IntelOnSurface,
                                    lineHeight = 22.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "LOCAL GEOGRAPHICAL NOTES",
                            style = MaterialTheme.typography.labelLarge,
                            color = IntelGold,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.5.sp,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = IntelSurface),
                            border = BorderStroke(1.dp, IntelOnSurface)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "CAPITAL METRIC & POSITION",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Black,
                                    color = IntelOnSurface
                                )
                                Text(
                                    text = "${country.capital} — ${country.capitalLocation}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = IntelOnSurface,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(14.dp))

                                Text(
                                    text = "CLIMATOLOGY NOTES",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Black,
                                    color = IntelOnSurface
                                )
                                Text(
                                    text = country.climate,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = IntelOnSurface,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    1 -> {
                        // PEOPLE AND SOCIETY TAB
                        Text(
                            text = "PEOPLE AND SOCIETY",
                            style = MaterialTheme.typography.labelLarge,
                            color = IntelGold,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.5.sp,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = IntelSurface),
                            border = BorderStroke(1.dp, IntelOnSurface)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                // Nationality
                                Text(
                                    text = "Nationality",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Black,
                                    color = IntelOnSurface
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "noun: ${facts.nationalityNoun}\nadjective: ${facts.nationalityAdjective}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = IntelOnSurface,
                                    lineHeight = 20.sp
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                HorizontalDivider(color = IntelOnSurface.copy(alpha = 0.15f))
                                Spacer(modifier = Modifier.height(12.dp))

                                // Ethnic Groups
                                Text(
                                    text = "Ethnic groups",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Black,
                                    color = IntelOnSurface
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = facts.ethnicGroups,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = IntelOnSurface,
                                    lineHeight = 20.sp
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                HorizontalDivider(color = IntelOnSurface.copy(alpha = 0.15f))
                                Spacer(modifier = Modifier.height(12.dp))

                                // Languages
                                Text(
                                    text = "Languages",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Black,
                                    color = IntelOnSurface
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = facts.languages,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = IntelOnSurface,
                                    lineHeight = 20.sp
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                HorizontalDivider(color = IntelOnSurface.copy(alpha = 0.15f))
                                Spacer(modifier = Modifier.height(12.dp))

                                // Religions
                                Text(
                                    text = "Religions",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Black,
                                    color = IntelOnSurface
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = facts.religions,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = IntelOnSurface,
                                    lineHeight = 20.sp
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                HorizontalDivider(color = IntelOnSurface.copy(alpha = 0.15f))
                                Spacer(modifier = Modifier.height(12.dp))

                                // Age Structure
                                Text(
                                    text = "Age structure",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Black,
                                    color = IntelOnSurface
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = facts.ageStructure,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = IntelOnSurface,
                                    lineHeight = 20.sp
                                )
                            }
                        }
                    }

                    2 -> {
                        // STATISTICS TAB
                        Text(
                            text = "STATISTICS",
                            style = MaterialTheme.typography.labelLarge,
                            color = IntelGold,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.5.sp,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        SovereignDemographicsBalanceChart(
                            countryName = country.name,
                            birthRate = facts.birthRate,
                            deathRate = facts.deathRate,
                            netMigrationRate = facts.netMigrationRate
                        )

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = IntelSurface),
                            border = BorderStroke(1.dp, IntelOnSurface)
                        ) {
                            Column {
                                StatFactRow(label = "Area", value = "${String.format("%,.0f", country.areaSqKm)} sq km (Density: ${String.format("%.1f", country.density)}/sq km)")
                                HorizontalDivider(color = IntelOnSurface.copy(alpha = 0.15f))
                                
                                StatFactRow(label = "Population", value = String.format("%,d", country.population))
                                HorizontalDivider(color = IntelOnSurface.copy(alpha = 0.15f))
                                
                                StatFactRow(label = "Population growth rate", value = facts.populationGrowthRate)
                                HorizontalDivider(color = IntelOnSurface.copy(alpha = 0.15f))
                                
                                StatFactRow(label = "Birth rate", value = facts.birthRate)
                                HorizontalDivider(color = IntelOnSurface.copy(alpha = 0.15f))
                                
                                StatFactRow(label = "Death rate", value = facts.deathRate)
                                HorizontalDivider(color = IntelOnSurface.copy(alpha = 0.15f))
                                
                                StatFactRow(label = "Net migration rate", value = facts.netMigrationRate)
                                HorizontalDivider(color = IntelOnSurface.copy(alpha = 0.15f))
                                
                                StatFactRow(label = "Maternal mortality rate", value = facts.maternalMortalityRate)
                                HorizontalDivider(color = IntelOnSurface.copy(alpha = 0.15f))
                                
                                StatFactRow(label = "Infant mortality rate", value = facts.infantMortalityRate)
                                HorizontalDivider(color = IntelOnSurface.copy(alpha = 0.15f))
                                
                                StatFactRow(label = "Life expectancy at birth", value = facts.lifeExpectancyAtBirth)
                                HorizontalDivider(color = IntelOnSurface.copy(alpha = 0.15f))
                                
                                StatFactRow(label = "Total fertility rate", value = facts.totalFertilityRate)
                                HorizontalDivider(color = IntelOnSurface.copy(alpha = 0.15f))
                                
                                StatFactRow(label = "Health expenditures", value = facts.healthExpenditures)
                                HorizontalDivider(color = IntelOnSurface.copy(alpha = 0.15f))
                                
                                StatFactRow(label = "HIV/AIDS - adult prevalence rate", value = facts.hivAidsAdultPrevalenceRate)
                            }
                        }
                    }

                    3 -> {
                        // MILITARY (GFP) TAB
                        Text(
                            text = "MILITARY STRENGTH & GLOBAL FIREPOWER (GFP)",
                            style = MaterialTheme.typography.labelLarge,
                            color = IntelGold,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.5.sp,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        TacticalMilitaryRadarChart(facts = facts)

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = IntelSurface),
                            border = BorderStroke(1.dp, IntelOnSurface)
                        ) {
                            Column {
                                StatFactRow(label = "GFP Index Global Rank", value = facts.gfpRank)
                                HorizontalDivider(color = IntelOnSurface.copy(alpha = 0.15f))

                                StatFactRow(label = "PowerIndex Score (PwrIndx)", value = "${facts.pwrIndx} (0.0000 is perfect score; lower is stronger)")
                                HorizontalDivider(color = IntelOnSurface.copy(alpha = 0.15f))

                                StatFactRow(label = "Active Duty Military Personnel", value = facts.activePersonnel)
                                HorizontalDivider(color = IntelOnSurface.copy(alpha = 0.15f))

                                StatFactRow(label = "Reserve Military Personnel", value = facts.reservePersonnel)
                                HorizontalDivider(color = IntelOnSurface.copy(alpha = 0.15f))

                                StatFactRow(label = "Total Airpower (Aircraft Strength)", value = facts.aircraftStrength)
                                HorizontalDivider(color = IntelOnSurface.copy(alpha = 0.15f))

                                StatFactRow(label = "Land Power (Combat Tanks Strength)", value = facts.tankStrength)
                                HorizontalDivider(color = IntelOnSurface.copy(alpha = 0.15f))

                                StatFactRow(label = "Naval Assets Strength", value = facts.navyStrength)
                                HorizontalDivider(color = IntelOnSurface.copy(alpha = 0.15f))

                                StatFactRow(label = "Annual Defense/Military Budget", value = facts.defenseBudget)
                            }
                        }
                    }

                    4 -> {
                        // AI OUTLOOK & RESEARCH JOURNAL
                        Text(
                            text = "CIA STRATEGIC AI OUTLOOK",
                            style = MaterialTheme.typography.labelLarge,
                            color = IntelGold,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.5.sp,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = PastelPinkBg),
                            border = BorderStroke(1.5.dp, IntelOnSurface)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Declassify dynamic intelligence dossier covering geographical friction points, historical axes, and economic stability metrics straight from satellite archives.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = IntelOnSurface,
                                    fontWeight = FontWeight.Bold
                                )

                                Spacer(modifier = Modifier.height(14.dp))

                                Button(
                                    onClick = { viewModel.loadAiDossier(country.name) },
                                    colors = ButtonDefaults.buttonColors(containerColor = IntelOnSurface, contentColor = Color.White),
                                    border = BorderStroke(1.5.dp, IntelOnSurface),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("fetch_ai_dossier_btn"),
                                    shape = RoundedCornerShape(30.dp)
                                ) {
                                    Icon(Icons.Default.Analytics, contentDescription = "Query model", modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    val stateSuccess = aiState as? ProfileAiState.Success
                                    val isOnline = stateSuccess?.isOnlineRecent ?: false
                                    Text(if (isOnline) "SYNC LATEST FACTS ONLINE AGAIN" else "DECLASSIFY LATEST FACTS ONLINE", fontWeight = FontWeight.Black, letterSpacing = 0.5.sp)
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                when (aiState) {
                                    ProfileAiState.Idle -> { }
                                    ProfileAiState.Loading -> {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(24.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                CircularProgressIndicator(color = IntelOnSurface)
                                                Spacer(modifier = Modifier.height(8.dp))
                                                Text(
                                                    "Decrypting strategic report channel...",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = IntelOnSurface,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                    is ProfileAiState.Success -> {
                                        val success = aiState as ProfileAiState.Success
                                        Column(modifier = Modifier.fillMaxWidth()) {
                                            val statusColor = if (success.isOnlineRecent) Color(0xFF0A5C22) else Color(0xFF00639B)
                                            val statusText = if (success.isOnlineRecent) "🟢 ONLINE INTEL DIRECT FEED" else "🔵 SECURE OFFLINE LOCAL ARCHIVE"
                                            Box(
                                                modifier = Modifier
                                                    .padding(bottom = 8.dp)
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(statusColor.copy(alpha = 0.12f))
                                                    .border(BorderStroke(1.dp, statusColor.copy(alpha = 0.8f)), RoundedCornerShape(4.dp))
                                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                                            ) {
                                                Text(
                                                    text = statusText,
                                                    fontSize = 9.sp,
                                                    fontFamily = FontFamily.Monospace,
                                                    fontWeight = FontWeight.Black,
                                                    color = statusColor
                                                )
                                            }

                                            Text(
                                                text = success.briefingMarkdown,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = IntelOnSurface,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(IntelSurface)
                                                .border(
                                                    BorderStroke(1.5.dp, IntelOnSurface),
                                                    shape = RoundedCornerShape(12.dp)
                                                )
                                                .padding(16.dp)
                                            )
                                        }
                                    }
                                    is ProfileAiState.Error -> {
                                        val error = aiState as ProfileAiState.Error
                                        Column(modifier = Modifier.fillMaxWidth()) {
                                        Card(
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = CardDefaults.cardColors(containerColor = IntelAlert.copy(alpha = 0.15f)),
                                            border = BorderStroke(1.5.dp, IntelAlert),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(12.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(Icons.Default.Error, contentDescription = "Error", tint = IntelAlert)
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = error.errorMessage,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = IntelAlert,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }

                                        error.fallbackMarkdown?.let { fallback ->
                                            Spacer(modifier = Modifier.height(12.dp))
                                            Text(
                                                text = fallback,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = IntelOnSurface,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .background(IntelSurface)
                                                    .border(
                                                        BorderStroke(1.5.dp, IntelOnSurface),
                                                        shape = RoundedCornerShape(12.dp)
                                                    )
                                                    .padding(16.dp)
                                            )
                                        }
                                    }
                                }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Text(
                            text = "ANALYST PRIVATE RESEARCH JOURNAL",
                            style = MaterialTheme.typography.labelLarge,
                            color = IntelGold,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.5.sp,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = IntelSurface),
                            border = BorderStroke(1.5.dp, IntelOnSurface)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Log observations, diplomatic events, or intelligence indicators. Notes are saved securely inside local persistent memory caches and remain accessible offline.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = IntelOnSurface,
                                    fontWeight = FontWeight.Bold
                                )

                                Spacer(modifier = Modifier.height(14.dp))

                                Text(
                                    text = "SECURITY CLASSIFICATION LEVEL:",
                                    fontSize = 9.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Black,
                                    color = IntelGold,
                                    letterSpacing = 0.5.sp
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    val classificationList = listOf("UNCLASSIFIED", "RESTRICTED", "CONFIDENTIAL", "TOP SECRET")
                                    classificationList.forEach { tagOption ->
                                        val isTagSelected = currentTag == tagOption
                                        val tagColor = when (tagOption) {
                                            "RESTRICTED" -> Color(0xFF00639B)
                                            "CONFIDENTIAL" -> Color(0xFF0A5C22)
                                            "TOP SECRET" -> Color(0xFFBA1A1A)
                                            else -> Color(0xFF64748B)
                                        }
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(if (isTagSelected) tagColor else IntelSurface)
                                                .border(
                                                    BorderStroke(
                                                        1.5.dp,
                                                        if (isTagSelected) IntelOnSurface else IntelOnSurface.copy(alpha = 0.15f)
                                                    ),
                                                    RoundedCornerShape(8.dp)
                                                )
                                                .clickable { viewModel.saveDossierTag(country.name, tagOption) }
                                                .padding(vertical = 6.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = tagOption,
                                                fontSize = 8.sp,
                                                fontWeight = FontWeight.Black,
                                                color = if (isTagSelected) Color.White else IntelOnSurface,
                                                maxLines = 1
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                TextField(
                                    value = userNotes,
                                    onValueChange = { userNotes = it },
                                    placeholder = { Text("Record strategic observations...", color = IntelMuted, fontWeight = FontWeight.Bold) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(100.dp)
                                        .testTag("analyst_notes_input")
                                        .border(BorderStroke(1.5.dp, IntelOnSurface), RoundedCornerShape(12.dp))
                                        .clip(RoundedCornerShape(12.dp)),
                                    colors = TextFieldDefaults.colors(
                                        focusedContainerColor = PastelPinkBg,
                                        unfocusedContainerColor = IntelSurfaceVariant,
                                        focusedTextColor = IntelOnSurface,
                                        unfocusedTextColor = IntelOnSurface,
                                        focusedIndicatorColor = Color.Transparent,
                                        unfocusedIndicatorColor = Color.Transparent
                                    )
                                )

                                Spacer(modifier = Modifier.height(14.dp))

                                Button(
                                    onClick = {
                                        viewModel.saveAnalystNotes(country.name, userNotes)
                                        focusManager.clearFocus()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = IntelOnSurface, contentColor = Color.White),
                                    border = BorderStroke(1.5.dp, IntelOnSurface),
                                    shape = RoundedCornerShape(30.dp),
                                    modifier = Modifier
                                        .align(Alignment.End)
                                        .testTag("save_notes_btn")
                                ) {
                                    Icon(Icons.Default.Save, contentDescription = "Commit to DB", modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("COMMIT TO DISK", fontWeight = FontWeight.Black, letterSpacing = 0.5.sp)
                                }

                                AnimatedVisibility(visible = savedDossier?.analystNotes == userNotes && userNotes.isNotEmpty()) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(top = 8.dp)
                                    ) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = "Saved", tint = IntelOnSurface, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Dossier write compiled: saved successfully to Room storage.", fontSize = 11.sp, color = IntelOnSurface, fontWeight = FontWeight.Black)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatFactRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp, horizontal = 16.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Black,
            color = IntelOnSurface,
            letterSpacing = 0.5.sp
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = IntelOnSurface.copy(alpha = 0.8f),
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun SovereignDemographicsBalanceChart(
    countryName: String,
    birthRate: String,
    deathRate: String,
    netMigrationRate: String,
    modifier: Modifier = Modifier
) {
    val birthVal = remember(birthRate) { parseToFloatClean(birthRate) }
    val deathVal = remember(deathRate) { parseToFloatClean(deathRate) }
    val migVal = remember(netMigrationRate) { parseToFloatClean(netMigrationRate) }

    // Visual intelligence indicators
    val growthInd = (birthVal + migVal) - deathVal
    val stateLabel = if (growthInd > 0) "NET POSITIVE CORRELATION" else "NET CONSOLIDATION CYCLE"
    val stateDesc = if (growthInd > 0) {
        "Demographic vectors indicate steady internal expansion (+${String.format("%.1f", growthInd)} per 1,000 population annually)."
    } else if (growthInd < 0) {
        "Demographic vectors indicate structural contraction (${String.format("%.1f", growthInd)} per 1,000 population annually)."
    } else {
        "Demographic equilibrium maintained."
    }

    var selectedBar by remember { mutableStateOf(-1) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
            .testTag("demographics_visual_chart"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = IntelSurface),
        border = BorderStroke(1.5.dp, IntelOnSurface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "TACTICAL DEMOGRAPHIC SPECTRUM",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Black,
                color = IntelGold,
                letterSpacing = 1.sp
            )
            Text(
                text = "Annual Flows (per 1,000 population)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = IntelOnSurface
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Canvas Drawing
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(IntelDarkBg)
                    .border(BorderStroke(1.dp, IntelOnSurface.copy(alpha = 0.08f)), RoundedCornerShape(8.dp))
                    .pointerInput(birthVal, deathVal, migVal) {
                        detectTapGestures { offset ->
                            val w = size.width
                            val third = w / 3f
                            val col = (offset.x / third).toInt().coerceIn(0, 2)
                            selectedBar = if (selectedBar == col) -1 else col
                        }
                    }
            ) {
                Canvas(modifier = Modifier.fillMaxSize().padding(14.dp)) {
                    val canvasWidth = size.width
                    val canvasHeight = size.height

                    // Max scale Reference
                    val maxVal = maxOf(15f, birthVal, deathVal, abs(migVal)) * 1.2f

                    // Draw grid lines
                    val gridLines = 4
                    for (i in 0..gridLines) {
                        val frac = i.toFloat() / gridLines
                        val y = canvasHeight * (1f - frac)
                        
                        // Thin grid dashes
                        drawLine(
                            color = IntelOnSurface.copy(alpha = 0.08f),
                            start = Offset(0f, y),
                            end = Offset(canvasWidth, y),
                            strokeWidth = 1.dp.toPx()
                        )
                    }

                    // Three bars (Birth, Death, Migration)
                    val barWidth = canvasWidth / 5f
                    val gap = canvasWidth / 15f

                    // 1. Birth Bar
                    val bHeight = (birthVal / maxVal).coerceIn(0f, 1f) * canvasHeight
                    val bLeft = gap * 1.5f
                    val bColor = if (selectedBar == 0 || selectedBar == -1) IntelSecondary else IntelSecondary.copy(alpha = 0.4f)
                    drawRoundRect(
                        color = bColor,
                        topLeft = Offset(bLeft, canvasHeight - bHeight),
                        size = androidx.compose.ui.geometry.Size(barWidth, bHeight),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(6.dp.toPx(), 6.dp.toPx())
                    )

                    // 2. Death Bar
                    val dHeight = (deathVal / maxVal).coerceIn(0f, 1f) * canvasHeight
                    val dLeft = bLeft + barWidth + gap
                    val dColor = if (selectedBar == 1 || selectedBar == -1) IntelAlert else IntelAlert.copy(alpha = 0.4f)
                    drawRoundRect(
                        color = dColor,
                        topLeft = Offset(dLeft, canvasHeight - dHeight),
                        size = androidx.compose.ui.geometry.Size(barWidth, dHeight),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(6.dp.toPx(), 6.dp.toPx())
                    )

                    // 3. Migration Bar
                    val mHeight = (abs(migVal) / maxVal).coerceIn(0f, 1f) * canvasHeight
                    val mLeft = dLeft + barWidth + gap
                    val mColor = if (migVal >= 0) {
                        if (selectedBar == 2 || selectedBar == -1) Color(0xFF0A5C22) else Color(0xFF0A5C22).copy(alpha = 0.4f)
                    } else {
                        if (selectedBar == 2 || selectedBar == -1) Color(0xFFFF8A00) else Color(0xFFFF8A00).copy(alpha = 0.4f)
                    }
                    val mTop = if (migVal >= 0) canvasHeight - mHeight else canvasHeight - 4.dp.toPx()
                    val mActualHeight = max(mHeight, 4.dp.toPx())
                    drawRoundRect(
                        color = mColor,
                        topLeft = Offset(mLeft, mTop),
                        size = androidx.compose.ui.geometry.Size(barWidth, mActualHeight),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(6.dp.toPx(), 6.dp.toPx())
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Legends & interactivity info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(10.dp).clip(RoundedCornerShape(2.dp)).background(IntelSecondary))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Births (${String.format("%.1f", birthVal)})", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = IntelOnSurface)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(10.dp).clip(RoundedCornerShape(2.dp)).background(IntelAlert))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Deaths (${String.format("%.1f", deathVal)})", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = IntelOnSurface)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(10.dp).clip(RoundedCornerShape(2.dp)).background(if (migVal >= 0) Color(0xFF0A5C22) else Color(0xFFFF8A00)))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Migration (${String.format("%.1f", migVal)})", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = IntelOnSurface)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Explanatory observation
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (growthInd >= 0) PastelGreenBg else PastelPinkBg)
                    .border(BorderStroke(1.dp, if (growthInd >= 0) PastelGreenIcon.copy(alpha = 0.3f) else PastelPinkIcon.copy(alpha = 0.3f)), RoundedCornerShape(12.dp))
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (growthInd > 0) Icons.Default.TrendingUp else if (growthInd < 0) Icons.Default.TrendingDown else Icons.Default.TrendingFlat,
                    contentDescription = null,
                    tint = if (growthInd >= 0) PastelGreenIcon else PastelPinkIcon,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = stateLabel,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = if (growthInd >= 0) PastelGreenText else PastelPinkText,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = stateDesc,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (growthInd >= 0) PastelGreenText else PastelPinkText,
                        lineHeight = 15.sp
                    )
                }
            }
        }
    }
}

@Composable
fun TacticalMilitaryRadarChart(
    facts: DetailedCountryFacts,
    modifier: Modifier = Modifier
) {
    val activeVal = remember(facts.activePersonnel) { parseToFloatClean(facts.activePersonnel) }
    val reserveVal = remember(facts.reservePersonnel) { parseToFloatClean(facts.reservePersonnel) }
    val airVal = remember(facts.aircraftStrength) { parseToFloatClean(facts.aircraftStrength) }
    val tankVal = remember(facts.tankStrength) { parseToFloatClean(facts.tankStrength) }
    val navyVal = remember(facts.navyStrength) { parseToFloatClean(facts.navyStrength) }
    val budgetVal = remember(facts.defenseBudget) { parseToFloatClean(facts.defenseBudget) }

    // Constants for reasonable normalization benchmarks
    val activeMax = 500_000f
    val reserveMax = 500_000f
    val airMax = 1_000f
    val tankMax = 1_500f
    val navyMax = 200f
    val budgetMax = 20_000_000_000f // $20 Billion

    val activeRatio = (activeVal / activeMax).coerceIn(0.05f, 1.0f)
    val reserveRatio = (reserveVal / reserveMax).coerceIn(0.05f, 1.0f)
    val airRatio = (airVal / airMax).coerceIn(0.05f, 1.0f)
    val tankRatio = (tankVal / tankMax).coerceIn(0.05f, 1.0f)
    val navyRatio = (navyVal / navyMax).coerceIn(0.05f, 1.0f)
    val budgetRatio = (budgetVal / budgetMax).coerceIn(0.05f, 1.0f)

    val ratios = listOf(activeRatio, reserveRatio, airRatio, tankRatio, navyRatio, budgetRatio)
    val labels = listOf("ACTIVE", "RESERVES", "AIRPOWER", "TANKS", "NAVY", "BUDGET")
    val rawVals = listOf(
        facts.activePersonnel,
        facts.reservePersonnel,
        facts.aircraftStrength,
        facts.tankStrength,
        facts.navyStrength,
        facts.defenseBudget
    )

    var highlightedSpoke by remember { mutableStateOf(-1) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
            .testTag("military_radar_chart"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = IntelSurface),
        border = BorderStroke(1.5.dp, IntelOnSurface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "TACTICAL CAPABILITY RADAR WEB",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Black,
                color = IntelGold,
                letterSpacing = 1.sp
            )
            Text(
                text = "6-Dimensional Threat Assessment",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = IntelOnSurface
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Radar Canvas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectTapGestures { offset ->
                                val cx = size.width / 2f
                                val cy = size.height / 2f
                                val dx = offset.x - cx
                                val dy = offset.y - cy
                                var angle = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()
                                if (angle < 0) angle += 360f
                                
                                val spokeAngles = listOf(270f, 330f, 30f, 90f, 150f, 210f)
                                var minDiff = 360f
                                var bestIdx = -1
                                spokeAngles.forEachIndexed { idx, spAngle ->
                                    var diff = abs(angle - spAngle)
                                    if (diff > 180f) diff = 360f - diff
                                    if (diff < minDiff) {
                                        minDiff = diff
                                        bestIdx = idx
                                    }
                                }
                                highlightedSpoke = if (highlightedSpoke == bestIdx) -1 else bestIdx
                            }
                        }
                ) {
                    val cx = size.width / 2f
                    val cy = size.height / 2f
                    val maxRadius = min(size.width, size.height) / 2.3f

                    // 1. Draw Web Levels (Concentric Hexagons)
                    val levels = 4
                    for (l in 1..levels) {
                        val frac = l.toFloat() / levels
                        val r = maxRadius * frac
                        val lvlPath = Path()
                        for (i in 0..5) {
                            val angleRad = (i * 60 - 90) * (PI / 180f)
                            val px = cx + (r * cos(angleRad)).toFloat()
                            val py = cy + (r * sin(angleRad)).toFloat()
                            if (i == 0) lvlPath.moveTo(px, py) else lvlPath.lineTo(px, py)
                        }
                        lvlPath.close()
                        drawPath(
                            path = lvlPath,
                            color = IntelOnSurface.copy(alpha = 0.08f),
                            style = Stroke(width = 1.dp.toPx())
                        )
                    }

                    // 2. Draw 6 Spokes radiating from center
                    for (i in 0..5) {
                        val angleRad = (i * 60 - 90) * (PI / 180f)
                        val px = cx + (maxRadius * cos(angleRad)).toFloat()
                        val py = cy + (maxRadius * sin(angleRad)).toFloat()
                        drawLine(
                            color = if (i == highlightedSpoke) IntelGold else IntelOnSurface.copy(alpha = 0.1f),
                            start = Offset(cx, cy),
                            end = Offset(px, py),
                            strokeWidth = if (i == highlightedSpoke) 2.dp.toPx() else 1.dp.toPx()
                        )
                    }

                    // 3. Draw Web Filled Polygon and vertex points
                    val filledPath = Path()
                    val points = mutableListOf<Offset>()
                    for (i in 0..5) {
                        val angleRad = (i * 60 - 90) * (PI / 180f)
                        val dist = maxRadius * ratios[i]
                        val px = cx + (dist * cos(angleRad)).toFloat()
                        val py = cy + (dist * sin(angleRad)).toFloat()
                        points.add(Offset(px, py))
                        if (i == 0) filledPath.moveTo(px, py) else filledPath.lineTo(px, py)
                    }
                    filledPath.close()

                    // Fill with secondary alpha
                    drawPath(
                        path = filledPath,
                        color = IntelSecondary.copy(alpha = 0.3f)
                    )

                    // Draw outer border path
                    drawPath(
                        path = filledPath,
                        color = IntelSecondary,
                        style = Stroke(width = 2.dp.toPx())
                    )

                    // Draw vertices
                    points.forEachIndexed { index, point ->
                        val color = if (index == highlightedSpoke) IntelGold else IntelSecondary
                        val radius = if (index == highlightedSpoke) 6.dp.toPx() else 4.dp.toPx()
                        drawCircle(
                            color = color,
                            radius = radius,
                            center = point
                        )
                    }
                }

                // Overlay labels positioned around the radar web
                Text(
                    text = "ACTIVE",
                    modifier = Modifier.align(Alignment.TopCenter).padding(top = 4.dp),
                    fontSize = 9.sp, fontWeight = FontWeight.Black, color = if (highlightedSpoke == 0) IntelGold else IntelMuted
                )
                Text(
                    text = "RESERVES",
                    modifier = Modifier.align(Alignment.TopEnd).padding(top = 48.dp, end = 12.dp),
                    fontSize = 9.sp, fontWeight = FontWeight.Black, color = if (highlightedSpoke == 1) IntelGold else IntelMuted
                )
                Text(
                    text = "AIRPOWER",
                    modifier = Modifier.align(Alignment.BottomEnd).padding(bottom = 48.dp, end = 12.dp),
                    fontSize = 9.sp, fontWeight = FontWeight.Black, color = if (highlightedSpoke == 2) IntelGold else IntelMuted
                )
                Text(
                    text = "TANKS",
                    modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 4.dp),
                    fontSize = 9.sp, fontWeight = FontWeight.Black, color = if (highlightedSpoke == 3) IntelGold else IntelMuted
                )
                Text(
                    text = "NAVY",
                    modifier = Modifier.align(Alignment.BottomStart).padding(bottom = 48.dp, start = 12.dp),
                    fontSize = 9.sp, fontWeight = FontWeight.Black, color = if (highlightedSpoke == 4) IntelGold else IntelMuted
                )
                Text(
                    text = "BUDGET",
                    modifier = Modifier.align(Alignment.TopStart).padding(top = 48.dp, start = 12.dp),
                    fontSize = 9.sp, fontWeight = FontWeight.Black, color = if (highlightedSpoke == 5) IntelGold else IntelMuted
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Visual Intelligence Detail card
            val focusIdx = if (highlightedSpoke != -1) highlightedSpoke else ratios.indexOf(ratios.maxOrNull() ?: activeRatio)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .border(BorderStroke(1.5.dp, IntelOnSurface), RoundedCornerShape(12.dp))
                    .background(PastelPinkBg)
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.MilitaryTech,
                    contentDescription = null,
                    tint = IntelSecondary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "TACTICAL STRENGTH SPECTRUM FOCI: ${labels[focusIdx]}",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        color = IntelSecondary,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "This asset group records a rating of ${rawVals[focusIdx]}. Operational capabilities and global metrics are plotted relative to regional standards.",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = IntelOnSurface,
                        lineHeight = 15.sp
                    )
                }
            }
        }
    }
}

private fun parseToFloatClean(text: String): Float {
    if (text == "N/A" || text.isBlank()) return 0f
    val clean = text.replace("$", "").replace("%", "").replace(",", "").trim()
    
    val multiplier = when {
        clean.contains("billion", ignoreCase = true) -> 1_000_000_000f
        clean.contains("million", ignoreCase = true) -> 1_000_000f
        else -> 1f
    }
    
    val numberPart = clean.replace("billion", "", ignoreCase = true)
                         .replace("million", "", ignoreCase = true)
                         .replace("births/1,000 population", "", ignoreCase = true)
                         .replace("deaths/1,000 population", "", ignoreCase = true)
                         .replace("migrant(s)/1,000 population", "", ignoreCase = true)
                         .trim()
                         
    val matcher = java.util.regex.Pattern.compile("-?\\d+([.]\\d+)?").matcher(numberPart)
    if (matcher.find()) {
        return (matcher.group().toFloatOrNull() ?: 0f) * multiplier
    }
    return 0f
}

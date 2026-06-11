package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Country
import com.example.ui.theme.*

@Composable
fun FavoritesScreen(
    viewModel: FactbookViewModel,
    modifier: Modifier = Modifier
) {
    val bookmarks by viewModel.bookmarks.collectAsState()
    val allCountries by viewModel.allCountries.collectAsState()
    val dossierTags by viewModel.dossierTags.collectAsState()
    val savedComparisons by viewModel.savedComparisons.collectAsState()

    var selectedTab by remember { mutableStateOf(0) }

    // Filter countries that are in the bookmarks map
    val favoritedCountries = remember(bookmarks, allCountries) {
        allCountries.filter { bookmarks.containsKey(it.name) }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(IntelDarkBg)
            .padding(16.dp)
    ) {
        // Title block
        Text(
            text = "AGENT PERSONAL DATA VAULT",
            style = MaterialTheme.typography.labelMedium,
            color = IntelGold,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp
        )
        Text(
            text = "Personal Dossiers",
            style = MaterialTheme.typography.headlineLarge,
            color = IntelOnSurface,
            fontWeight = FontWeight.ExtraBold
        )

        Spacer(modifier = Modifier.height(18.dp))

        // TAB NAVIGATION
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = IntelDarkBg,
            contentColor = IntelGold,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = IntelGold
                )
            },
            divider = { HorizontalDivider(color = IntelOnSurface.copy(alpha = 0.12f)) }
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("SOVEREIGN ARCHIVES", fontWeight = FontWeight.Black, fontSize = 11.sp, letterSpacing = 0.5.sp) },
                selectedContentColor = IntelGold,
                unselectedContentColor = IntelMuted,
                modifier = Modifier.testTag("tab_sovereign_profiles")
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("BILATERAL STRATEGIES", fontWeight = FontWeight.Black, fontSize = 11.sp, letterSpacing = 0.5.sp) },
                selectedContentColor = IntelGold,
                unselectedContentColor = IntelMuted,
                modifier = Modifier.testTag("tab_saved_comparisons")
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        if (selectedTab == 0) {
            // ORIGINAL PROFILE ARCHIVE
            if (favoritedCountries.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.FolderSpecial,
                            contentDescription = "Empty file cabinet",
                            tint = IntelMuted,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Your profile vault is empty.",
                            style = MaterialTheme.typography.titleMedium,
                            color = IntelOnSurface,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Bookmark sovereign nations in the main factbook archive to build personal intelligence dossiers and write regional observer logs.",
                            style = MaterialTheme.typography.bodySmall,
                            color = IntelMuted,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 24.dp)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        AgentVaultSecurityBreakdownChart(
                            favoritedCountries = favoritedCountries,
                            dossierTags = dossierTags
                        )
                    }
                    items(favoritedCountries) { country ->
                        val dossier = bookmarks[country.name]
                        val privateNotes = dossier?.analystNotes ?: ""
                        val currentTag = dossierTags[country.name] ?: "UNCLASSIFIED"

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.navigateTo(Screen.CountryDetail(country)) }
                                .testTag("favorite_card_${country.name.lowercase().replace(" ", "_")}"),
                            colors = CardDefaults.cardColors(containerColor = IntelSurface),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, IntelOnSurface.copy(alpha = 0.12f))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = country.flagEmoji,
                                        fontSize = 42.sp,
                                        modifier = Modifier.padding(end = 14.dp)
                                    )
                                    Column(modifier = Modifier.weight(1f)) {
                                        val currentTagColor = when (currentTag) {
                                            "RESTRICTED" -> Color(0xFF00639B)
                                            "CONFIDENTIAL" -> Color(0xFF0A5C22)
                                            "TOP SECRET" -> Color(0xFFBA1A1A)
                                            else -> Color(0xFF64748B)
                                        }
                                        Box(
                                            modifier = Modifier
                                                .padding(bottom = 4.dp)
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(currentTagColor.copy(alpha = 0.12f))
                                                .border(BorderStroke(1.dp, currentTagColor.copy(alpha = 0.8f)), RoundedCornerShape(4.dp))
                                                .padding(horizontal = 5.dp, vertical = 1.5.dp)
                                        ) {
                                            Text(
                                                text = currentTag,
                                                fontSize = 7.5.sp,
                                                fontFamily = FontFamily.Monospace,
                                                fontWeight = FontWeight.Black,
                                                color = currentTagColor,
                                                letterSpacing = 0.5.sp
                                            )
                                        }

                                        Text(
                                            text = country.name,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Black,
                                            color = IntelOnSurface
                                        )
                                        Text(
                                            text = "Capital: ${country.capital} • Region: ${country.region}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = IntelOnSurface,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    IconButton(
                                        onClick = { viewModel.toggleBookmark(country) },
                                        modifier = Modifier.testTag("unbookmark_btn_${country.name.lowercase().replace(" ", "_")}")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Bookmark,
                                            contentDescription = "Unbookmark",
                                            tint = IntelGold
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = country.shortOverview,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = IntelOnBg.copy(alpha = 0.8f),
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )

                                if (privateNotes.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(PastelPinkBg)
                                            .border(
                                                BorderStroke(1.dp, IntelOnSurface.copy(alpha = 0.15f)),
                                                shape = RoundedCornerShape(12.dp)
                                            )
                                            .padding(12.dp)
                                    ) {
                                        Column {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Default.StickyNote2,
                                                    contentDescription = "Analyst notes icon",
                                                    tint = IntelOnSurface,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = "OBSERVATION LOG BRIEFING",
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Black,
                                                    color = IntelOnSurface,
                                                    fontFamily = FontFamily.Monospace,
                                                    letterSpacing = 0.5.sp
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(
                                                text = privateNotes,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = IntelOnSurface,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // NEW BILATERAL STRATEGIES COMP-FILE ARCHES
            if (savedComparisons.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.CompareArrows,
                            contentDescription = "Empty comparison stack",
                            tint = IntelMuted,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Your bilateral vault is empty.",
                            style = MaterialTheme.typography.titleMedium,
                            color = IntelOnSurface,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Configure dual sovereign comparisons on the briefing deck and log strategic briefings to catalog comparative records.",
                            style = MaterialTheme.typography.bodySmall,
                            color = IntelMuted,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 24.dp)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(savedComparisons) { comparison ->
                        val countryA = allCountries.find { it.name == comparison.countryNameA }
                        val countryB = allCountries.find { it.name == comparison.countryNameB }

                        val flagA = countryA?.flagEmoji ?: "🏳️"
                        val flagB = countryB?.flagEmoji ?: "🏳️"

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (countryA != null && countryB != null) {
                                        viewModel.setCompareCountry("A", countryA)
                                        viewModel.setCompareCountry("B", countryB)
                                        viewModel.navigateTo(Screen.Compare)
                                    }
                                }
                                .testTag("comparison_card_${comparison.id.lowercase()}"),
                            colors = CardDefaults.cardColors(containerColor = IntelSurface),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, IntelOnSurface.copy(alpha = 0.12f))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Flags Side by Side
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(end = 12.dp)
                                    ) {
                                        Text(flagA, fontSize = 28.sp)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(
                                            imageVector = Icons.Default.CompareArrows,
                                            contentDescription = null,
                                            tint = IntelGold,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(flagB, fontSize = 28.sp)
                                    }

                                    Column(modifier = Modifier.weight(1f)) {
                                        Box(
                                            modifier = Modifier
                                                .padding(bottom = 4.dp)
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(IntelSecondary.copy(alpha = 0.12f))
                                                .border(BorderStroke(1.dp, IntelSecondary.copy(alpha = 0.8f)), RoundedCornerShape(4.dp))
                                                .padding(horizontal = 5.dp, vertical = 1.5.dp)
                                        ) {
                                            Text(
                                                text = "COMP-FILE: DOSS-CO-${comparison.countryNameA.take(3).uppercase()}-${comparison.countryNameB.take(3).uppercase()}",
                                                fontSize = 7.5.sp,
                                                fontFamily = FontFamily.Monospace,
                                                fontWeight = FontWeight.Black,
                                                color = IntelSecondary,
                                                letterSpacing = 0.5.sp
                                            )
                                        }

                                        Text(
                                            text = "${comparison.countryNameA} vs ${comparison.countryNameB}",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Black,
                                            color = IntelOnSurface
                                        )
                                    }

                                    IconButton(
                                        onClick = { viewModel.deleteGeopoliticalComparison(comparison.id) },
                                        modifier = Modifier.testTag("delete_comparison_btn_${comparison.id.lowercase()}")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete comparison",
                                            tint = IntelAlert
                                        )
                                    }
                                }

                                if (comparison.analystNotes.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(PastelPinkBg)
                                            .border(
                                                BorderStroke(1.dp, IntelOnSurface.copy(alpha = 0.15f)),
                                                shape = RoundedCornerShape(12.dp)
                                            )
                                            .padding(12.dp)
                                    ) {
                                        Column {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Default.StickyNote2,
                                                    contentDescription = "Debrief notes icon",
                                                    tint = IntelOnSurface,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = "STRATEGIC MEMORANDUM BRIEFING",
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Black,
                                                    color = IntelOnSurface,
                                                    fontFamily = FontFamily.Monospace,
                                                    letterSpacing = 0.5.sp
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(
                                                text = comparison.analystNotes,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = IntelOnSurface,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 3,
                                                overflow = TextOverflow.Ellipsis
                                            )
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
}

@Composable
fun AgentVaultSecurityBreakdownChart(
    favoritedCountries: List<Country>,
    dossierTags: Map<String, String>,
    modifier: Modifier = Modifier
) {
    val totalCount = favoritedCountries.size
    if (totalCount == 0) return

    val counts = remember(favoritedCountries, dossierTags) {
        val distribution = mutableMapOf(
            "TOP SECRET" to 0,
            "CONFIDENTIAL" to 0,
            "RESTRICTED" to 0,
            "UNCLASSIFIED" to 0
        )
        favoritedCountries.forEach { country ->
            val tag = dossierTags[country.name] ?: "UNCLASSIFIED"
            distribution[tag] = (distribution[tag] ?: 0) + 1
        }
        distribution
    }

    val topSecretCount = counts["TOP SECRET"] ?: 0
    val confidentialCount = counts["CONFIDENTIAL"] ?: 0
    val restrictedCount = counts["RESTRICTED"] ?: 0
    val unclassifiedCount = counts["UNCLASSIFIED"] ?: 0

    val topSecretPct = if (totalCount > 0) topSecretCount.toFloat() / totalCount else 0.01f
    val confidentialPct = if (totalCount > 0) confidentialCount.toFloat() / totalCount else 0.01f
    val restrictedPct = if (totalCount > 0) restrictedCount.toFloat() / totalCount else 0.01f
    val unclassifiedPct = if (totalCount > 0) unclassifiedCount.toFloat() / totalCount else 0.01f

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
            .testTag("vault_security_breakdown"),
        colors = CardDefaults.cardColors(containerColor = IntelSurfaceVariant),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, IntelOnSurface.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "AGENT SECURITY CLEARANCE PORTFOLIO",
                        style = MaterialTheme.typography.labelSmall,
                        color = IntelGold,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Vault Classification Density",
                        style = MaterialTheme.typography.titleSmall,
                        color = IntelOnSurface,
                        fontWeight = FontWeight.Bold
                    )
                }
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = IntelGold,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Stacked Segmented Horizontal Bar Chart
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(16.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(IntelDarkBg)
            ) {
                Row(modifier = Modifier.fillMaxSize()) {
                    if (topSecretCount > 0) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .weight(topSecretPct)
                                .background(Color(0xFFBA1A1A))
                        )
                    }
                    if (confidentialCount > 0) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .weight(confidentialPct)
                                .background(Color(0xFF0A5C22))
                        )
                    }
                    if (restrictedCount > 0) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .weight(restrictedPct)
                                .background(Color(0xFF00639B))
                        )
                    }
                    if (unclassifiedCount > 0) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .weight(unclassifiedPct)
                                .background(Color(0xFF64748B))
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Legend / Metrics section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ClassificationLegendItem(
                    label = "TOP SECRET",
                    count = topSecretCount,
                    percentage = (topSecretPct * 100).toInt(),
                    color = Color(0xFFBA1A1A)
                )
                ClassificationLegendItem(
                    label = "CONFIDENTIAL",
                    count = confidentialCount,
                    percentage = (confidentialPct * 100).toInt(),
                    color = Color(0xFF0A5C22)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ClassificationLegendItem(
                    label = "RESTRICTED",
                    count = restrictedCount,
                    percentage = (restrictedPct * 100).toInt(),
                    color = Color(0xFF00639B)
                )
                ClassificationLegendItem(
                    label = "UNCLASSIFIED",
                    count = unclassifiedCount,
                    percentage = (unclassifiedPct * 100).toInt(),
                    color = Color(0xFF64748B)
                )
            }
        }
    }
}

@Composable
fun ClassificationLegendItem(
    label: String,
    count: Int,
    percentage: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.width(140.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(color)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = "$label ($count)",
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = IntelOnSurface
            )
            Text(
                text = if (count > 0) "$percentage% of archives" else "0% of archives",
                fontSize = 8.sp,
                color = IntelMuted
            )
        }
    }
}

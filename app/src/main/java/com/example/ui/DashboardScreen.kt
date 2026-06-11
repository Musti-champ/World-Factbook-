package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Country
import com.example.ui.theme.*
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.ui.text.font.FontFamily
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun DashboardScreen(
    viewModel: FactbookViewModel,
    modifier: Modifier = Modifier
) {
    val countries by viewModel.allCountries.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedRegion by viewModel.selectedRegion.collectAsState()
    val bookmarks by viewModel.bookmarks.collectAsState()
    val searchAllState by viewModel.searchAllState.collectAsState()

    // Control which view mode they prefer: Feed Stream vs Grid Matrix
    var isGridViewActive by remember { mutableStateOf(false) }

    // Filtered countries based on search and regional story selections
    val filteredCountries = remember(searchQuery, selectedRegion, countries) {
        countries.filter { country ->
            val matchSearch = country.name.contains(searchQuery, ignoreCase = true) ||
                    country.capital.contains(searchQuery, ignoreCase = true) ||
                    country.officialName.contains(searchQuery, ignoreCase = true)
            val matchRegion = searchQuery.isNotEmpty() || selectedRegion == "All" || country.region.equals(selectedRegion, ignoreCase = true)
            matchSearch && matchRegion
        }
    }

    // Static Country of the Day tracker (deterministic based on day or first item)
    val countryOfTheDay = remember(countries) {
        if (countries.isNotEmpty()) countries.getOrNull(3) else null // Brazil represents country of the day!
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(IntelDarkBg)
    ) {
        // Upper Intel brief title - Genuinely crisp, high-end white header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "WORLD DECLASSIFIED",
                        style = MaterialTheme.typography.labelSmall,
                        color = IntelGold,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.5.sp
                    )
                    Text(
                        text = "Factbook",
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 28.sp,
                        color = IntelOnSurface,
                        letterSpacing = (-0.5).sp
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Public,
                        contentDescription = "Globe Logo",
                        tint = IntelSecondary,
                        modifier = Modifier.size(24.dp)
                    )
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(UserProfileBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "HQ",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = UserProfileText
                        )
                    }
                }
            }
        }

        // Horizontal region filter pills
        item {
            RegionFilterSection(
                selectedRegion = selectedRegion,
                onRegionSelect = { viewModel.updateSelectedRegion(it) },
                searchQuery = searchQuery,
                onProbeClick = { viewModel.probeSatelliteCountry(searchQuery) }
            )
        }

        // Quick Search Bar
        item {
            PaddingValues(horizontal = 16.dp, vertical = 6.dp).let {
                Box(modifier = Modifier.padding(it)) {
                    LookupSearchBar(
                        query = searchQuery,
                        onQueryChange = { viewModel.updateSearchQuery(it) },
                        matchCount = filteredCountries.size
                    )
                }
            }
        }

        // Feed Switcher Selector (List Stream vs Grid Layout)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .background(IntelSurface, shape = RoundedCornerShape(12.dp))
                    .padding(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (!isGridViewActive) IntelSurfaceVariant else Color.Transparent)
                        .clickable { isGridViewActive = false }
                        .padding(vertical = 8.dp, horizontal = 12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "Standard Feed Flow",
                        tint = if (!isGridViewActive) IntelSecondary else IntelMuted,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Strategic Feed",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (!isGridViewActive) IntelOnSurface else IntelOnBg
                    )
                }

                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isGridViewActive) IntelSurfaceVariant else Color.Transparent)
                        .clickable { isGridViewActive = true }
                        .padding(vertical = 8.dp, horizontal = 12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.GridView,
                        contentDescription = "Strict Grid Matrix",
                        tint = if (isGridViewActive) IntelSecondary else IntelMuted,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Tactical Grid",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isGridViewActive) IntelOnSurface else IntelOnBg
                    )
                }
            }
        }

        // Featured Nation Post (Shown at the top of the feed stream)
        if (!isGridViewActive && searchQuery.isEmpty() && selectedRegion == "All") {
            countryOfTheDay?.let { c ->
                item {
                    Text(
                        text = "SOVEREIGN RECORD OF THE DAY",
                        fontSize = 11.sp,
                        color = IntelGold,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.5.sp,
                        modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 6.dp)
                    )
                    InstagramFeedPost(
                        country = c,
                        isBookmarked = bookmarks.containsKey(c.name),
                        isFeatured = true,
                        onCardClick = { viewModel.navigateTo(Screen.CountryDetail(c)) },
                        onBookmarkClick = { viewModel.toggleBookmark(c) },
                        onCommentClick = { viewModel.navigateTo(Screen.CountryDetail(c)) }
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                }
            }
        }

        // Feed List or Empty Satellite Probing section
        if (filteredCountries.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Empty list",
                            modifier = Modifier.size(48.dp),
                            tint = IntelMuted
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No intelligence matches found in regional records.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = IntelMuted,
                            textAlign = TextAlign.Center
                        )

                        if (searchQuery.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(20.dp))

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(BorderStroke(1.dp, IntelOnSurface.copy(alpha = 0.15f)), RoundedCornerShape(16.dp))
                                    .testTag("satellite_probe_card"),
                                colors = CardDefaults.cardColors(containerColor = IntelSurfaceVariant),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Public,
                                            contentDescription = "Satellite Probing",
                                            tint = IntelSecondary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "COSMIC SATELLITE TELEMETRY",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Black,
                                            color = IntelSecondary,
                                            letterSpacing = 1.sp
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Text(
                                        text = "'$searchQuery' is not downloaded in local directory. Connect to global server archives?",
                                        fontSize = 12.sp,
                                        color = IntelOnSurface,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center
                                    )

                                    Spacer(modifier = Modifier.height(14.dp))

                                    when (val state = searchAllState) {
                                        is SearchAllState.Idle -> {
                                            Button(
                                                onClick = { viewModel.probeSatelliteCountry(searchQuery) },
                                                colors = ButtonDefaults.buttonColors(containerColor = IntelSecondary, contentColor = Color.White),
                                                shape = RoundedCornerShape(30.dp),
                                                modifier = Modifier.testTag("probe_satellite_btn")
                                            ) {
                                                Icon(Icons.Default.Wifi, contentDescription = "Connect", modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text("ACTIVATE ORBITAL LINK", fontSize = 11.sp, fontWeight = FontWeight.Black)
                                            }
                                        }
                                        is SearchAllState.Probing -> {
                                            CircularProgressIndicator(color = IntelSecondary, modifier = Modifier.size(24.dp))
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                                                "Broadcasting satellite probe for '$searchQuery'...",
                                                fontSize = 10.sp,
                                                fontFamily = FontFamily.Monospace,
                                                fontWeight = FontWeight.Bold,
                                                color = IntelSecondary
                                            )
                                        }
                                        is SearchAllState.Success -> {
                                            Icon(Icons.Default.CheckCircle, contentDescription = "Success", tint = PastelGreenIcon, modifier = Modifier.size(24.dp))
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                "Dossier compiled successfully: '${state.countryName}' has been added to cache.",
                                                fontSize = 11.sp,
                                                color = IntelOnSurface,
                                                fontWeight = FontWeight.Black,
                                                textAlign = TextAlign.Center
                                            )
                                            LaunchedEffect(state) {
                                                delay(2500)
                                                viewModel.clearSearchAllState()
                                            }
                                        }
                                        is SearchAllState.Error -> {
                                            Text(
                                                text = state.errorMessage,
                                                color = IntelAlert,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                textAlign = TextAlign.Center
                                            )
                                            Spacer(modifier = Modifier.height(10.dp))
                                            Button(
                                                onClick = { viewModel.probeSatelliteCountry(searchQuery) },
                                                colors = ButtonDefaults.buttonColors(containerColor = IntelAlert, contentColor = Color.White),
                                                shape = RoundedCornerShape(30.dp)
                                            ) {
                                                Text("RETRY INTERNET PROBE", fontSize = 11.sp, fontWeight = FontWeight.Black)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Instagram Grid Mode or List Stream Mode
            if (isGridViewActive) {
                item {
                    Box(modifier = Modifier.padding(horizontal = 12.dp)) {
                        Column {
                            val rows = (filteredCountries.size + 1) / 2
                            for (rowIndex in 0 until rows) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    val leftIndex = rowIndex * 2
                                    val rightIndex = rowIndex * 2 + 1

                                    if (leftIndex < filteredCountries.size) {
                                        val country = filteredCountries[leftIndex]
                                        Box(modifier = Modifier.weight(1f)) {
                                            CountryGridCard(
                                                country = country,
                                                isBookmarked = bookmarks.containsKey(country.name),
                                                onCardClick = { viewModel.navigateTo(Screen.CountryDetail(country)) },
                                                onBookmarkClick = { viewModel.toggleBookmark(country) }
                                            )
                                        }
                                    }

                                    if (rightIndex < filteredCountries.size) {
                                        val country = filteredCountries[rightIndex]
                                        Box(modifier = Modifier.weight(1f)) {
                                            CountryGridCard(
                                                country = country,
                                                isBookmarked = bookmarks.containsKey(country.name),
                                                onCardClick = { viewModel.navigateTo(Screen.CountryDetail(country)) },
                                                onBookmarkClick = { viewModel.toggleBookmark(country) }
                                            )
                                        }
                                    } else {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                items(filteredCountries) { country ->
                    // Avoid duplicating Featured Nation if it's already shown and no search is active
                    val isDuplicateFeatured = searchQuery.isEmpty() && selectedRegion == "All" && country.name == countryOfTheDay?.name
                    if (!isDuplicateFeatured) {
                        InstagramFeedPost(
                            country = country,
                            isBookmarked = bookmarks.containsKey(country.name),
                            onCardClick = { viewModel.navigateTo(Screen.CountryDetail(country)) },
                            onBookmarkClick = { viewModel.toggleBookmark(country) },
                            onCommentClick = { viewModel.navigateTo(Screen.CountryDetail(country)) }
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                    }
                }
            }
        }

        // Bottom Safe Area padding to keep things clean from navigation bar overlapping
        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

// Horizontal Filter Chips Section
@Composable
fun RegionFilterSection(
    selectedRegion: String,
    onRegionSelect: (String) -> Unit,
    searchQuery: String,
    onProbeClick: () -> Unit
) {
    val storiesList = remember {
        listOf(
            StoryData("All", "🌍", "All Regions"),
            StoryData("Africa", "🦁", "Africa"),
            StoryData("Americas", "🗽", "Americas"),
            StoryData("Asia", "⛩️", "Asia"),
            StoryData("Europe", "🏰", "Europe"),
            StoryData("Oceania", "🏝️", "Oceania")
        )
    }

    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        items(storiesList) { item ->
            val isActive = selectedRegion == item.regionId
            FilterChip(
                selected = isActive,
                onClick = { onRegionSelect(item.regionId) },
                label = { Text("${item.emoji} ${item.displayName}", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = IntelSecondary,
                    selectedLabelColor = Color.White,
                    containerColor = IntelSurface,
                    labelColor = IntelOnSurface
                ),
                modifier = Modifier.testTag("filter_chip_${item.regionId}")
            )
        }

        if (searchQuery.isNotEmpty()) {
            item {
                FilterChip(
                    selected = true,
                    onClick = onProbeClick,
                    label = { Text("🛰️ Satellite Probe", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = IntelAlert,
                        selectedLabelColor = Color.White
                    ),
                    modifier = Modifier.testTag("probe_special_story")
                )
            }
        }
    }
}

data class StoryData(val regionId: String, val emoji: String, val displayName: String)

// Clean and iconic country card layout based exactly on the user-uploaded mockup
@Composable
fun InstagramFeedPost(
    country: Country,
    isBookmarked: Boolean,
    isFeatured: Boolean = false,
    onCardClick: () -> Unit,
    onBookmarkClick: () -> Unit,
    onCommentClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable { onCardClick() }
            .testTag("country_card_${country.name.lowercase().replace(" ", "_")}"),
        colors = CardDefaults.cardColors(containerColor = IntelSurface),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)), // faint light gray border/outline as in screenshot
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp, horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Centered Rectangular Flag Box mimicking the realistic rectangular flag canvas in mockup
            Box(
                modifier = Modifier
                    .size(width = 120.dp, height = 80.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFF8FAFC))
                    .border(BorderStroke(1.dp, Color(0xFFE2E8F0)), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = country.flagEmoji,
                    fontSize = 52.sp // prominent, high-quality central flag icon representation
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Country Name (Centered, prominent bold sans-serif)
            Text(
                text = country.name,
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 22.sp,
                color = IntelOnSurface,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Subregion or Region (Centered, smaller grey sub-caption)
            Text(
                text = country.subregion.ifEmpty { country.region },
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp,
                color = IntelOnBg,
                textAlign = TextAlign.Center
            )
        }
    }
}

// Leftover stat grid items for switching modes
@Composable
fun StatItem(label: String, value: String, accentColor: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            fontSize = 24.sp,
            fontWeight = FontWeight.Black,
            color = IntelOnSurface,
            letterSpacing = (-0.5).sp
        )
        Text(
            text = label,
            fontSize = 9.sp,
            color = IntelMuted,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.sp
        )
    }
}

@Composable
fun CountryGridCard(
    country: Country,
    isBookmarked: Boolean,
    onCardClick: () -> Unit,
    onBookmarkClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCardClick() }
            .testTag("country_card_${country.name.lowercase().replace(" ", "_")}"),
        colors = CardDefaults.cardColors(containerColor = IntelSurface),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)), // faint light gray border/outline as in screenshot
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp, horizontal = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Centered Rectangular Flag Box representing realistic flag canvas
            Box(
                modifier = Modifier
                    .size(width = 80.dp, height = 54.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFFF8FAFC))
                    .border(BorderStroke(1.dp, Color(0xFFE2E8F0)), RoundedCornerShape(6.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = country.flagEmoji,
                    fontSize = 32.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Country Name (Centered, Bold)
            Text(
                text = country.name,
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 15.sp,
                color = IntelOnSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(2.dp))

            // Subregion or Region (Centered)
            Text(
                text = country.subregion.ifEmpty { country.region },
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Normal,
                fontSize = 11.sp,
                color = IntelOnBg,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
        }
    }
}

package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Analytics
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

    // Filtered countries: if user is typing, we bypass regional constraints so search handles all regions nicely
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

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(IntelDarkBg)
            .padding(16.dp)
    ) {
        // Upper Intel brief title
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "WORLD INTELLIGENCE ARCHIVE",
                    style = MaterialTheme.typography.labelMedium,
                    color = IntelGold,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.5.sp
                )
                Text(
                    text = "Factbook Explorer",
                    style = MaterialTheme.typography.headlineLarge,
                    color = IntelOnSurface,
                    fontWeight = FontWeight.Black
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Public,
                    contentDescription = "Globe Icon",
                    tint = IntelGold,
                    modifier = Modifier.size(28.dp)
                )
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(UserProfileBg)
                        .border(BorderStroke(1.5.dp, IntelOnSurface), RoundedCornerShape(18.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "WF",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        color = UserProfileText
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Stats summary band
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = IntelSurfaceVariant),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(2.dp, IntelOnSurface)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatItem(label = "MONITORED", value = "${countries.size}", accentColor = IntelGold)
                VerticalDivider(color = IntelOnSurface.copy(alpha = 0.15f), modifier = Modifier.height(28.dp))
                StatItem(label = "REGIONS", value = "5", accentColor = IntelGold)
                VerticalDivider(color = IntelOnSurface.copy(alpha = 0.15f), modifier = Modifier.height(28.dp))
                StatItem(label = "DOSSIERS", value = "${bookmarks.size}", accentColor = IntelGold)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Featured "Country of the Day"
        countryOfTheDay?.let { c ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.navigateTo(Screen.CountryDetail(c)) }
                    .testTag("featured_country_card"),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(2.dp, IntelOnSurface),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(IntelGold, IntelSecondary)
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = c.flagEmoji,
                                fontSize = 56.sp,
                                modifier = Modifier.padding(end = 16.dp)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(30.dp))
                                        .background(IntelGoldLight)
                                        .border(BorderStroke(1.dp, IntelOnSurface), RoundedCornerShape(30.dp))
                                        .padding(horizontal = 10.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "FEATURED NATION",
                                        fontSize = 9.sp,
                                        color = IntelOnSurface,
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = 1.sp
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = c.name,
                                    style = MaterialTheme.typography.headlineLarge,
                                    color = Color.White,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Capital: ${c.capital}   •   Region: ${c.region}   •   Pop: ${String.format("%,d", c.population)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = IntelGoldLight,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = c.shortOverview,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.9f),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = { viewModel.navigateTo(Screen.CountryDetail(c)) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = IntelSurface,
                                contentColor = IntelOnSurface
                            ),
                            border = BorderStroke(1.5.dp, IntelOnSurface),
                            shape = RoundedCornerShape(30.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                            modifier = Modifier.height(38.dp)
                        ) {
                            Text(
                                text = "VIEW DETAILED DOSSIER",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Search bar component with real-time feedback
        LookupSearchBar(
            query = searchQuery,
            onQueryChange = { viewModel.updateSearchQuery(it) },
            matchCount = filteredCountries.size
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Regions filter row
        val regions = listOf("All", "Africa", "Americas", "Asia", "Europe", "Oceania")
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            regions.forEach { r ->
                val isSelected = selectedRegion == r
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isSelected) IntelGold else IntelSurface)
                        .border(
                            BorderStroke(
                                width = 2.dp,
                                color = if (isSelected) IntelOnSurface else IntelOnSurface.copy(alpha = 0.15f)
                            ),
                            RoundedCornerShape(20.dp)
                        )
                        .clickable { viewModel.updateSelectedRegion(r) }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                        .testTag("filter_chip_$r")
                ) {
                    Text(
                        text = r,
                        color = if (isSelected) Color.White else IntelOnSurface,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Grid List of countries
        if (filteredCountries.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
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
                                .fillMaxWidth(0.95f)
                                .border(BorderStroke(2.dp, IntelOnSurface), RoundedCornerShape(16.dp))
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
                                        tint = IntelGold,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "GLOBAL SATELLITE SEARCH",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Black,
                                        color = IntelGold,
                                        letterSpacing = 1.sp
                                    )
                                }
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                Text(
                                    text = "'$searchQuery' is not downloaded in regional archives. Establish a telemetry link to query the global directory?",
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
                                            colors = ButtonDefaults.buttonColors(containerColor = IntelOnSurface, contentColor = Color.White),
                                            border = BorderStroke(1.5.dp, IntelOnSurface),
                                            shape = RoundedCornerShape(30.dp),
                                            modifier = Modifier.testTag("probe_satellite_btn")
                                        ) {
                                            Icon(Icons.Default.Wifi, contentDescription = "Connect", modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("ACTIVATE SATELLITE PROBE", fontSize = 11.sp, fontWeight = FontWeight.Black)
                                        }
                                    }
                                    is SearchAllState.Probing -> {
                                        CircularProgressIndicator(color = IntelOnSurface, modifier = Modifier.size(24.dp))
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            "Probing orbital archives for '$searchQuery'...",
                                            fontSize = 10.sp,
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Bold,
                                            color = IntelOnSurface
                                        )
                                    }
                                    is SearchAllState.Success -> {
                                        Icon(Icons.Default.CheckCircle, contentDescription = "Success", tint = IntelOnSurface, modifier = Modifier.size(24.dp))
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            "Dossier compiled successfully: '${state.countryName}' added directly to Factbook.",
                                            fontSize = 11.sp,
                                            color = IntelOnSurface,
                                            fontWeight = FontWeight.Black,
                                            textAlign = TextAlign.Center
                                        )
                                        LaunchedEffect(state) {
                                            delay(3000)
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
                                            shape = RoundedCornerShape(30.dp),
                                            border = BorderStroke(1.5.dp, IntelOnSurface)
                                        ) {
                                            Text("RETRY LINK", fontSize = 11.sp, fontWeight = FontWeight.Black)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredCountries) { country ->
                    CountryGridCard(
                        country = country,
                        isBookmarked = bookmarks.containsKey(country.name),
                        onCardClick = { viewModel.navigateTo(Screen.CountryDetail(country)) },
                        onBookmarkClick = { viewModel.toggleBookmark(country) }
                    )
                }
            }
        }
    }
}

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
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(2.dp, IntelOnSurface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = country.flagEmoji,
                    fontSize = 32.sp
                )
                IconButton(
                    onClick = onBookmarkClick,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("bookmark_btn_${country.name.lowercase().replace(" ", "_")}")
                ) {
                    Icon(
                        imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = "Bookmark country",
                        tint = if (isBookmarked) IntelGold else IntelOnSurface.copy(alpha = 0.5f),
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = country.name,
                style = MaterialTheme.typography.titleMedium,
                color = IntelOnSurface,
                fontWeight = FontWeight.Black,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = country.capital,
                style = MaterialTheme.typography.bodySmall,
                color = IntelOnBg,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(IntelGoldLight)
                        .border(BorderStroke(1.5.dp, IntelOnSurface), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = country.region.uppercase(),
                        fontSize = 8.sp,
                        color = IntelOnSurface,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp
                    )
                }

                Icon(
                    imageVector = Icons.Outlined.Analytics,
                    contentDescription = "Dossier details",
                    tint = IntelGold,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

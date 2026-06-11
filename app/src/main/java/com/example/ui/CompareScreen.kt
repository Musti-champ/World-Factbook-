package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Country
import com.example.ui.theme.*
import kotlin.math.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompareScreen(
    viewModel: FactbookViewModel,
    modifier: Modifier = Modifier
) {
    val countries by viewModel.allCountries.collectAsState()
    val cA by viewModel.compareCountryA.collectAsState()
    val cB by viewModel.compareCountryB.collectAsState()
    val compareState by viewModel.compareState.collectAsState()
    val savedComparisons by viewModel.savedComparisons.collectAsState()

    var showMenuA by remember { mutableStateOf(false) }
    var showMenuB by remember { mutableStateOf(false) }

    // Derive current comparison ID and any saved data
    val currentCompareId = remember(cA, cB) {
        if (cA != null && cB != null) "${cA!!.name}_vs_${cB!!.name}" else ""
    }
    val savedRecord = remember(currentCompareId, savedComparisons) {
        savedComparisons.find { it.id == currentCompareId }
    }

    var analystNotesText by remember(cA, cB, savedRecord) {
        mutableStateOf(savedRecord?.analystNotes ?: "")
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(IntelDarkBg)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Title
        Text(
            text = "DIPLOMATIC BRIEFING CELL",
            style = MaterialTheme.typography.labelMedium,
            color = IntelGold,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.5.sp
        )
        Text(
            text = "Strategic Comparison",
            style = MaterialTheme.typography.headlineLarge,
            color = IntelOnSurface,
            fontWeight = FontWeight.Black
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Country selectors (Slot A vs Slot B)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Slot A
            Box(modifier = Modifier.weight(1f)) {
                Card(
                    onClick = { showMenuA = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .testTag("selector_a_btn"),
                    colors = CardDefaults.cardColors(containerColor = IntelSurface),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, IntelOnSurface.copy(alpha = 0.12f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (cA == null) {
                            Icon(Icons.Default.Add, contentDescription = "Select nation A", tint = IntelGold)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("SELECT NATION A", fontSize = 11.sp, fontWeight = FontWeight.Black, color = IntelGold, letterSpacing = 0.5.sp)
                        } else {
                            Text(cA!!.flagEmoji, fontSize = 42.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(cA!!.name, style = MaterialTheme.typography.titleMedium, color = IntelOnSurface, fontWeight = FontWeight.Black, textAlign = TextAlign.Center)
                            Text(cA!!.region.uppercase(), fontSize = 9.sp, color = IntelMuted, fontWeight = FontWeight.Black)
                        }
                    }
                }

                DropdownMenu(
                    expanded = showMenuA,
                    onDismissRequest = { showMenuA = false },
                    modifier = Modifier
                        .background(IntelSurface)
                        .heightIn(max = 280.dp)
                ) {
                    countries.forEach { country ->
                        DropdownMenuItem(
                            text = { Text("${country.flagEmoji} ${country.name}", color = IntelOnSurface, fontWeight = FontWeight.Bold) },
                            onClick = {
                                viewModel.setCompareCountry("A", country)
                                showMenuA = false
                            },
                            modifier = Modifier.testTag("dropdown_item_a_${country.name.lowercase().replace(" ", "_")}")
                        )
                    }
                }
            }

            // VS display
            Box(
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .size(36.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(PastelPinkBg)
                    .border(BorderStroke(1.dp, IntelGold.copy(alpha = 0.5f)), RoundedCornerShape(18.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text("VS", fontSize = 12.sp, fontWeight = FontWeight.Black, color = IntelOnSurface)
            }

            // Slot B
            Box(modifier = Modifier.weight(1f)) {
                Card(
                    onClick = { showMenuB = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .testTag("selector_b_btn"),
                    colors = CardDefaults.cardColors(containerColor = IntelSurface),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, IntelOnSurface.copy(alpha = 0.12f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (cB == null) {
                            Icon(Icons.Default.Add, contentDescription = "Select nation B", tint = IntelGold)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("SELECT NATION B", fontSize = 11.sp, fontWeight = FontWeight.Black, color = IntelGold, letterSpacing = 0.5.sp)
                        } else {
                            Text(cB!!.flagEmoji, fontSize = 42.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(cB!!.name, style = MaterialTheme.typography.titleMedium, color = IntelOnSurface, fontWeight = FontWeight.Black, textAlign = TextAlign.Center)
                            Text(cB!!.region.uppercase(), fontSize = 9.sp, color = IntelMuted, fontWeight = FontWeight.Black)
                        }
                    }
                }

                DropdownMenu(
                    expanded = showMenuB,
                    onDismissRequest = { showMenuB = false },
                    modifier = Modifier
                        .background(IntelSurface)
                        .heightIn(max = 280.dp)
                ) {
                    countries.forEach { country ->
                        DropdownMenuItem(
                            text = { Text("${country.flagEmoji} ${country.name}", color = IntelOnSurface) },
                            onClick = {
                                viewModel.setCompareCountry("B", country)
                                showMenuB = false
                            },
                            modifier = Modifier.testTag("dropdown_item_b_${country.name.lowercase().replace(" ", "_")}")
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Side-by-Side Table Matrix (if A and B are both set)
        if (cA != null && cB != null) {
            val countryA = cA!!
            val countryB = cB!!

            Text(
                text = "GEOPOLITICAL METRIC MATRIX",
                style = MaterialTheme.typography.labelMedium,
                color = IntelGold,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.5.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = IntelSurface),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, IntelOnSurface.copy(alpha = 0.12f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    ComparisonMatrixRow(label = "Official Name", valA = countryA.officialName, valB = countryB.officialName)
                    HorizontalDivider(color = IntelOnSurface.copy(alpha = 0.1f), modifier = Modifier.padding(vertical = 10.dp))
                    ComparisonMatrixRow(label = "Capital", valA = countryA.capital, valB = countryB.capital)
                    HorizontalDivider(color = IntelOnSurface.copy(alpha = 0.1f), modifier = Modifier.padding(vertical = 10.dp))
                    ComparisonMatrixRow(label = "Region", valA = countryA.region, valB = countryB.region)
                    HorizontalDivider(color = IntelOnSurface.copy(alpha = 0.1f), modifier = Modifier.padding(vertical = 10.dp))

                    // Population Compare
                    val popHigher = if (countryA.population > countryB.population) "A" else "B"
                    val totalP = (countryA.population + countryB.population).toFloat()
                    val popRatio = if (totalP > 0f) countryA.population.toFloat() / totalP else 0.5f
                    ComparisonMatrixRow(
                        label = "Population",
                        valA = String.format("%,d", countryA.population),
                        valB = String.format("%,d", countryB.population),
                        highlightSlot = popHigher,
                        ratio = popRatio
                    )
                    HorizontalDivider(color = IntelOnSurface.copy(alpha = 0.1f), modifier = Modifier.padding(vertical = 10.dp))

                    // Area Compare
                    val areaHigher = if (countryA.areaSqKm > countryB.areaSqKm) "A" else "B"
                    val totalAreaVal = (countryA.areaSqKm + countryB.areaSqKm).toFloat()
                    val areaRatio = if (totalAreaVal > 0f) countryA.areaSqKm.toFloat() / totalAreaVal else 0.5f
                    ComparisonMatrixRow(
                        label = "Land Area",
                        valA = "${String.format("%,.0f", countryA.areaSqKm)} sq km",
                        valB = "${String.format("%,.0f", countryB.areaSqKm)} sq km",
                        highlightSlot = areaHigher,
                        ratio = areaRatio
                    )
                    HorizontalDivider(color = IntelOnSurface.copy(alpha = 0.1f), modifier = Modifier.padding(vertical = 10.dp))

                    // Density Compare
                    val densHigher = if (countryA.density > countryB.density) "A" else "B"
                    val totalDensVal = (countryA.density + countryB.density).toFloat()
                    val densRatio = if (totalDensVal > 0f) countryA.density.toFloat() / totalDensVal else 0.5f
                    ComparisonMatrixRow(
                        label = "People Density",
                        valA = "${String.format("%.1f", countryA.density)}/sq km",
                        valB = "${String.format("%.1f", countryB.density)}/sq km",
                        highlightSlot = densHigher,
                        ratio = densRatio
                    )
                    HorizontalDivider(color = IntelOnSurface.copy(alpha = 0.1f), modifier = Modifier.padding(vertical = 10.dp))
                    ComparisonMatrixRow(label = "Government", valA = countryA.governmentType, valB = countryB.governmentType)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // MILITARY & STATISTICAL STRENGTH MATRIX (TACTICAL CELL)
            Text(
                text = "MILITARY & PLENUM SECURITY MATRIX",
                style = MaterialTheme.typography.labelMedium,
                color = IntelGold,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.5.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            val factsA = com.example.data.DetailedCountryFactsProvider.getDetailedFacts(countryA.name, countryA)
            val factsB = com.example.data.DetailedCountryFactsProvider.getDetailedFacts(countryB.name, countryB)

            // Parse power indexes: PowerIndex (pwrIndx in GFP) ranges normally between 0.05 and 3.0+. Lower is better.
            val pwrValA = factsA.pwrIndx.replace(",", "").substringBefore(" ").toFloatOrNull() ?: 1.5f
            val pwrValB = factsB.pwrIndx.replace(",", "").substringBefore(" ").toFloatOrNull() ?: 1.5f
            val effValA = 1.0f / pwrValA.coerceAtLeast(0.0001f)
            val effValB = 1.0f / pwrValB.coerceAtLeast(0.0001f)
            val totalEff = effValA + effValB
            val balanceRatioA = if (totalEff > 0f) effValA / totalEff else 0.5f

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = IntelSurface),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, IntelOnSurface.copy(alpha = 0.12f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Balance of Power visual bar
                    Column {
                        Text(
                            text = "Standing Tactical Power Balance",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = IntelOnSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Relative percentage computed from certified global firepower metrics",
                            style = MaterialTheme.typography.bodySmall,
                            color = IntelMuted
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${String.format("%.1f", balanceRatioA * 100)}%",
                                fontWeight = FontWeight.Black,
                                fontSize = 18.sp,
                                color = IntelSecondary
                            )
                            Text(
                                text = "vs",
                                style = MaterialTheme.typography.labelMedium,
                                color = IntelMuted
                            )
                            Text(
                                text = "${String.format("%.1f", (1.0f - balanceRatioA) * 100)}%",
                                fontWeight = FontWeight.Black,
                                fontSize = 18.sp,
                                color = PastelPinkIcon
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Visual bar indicator
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(12.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(IntelOnSurface.copy(alpha = 0.08f))
                        ) {
                            val leftWeight = if (balanceRatioA <= 0.01f) 0.01f else if (balanceRatioA >= 0.99f) 0.99f else balanceRatioA
                            val rightWeight = 1.0f - leftWeight

                            Box(
                                modifier = Modifier
                                    .weight(leftWeight)
                                    .fillMaxHeight()
                                    .background(IntelSecondary)
                            )
                            Box(
                                modifier = Modifier
                                    .weight(rightWeight)
                                    .fillMaxHeight()
                                    .background(PastelPinkIcon)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Verdict text
                        val superiorCountry = if (balanceRatioA > 0.52f) countryA.name else if (balanceRatioA < 0.48f) countryB.name else null
                        val verdictColor = if (superiorCountry == null) IntelOnSurface else if (superiorCountry == countryA.name) IntelSecondary else PastelPinkIcon
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(IntelDarkBg, shape = RoundedCornerShape(8.dp))
                                .border(1.dp, IntelOnSurface.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                                .padding(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = verdictColor,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = when {
                                        balanceRatioA > 0.52f -> "Tactical Superiority: ${countryA.name} maintains a calculated advantage due to a superior Global Firepower ranking."
                                        balanceRatioA < 0.48f -> "Tactical Superiority: ${countryB.name} maintains a calculated advantage due to a superior Global Firepower ranking."
                                        else -> "Strategic Parity: Tactical defense forces and ranking structures reflect symmetrical capabilities."
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = IntelOnSurface,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        BilateralStrategicRadarChart(
                            factsA = factsA,
                            factsB = factsB,
                            nameA = countryA.name,
                            nameB = countryB.name
                        )
                    }

                    HorizontalDivider(color = IntelOnSurface.copy(alpha = 0.1f))

                    // GFP Rank & PowerIndex
                    val indexHigher = if (pwrValA < pwrValB) "A" else "B" // smaller score is stronger
                    val totalPwrVal = pwrValA + pwrValB
                    val indexRatio = if (totalPwrVal > 0f) (pwrValB / totalPwrVal) else 0.5f // reverse because smaller is better
                    TacticalAssetRow(
                        label = "Firepower Index Rating",
                        valA = "${factsA.gfpRank} (Index: ${factsA.pwrIndx})",
                        valB = "${factsB.gfpRank} (Index: ${factsB.pwrIndx})",
                        icon = Icons.Default.Security,
                        highlightSlot = indexHigher,
                        ratio = indexRatio
                    )

                    HorizontalDivider(color = IntelOnSurface.copy(alpha = 0.1f))

                    // Active Personnel
                    val actA = parseAssetValue(factsA.activePersonnel)
                    val actB = parseAssetValue(factsB.activePersonnel)
                    val totalAct = actA + actB
                    val actRatio = if (totalAct > 0f) actA / totalAct else 0.5f
                    val actHigher = if (actA > actB) "A" else if (actB > actA) "B" else null
                    TacticalAssetRow(
                        label = "Active Duty Personnel",
                        valA = factsA.activePersonnel,
                        valB = factsB.activePersonnel,
                        icon = Icons.Default.People,
                        highlightSlot = actHigher,
                        ratio = actRatio
                    )

                    HorizontalDivider(color = IntelOnSurface.copy(alpha = 0.1f))

                    // Reserve Personnel
                    val resA = parseAssetValue(factsA.reservePersonnel)
                    val resB = parseAssetValue(factsB.reservePersonnel)
                    val totalRes = resA + resB
                    val resRatio = if (totalRes > 0f) resA / totalRes else 0.5f
                    val resHigher = if (resA > resB) "A" else if (resB > resA) "B" else null
                    TacticalAssetRow(
                        label = "Reserve Standby Personnel",
                        valA = factsA.reservePersonnel,
                        valB = factsB.reservePersonnel,
                        icon = Icons.Default.People,
                        highlightSlot = resHigher,
                        ratio = resRatio
                    )

                    HorizontalDivider(color = IntelOnSurface.copy(alpha = 0.1f))

                    // Aircraft Strength
                    val airA = parseAssetValue(factsA.aircraftStrength)
                    val airB = parseAssetValue(factsB.aircraftStrength)
                    val totalAir = airA + airB
                    val airRatio = if (totalAir > 0f) airA / totalAir else 0.5f
                    val airHigher = if (airA > airB) "A" else if (airB > airA) "B" else null
                    TacticalAssetRow(
                        label = "Air Superiority Fleet",
                        valA = factsA.aircraftStrength,
                        valB = factsB.aircraftStrength,
                        icon = Icons.Default.AirplanemodeActive,
                        highlightSlot = airHigher,
                        ratio = airRatio
                    )

                    HorizontalDivider(color = IntelOnSurface.copy(alpha = 0.1f))

                    // Tank / Ground armor strength
                    val tankA = parseAssetValue(factsA.tankStrength)
                    val tankB = parseAssetValue(factsB.tankStrength)
                    val totalTank = tankA + tankB
                    val tankRatio = if (totalTank > 0f) tankA / totalTank else 0.5f
                    val tankHigher = if (tankA > tankB) "A" else if (tankB > tankA) "B" else null
                    TacticalAssetRow(
                        label = "Combat Armor / Battle Tanks",
                        valA = factsA.tankStrength,
                        valB = factsB.tankStrength,
                        icon = Icons.Default.LocalShipping,
                        highlightSlot = tankHigher,
                        ratio = tankRatio
                    )

                    HorizontalDivider(color = IntelOnSurface.copy(alpha = 0.1f))

                    // Navy strength
                    val navyA = parseAssetValue(factsA.navyStrength)
                    val navyB = parseAssetValue(factsB.navyStrength)
                    val totalNavy = navyA + navyB
                    val navyRatio = if (totalNavy > 0f) navyA / totalNavy else 0.5f
                    val navyHigher = if (navyA > navyB) "A" else if (navyB > navyA) "B" else null
                    TacticalAssetRow(
                        label = "Naval Fleet Assets",
                        valA = factsA.navyStrength,
                        valB = factsB.navyStrength,
                        icon = Icons.Default.DirectionsBoat,
                        highlightSlot = navyHigher,
                        ratio = navyRatio
                    )

                    HorizontalDivider(color = IntelOnSurface.copy(alpha = 0.1f))

                    // Defense Budget
                    val budA = parseAssetValue(factsA.defenseBudget)
                    val budB = parseAssetValue(factsB.defenseBudget)
                    val totalBud = budA + budB
                    val budRatio = if (totalBud > 0f) budA / totalBud else 0.5f
                    val budHigher = if (budA > budB) "A" else if (budB > budA) "B" else null
                    TacticalAssetRow(
                        label = "Annual Defense Allocations",
                        valA = factsA.defenseBudget,
                        valB = factsB.defenseBudget,
                        icon = Icons.Default.AttachMoney,
                        highlightSlot = budHigher,
                        ratio = budRatio
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // SOCIETAL & ECONOMIC STRENGTH SPECTRUM (UPGRADED CORE STATS)
            Text(
                text = "SOCIETAL & ECONOMIC STRENGTH SPECTRUM",
                style = MaterialTheme.typography.labelMedium,
                color = IntelGold,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.5.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            DemographicEconomicCard(
                countryA = countryA,
                factsA = factsA,
                countryB = countryB,
                factsB = factsB
            )

            Spacer(modifier = Modifier.height(24.dp))

            // AI Diplomatic Assessment Action
            Text(
                text = "ENVOY ASSESSMENT GENERATOR",
                style = MaterialTheme.typography.labelMedium,
                color = IntelGold,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.5.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = PastelPinkBg),
                border = BorderStroke(2.dp, IntelOnSurface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Synthesize an AI structural comparison analyzing trade routes, historic conflicts or treaties, currency friction, and balance of power between ${countryA.name} and ${countryB.name}.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = IntelOnSurface,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = { viewModel.makeAiComparisonReport() },
                        colors = ButtonDefaults.buttonColors(containerColor = IntelOnSurface, contentColor = Color.White),
                        border = BorderStroke(1.5.dp, IntelOnSurface),
                        shape = RoundedCornerShape(30.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("compare_report_btn")
                    ) {
                        Icon(Icons.Default.Analytics, contentDescription = "Assess relationship")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("RUN DIPLOMACY SYMPOSIUM", fontWeight = FontWeight.Black, letterSpacing = 0.5.sp)
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // AI results drawer
                    when (compareState) {
                        CompareAiState.Idle -> {
                            // Empty state
                        }
                        CompareAiState.Loading -> {
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
                                        "Synthesizing trade flow correlations...",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = IntelOnSurface,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                        is CompareAiState.Success -> {
                            Text(
                                text = (compareState as CompareAiState.Success).comparisonMarkdown,
                                style = MaterialTheme.typography.bodyMedium,
                                color = IntelOnSurface,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(
                                        1.5.dp,
                                        IntelOnSurface,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .background(IntelSurface)
                                    .padding(16.dp)
                            )
                        }
                        is CompareAiState.Error -> {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = IntelAlert.copy(alpha = 0.2f)),
                                border = BorderStroke(1.5.dp, IntelAlert),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Error, contentDescription = "Error icon", tint = IntelAlert)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = (compareState as CompareAiState.Error).errorMessage,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = IntelAlert,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ANALYST DUAL-NATION MEMORANDUM (UPGRADED PERSISTED COMPARISONS)
            Text(
                text = "ANALYST REPORT ARCHIVING",
                style = MaterialTheme.typography.labelMedium,
                color = IntelGold,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.5.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = IntelSurface),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(2.dp, if (savedRecord != null) IntelSecondary else IntelOnSurface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Archived Intel memorandum regarding relationships between ${countryA.name} and ${countryB.name}.",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = IntelOnSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Document strategic insights, tension logs, trade frictions, or joint exercises. This dossier comparison will be persistently bookmarked under the Geopolitical Comparisons Vault.",
                        style = MaterialTheme.typography.bodySmall,
                        color = IntelMuted
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = analystNotesText,
                        onValueChange = { analystNotesText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 100.dp)
                            .testTag("analyst_compare_notes"),
                        maxLines = 6,
                        placeholder = { Text("Write analyst comments or notes...", color = IntelMuted) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = IntelGold,
                            unfocusedBorderColor = IntelOnSurface.copy(alpha = 0.2f),
                            focusedTextColor = IntelOnSurface,
                            unfocusedTextColor = IntelOnSurface,
                            focusedContainerColor = IntelDarkBg,
                            unfocusedContainerColor = IntelDarkBg
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                viewModel.saveGeopoliticalComparison(countryA.name, countryB.name, analystNotesText)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = if (savedRecord != null) IntelSecondary else IntelGold, contentColor = Color.White),
                            shape = RoundedCornerShape(30.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("save_compare_btn"),
                            border = BorderStroke(1.5.dp, IntelOnSurface)
                        ) {
                            Icon(Icons.Default.Save, contentDescription = "Save Report")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (savedRecord != null) "UPDATE COMP-FILE" else "ARCHIVE REPORT",
                                fontWeight = FontWeight.Black,
                                fontSize = 12.sp,
                                letterSpacing = 0.5.sp
                            )
                        }

                        if (savedRecord != null) {
                            IconButton(
                                onClick = {
                                    viewModel.deleteGeopoliticalComparison(savedRecord.id)
                                },
                                modifier = Modifier
                                    .background(IntelDarkBg, shape = RoundedCornerShape(8.dp))
                                    .border(1.5.dp, IntelAlert, shape = RoundedCornerShape(8.dp))
                                    .testTag("delete_compare_btn")
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Purge Report", tint = IntelAlert)
                            }
                        }
                    }

                    if (savedRecord != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Archived icon",
                                tint = IntelSecondary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "PERSISTED FILE: DOSS-CO-${countryA.name.take(3).uppercase()}-${countryB.name.take(3).uppercase()}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = IntelSecondary,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        } else {
            // Unselected prompt
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.CompareArrows,
                        contentDescription = "Compare illustration",
                        tint = IntelMuted,
                        modifier = Modifier.size(62.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Identify two target countries to begin diplomatic comparison.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = IntelMuted,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun ComparisonMatrixRow(
    label: String,
    valA: String,
    valB: String,
    highlightSlot: String? = null, // "A" or "B" or null
    ratio: Float = -1f
) {
    Column {
        Text(
            text = label.uppercase(),
            fontSize = 10.sp,
            fontWeight = FontWeight.Black,
            color = IntelGold,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = valA,
                fontSize = 14.sp,
                fontWeight = if (highlightSlot == "A") FontWeight.Black else FontWeight.Bold,
                color = if (highlightSlot == "A") IntelGold else IntelOnSurface,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Start
            )
            Icon(
                imageVector = Icons.Default.CompareArrows,
                contentDescription = null,
                tint = IntelOnSurface.copy(alpha = 0.2f),
                modifier = Modifier
                    .weight(0.2f)
                    .size(18.dp)
            )
            Text(
                text = valB,
                fontSize = 14.sp,
                fontWeight = if (highlightSlot == "B") FontWeight.Black else FontWeight.Bold,
                color = if (highlightSlot == "B") IntelGold else IntelOnSurface,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.End
            )
        }

        if (ratio >= 0f) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(IntelOnSurface.copy(alpha = 0.08f))
            ) {
                val leftWeight = if (ratio <= 0.01f) 0.01f else if (ratio >= 0.99f) 0.99f else ratio
                val rightWeight = 1.0f - leftWeight
                
                Box(
                    modifier = Modifier
                        .weight(leftWeight)
                        .fillMaxHeight()
                        .background(IntelSecondary)
                )
                Box(
                    modifier = Modifier
                        .weight(rightWeight)
                        .fillMaxHeight()
                        .background(PastelPinkIcon)
                )
            }
        }
    }
}

@Composable
fun TacticalAssetRow(
    label: String,
    valA: String,
    valB: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    highlightSlot: String? = null,
    ratio: Float = -1f
) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = IntelGold,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label.uppercase(),
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                color = IntelGold,
                letterSpacing = 1.sp
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = valA,
                fontSize = 14.sp,
                fontWeight = if (highlightSlot == "A") FontWeight.Black else FontWeight.Bold,
                color = if (highlightSlot == "A") IntelGold else IntelOnSurface,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Start
            )
            Icon(
                imageVector = Icons.Default.CompareArrows,
                contentDescription = null,
                tint = IntelOnSurface.copy(alpha = 0.2f),
                modifier = Modifier
                    .weight(0.2f)
                    .size(18.dp)
            )
            Text(
                text = valB,
                fontSize = 14.sp,
                fontWeight = if (highlightSlot == "B") FontWeight.Black else FontWeight.Bold,
                color = if (highlightSlot == "B") IntelGold else IntelOnSurface,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.End
            )
        }

        if (ratio >= 0f) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(IntelOnSurface.copy(alpha = 0.08f))
            ) {
                val leftWeight = if (ratio <= 0.01f) 0.01f else if (ratio >= 0.99f) 0.99f else ratio
                val rightWeight = 1.0f - leftWeight
                
                Box(
                    modifier = Modifier
                        .weight(leftWeight)
                        .fillMaxHeight()
                        .background(IntelSecondary)
                )
                Box(
                    modifier = Modifier
                        .weight(rightWeight)
                        .fillMaxHeight()
                        .background(PastelPinkIcon)
                )
            }
        }
    }
}

fun parseAssetValue(text: String): Float {
    val clean = text.replace(",", "").trim()
    val matcher = java.util.regex.Pattern.compile("\\d+([.]\\d+)?").matcher(clean)
    if (matcher.find()) {
        return matcher.group().toFloatOrNull() ?: 0f
    }
    return 0f
}

@Composable
fun DemographicEconomicCard(
    countryA: Country,
    factsA: com.example.data.DetailedCountryFacts,
    countryB: Country,
    factsB: com.example.data.DetailedCountryFacts
) {
    val lifeA = parsePercentageOrValue(factsA.lifeExpectancyAtBirth)
    val lifeB = parsePercentageOrValue(factsB.lifeExpectancyAtBirth)
    val totalLife = lifeA + lifeB
    val lifeRatio = if (totalLife > 0f) lifeA / totalLife else 0.5f
    val lifeHigher = if (lifeA > lifeB) "A" else if (lifeB > lifeA) "B" else null

    val healthA = parsePercentageOrValue(factsA.healthExpenditures)
    val healthB = parsePercentageOrValue(factsB.healthExpenditures)
    val totalHealth = healthA + healthB
    val healthRatio = if (totalHealth > 0f) healthA / totalHealth else 0.5f
    val healthHigher = if (healthA > healthB) "A" else if (healthB > healthA) "B" else null

    val birthA = parsePercentageOrValue(factsA.birthRate)
    val birthB = parsePercentageOrValue(factsB.birthRate)
    val totalBirth = birthA + birthB
    val birthRatio = if (totalBirth > 0f) birthA / totalBirth else 0.5f
    val birthHigher = if (birthA > birthB) "A" else if (birthB > birthA) "B" else null

    val growthA = parsePercentageOrValue(factsA.populationGrowthRate)
    val growthB = parsePercentageOrValue(factsB.populationGrowthRate)
    val totalGrowth = Math.abs(growthA) + Math.abs(growthB)
    val growthRatio = if (totalGrowth > 0f) (growthA + 10f) / ((growthA + growthB) + 20f) else 0.5f
    val growthHigher = if (growthA > growthB) "A" else if (growthB > growthA) "B" else null

    Card(
        modifier = Modifier.fillMaxWidth().testTag("demographic_economic_card"),
        colors = CardDefaults.cardColors(containerColor = IntelSurface),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(2.dp, IntelOnSurface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "BIOMETRIC & SOCIETAL SPECTRUMS",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Black,
                color = IntelGold,
                letterSpacing = 1.sp
            )

            TacticalAssetRow(
                label = "Life Expectancy at Birth",
                valA = factsA.lifeExpectancyAtBirth,
                valB = factsB.lifeExpectancyAtBirth,
                icon = Icons.Default.Favorite,
                highlightSlot = lifeHigher,
                ratio = lifeRatio
            )

            HorizontalDivider(color = IntelOnSurface.copy(alpha = 0.1f))

            TacticalAssetRow(
                label = "Health Care Expenditures (% of GDP)",
                valA = factsA.healthExpenditures,
                valB = factsB.healthExpenditures,
                icon = Icons.Default.MedicalServices,
                highlightSlot = healthHigher,
                ratio = healthRatio
            )

            HorizontalDivider(color = IntelOnSurface.copy(alpha = 0.1f))

            TacticalAssetRow(
                label = "Crude Birth Rate",
                valA = factsA.birthRate,
                valB = factsB.birthRate,
                icon = Icons.Default.ChildCare,
                highlightSlot = birthHigher,
                ratio = birthRatio
            )

            HorizontalDivider(color = IntelOnSurface.copy(alpha = 0.1f))

            TacticalAssetRow(
                label = "Population Growth Rate",
                valA = factsA.populationGrowthRate,
                valB = factsB.populationGrowthRate,
                icon = Icons.Default.TrendingUp,
                highlightSlot = growthHigher,
                ratio = growthRatio
            )
        }
    }
}

fun parsePercentageOrValue(text: String): Float {
    val clean = text.replace("%", "").replace("years", "").replace("births/1,000 population", "").replace(",", "").trim()
    val matcher = java.util.regex.Pattern.compile("-?\\d+([.]\\d+)?").matcher(clean)
    if (matcher.find()) {
        return matcher.group().toFloatOrNull() ?: 0f
    }
    return 0f
}

@Composable
fun BilateralStrategicRadarChart(
    factsA: com.example.data.DetailedCountryFacts,
    factsB: com.example.data.DetailedCountryFacts,
    nameA: String,
    nameB: String,
    modifier: Modifier = Modifier
) {
    val actA = remember(factsA.activePersonnel) { parseToFloatClean(factsA.activePersonnel) }
    val actB = remember(factsB.activePersonnel) { parseToFloatClean(factsB.activePersonnel) }

    val resA = remember(factsA.reservePersonnel) { parseToFloatClean(factsA.reservePersonnel) }
    val resB = remember(factsB.reservePersonnel) { parseToFloatClean(factsB.reservePersonnel) }

    val airA = remember(factsA.aircraftStrength) { parseToFloatClean(factsA.aircraftStrength) }
    val airB = remember(factsB.aircraftStrength) { parseToFloatClean(factsB.aircraftStrength) }

    val tankA = remember(factsA.tankStrength) { parseToFloatClean(factsA.tankStrength) }
    val tankB = remember(factsB.tankStrength) { parseToFloatClean(factsB.tankStrength) }

    val navyA = remember(factsA.navyStrength) { parseToFloatClean(factsA.navyStrength) }
    val navyB = remember(factsB.navyStrength) { parseToFloatClean(factsB.navyStrength) }

    val budA = remember(factsA.defenseBudget) { parseToFloatClean(factsA.defenseBudget) }
    val budB = remember(factsB.defenseBudget) { parseToFloatClean(factsB.defenseBudget) }

    // Normalize relative to maximum of either country to ensure proper visual scale bounds
    val maxActive = max(actA, actB).coerceAtLeast(1f)
    val maxReserve = max(resA, resB).coerceAtLeast(1f)
    val maxAir = max(airA, airB).coerceAtLeast(1f)
    val maxTank = max(tankA, tankB).coerceAtLeast(1f)
    val maxNavy = max(navyA, navyB).coerceAtLeast(1f)
    val maxBudget = max(budA, budB).coerceAtLeast(1f)

    val rActA = (actA / maxActive).coerceIn(0.1f, 1.0f)
    val rActB = (actB / maxActive).coerceIn(0.1f, 1.0f)

    val rResA = (resA / maxReserve).coerceIn(0.1f, 1.0f)
    val rResB = (resB / maxReserve).coerceIn(0.1f, 1.0f)

    val rAirA = (airA / maxAir).coerceIn(0.1f, 1.0f)
    val rAirB = (airB / maxAir).coerceIn(0.1f, 1.0f)

    val rTankA = (tankA / maxTank).coerceIn(0.1f, 1.0f)
    val rTankB = (tankB / maxTank).coerceIn(0.1f, 1.0f)

    val rNavyA = (navyA / maxNavy).coerceIn(0.1f, 1.0f)
    val rNavyB = (navyB / maxNavy).coerceIn(0.1f, 1.0f)

    val rBudA = (budA / maxBudget).coerceIn(0.1f, 1.0f)
    val rBudB = (budB / maxBudget).coerceIn(0.1f, 1.0f)

    val ratiosA = listOf(rActA, rResA, rAirA, rTankA, rNavyA, rBudA)
    val ratiosB = listOf(rActB, rResB, rAirB, rTankB, rNavyB, rBudB)

    val labels = listOf("ACTIVE STRENGTH", "RESERVE COHORTS", "AIR FLEETS", "BATTLE TANKS", "NAVAL FLEET", "DEFENSE BUDGET")
    val rawValsA = listOf(factsA.activePersonnel, factsA.reservePersonnel, factsA.aircraftStrength, factsA.tankStrength, factsA.navyStrength, factsA.defenseBudget)
    val rawValsB = listOf(factsB.activePersonnel, factsB.reservePersonnel, factsB.aircraftStrength, factsB.tankStrength, factsB.navyStrength, factsB.defenseBudget)

    var highlightedSpoke by remember { mutableStateOf(-1) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .testTag("bilateral_strategic_radar_chart")
    ) {
        // Legend on top
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(10.dp).clip(RoundedCornerShape(2.dp)).background(IntelSecondary))
                Spacer(modifier = Modifier.width(6.dp))
                Text(nameA, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = IntelSecondary)
            }
            Spacer(modifier = Modifier.width(24.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(10.dp).clip(RoundedCornerShape(2.dp)).background(PastelPinkIcon))
                Spacer(modifier = Modifier.width(6.dp))
                Text(nameB, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PastelPinkIcon)
            }
        }

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

                // Concentric hexagonal levels
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

                // Spoke lines
                for (i in 0..5) {
                    val angleRad = (i * 60 - 90) * (PI / 180f)
                    val px = cx + (maxRadius * cos(angleRad)).toFloat()
                    val py = cy + (maxRadius * sin(angleRad)).toFloat()
                    drawLine(
                        color = if (i == highlightedSpoke) IntelGold else IntelOnSurface.copy(alpha = 0.08f),
                        start = Offset(cx, cy),
                        end = Offset(px, py),
                        strokeWidth = if (i == highlightedSpoke) 2.dp.toPx() else 1.dp.toPx()
                    )
                }

                // DRAW COUNTRY A HEXAGON
                val pathA = Path()
                val pointsA = mutableListOf<Offset>()
                for (i in 0..5) {
                    val angleRad = (i * 60 - 90) * (PI / 180f)
                    val dist = maxRadius * ratiosA[i]
                    val px = cx + (dist * cos(angleRad)).toFloat()
                    val py = cy + (dist * sin(angleRad)).toFloat()
                    pointsA.add(Offset(px, py))
                    if (i == 0) pathA.moveTo(px, py) else pathA.lineTo(px, py)
                }
                pathA.close()
                drawPath(path = pathA, color = IntelSecondary.copy(alpha = 0.3f))
                drawPath(path = pathA, color = IntelSecondary, style = Stroke(width = 2.dp.toPx()))

                // DRAW COUNTRY B HEXAGON
                val pathB = Path()
                val pointsB = mutableListOf<Offset>()
                for (i in 0..5) {
                    val angleRad = (i * 60 - 90) * (PI / 180f)
                    val dist = maxRadius * ratiosB[i]
                    val px = cx + (dist * cos(angleRad)).toFloat()
                    val py = cy + (dist * sin(angleRad)).toFloat()
                    pointsB.add(Offset(px, py))
                    if (i == 0) pathB.moveTo(px, py) else pathB.lineTo(px, py)
                }
                pathB.close()
                drawPath(path = pathB, color = PastelPinkIcon.copy(alpha = 0.3f))
                drawPath(path = pathB, color = PastelPinkIcon, style = Stroke(width = 2.dp.toPx()))

                // Highlight Vertex points belonging to both nations
                for (i in 0..5) {
                    val colorA = if (i == highlightedSpoke) IntelGold else IntelSecondary
                    val colorB = if (i == highlightedSpoke) IntelGold else PastelPinkIcon
                    val rA = if (i == highlightedSpoke) 6.dp.toPx() else 4.dp.toPx()
                    val rB = if (i == highlightedSpoke) 6.dp.toPx() else 4.dp.toPx()
                    drawCircle(color = colorA, radius = rA, center = pointsA[i])
                    drawCircle(color = colorB, radius = rB, center = pointsB[i])
                }
            }

            // Radar Spoke Labels Overlay
            Text(
                text = "ACTIVE",
                modifier = Modifier.align(Alignment.TopCenter).padding(top = 4.dp),
                fontSize = 8.sp, fontWeight = FontWeight.Black, color = if (highlightedSpoke == 0) IntelGold else IntelMuted
            )
            Text(
                text = "RESERVES",
                modifier = Modifier.align(Alignment.TopEnd).padding(top = 48.dp, end = 12.dp),
                fontSize = 8.sp, fontWeight = FontWeight.Black, color = if (highlightedSpoke == 1) IntelGold else IntelMuted
            )
            Text(
                text = "AIRPOWER",
                modifier = Modifier.align(Alignment.BottomEnd).padding(bottom = 48.dp, end = 12.dp),
                fontSize = 8.sp, fontWeight = FontWeight.Black, color = if (highlightedSpoke == 2) IntelGold else IntelMuted
            )
            Text(
                text = "TANKS",
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 4.dp),
                fontSize = 8.sp, fontWeight = FontWeight.Black, color = if (highlightedSpoke == 3) IntelGold else IntelMuted
            )
            Text(
                text = "NAVY",
                modifier = Modifier.align(Alignment.BottomStart).padding(bottom = 48.dp, start = 12.dp),
                fontSize = 8.sp, fontWeight = FontWeight.Black, color = if (highlightedSpoke == 4) IntelGold else IntelMuted
            )
            Text(
                text = "BUDGET",
                modifier = Modifier.align(Alignment.TopStart).padding(top = 48.dp, start = 12.dp),
                fontSize = 8.sp, fontWeight = FontWeight.Black, color = if (highlightedSpoke == 5) IntelGold else IntelMuted
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Interactivity details card
        val focusIdx = if (highlightedSpoke != -1) highlightedSpoke else ratiosA.indexOf(ratiosA.maxOrNull() ?: rActA)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .border(BorderStroke(1.dp, IntelOnSurface.copy(alpha = 0.2f)), RoundedCornerShape(12.dp))
                .background(IntelDarkBg)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.TrendingUp,
                contentDescription = null,
                tint = IntelGold,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = "COMPARATIVE OBSERVATION: ${labels[focusIdx]}",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black,
                    color = IntelGold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(
                        text = "$nameA Score: ${rawValsA[focusIdx]}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = IntelSecondary
                    )
                    Text(
                        text = "vs",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = IntelMuted
                    )
                    Text(
                        text = "$nameB Score: ${rawValsB[focusIdx]}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = PastelPinkIcon
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

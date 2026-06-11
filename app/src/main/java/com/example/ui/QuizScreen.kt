package com.example.ui

import androidx.compose.animation.*
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import kotlin.math.abs

@Composable
fun QuizScreen(
    viewModel: FactbookViewModel,
    modifier: Modifier = Modifier
) {
    val qType by viewModel.quizQuestionType.collectAsState()
    val question by viewModel.quizQuestion.collectAsState()
    val isLoading by viewModel.quizIsLoading.collectAsState()
    val score by viewModel.quizScore.collectAsState()
    val lives by viewModel.quizLives.collectAsState()
    val answeredCorrectly by viewModel.answeredCorrectly.collectAsState()
    val selectedIndex by viewModel.selectedOptionIndex.collectAsState()
    val highScores by viewModel.highScores.collectAsState()
    val quizHistory by viewModel.quizHistory.collectAsState()

    var activeSession by remember { mutableStateOf(false) }
    var isShowingLeaderboard by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(IntelDarkBg)
    ) {
        if (!activeSession) {
            // Mode Select UI
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "ACADEMY TESTING OFFICE",
                    style = MaterialTheme.typography.labelMedium,
                    color = IntelGold,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
                Text(
                    text = "Factbook Explorer Quiz",
                    style = MaterialTheme.typography.headlineLarge,
                    color = IntelOnSurface,
                    fontWeight = FontWeight.ExtraBold
                )

                Spacer(modifier = Modifier.height(10.dp))
                
                Text(
                    text = "Test your strategic geographical competence. Earn credentials across multiple simulation tiers.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = IntelMuted,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Mode Cards
                QuizModeCard(
                    title = "Capital City Hunt",
                    description = "Assess capital city assignments of strategic global regions.",
                    icon = Icons.Default.LocationCity,
                    highScore = highScores["capital"]?.highScore ?: 0,
                    plays = highScores["capital"]?.totalPlayed ?: 0,
                    tint = IntelGold,
                    onClick = {
                        viewModel.startNewQuizSession("capital")
                        activeSession = true
                    },
                    modifier = Modifier.testTag("mode_capital_btn")
                )

                Spacer(modifier = Modifier.height(12.dp))

                QuizModeCard(
                    title = "National Flag Patrol",
                    description = "Correctly match the iconic designs and colors of country flags.",
                    icon = Icons.Default.Flag,
                    highScore = highScores["flag"]?.highScore ?: 0,
                    plays = highScores["flag"]?.totalPlayed ?: 0,
                    tint = IntelGoldLight,
                    onClick = {
                        viewModel.startNewQuizSession("flag")
                        activeSession = true
                    },
                    modifier = Modifier.testTag("mode_flag_btn")
                )

                Spacer(modifier = Modifier.height(12.dp))

                QuizModeCard(
                    title = "Dynamic AI Trivia",
                    description = "Request real-time unexpected trivia questions compiled dynamically by the satellite model.",
                    icon = Icons.Default.Psychology,
                    highScore = highScores["trivia"]?.highScore ?: 0,
                    plays = highScores["trivia"]?.totalPlayed ?: 0,
                    tint = IntelGold,
                    onClick = {
                        viewModel.startNewQuizSession("trivia")
                        activeSession = true
                    },
                    modifier = Modifier.testTag("mode_trivia_btn")
                )

                Spacer(modifier = Modifier.height(12.dp))

                QuizModeCard(
                    title = "Active Facts Quiz",
                    description = "Evaluate tactical analytics of sovereign territories derived natively from local databases.",
                    icon = Icons.Default.Info,
                    highScore = highScores["active_facts"]?.highScore ?: 0,
                    plays = highScores["active_facts"]?.totalPlayed ?: 0,
                    tint = IntelGoldLight,
                    onClick = {
                        viewModel.startNewQuizSession("active_facts")
                        activeSession = true
                    },
                    modifier = Modifier.testTag("mode_active_facts_btn")
                )

                if (quizHistory.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(28.dp))
                    
                    Text(
                        text = "AGENT PERFORMANCE REPORT",
                        style = MaterialTheme.typography.labelMedium,
                        color = IntelGold,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp,
                        modifier = Modifier.align(Alignment.Start)
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    val totalMissions = quizHistory.size
                    val avgScore = quizHistory.map { it.score }.average().let { if (it.isNaN()) 0 else it.toInt() }
                    val agentRank = when {
                        avgScore >= 8 -> "DIRECTOR OF INTEL"
                        avgScore >= 5 -> "SENIOR ANALYST"
                        avgScore >= 3 -> "FIELD AGENT"
                        else -> "CADET OBSERVER"
                    }
                    val rankColor = when {
                        avgScore >= 8 -> Color(0xFFBA1A1A)
                        avgScore >= 5 -> Color(0xFF00639B)
                        avgScore >= 3 -> Color(0xFF0A5C22)
                        else -> Color(0xFF64748B)
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth().testTag("agent_performance_card"),
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
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "$totalMissions",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Black,
                                    color = IntelOnSurface
                                )
                                Text(
                                    text = "MISSIONS",
                                    fontSize = 8.sp,
                                    color = IntelMuted,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.sp
                                )
                            }
                            
                            VerticalDivider(color = IntelOnSurface.copy(alpha = 0.15f), modifier = Modifier.height(24.dp))
                            
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "$avgScore",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Black,
                                    color = IntelOnSurface
                                )
                                Text(
                                    text = "AVG SCORE",
                                    fontSize = 8.sp,
                                    color = IntelMuted,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.sp
                                )
                            }
                            
                            VerticalDivider(color = IntelOnSurface.copy(alpha = 0.15f), modifier = Modifier.height(24.dp))
                            
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(rankColor.copy(alpha = 0.12f))
                                        .border(BorderStroke(1.dp, rankColor.copy(alpha = 0.8f)), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = agentRank,
                                        fontSize = 9.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = rankColor,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "ACADEMY RANK",
                                    fontSize = 8.sp,
                                    color = IntelMuted,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    AgentPerformanceTrendChart(history = quizHistory)

                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "RECENT PLANS",
                            style = MaterialTheme.typography.labelMedium,
                            color = if (!isShowingLeaderboard) IntelGold else IntelMuted,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp,
                            modifier = Modifier
                                .clickable { isShowingLeaderboard = false }
                                .padding(vertical = 4.dp, horizontal = 8.dp)
                                .testTag("tab_history")
                        )
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        Text(
                            text = "LEADERBOARD",
                            style = MaterialTheme.typography.labelMedium,
                            color = if (isShowingLeaderboard) IntelGold else IntelMuted,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp,
                            modifier = Modifier
                                .clickable { isShowingLeaderboard = true }
                                .padding(vertical = 4.dp, horizontal = 8.dp)
                                .testTag("tab_leaderboard")
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = IntelSurface),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, IntelOnSurface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            val itemsToShow = if (isShowingLeaderboard) {
                                quizHistory.sortedByDescending { it.score }.take(10)
                            } else {
                                quizHistory.take(5)
                            }
                            
                            itemsToShow.forEachIndexed { index, round ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        if (isShowingLeaderboard) {
                                            Text(
                                                text = "#${index + 1}",
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = when (index) {
                                                    0 -> IntelGold
                                                    1 -> IntelGoldLight
                                                    else -> IntelMuted
                                                },
                                                modifier = Modifier.width(28.dp)
                                            )
                                        } else {
                                            Icon(
                                                imageVector = when (round.quizMode) {
                                                    "capital" -> Icons.Default.LocationCity
                                                    "flag" -> Icons.Default.Flag
                                                    "trivia" -> Icons.Default.Psychology
                                                    "active_facts" -> Icons.Default.Info
                                                    else -> Icons.Default.Star
                                                },
                                                contentDescription = null,
                                                tint = IntelGold,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(10.dp))
                                        }
                                        Column {
                                            Text(
                                                text = when (round.quizMode) {
                                                    "capital" -> "Capital Hunt"
                                                    "flag" -> "Flag Patrol"
                                                    "trivia" -> "AI Trivia"
                                                    "active_facts" -> "Active Facts"
                                                    else -> round.quizMode.uppercase()
                                                },
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = IntelOnSurface,
                                                fontWeight = FontWeight.Bold
                                            )
                                            
                                            val dateStr = remember(round.completedAt) {
                                                val sdf = java.text.SimpleDateFormat("MMM dd, HH:mm", java.util.Locale.getDefault())
                                                sdf.format(java.util.Date(round.completedAt))
                                            }
                                            Text(
                                                text = dateStr,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = IntelMuted
                                            )
                                        }
                                    }
                                    
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(IntelSecondary.copy(alpha = 0.2f))
                                            .padding(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "${round.score} PTS",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = IntelGold,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                }
                                if (index < itemsToShow.lastIndex) {
                                    HorizontalDivider(color = IntelOnSurface.copy(alpha = 0.2f), thickness = 1.dp)
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Game Active UI
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                // Header (Score & Lives & Quit)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { activeSession = false },
                        modifier = Modifier.testTag("quit_quiz_btn")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Abort session", tint = IntelAlert)
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(IntelSurface)
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "SCORE: $score",
                            color = IntelGold,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    // Lives representation (Hearts)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        repeat(3) { index ->
                            val isAlive = index < lives
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = "Life heart",
                                tint = if (isAlive) IntelAlert else IntelMuted.copy(alpha = 0.3f),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                if (lives <= 0) {
                    // Game Over Screen
                    GameOverLayout(
                        score = score,
                        quizMode = qType,
                        highScore = highScores[qType]?.highScore ?: 0,
                        onReplay = { viewModel.startNewQuizSession(qType) },
                        onHome = { activeSession = false }
                    )
                } else if (isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = IntelGold)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                "Compiling tactical challenge metrics...",
                                style = MaterialTheme.typography.bodySmall,
                                color = IntelGold
                            )
                        }
                    }
                } else if (question != null) {
                    val q = question!!

                    // Question Box
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = IntelSurface)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(IntelSecondary.copy(alpha = 0.2f), IntelSurface)
                                    )
                                )
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = q.question,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = IntelOnSurface,
                                textAlign = TextAlign.Center,
                                lineHeight = 30.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Option Buttons
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        q.options.forEachIndexed { idx, opt ->
                            val isAnswered = answeredCorrectly != null
                            val isSelected = selectedIndex == idx
                            val isCorrectIdx = idx == q.correctIndex

                            val buttonColor = when {
                                !isAnswered -> IntelSurfaceVariant // Unanswered state
                                isCorrectIdx -> Color(0xFF15803D) // Green for correct option
                                isSelected -> IntelAlert // Red for selected wrong option
                                else -> IntelSurfaceVariant.copy(alpha = 0.4f) // Dim others
                            }

                            Card(
                                onClick = {
                                    if (!isAnswered) {
                                        viewModel.submitAnswer(idx)
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 52.dp)
                                    .testTag("option_btn_$idx"),
                                colors = CardDefaults.cardColors(containerColor = buttonColor),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(
                                    1.dp,
                                    if (isSelected && isAnswered) Color.White else Color.Transparent
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = 16.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = opt,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = IntelOnSurface,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.weight(1f)
                                    )

                                    if (isAnswered) {
                                        if (isCorrectIdx) {
                                            Icon(Icons.Default.CheckCircle, contentDescription = "Correct", tint = Color.Green)
                                        } else if (isSelected) {
                                            Icon(Icons.Default.Cancel, contentDescription = "Wrong", tint = Color.White)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Explanation & Next button drawer
                    AnimatedVisibility(
                        visible = answeredCorrectly != null,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        Column {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = IntelSurfaceVariant)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = "Analysis explanation",
                                        tint = IntelGold,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "INTELLIGENCE BRIEFING",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = IntelGold
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = q.explanation,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = IntelOnBg
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = { viewModel.loadNextQuizQuestion() },
                                colors = ButtonDefaults.buttonColors(containerColor = IntelGold, contentColor = IntelDarkBg),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("next_question_btn")
                            ) {
                                Text("NEXT INTEL CHALLENGE", fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(Icons.Default.ArrowForward, contentDescription = "Next")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun QuizModeCard(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    highScore: Int,
    plays: Int,
    tint: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = IntelSurface),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(2.dp, IntelOnSurface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(tint.copy(alpha = 0.15f))
                    .border(BorderStroke(1.5.dp, IntelOnSurface), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = IntelOnSurface, modifier = Modifier.size(26.dp))
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black, color = IntelOnSurface)
                Text(text = description, style = MaterialTheme.typography.bodySmall, color = IntelOnBg, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 4.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "RECORD: $highScore PTS",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = IntelGold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "TRIALS: $plays",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = IntelMuted,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            Icon(Icons.Default.PlayArrow, contentDescription = "Start game", tint = IntelOnSurface, modifier = Modifier.size(28.dp))
        }
    }
}

@Composable
fun GameOverLayout(
    score: Int,
    quizMode: String,
    highScore: Int,
    onReplay: () -> Unit,
    onHome: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = IntelSurface),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(2.dp, IntelOnSurface)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.EmojiEvents,
                contentDescription = "Throphy icon",
                tint = IntelGold,
                modifier = Modifier.size(64.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "SIMULATION TERMINATED",
                style = MaterialTheme.typography.titleSmall,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Black,
                color = IntelAlert
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Intelligence Exam Finished",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black,
                color = IntelOnSurface
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Score plate
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(PastelPinkBg)
                    .border(BorderStroke(1.5.dp, IntelOnSurface), RoundedCornerShape(12.dp))
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "EARNED POINTS", fontSize = 10.sp, color = IntelOnSurface, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                    Text(text = "$score", fontSize = 36.sp, fontWeight = FontWeight.Black, color = IntelOnSurface)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (score >= highScore && score > 0) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(30.dp))
                        .background(IntelGoldLight)
                        .border(BorderStroke(1.dp, IntelOnSurface), RoundedCornerShape(30.dp))
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text("NEW DECLASSIFIED RECORD!", fontSize = 11.sp, color = IntelOnSurface, fontWeight = FontWeight.Black)
                }
            } else {
                Text("Pre-existing record: $highScore pts", fontSize = 11.sp, color = IntelMuted, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onReplay,
                    colors = ButtonDefaults.buttonColors(containerColor = IntelOnSurface, contentColor = Color.White),
                    border = BorderStroke(1.5.dp, IntelOnSurface),
                    shape = RoundedCornerShape(30.dp),
                    modifier = Modifier
                        .weight(1.5f)
                        .testTag("quiz_replay_btn")
                ) {
                    Text("RETRY ATTEMPT", fontWeight = FontWeight.Black, letterSpacing = 0.5.sp)
                }

                Button(
                    onClick = onHome,
                    colors = ButtonDefaults.buttonColors(containerColor = IntelSurface, contentColor = IntelOnSurface),
                    border = BorderStroke(1.5.dp, IntelOnSurface),
                    shape = RoundedCornerShape(30.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("quiz_home_btn")
                ) {
                    Text("EXIT CELL", fontWeight = FontWeight.Black, letterSpacing = 0.5.sp)
                }
            }
        }
    }
}

@Composable
fun AgentPerformanceTrendChart(
    history: List<com.example.ui.QuizRound>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("agent_performance_trend_chart"),
        colors = CardDefaults.cardColors(containerColor = IntelSurface),
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
                        text = "TACTICAL DIAGNOSTIC HISTORY VECTOR",
                        style = MaterialTheme.typography.labelSmall,
                        color = IntelGold,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Competence Trajectory (Last 10 Runs)",
                        style = MaterialTheme.typography.titleSmall,
                        color = IntelOnSurface,
                        fontWeight = FontWeight.Bold
                    )
                }
                Icon(
                    imageVector = Icons.Default.TrendingUp,
                    contentDescription = null,
                    tint = IntelGold,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            val chronologicalHistory = remember(history) {
                history.take(10).reversed()
            }

            if (chronologicalHistory.size < 2) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .background(IntelDarkBg.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .border(BorderStroke(1.dp, IntelOnSurface.copy(alpha = 0.08f)), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Timeline,
                            contentDescription = null,
                            tint = IntelMuted,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "INSUFFICIENT TELEMETRY VECTOR",
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = IntelGold
                        )
                        Text(
                            text = "Complete at least 2 simulator sessions to plot skills index",
                            fontSize = 9.sp,
                            color = IntelMuted,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                var selectedPointIndex by remember { mutableStateOf(-1) }

                val maxScore = remember(chronologicalHistory) {
                    val maxInHistory = chronologicalHistory.maxOf { it.score.toFloat() }
                    maxOf(30f, maxInHistory) * 1.15f
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                ) {
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(chronologicalHistory) {
                                detectTapGestures { offset ->
                                    val leftOffset = 24.dp.toPx()
                                    val rightOffset = 16.dp.toPx()
                                    val topOffset = 12.dp.toPx()
                                    val bottomOffset = 20.dp.toPx()

                                    val chartWidth = size.width - leftOffset - rightOffset
                                    val stepX = chartWidth / (chronologicalHistory.size - 1)

                                    var minDistance = Float.MAX_VALUE
                                    var closestIndex = -1

                                    for (i in chronologicalHistory.indices) {
                                        val x = leftOffset + (i * stepX)
                                        val dist = abs(offset.x - x)
                                        if (dist < minDistance && dist < stepX / 2f) {
                                            minDistance = dist
                                            closestIndex = i
                                        }
                                    }

                                    selectedPointIndex = if (selectedPointIndex == closestIndex) -1 else closestIndex
                                }
                            }
                    ) {
                        val leftOffset = 24.dp.toPx()
                        val rightOffset = 16.dp.toPx()
                        val topOffset = 12.dp.toPx()
                        val bottomOffset = 20.dp.toPx()

                        val chartWidth = size.width - leftOffset - rightOffset
                        val chartHeight = size.height - topOffset - bottomOffset

                        // Draw Grid lines
                        val gridLines = 4
                        for (i in 0 until gridLines) {
                            val ratio = i.toFloat() / (gridLines - 1)
                            val y = topOffset + chartHeight * (1f - ratio)
                            drawLine(
                                color = IntelOnSurface.copy(alpha = 0.08f),
                                start = Offset(leftOffset, y),
                                end = Offset(size.width - rightOffset, y),
                                strokeWidth = 1.dp.toPx()
                            )
                        }

                        // Coordinates
                        val points = chronologicalHistory.mapIndexed { i, item ->
                            val ratioX = if (chronologicalHistory.size > 1) i.toFloat() / (chronologicalHistory.size - 1) else 0f
                            val ratioY = item.score.toFloat() / maxScore
                            val x = leftOffset + ratioX * chartWidth
                            val y = topOffset + (1f - ratioY) * chartHeight
                            Offset(x, y)
                        }

                        // Gradient Area Path
                        val gradientPath = Path()
                        if (points.isNotEmpty()) {
                            gradientPath.moveTo(points.first().x, size.height - bottomOffset)
                            for (p in points) {
                                gradientPath.lineTo(p.x, p.y)
                            }
                            gradientPath.lineTo(points.last().x, size.height - bottomOffset)
                            gradientPath.close()

                            drawPath(
                                path = gradientPath,
                                brush = Brush.verticalGradient(
                                    colors = listOf(IntelGold.copy(alpha = 0.2f), Color.Transparent),
                                    startY = topOffset,
                                    endY = size.height - bottomOffset
                                )
                            )
                        }

                        // Trend Line Path
                        val linePath = Path()
                        if (points.isNotEmpty()) {
                            linePath.moveTo(points.first().x, points.first().y)
                            for (i in 1 until points.size) {
                                val prev = points[i - 1]
                                val curr = points[i]
                                // Cubic bezier curve for beautiful smooth tracking
                                val cx1 = prev.x + (curr.x - prev.x) / 2f
                                val cy1 = prev.y
                                val cx2 = prev.x + (curr.x - prev.x) / 2f
                                val cy2 = curr.y
                                linePath.cubicTo(cx1, cy1, cx2, cy2, curr.x, curr.y)
                            }
                            drawPath(
                                path = linePath,
                                color = IntelGold,
                                style = Stroke(width = 2.5.dp.toPx())
                            )
                        }

                        // Highlight points
                        points.forEachIndexed { idx, pt ->
                            val isSelected = idx == selectedPointIndex
                            val radius = if (isSelected) 6.dp.toPx() else 4.dp.toPx()
                            val outlineColor = if (isSelected) Color.White else IntelGold

                            if (isSelected) {
                                drawLine(
                                    color = IntelGold.copy(alpha = 0.4f),
                                    start = Offset(pt.x, topOffset),
                                    end = Offset(pt.x, size.height - bottomOffset),
                                    strokeWidth = 1.dp.toPx()
                                )
                            }

                            drawCircle(
                                color = IntelDarkBg,
                                radius = radius,
                                center = pt
                            )
                            drawCircle(
                                color = outlineColor,
                                radius = radius,
                                center = pt,
                                style = Stroke(width = 2.dp.toPx())
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                val activePointIdx = if (selectedPointIndex != -1) selectedPointIndex else chronologicalHistory.lastIndex
                val activeRound = chronologicalHistory[activePointIdx]
                val modeLabel = when (activeRound.quizMode) {
                    "capital" -> "CAPITAL HUNT"
                    "flag" -> "FLAG PATROL"
                    "trivia" -> "AI TRIVIA"
                    "active_facts" -> "ACTIVE FACTS"
                    else -> activeRound.quizMode.uppercase()
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(IntelDarkBg.copy(alpha = 0.6f))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "POINT DATA: $modeLabel",
                        fontSize = 8.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = IntelGold
                    )
                    Text(
                        text = "Score: ${activeRound.score} pts",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = IntelOnSurface
                    )
                }
            }
        }
    }
}

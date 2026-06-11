package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.CompareArrows
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.School
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.*
import com.example.ui.theme.FactbookTheme
import com.example.ui.theme.IntelDarkBg
import com.example.ui.theme.IntelGold
import com.example.ui.theme.IntelSurface
import com.example.ui.theme.IntelMuted
import com.example.ui.theme.IntelSecondary

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FactbookTheme {
                val viewModel: FactbookViewModel = viewModel()
                val currentScreen by viewModel.currentScreen.collectAsState()

                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(IntelDarkBg),
                    bottomBar = {
                        // We only show bottom navigation if NOT in deep-country details to maximize focus!
                        if (currentScreen !is Screen.CountryDetail) {
                                NavigationBar(
                                    containerColor = IntelDarkBg,
                                    tonalElevation = 0.dp,
                                    modifier = Modifier
                                        .windowInsetsPadding(WindowInsets.navigationBars)
                                        .border(BorderStroke(0.5.dp, IntelMuted.copy(alpha = 0.15f)), RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                                ) {
                                    val isDashboard = currentScreen is Screen.Dashboard
                                    NavigationBarItem(
                                        selected = isDashboard,
                                        onClick = { viewModel.navigateTo(Screen.Dashboard) },
                                        icon = { 
                                            Icon(
                                                imageVector = if (isDashboard) Icons.Filled.Explore else Icons.Outlined.Explore, 
                                                contentDescription = "Factbook Archive"
                                            ) 
                                        },
                                        label = { Text("Factbook") },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = IntelSecondary,
                                            selectedTextColor = IntelSecondary,
                                            indicatorColor = Color.Transparent,
                                            unselectedIconColor = IntelMuted,
                                            unselectedTextColor = IntelMuted
                                        ),
                                        modifier = Modifier.testTag("nav_factbook")
                                    )

                                    val isCompare = currentScreen is Screen.Compare
                                    NavigationBarItem(
                                        selected = isCompare,
                                        onClick = { viewModel.navigateTo(Screen.Compare) },
                                        icon = { 
                                            Icon(
                                                imageVector = if (isCompare) Icons.Filled.CompareArrows else Icons.Outlined.CompareArrows, 
                                                contentDescription = "Diplomatic Compare"
                                            ) 
                                        },
                                        label = { Text("Compare") },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = IntelSecondary,
                                            selectedTextColor = IntelSecondary,
                                            indicatorColor = Color.Transparent,
                                            unselectedIconColor = IntelMuted,
                                            unselectedTextColor = IntelMuted
                                        ),
                                        modifier = Modifier.testTag("nav_compare")
                                    )

                                    val isQuiz = currentScreen is Screen.Quiz
                                    NavigationBarItem(
                                        selected = isQuiz,
                                        onClick = { viewModel.navigateTo(Screen.Quiz) },
                                        icon = { 
                                            Icon(
                                                imageVector = if (isQuiz) Icons.Filled.School else Icons.Outlined.School, 
                                                contentDescription = "Training Academy"
                                            ) 
                                        },
                                        label = { Text("Academy") },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = IntelSecondary,
                                            selectedTextColor = IntelSecondary,
                                            indicatorColor = Color.Transparent,
                                            unselectedIconColor = IntelMuted,
                                            unselectedTextColor = IntelMuted
                                        ),
                                        modifier = Modifier.testTag("nav_quiz")
                                    )

                                    val isFavorites = currentScreen is Screen.Favorites
                                    NavigationBarItem(
                                        selected = isFavorites,
                                        onClick = { viewModel.navigateTo(Screen.Favorites) },
                                        icon = { 
                                            Icon(
                                                imageVector = if (isFavorites) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder, 
                                                contentDescription = "Private Vault"
                                            ) 
                                        },
                                        label = { Text("Vault") },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = IntelSecondary,
                                            selectedTextColor = IntelSecondary,
                                            indicatorColor = Color.Transparent,
                                            unselectedIconColor = IntelMuted,
                                            unselectedTextColor = IntelMuted
                                        ),
                                        modifier = Modifier.testTag("nav_vault")
                                    )
                                }
                        }
                    }
                ) { innerPadding ->
                    // Animated transitions between screen changes!
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(
                                bottom = if (currentScreen is Screen.CountryDetail) 0.dp else innerPadding.calculateBottomPadding(),
                                top = innerPadding.calculateTopPadding()
                            )
                    ) {
                        AnimatedContent(
                            targetState = currentScreen,
                            transitionSpec = {
                                fadeIn() togetherWith fadeOut()
                            },
                            label = "screen_navigation"
                        ) { screen ->
                            when (screen) {
                                is Screen.Dashboard -> {
                                    DashboardScreen(viewModel = viewModel)
                                }
                                is Screen.CountryDetail -> {
                                    CountryDetailScreen(country = screen.country, viewModel = viewModel)
                                }
                                is Screen.Compare -> {
                                    CompareScreen(viewModel = viewModel)
                                }
                                is Screen.Quiz -> {
                                    QuizScreen(viewModel = viewModel)
                                }
                                is Screen.Favorites -> {
                                    FavoritesScreen(viewModel = viewModel)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

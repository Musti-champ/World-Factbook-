package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.FolderSpecial
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.*
import com.example.ui.theme.FactbookTheme
import com.example.ui.theme.IntelDarkBg
import com.example.ui.theme.IntelGold
import com.example.ui.theme.IntelSurface

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
                                containerColor = IntelSurface,
                                tonalElevation = 8.dp,
                                modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
                            ) {
                                NavigationBarItem(
                                    selected = currentScreen is Screen.Dashboard,
                                    onClick = { viewModel.navigateTo(Screen.Dashboard) },
                                    icon = { Icon(Icons.Default.Public, contentDescription = "Factbook Archive") },
                                    label = { Text("Factbook") },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = IntelSurface,
                                        selectedTextColor = IntelGold,
                                        indicatorColor = IntelGold,
                                        unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                        unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    ),
                                    modifier = Modifier.testTag("nav_factbook")
                                )

                                NavigationBarItem(
                                    selected = currentScreen is Screen.Compare,
                                    onClick = { viewModel.navigateTo(Screen.Compare) },
                                    icon = { Icon(Icons.Default.CompareArrows, contentDescription = "Diplomatic Compare") },
                                    label = { Text("Compare") },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = IntelSurface,
                                        selectedTextColor = IntelGold,
                                        indicatorColor = IntelGold,
                                        unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                        unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    ),
                                    modifier = Modifier.testTag("nav_compare")
                                )

                                NavigationBarItem(
                                    selected = currentScreen is Screen.Quiz,
                                    onClick = { viewModel.navigateTo(Screen.Quiz) },
                                    icon = { Icon(Icons.Default.Psychology, contentDescription = "Training Academy") },
                                    label = { Text("Academy") },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = IntelSurface,
                                        selectedTextColor = IntelGold,
                                        indicatorColor = IntelGold,
                                        unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                        unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    ),
                                    modifier = Modifier.testTag("nav_quiz")
                                )

                                NavigationBarItem(
                                    selected = currentScreen is Screen.Favorites,
                                    onClick = { viewModel.navigateTo(Screen.Favorites) },
                                    icon = { Icon(Icons.Default.FolderSpecial, contentDescription = "Private Vault") },
                                    label = { Text("Vault") },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = IntelSurface,
                                        selectedTextColor = IntelGold,
                                        indicatorColor = IntelGold,
                                        unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                        unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
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

package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.data.KeyboardPreferences
import com.example.ui.screens.CustomizationScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.OnboardingScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.ThemesScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val preferences = KeyboardPreferences.getInstance(this)

        setContent {
            KeyWaveApp(preferences = preferences)
        }
    }
}

@Composable
fun KeyWaveApp(preferences: KeyboardPreferences) {
    val config by preferences.configFlow.collectAsState()
    val navController = rememberNavController()

    val darkColors = remember {
        darkColorScheme(
            primary = Color(0xFF00F5D4),
            secondary = Color(0xFF7B2CBF),
            tertiary = Color(0xFFFF007F),
            background = Color(0xFF080914),
            surface = Color(0xFF121424),
            onPrimary = Color(0xFF0A0C16),
            onBackground = Color(0xFFF1F5F9),
            onSurface = Color(0xFFF1F5F9)
        )
    }

    MaterialTheme(colorScheme = darkColors) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF080914))
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            NavHost(
                navController = navController,
                startDestination = "home"
            ) {
                composable("home") {
                    HomeScreen(
                        config = config,
                        preferences = preferences,
                        onNavigateToCustomize = { navController.navigate("customize") },
                        onNavigateToThemes = { navController.navigate("themes") },
                        onNavigateToSettings = { navController.navigate("settings") },
                        onNavigateToOnboarding = { navController.navigate("onboarding") }
                    )
                }

                composable("customize") {
                    CustomizationScreen(
                        config = config,
                        preferences = preferences,
                        onBack = { navController.popBackStack() }
                    )
                }

                composable("themes") {
                    ThemesScreen(
                        config = config,
                        preferences = preferences,
                        onBack = { navController.popBackStack() }
                    )
                }

                composable("settings") {
                    SettingsScreen(
                        config = config,
                        preferences = preferences,
                        onNavigateToCustomize = { navController.navigate("customize") },
                        onNavigateToThemes = { navController.navigate("themes") },
                        onBack = { navController.popBackStack() }
                    )
                }

                composable("onboarding") {
                    OnboardingScreen(
                        config = config,
                        preferences = preferences,
                        onFinish = {
                            navController.navigate("home") {
                                popUpTo("onboarding") { inclusive = true }
                            }
                        }
                    )
                }
            }
        }
    }
}

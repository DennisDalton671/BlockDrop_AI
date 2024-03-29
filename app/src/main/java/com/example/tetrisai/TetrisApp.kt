package com.example.tetrisai

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.tetrisai.View.GameScreen
import com.example.tetrisai.View.MenuScreen
import com.example.tetrisai.ViewModel.GameStateManager

@Composable
fun TetrisApp(appContainer: AppContainer) {
// Use Material3 if available, or Material2's Scaffold as shown
    val navController = rememberNavController()

    Scaffold(
        // Scaffold structure here, if you're using additional UI elements like TopAppBar
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "menuScreen",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("menuScreen") {
                MenuScreen().MenuScreenSetup(navController)
            }
            composable("gameScreen") {
                val gameState = viewModel<GameStateManager>()
                GameScreen().GameScreenSetup(gameView = gameState, navController)
            }
            // Add more destinations as needed
        }
    }

}
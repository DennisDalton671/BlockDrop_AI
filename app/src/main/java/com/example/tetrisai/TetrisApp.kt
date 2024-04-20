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
import com.example.tetrisai.ViewModel.GameStateManagerFactory

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
            composable("userGameScreen") {
                val factory = GameStateManagerFactory(gameMode = 0)
                val gameState = viewModel<GameStateManager>(factory = factory)
                //val gameState = viewModel<GameStateManager>()
                GameScreen().GameScreenSetup(gameView = gameState, navController)
            }
            composable("aiGameScreen") {
                val factory = GameStateManagerFactory(gameMode = 1)
                val gameState = viewModel<GameStateManager>(factory = factory)
                //val gameState = viewModel<GameStateManager>()
                GameScreen().GameScreenSetup(gameView = gameState, navController = navController)
            }
            // Add more destinations as needed
        }
    }

}
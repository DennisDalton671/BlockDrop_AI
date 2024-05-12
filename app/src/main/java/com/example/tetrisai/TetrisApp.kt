package com.example.tetrisai

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.tetrisai.View.EndGameScreen
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
                GameScreen().GameScreenSetup(gameView = gameState, navController, 0)
            }
            composable("aiGameScreen") {
                val factory = GameStateManagerFactory(gameMode = 1)
                val gameState = viewModel<GameStateManager>(factory = factory)
                //val gameState = viewModel<GameStateManager>()
                GameScreen().GameScreenSetup(gameView = gameState, navController = navController,1)
            }
            composable("machineAiGameScreen") {
                val factory = GameStateManagerFactory(gameMode = 2)
                val gameState = viewModel<GameStateManager>(factory = factory)
                //val gameState = viewModel<GameStateManager>()
                GameScreen().GameScreenSetup(gameView = gameState, navController = navController,1)
            }
            composable("endGameScreen/{score}/{linesCleared}/{level}/{timePlayed}/{singleLines}/{doubleLines}/{tripleLines}/{tetrisLines}/{gameState}",
                arguments = listOf(
                    navArgument("score") { type = NavType.IntType },
                    navArgument("linesCleared") { type = NavType.IntType },
                    navArgument("level") { type = NavType.IntType },
                    navArgument("timePlayed") { type = NavType.StringType },
                    navArgument("singleLines") { type = NavType.IntType },
                    navArgument("doubleLines") { type = NavType.IntType },
                    navArgument("tripleLines") { type = NavType.IntType },
                    navArgument("tetrisLines") { type = NavType.IntType },
                    navArgument("gameState") { type = NavType.IntType }
                )
            ) { backStackEntry ->
                EndGameScreen().GameEndScreen(
                    score = backStackEntry.arguments?.getInt("score") ?: 0,
                    linesCleared = backStackEntry.arguments?.getInt("linesCleared") ?: 0,
                    level = backStackEntry.arguments?.getInt("level") ?: 0,
                    timePlayed = backStackEntry.arguments?.getString("timePlayed") ?: "0:00",
                    singleLines = backStackEntry.arguments?.getInt("singleLines") ?: 0,
                    doubleLines = backStackEntry.arguments?.getInt("doubleLines") ?: 0,
                    tripleLines = backStackEntry.arguments?.getInt("tripleLines") ?: 0,
                    tetrisLines = backStackEntry.arguments?.getInt("tetrisLines") ?: 0,
                    gameState = backStackEntry.arguments?.getInt("gameState") ?: 0,
                    navController = navController
                )
            }
            // Add more destinations as needed
        }
    }

}


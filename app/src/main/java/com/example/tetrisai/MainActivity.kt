package com.example.tetrisai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.tetrisai.View.GameScreen
import com.example.tetrisai.View.MenuScreen
import com.example.tetrisai.ui.theme.TetrisAITheme


class MainActivity : ComponentActivity() {

    private val appContainer: AppContainer by lazy {
        DefaultAppContainer()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TetrisAITheme {
                TetrisApp(appContainer = appContainer)
            }
        }
    }

}


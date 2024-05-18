package com.example.tetrisai

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        val app = application as TetrisApplication
        val mediaPlayer = app.mediaPlayer
        val currentVolume = app.currentVolume

        setContent {
            val themeSettings = remember { mutableStateOf(app.themeSettings) }

            TetrisAITheme {
                TetrisApp(
                    appContainer = app.container,
                    mediaPlayer = mediaPlayer,
                    initialVolume = currentVolume,
                    saveVolume = app::saveVolume,
                    themeSettings = themeSettings,
                    saveThemeSettings = app::saveThemeSettings,
                    context = this
                )
            }
        }
    }

}


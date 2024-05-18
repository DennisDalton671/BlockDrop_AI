package com.example.tetrisai

import android.app.Application
import android.content.Context
import android.media.MediaPlayer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.example.tetrisai.View.ThemeSettings

class TetrisApplication : Application() {
    lateinit var container: AppContainer
        private set
    lateinit var mediaPlayer: MediaPlayer
    var currentVolume: Float = 0.5f
    var themeSettings: ThemeSettings = ThemeSettings(Color.Black, Color.LightGray, Color.Red) // Default theme
    override fun onCreate() {

        super.onCreate()

        val sharedPreferences = getSharedPreferences("TetrisPreferences", Context.MODE_PRIVATE)
        currentVolume = sharedPreferences.getFloat("volume", 0.5f) // Get saved volume or default to 0.5f

        val backgroundColor = Color(sharedPreferences.getInt("backgroundColor", Color.Black.toArgb()))
        val textColor = Color(sharedPreferences.getInt("textColor", Color.LightGray.toArgb()))
        val blockColor = Color(sharedPreferences.getInt("blockColor", Color.Red.toArgb()))

        themeSettings = ThemeSettings(backgroundColor, textColor, blockColor)

        val randomInt = (1..2).random()
        mediaPlayer = if (randomInt == 1) {
            MediaPlayer.create(this, R.raw.pixel_dropper)
        } else {
            MediaPlayer.create(this, R.raw.block_droppin)
        }

        mediaPlayer.isLooping = true
        setMediaPlayerVolume(currentVolume)

        mediaPlayer.start()

        container = DefaultAppContainer()


    }

    private fun setMediaPlayerVolume(volume: Float) {
        mediaPlayer.setVolume(volume, volume)
    }

    fun saveVolume(volume: Float) {
        val sharedPreferences = getSharedPreferences("TetrisPreferences", Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putFloat("volume", volume)
            apply()
        }
    }

    fun saveThemeSettings(themeSettings: ThemeSettings) {
        val sharedPreferences = getSharedPreferences("TetrisPreferences", Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putInt("backgroundColor", themeSettings.backgroundColor.toArgb())
            putInt("textColor", themeSettings.textColor.toArgb())
            putInt("blockColor", themeSettings.blockColor.toArgb())
            apply()
        }
    }


    override fun onTerminate() {
        super.onTerminate()
        mediaPlayer.release()
    }

}
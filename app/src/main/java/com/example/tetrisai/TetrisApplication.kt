package com.example.tetrisai

import android.app.Application
import android.media.MediaPlayer

class TetrisApplication : Application() {

    private lateinit var mediaPlayer: MediaPlayer
    lateinit var container: AppContainer
        private set
    override fun onCreate() {

        super.onCreate()

        mediaPlayer = MediaPlayer.create(this, R.raw.pixel_dropper)
        mediaPlayer.isLooping = true

        mediaPlayer.start()

        container = DefaultAppContainer()


    }

}
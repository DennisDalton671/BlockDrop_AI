package com.example.tetrisai

import android.app.Application
import android.media.MediaPlayer

class TetrisApplication : Application() {

    private lateinit var mediaPlayer: MediaPlayer
    lateinit var container: AppContainer
        private set
    override fun onCreate() {

        super.onCreate()

        val randomInt = (1..2).random()

        if (randomInt == 1) {
            mediaPlayer = MediaPlayer.create(this, R.raw.pixel_dropper)
        }

        if (randomInt == 2) {
            mediaPlayer = MediaPlayer.create(this, R.raw.block_droppin)
        }

        mediaPlayer.isLooping = true

        mediaPlayer.start()

        container = DefaultAppContainer()


    }

}
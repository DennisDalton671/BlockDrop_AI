package com.example.tetrisai.model

class GameStats {
    var singleLinesCleared: Int = 0
    var doubleLinesCleared: Int = 0
    var tripleLinesCleared: Int = 0
    var tetrisLinesCleared: Int = 0  // Typically, 'Tetris' means clearing four lines at once

    fun incrementSingleLines() {
        singleLinesCleared++
    }

    fun incrementDoubleLines() {
        doubleLinesCleared++
    }

    fun incrementTripleLines() {
        tripleLinesCleared++
    }

    fun incrementTetrisLines() {
        tetrisLinesCleared++
    }
}
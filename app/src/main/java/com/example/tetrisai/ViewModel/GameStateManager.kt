package com.example.tetrisai.ViewModel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.tetrisai.model.Cell
import com.example.tetrisai.model.GameLogic
import com.example.tetrisai.model.GameStats
import com.example.tetrisai.model.GridRepresentation
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.abs

class GameStateManager(private val gameMode: Int, context: Context) : ViewModel() {

    private val _score = MutableStateFlow(0)
    val score: StateFlow<Int> = _score.asStateFlow()

    private val _level = MutableStateFlow(1)
    val level: StateFlow<Int> = _level.asStateFlow()

    private val _lines = MutableStateFlow(1)
    val lines: StateFlow<Int> = _lines.asStateFlow()

    private val _lockedPiece = MutableStateFlow(false)
    val lockedPiece: StateFlow<Boolean> = _lockedPiece.asStateFlow()

    private val _gridRepresentation = MutableStateFlow<Array<Array<Cell>>>(emptyArray())
    val gridRepresentation: StateFlow<Array<Array<Cell>>> = _gridRepresentation.asStateFlow()

    private val _gameOver = MutableStateFlow(false)
    val gameOver: StateFlow<Boolean> = _gameOver.asStateFlow()

    private val gameLogic = GameLogic(context)

    init {
        startGame(gameMode)
        gameLogic.currentGridState.onEach { newGrid ->
            _gridRepresentation.value = newGrid
        }.launchIn(viewModelScope)
        gameLogic.onScoreUpdated = { newScore ->
            _score.value = newScore
        }
        gameLogic.onLevelUpdated = { newLevel ->
            _level.value = newLevel
        }
        gameLogic.onLineUpdated = { newLine ->
            _lines.value = newLine
        }
        gameLogic.onGameOver = { newState ->
            _gameOver.value = newState
        }
    }

    fun startGame(mode: Int) {
        // Initialize game state here
        // For example, reset score and level and generate a starting grid
        gameLogic.initializeGame(gameMode = mode)
        _score.value = gameLogic.score.value
        _level.value = gameLogic.level.value
        _lines.value = gameLogic.linesCleared
        _lockedPiece.value = gameLogic.lockedPiece
    }

    fun rotateTetrimino() {
        // Rotate the current Tetrimino and update the grid
        gameLogic.rotate()
    }

    fun dropTetrimino() {
        // Drop the Tetrimino down instantly and lock it in place
        gameLogic.drop()
    }

    fun moveLeft() {
        gameLogic.moveLeft()
    }

    fun moveRight() {
        gameLogic.moveRight()
    }


    // Method to calculate the duration in a formatted string
    fun gameStats() : GameStats {
        return gameLogic.stats
    }

}
class GameStateManagerFactory(
    private val gameMode: Int,
    private val context: Context // Accept the context here
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GameStateManager::class.java)) {
            return GameStateManager(gameMode, context) as T // Pass the context here
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
package com.example.tetrisai.model

import android.content.Context
import android.os.Handler
import android.os.Looper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.random.Random

class GameLogic(val context: Context) {

    lateinit var grid: GridRepresentation
    var currentTetrimino: TetrisBlock? = null
    var currentX: Int = 0
    var currentY: Int = 0
    var currentRotation: Int = 0
    var linesCleared: Int = 1
    var lockedPiece: Boolean = false
    var gameMode: Int = 0
    private var handler = Handler(Looper.getMainLooper())
    private var lastDropTime = System.currentTimeMillis()
    private var dropInterval = 1000L
    private val updateInterval = 1000L / 60
    var levelSpeed: Int = 1
    private val _currentGridState = MutableStateFlow<Array<Array<Cell>>>(emptyArray())
    val currentGridState: StateFlow<Array<Array<Cell>>> = _currentGridState.asStateFlow()
    private val _score = MutableStateFlow(0)
    val score: StateFlow<Int> = _score
    private val _level = MutableStateFlow(1)
    var onGameOver: ((Boolean) -> Unit)? = null
    var gameOverState = MutableStateFlow(false)
    val level: StateFlow<Int> = _level
    private lateinit var ai: TetrisAI
    private lateinit var machineAi: TetrisMachineAi

    var onLineUpdated: ((Int) -> Unit)? = null
    var onScoreUpdated: ((Int) -> Unit)? = null
    var onLevelUpdated: ((Int) -> Unit)? = null

    val stats = GameStats()

    var totalLinesCleared: Int = 0
        private set

    var previousLinesCleared: Int = 0
    init {
        this.grid = GridRepresentation(10, 20)
    }

    fun initializeGame(gameMode: Int) {
        println("Initializing game with mode $gameMode")
        val startTime = System.currentTimeMillis()

        this.gameMode = gameMode
        grid.clear()
        _currentGridState.value = grid.getGridForUI(currentTetrimino, currentX, currentY, currentRotation)
        spawnTetrimino()
       // stopAllAI()

        if (gameMode == 1) {
            ai = TetrisAI(this)
            val (rotation, xPosition) = ai.chooseMove()
            applyAIMove(rotation, xPosition)
        }

        if (gameMode == 2) {
            machineAi = TetrisMachineAi(this, context)
            machineAi.stopAi()
            machineAi.startAi()
        }

        startGameLoop()

        val endTime = System.currentTimeMillis()
        println("Game initialized in ${endTime - startTime} ms")
    }

    private fun stopAllAI() {
        println("Stopping all AI")
        machineAi.stopAi()
    }

    private fun startGameLoop() {
        println("Starting game loop")
        handler.postDelayed(object : Runnable {
            override fun run() {
                if (!gameOverState.value) {
                    val startTickTime = System.currentTimeMillis()
                    gameTick()
                    val endTickTime = System.currentTimeMillis()
                    println("Tick duration: ${endTickTime - startTickTime} ms")
                    handler.postDelayed(this, updateInterval)
                    val scheduledTime = System.currentTimeMillis()
                    println("Next tick scheduled in ${updateInterval} ms at ${scheduledTime + updateInterval}")
                }
            }
        }, updateInterval)
    }

    private fun getRandomTetrimino(): TetrisBlock {
        println("Getting random Tetrimino")
        val tetriminos = TetrisBlock.entries.filter { it != TetrisBlock.EMPTY }
        return tetriminos[Random.nextInt(tetriminos.size)]
    }

    private fun spawnTetrimino() {
        println("Spawning Tetrimino")
        val startTime = System.currentTimeMillis()

        lockedPiece = false
        currentTetrimino = getRandomTetrimino()
        currentRotation = 0
        currentX = 4
        currentY = -currentTetrimino!!.getShape(currentRotation).size

        val endTime = System.currentTimeMillis()
        println("Tetrimino spawned in ${endTime - startTime} ms")
    }

    fun gameTick() {
        println("Game tick")
        val startTime = System.currentTimeMillis()

        val currentTime = System.currentTimeMillis()
        val gridStartTime = System.currentTimeMillis()
        _currentGridState.value = grid.getGridForUI(currentTetrimino, currentX, currentY, currentRotation)
        val gridEndTime = System.currentTimeMillis()
        println("Updating grid UI took ${gridEndTime - gridStartTime} ms")

        if (currentTime - lastDropTime >= dropInterval) {
            val dropStartTime = System.currentTimeMillis()
            if (!tryMoveCurrentTetriminoDown()) {
                val lockStartTime = System.currentTimeMillis()
                lockTetriminoInPlace()
                val lockEndTime = System.currentTimeMillis()
                println("Locking Tetrimino took ${lockEndTime - lockStartTime} ms")

                val lineClearStartTime = System.currentTimeMillis()
                lineClearing()
                val lineClearEndTime = System.currentTimeMillis()
                println("Line clearing took ${lineClearEndTime - lineClearStartTime} ms")

                val spawnStartTime = System.currentTimeMillis()
                spawnTetrimino()
                val spawnEndTime = System.currentTimeMillis()
                println("Spawning Tetrimino took ${spawnEndTime - spawnStartTime} ms")

                if (gameMode == 1) {
                    val aiMoveStartTime = System.currentTimeMillis()
                    val (rotation, xPosition) = ai.chooseMove()
                    applyAIMove(rotation, xPosition)
                    val aiMoveEndTime = System.currentTimeMillis()
                    println("AI move application took ${aiMoveEndTime - aiMoveStartTime} ms")
                }

                currentTetrimino?.let { tetrimino ->
                    println("Checking game over for Tetrimino at position ($currentX, $currentY)")
                    println("Tetrimino shape: ${tetrimino.getShape().contentDeepToString()}")

                    val gameOverCheckStartTime = System.currentTimeMillis()
                    if (grid.checkGameOver()) {
                        gameOverState.value = true
                        println("Game over triggered.")
                        endGame()
                    } else {
                        println("Game continues.")
                    }
                    val gameOverCheckEndTime = System.currentTimeMillis()
                    println("Game over check took ${gameOverCheckEndTime - gameOverCheckStartTime} ms")
                }
            }
            val dropEndTime = System.currentTimeMillis()
            println("Drop attempt took ${dropEndTime - dropStartTime} ms")
            lastDropTime = currentTime
        }

        val endTime = System.currentTimeMillis()
        println("Game tick completed in ${endTime - startTime} ms")
    }

    private fun applyAIMove(desiredRotation: Int, desiredXPosition: Int) {
        println("Applying AI move to rotation $desiredRotation and X position $desiredXPosition")
        val startTime = System.currentTimeMillis()

        while (currentRotation != desiredRotation) {
            rotate()
            if (currentRotation == desiredRotation) break
        }

        if (currentX < desiredXPosition) {
            for (i in currentX until desiredXPosition) {
                moveRight()
            }
        } else if (currentX > desiredXPosition) {
            for (i in desiredXPosition until currentX) {
                moveLeft()
            }
        }

        drop()

        val endTime = System.currentTimeMillis()
        println("AI move applied in ${endTime - startTime} ms")
    }

    fun lockTetriminoInPlace() {
        println("Locking Tetrimino in place")
        val startTime = System.currentTimeMillis()

        val shape = currentTetrimino?.getShape(currentRotation)
        if (shape != null) {
            lockedPiece = true
            for (y in shape.indices) {
                for (x in shape[0].indices) {
                    if (shape[y][x]) {
                        grid.placeBlock(currentX + x, currentY + y)
                    }
                }
            }
            if (gameMode == 2) {
                machineAi.onPieceLocked()
            }
        }

        val endTime = System.currentTimeMillis()
        println("Tetrimino locked in ${endTime - startTime} ms")
    }

    fun tryMoveCurrentTetriminoDown(): Boolean {
        println("Trying to move current Tetrimino down")
        val startTime = System.currentTimeMillis()

        val result = if (isValidMove(currentX, currentY + 1, currentRotation)) {
            currentY += 1
            true
        } else {
            false
        }

        val endTime = System.currentTimeMillis()
        println("Move down attempt completed in ${endTime - startTime} ms")
        return result
    }
    fun moveLeft() {
        println("Moving left")
        val startTime = System.currentTimeMillis()

        if (isValidMove(currentX - 1, currentY, currentRotation)) {
            currentX -= 1
            _currentGridState.value = grid.getGridForUI(currentTetrimino, currentX, currentY, currentRotation)
        } else {
            println("Move left blocked")
        }

        val endTime = System.currentTimeMillis()
        println("Move left completed in ${endTime - startTime} ms")
    }

    fun moveRight() {
        println("Moving right")
        val startTime = System.currentTimeMillis()

        if (isValidMove(currentX + 1, currentY, currentRotation)) {
            currentX += 1
            _currentGridState.value = grid.getGridForUI(currentTetrimino, currentX, currentY, currentRotation)
        } else {
            println("Move right blocked")
        }

        val endTime = System.currentTimeMillis()
        println("Move right completed in ${endTime - startTime} ms")
    }
    fun movePieceToX(xPosition: Int) {
        println("Moving Tetrimino to X position $xPosition")
        val startTime = System.currentTimeMillis()

        if (isValidMove(xPosition, currentY, currentRotation)) {
            currentX = xPosition
            _currentGridState.value = grid.getGridForUI(currentTetrimino, currentX, currentY, currentRotation)
        }

        val endTime = System.currentTimeMillis()
        println("Move to X position completed in ${endTime - startTime} ms")
    }

    fun rotatePieceTo(rotation: Int) {
        println("Rotating Tetrimino to rotation $rotation")
        val startTime = System.currentTimeMillis()

        val newRotation = (rotation) % currentTetrimino!!.shapes.size
        if (isValidMove(currentX, currentY, newRotation)) {
            currentRotation = newRotation
            _currentGridState.value = grid.getGridForUI(currentTetrimino, currentX, currentY, currentRotation)
        }

        val endTime = System.currentTimeMillis()
        println("Rotate to completed in ${endTime - startTime} ms")
    }

    fun rotate() {
        println("Rotating Tetrimino")
        val startTime = System.currentTimeMillis()

        val newRotation = (currentRotation + 1) % currentTetrimino!!.shapes.size
        if (isValidMove(currentX, currentY, newRotation)) {
            currentRotation = newRotation
            _currentGridState.value = grid.getGridForUI(currentTetrimino, currentX, currentY, currentRotation)
        }

        val endTime = System.currentTimeMillis()
        println("Rotation completed in ${endTime - startTime} ms")
    }

    fun drop() {
        println("Dropping Tetrimino")
        val startTime = System.currentTimeMillis()

        while (isValidMove(currentX, currentY + 1, currentRotation)) {
            currentY += 1
            _currentGridState.value = grid.getGridForUI(currentTetrimino, currentX, currentY, currentRotation)
        }

        val endTime = System.currentTimeMillis()
        println("Drop completed in ${endTime - startTime} ms")
    }

    fun isValidMove(newX: Int, newY: Int, newRotation: Int): Boolean {
        val startTime = System.currentTimeMillis()
        val shape = currentTetrimino?.getShape(newRotation)
        if (shape != null) {
            for (y in shape.indices) {
                for (x in shape[0].indices) {
                    if (shape[y][x]) {
                        val gridX = newX + x
                        val gridY = newY + y

                        if (gridX < 0 || gridX >= grid.width || gridY >= grid.height) {
                            val endTime = System.currentTimeMillis()
                            println("isValidMove out of bounds check took ${endTime - startTime} ms")
                            return false
                        }

                        if (gridY >= 0 && grid.isOccupied(gridX, gridY)) {
                            val endTime = System.currentTimeMillis()
                            println("isValidMove collision check took ${endTime - startTime} ms")
                            return false
                        }
                    }
                }
            }
        }
        val endTime = System.currentTimeMillis()
        println("isValidMove completed in ${endTime - startTime} ms")
        return true
    }

    private fun lineClearing() {
        println("Clearing lines")
        val startTime = System.currentTimeMillis()

        val linesClearedNow: Int = grid.clearLines()
        linesCleared += linesClearedNow

        val basePoints = when (linesClearedNow) {
            1 -> 40
            2 -> 100
            3 -> 300
            4 -> 1200
            else -> 0
        }

        when (linesClearedNow) {
            1 -> stats.incrementSingleLines()
            2 -> stats.incrementDoubleLines()
            3 -> stats.incrementTripleLines()
            4 -> stats.incrementTetrisLines()
        }

        _score.value += basePoints * _level.value

        if (linesCleared % 10 == 0 && linesClearedNow > 0) {
            _level.value++
            dropInterval = (dropInterval / 1.5).toLong()
        }

        onLineUpdated?.invoke(linesCleared)
        onScoreUpdated?.invoke(_score.value)
        onLevelUpdated?.invoke(_level.value)

        previousLinesCleared = linesCleared

        val endTime = System.currentTimeMillis()
        println("Lines cleared in ${endTime - startTime} ms")
    }

    fun linesClearedSinceLast(): Int {
        val clearedSinceLast = linesCleared - previousLinesCleared
        previousLinesCleared = linesCleared
        return clearedSinceLast
    }

    fun endGame() {
        println("Ending game")
        val startTime = System.currentTimeMillis()

        //stopAllAI()
        if (gameMode == 2) {
            machineAi.saveQTable()
            machineAi.stopAi()
            println("Machine AI game over. Restarting...")
            machineAi.trackPerformance()
            resetGame()
            initializeGame(2)
        } else {
            gameOverState.value = true
            onGameOver?.invoke(true)
        }

        val endTime = System.currentTimeMillis()
        println("Game ended in ${endTime - startTime} ms")
    }

    fun resetGame() {
        println("Resetting game")
        val startTime = System.currentTimeMillis()

        grid = GridRepresentation(10, 20)
        currentTetrimino = null
        currentX = 0
        currentY = 0
        currentRotation = 0
        linesCleared = 1
        lockedPiece = false
        _currentGridState.value = grid.getGridForUI(currentTetrimino, currentX, currentY, currentRotation)
        _score.value = 0
        _level.value = 1
        totalLinesCleared = 0
        previousLinesCleared = 0
        gameOverState.value = false

        onLineUpdated?.invoke(linesCleared)
        onScoreUpdated?.invoke(_score.value)
        onLevelUpdated?.invoke(_level.value)

        val endTime = System.currentTimeMillis()
        println("Game reset in ${endTime - startTime} ms")
    }

    fun gridToString(): String {
        val sb = StringBuilder()
        for (row in grid.grid) {
            for (cell in row) {
                sb.append(if (cell == Cell.EMPTY) '0' else '1')
            }
            sb.append('|')
        }
        return sb.toString()
    }
}
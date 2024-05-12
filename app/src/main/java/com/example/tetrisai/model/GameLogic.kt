package com.example.tetrisai.model

import android.os.Handler
import android.os.Looper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.random.Random

class GameLogic {

    lateinit var grid: GridRepresentation
    var currentTetrimino: TetrisBlock? = null
    var currentX: Int = 0
    var currentY: Int = 0
    var currentRotation: Int = 0
    var linesCleared: Int = 1
    var lockedPiece: Boolean = false
    private var gameMode: Int = 0;
    private var handler = Handler(Looper.getMainLooper())
    private var lastDropTime = System.currentTimeMillis()
    private var dropInterval = 1000L
    private val updateInterval = 1000L/60
    //var gameOver: Boolean = false
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

    private var previousLinesCleared: Int = 0

    constructor() {
        this.grid = GridRepresentation(10,20)
        ai = TetrisAI(this)
        machineAi = TetrisMachineAi(this)
        //initializeGame()
    }

    fun initializeGame(gameMode: Int) {
        this.gameMode = gameMode
        // Reset or initialize other game state variables
        grid.clear() // Assuming you have a method to reset the grid
        _currentGridState.value = grid.getGridForUI(currentTetrimino,currentX,currentY,currentRotation)
        spawnTetrimino()
        when (gameMode) {
            1 -> {
                // Heuristic AI Mode
                val (rotation, xPosition) = ai.chooseMove()
                applyAIMove(rotation, xPosition)
            }
            2 -> {
                // Machine Learning AI Mode
                val previousState = machineAi.getState()  // Capture the current state before applying action
                val possibleActions = machineAi.getPossibleActions()

                // Choose an action based on exploration/exploitation
                val selectedAction = machineAi.selectAction(previousState, possibleActions)

                // Apply the selected action
                machineAi.applyAction(selectedAction)

                // Capture the new state and calculate the reward
                val newState = machineAi.getState()
                val reward = machineAi.calculateReward()

                // Update the Q-table based on the action's outcome
                machineAi.updateQTable(previousState, selectedAction, reward, newState)
            }
        }
        startGameLoop()
        // Any other game initialization logic
    }

    private fun startGameLoop() {
        handler.postDelayed(object : Runnable {
            override fun run() {
                if (!gameOverState.value) {
                    gameTick()
                    handler.postDelayed(this, updateInterval)
                }
            }
        }, updateInterval)
    }

    private fun getRandomTetrimino(): TetrisBlock {
        val tetriminos = TetrisBlock.entries.filter { it != TetrisBlock.EMPTY }
        return tetriminos[Random.nextInt(tetriminos.size)]
    }

    private fun spawnTetrimino() {
        lockedPiece = false
        currentTetrimino = getRandomTetrimino()
        currentRotation = 0 // Start with default orientation
        currentX = 4 // Center the Tetrimino horizontally; adjust as needed
        currentY = -currentTetrimino!!.getShape(currentRotation).size // Start just above the visible grid
    }

    fun gameTick() {
        val currentTime = System.currentTimeMillis()
        _currentGridState.value = grid.getGridForUI(currentTetrimino,currentX,currentY,currentRotation)
        if (currentTime - lastDropTime >= dropInterval) {
            if (!tryMoveCurrentTetriminoDown()) {
                lockTetriminoInPlace()
                lineClearing()
                spawnTetrimino()

                when (gameMode) {
                    1 -> {
                        // Heuristic AI Mode
                        val (rotation, xPosition) = ai.chooseMove()
                        applyAIMove(rotation, xPosition)
                    }
                    2 -> {
                        // Machine Learning AI Mode
                        val previousState = machineAi.getState()  // Capture the current state before applying action
                        val possibleActions = machineAi.getPossibleActions()

                        // Choose an action based on exploration/exploitation
                        val selectedAction = machineAi.selectAction(previousState, possibleActions)

                        // Apply the selected action
                        machineAi.applyAction(selectedAction)

                        // Capture the new state and calculate the reward
                        val newState = machineAi.getState()
                        val reward = machineAi.calculateReward()

                        // Update the Q-table based on the action's outcome
                        machineAi.updateQTable(previousState, selectedAction, reward, newState)
                    }
                }
                currentTetrimino?.let { tetrimino ->
                    println("Checking game over for Tetrimino at position ($currentX, $currentY)")
                    println("Tetrimino shape: ${tetrimino.getShape().contentDeepToString()}")

                    if (grid.checkGameOver()) {
                        gameOverState.value = true
                        println("Game over triggered.")
                        endGame()
                    } else {
                        println("Game continues.")
                    }
                }

            }
            lastDropTime = currentTime
        }
    }

    private fun applyAIMove(desiredRotation: Int, desiredXPosition: Int) {
        System.out.println("X Position: " + desiredXPosition)
        // Adjust the rotation to match the AI's decision
        // This loop accounts for the possibility that the Tetrimino needs to be rotated multiple times.
        while (currentRotation != desiredRotation) {
            rotate() // Utilizes your existing rotate method
            // Note: This assumes that your rotation can cycle through all states back to the original.
        }

        // Move the Tetrimino horizontally to match the AI's decision
        // Depending on the current and desired X positions, move left or right as needed.
        while (currentX < desiredXPosition) {
            moveRight() // Utilizes your existing moveRight method
        }
        while (currentX > desiredXPosition) {
            moveLeft() // Utilizes your existing moveLeft method
        }

        // Optionally, drop the Tetrimino immediately after positioning it
        // This step depends on your game's design and whether such immediate drops are desirable.
        drop() // Utilizes your existing drop method
    }

    fun lockTetriminoInPlace() {
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
        }
    }

    fun tryMoveCurrentTetriminoDown(): Boolean {
        if (isValidMove(currentX, currentY + 1, currentRotation)) {
            currentY += 1
            return true
        }
        return false
    }

    fun moveLeft() {
        if (isValidMove(currentX - 1, currentY, currentRotation)) {
            currentX -= 1
            _currentGridState.value = grid.getGridForUI(currentTetrimino,currentX,currentY,currentRotation)
        }
    }

    fun moveRight() {
        if (isValidMove(currentX + 1, currentY, currentRotation)) {
            currentX += 1
            _currentGridState.value = grid.getGridForUI(currentTetrimino,currentX,currentY,currentRotation)
        }
    }

    fun movePieceToX(xPosition: Int) {
        if (isValidMove(xPosition, currentY, currentRotation)) {
            currentX = xPosition
            _currentGridState.value = grid.getGridForUI(currentTetrimino,currentX,currentY,currentRotation)
        }
    }

    fun rotatePieceTo(rotation: Int) {
        val newRotation = (rotation) % currentTetrimino!!.shapes.size // Assuming 4 rotations
        if (isValidMove(currentX, currentY, newRotation)) {
            currentRotation = newRotation
            _currentGridState.value = grid.getGridForUI(currentTetrimino,currentX,currentY,currentRotation)
        }
    }

    fun rotate() {
        val newRotation = (currentRotation + 1) % currentTetrimino!!.shapes.size // Assuming 4 rotations
        if (isValidMove(currentX, currentY, newRotation)) {
            currentRotation = newRotation
            _currentGridState.value = grid.getGridForUI(currentTetrimino,currentX,currentY,currentRotation)
        }
    }

    fun drop() {
        while (isValidMove(currentX, currentY + 1, currentRotation)) {
            currentY += 1
            _currentGridState.value = grid.getGridForUI(currentTetrimino,currentX,currentY,currentRotation)
        }
    }

    fun isValidMove(newX: Int, newY: Int, newRotation: Int): Boolean {
        val shape = currentTetrimino?.getShape(newRotation)
        if (shape != null) {
            for (y in shape.indices) {
                for (x in shape[0].indices) {
                    if (shape[y][x]) {
                        val gridX = newX + x
                        val gridY = newY + y

                        // Check boundary conditions
                        if (gridX < 0 || gridX >= grid.width || gridY >= grid.height) {
                            return false
                        }

                        // Check for collision with placed blocks
                        if (gridY >= 0 && grid.isOccupied(gridX, gridY)) {
                            return false
                        }
                    }
                }
            }
        }
        return true
    }

    private fun lineClearing() {
        // Clear lines and get the number cleared
        val linesClearedNow: Int = grid.clearLines()
        linesCleared += linesClearedNow

        // Calculate points based on lines cleared at once
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

        // Update level based on the total lines cleared
        if (linesCleared % 10 == 0 && linesClearedNow > 0) {
            _level.value++
            dropInterval = (dropInterval / 1.5).toLong()
        }

        // Notify listeners about updates
        onLineUpdated?.invoke(linesCleared)
        onScoreUpdated?.invoke(_score.value)
        onLevelUpdated?.invoke(_level.value)

        // Store how many lines were cleared since the last check
        previousLinesCleared = totalLinesCleared
    }

    fun linesClearedSinceLast(): Int {
        val clearedSinceLast = totalLinesCleared - previousLinesCleared
        previousLinesCleared = totalLinesCleared

        return clearedSinceLast
    }

    fun endGame() {
        gameOverState.value = true
        onGameOver?.invoke(true) // Notify observers that the game is over
       // initializeGame(gameMode = gameMode)
    }

}
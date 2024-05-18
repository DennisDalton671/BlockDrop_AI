package com.example.tetrisai.model

import android.content.Context
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

@Serializable
data class QTableEntry(val state: String, val action: String, val value: Double)

class TetrisMachineAi(private val gameLogic: GameLogic, private val context: Context) {

    private val explorationRate: Double = 1.0  // Exploration rate (epsilon) for epsilon-greedy policy
    private val minExplorationRate: Double = 0.01  // Minimum exploration rate
    private val explorationDecayRate: Double = 0.995  // Exploration decay rate
    private val learningRate: Double = 0.1     // Learning rate (alpha)
    private val discountFactor: Double = 0.99   // Discount factor (gamma) for future rewards
    private val qTable = mutableMapOf<String, MutableMap<String, Double>>()

    private var currentExplorationRate = explorationRate
    private var episodeCount = 0
    private val random = java.util.Random()

    private var isRunning = false
    private var aiThread: Thread? = null

    // Performance tracking variables
    private var totalScore = 0.0
    private var totalLinesCleared = 0
    private var episodeScores = mutableListOf<Double>()
    private var episodeLinesCleared = mutableListOf<Int>()

    // Define the Q-table file
    private val qTableFile: File = File(context.filesDir, "qtable.json")

    init {
        // Initialize the Q-table when the machine AI is instantiated
        loadQTable()
    }

    // Convert the current grid state to a string representation
    fun getState(): String {
        val gridState = gameLogic.gridToString()
        val currentPiece = gameLogic.currentTetrimino?.name ?: "NONE"
        val currentPosition = "${gameLogic.currentX}_${gameLogic.currentY}"
        return "$gridState|$currentPiece|$currentPosition"
    }

    // Get possible actions based on the current Tetrimino's rotation and position
    fun getPossibleActions(): List<String> {
        val currentTetrimino = gameLogic.currentTetrimino ?: return emptyList()
        val actions = mutableListOf<String>()

        for (rotation in 0 until currentTetrimino.getNumberOfRotations()) {
            for (xPosition in 0 until gameLogic.grid.width) {
                val action = "ROTATE_$rotation|MOVE_$xPosition"
                actions.add(action)
            }
        }
        actions.add("DROP")
        return actions
    }

    // Select an action based on the epsilon-greedy policy
    fun selectAction(state: String, possibleActions: List<String>): String {
        return if (random.nextDouble() < currentExplorationRate) {
            possibleActions[random.nextInt(possibleActions.size)]
        } else {
            val stateActions = qTable[state] ?: return possibleActions[random.nextInt(possibleActions.size)]
            stateActions.maxByOrNull { it.value }?.key ?: possibleActions[random.nextInt(possibleActions.size)]
        }
    }

    // Calculate the reward based on the grid state
    fun calculateReward(): Double {
        val linesClearedReward = 10.0
        val holePenalty = -5.0
        val gameOverPenalty = -100.0

        val linesCleared = gameLogic.linesClearedSinceLast()
        val holes = gameLogic.grid.countHoles()

        return if (gameLogic.gameOverState.value) {
            gameOverPenalty
        } else {
            linesCleared * linesClearedReward + holes * holePenalty
        }
    }

    // Update the Q-table after an action is taken
    fun updateQTable(state: String, action: String, reward: Double, newState: String) {
        val stateMap = qTable.getOrPut(state) { mutableMapOf() }
        val currentQValue = stateMap.getOrDefault(action, 0.0)

        val maxFutureQValue = qTable.getOrDefault(newState, emptyMap()).values.maxOrNull() ?: 0.0
        val newQValue = (1 - learningRate) * currentQValue + learningRate * (reward + discountFactor * maxFutureQValue)
        stateMap[action] = newQValue
    }

    // Choose the best move based on the current state and Q-table
    fun chooseMove(): Pair<Int, Int> {
        val state = getState()
        val possibleActions = getPossibleActions()
        val selectedAction = selectAction(state, possibleActions)
        return parseAction(selectedAction)
    }

    private fun parseAction(action: String): Pair<Int, Int> {
        val parts = action.split("|")
        var rotation = 0
        var xPosition = 0
        parts.forEach { part ->
            when {
                part.startsWith("ROTATE") -> {
                    rotation = part.substringAfter("_").toInt()
                }
                part.startsWith("MOVE") -> {
                    xPosition = part.substringAfter("_").toInt()
                }
            }
        }
        return Pair(rotation, xPosition)
    }

    // Start the AI
    fun startAi() {
        isRunning = true
        aiThread = Thread {
            while (isRunning) {
                if (!gameLogic.gameOverState.value) {
                    val previousState = getState()
                    val possibleActions = getPossibleActions()
                    val selectedAction = selectAction(previousState, possibleActions)
                    applyAction(selectedAction)
                    val newState = getState()
                    val reward = calculateReward()
                    updateQTable(previousState, selectedAction, reward, newState)

                    // Add delay for AI actions
                    Thread.sleep(100)  // Adjust the delay as needed
                }
            }
        }
        aiThread?.start()
    }

    // Stop the AI
    fun stopAi() {
        isRunning = false
        aiThread?.join()
    }

    // Apply the chosen action to the game
    fun applyAction(action: String) {
        val parts = action.split("|")
        parts.forEach { part ->
            when {
                part.startsWith("ROTATE") -> {
                    val rotation = part.substringAfter("_").toInt()
                    // Ensure each rotation step is valid
                    while (gameLogic.currentRotation != rotation) {
                        if (gameLogic.isValidMove(gameLogic.currentX, gameLogic.currentY, (gameLogic.currentRotation + 1) % gameLogic.currentTetrimino!!.shapes.size)) {
                            gameLogic.rotate()
                        } else {
                            break
                        }
                    }
                }
                part.startsWith("MOVE") -> {
                    val targetX = part.substringAfter("_").toInt()
                    // Move step-by-step to the target position
                    while (gameLogic.currentX < targetX && gameLogic.isValidMove(gameLogic.currentX + 1, gameLogic.currentY, gameLogic.currentRotation)) {
                        gameLogic.moveRight()
                    }
                    while (gameLogic.currentX > targetX && gameLogic.isValidMove(gameLogic.currentX - 1, gameLogic.currentY, gameLogic.currentRotation)) {
                        gameLogic.moveLeft()
                    }
                }
                part == "DROP" -> {
                    gameLogic.drop()
                }
            }
        }
    }

    fun onPieceLocked() {
        val previousState = getState()
        val reward = calculateReward()
        val newState = getState()

        updateQTable(previousState, "LOCK", reward, newState)

        if (currentExplorationRate > minExplorationRate) {
            currentExplorationRate *= explorationDecayRate
        }

        // Track performance
        totalScore += reward
        if (gameLogic.linesClearedSinceLast() > 0) {
            totalLinesCleared += gameLogic.linesClearedSinceLast()
        }

        // Save the Q-table after each piece lock
        saveQTable()
    }

    // Track and print performance metrics
    fun trackPerformance() {
        episodeCount++
        episodeScores.add(totalScore)
        episodeLinesCleared.add(totalLinesCleared)

        if (episodeCount % 10 == 0) { // Adjust frequency as needed
            val averageScore = episodeScores.average()
            val averageLinesCleared = episodeLinesCleared.average()
            println("Episode: $episodeCount, Average Score: $averageScore, Average Lines Cleared: $averageLinesCleared")
        }

        // Reset for next episode
        totalScore = 0.0
        totalLinesCleared = 0
    }

    // Save the Q-table to a file
    fun saveQTable() {
        val qTableEntries = qTable.flatMap { (state, actions) ->
            actions.map { (action, value) ->
                QTableEntry(state, action, value)
            }
        }
        val json = Json.encodeToString(qTableEntries)
        qTableFile.writeText(json)
    }

    // Load the Q-table from a file
    fun loadQTable() {
        if (qTableFile.exists()) {
            val json = qTableFile.readText()
            val qTableEntries = Json.decodeFromString<List<QTableEntry>>(json)
            qTableEntries.forEach { entry ->
                val stateMap = qTable.getOrPut(entry.state) { mutableMapOf() }
                stateMap[entry.action] = entry.value
            }
        }
    }

    private fun initializeQTable() {
        // Load the Q-table when the machine AI is instantiated
        loadQTable()
    }
}
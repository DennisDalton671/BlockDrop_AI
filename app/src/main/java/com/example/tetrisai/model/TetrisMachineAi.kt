package com.example.tetrisai.model

class TetrisMachineAi(val gameLogic: GameLogic) {

    private val explorationRate: Double = 1.0  // Exploration rate (epsilon) for epsilon-greedy policy
    private val learningRate: Double = 0.1     // Learning rate (alpha)
    private val discountFactor: Double = 0.99   // Discount factor (gamma) for future rewards
    // Q-Table or Model
    private val qTable = mutableMapOf<String, MutableMap<String, Double>>()

    // Track current exploration rate
    private var currentExplorationRate = explorationRate

    // Episode counter
    private var episodeCount = 0

    // Initialize a random seed (if needed)
    private val random = java.util.Random()

    init {
        // Initialize the Q-table or model structure if needed
        initializeQTable()
    }

    fun getState(): String {
        // Extract the current grid state and Tetrimino position, representing it as a string or another identifier
        val gridState = gameLogic.grid
        val currentPiece = gameLogic.currentTetrimino?.name ?: "NONE"
        val currentPosition = "${gameLogic.currentX}_${gameLogic.currentY}"

        // Combine grid state with current piece and position
        return "$gridState|$currentPiece|$currentPosition"
    }

    // Get possible actions based on the current Tetrimino's rotation and position
    fun getPossibleActions(): List<String> {
        val currentTetrimino = gameLogic.currentTetrimino ?: return emptyList()
        val actions = mutableListOf<String>()

        // List all possible rotations and positions the Tetrimino can be moved to
        for (rotation in 0 until currentTetrimino.getNumberOfRotations()) {
            for (xPosition in 0 until gameLogic.grid.width) {
                val action = "ROTATE_$rotation|MOVE_$xPosition"
                actions.add(action)
            }
        }

        return actions
    }

    fun selectAction(state: String, possibleActions: List<String>): String {
        // Use a random number to decide between exploration and exploitation
        return if (random.nextDouble() < currentExplorationRate) {
            // Exploration: Randomly pick an action
            possibleActions[random.nextInt(possibleActions.size)]
        } else {
            // Exploitation: Choose the best action based on the Q-Table
            val stateActions = qTable[state] ?: return possibleActions[random.nextInt(possibleActions.size)]
            stateActions.maxByOrNull { it.value }?.key ?: possibleActions[random.nextInt(possibleActions.size)]
        }
    }

    fun calculateReward(): Double {
        // Define some basic reward/penalty metrics
        val linesClearedReward = 10.0  // Example reward for each line cleared
        val holePenalty = -5.0         // Penalty per hole created
        val gameOverPenalty = -100.0   // High penalty for losing

        // Compute the number of lines cleared since the last action
        val linesCleared = gameLogic.linesClearedSinceLast()

        // Count the number of holes in the current grid state
        val holes = gameLogic.grid.countHoles()

        // If the game is over, apply a penalty
        if (gameLogic.gameOverState.value) {
            return gameOverPenalty
        }

        // Calculate the final reward using your own formula
        return linesCleared * linesClearedReward + holes * holePenalty
    }

    // A simple example of updating the Q-table after an action is taken
    fun updateQTable(state: String, action: String, reward: Double, newState: String) {
        // Initialize the state-action pair in the Q-table if it doesn't exist
        val stateMap = qTable.getOrPut(state) { mutableMapOf() }
        val currentQValue = stateMap.getOrDefault(action, 0.0)

        // Update Q-value using the Q-learning formula
        val maxFutureQValue = qTable.getOrDefault(newState, emptyMap()).values.maxOrNull() ?: 0.0
        val newQValue = (1 - learningRate) * currentQValue + learningRate * (reward + discountFactor * maxFutureQValue)
        stateMap[action] = newQValue
    }

    // Execute the chosen action on the game
    fun applyAction(action: String) {
        val parts = action.split("|")
        parts.forEach { part ->
            when {
                part.startsWith("ROTATE") -> {
                    val rotation = part.substringAfter("_").toInt()
                    // Apply rotation in the game logic
                    rotatePieceGraduallyTo(rotation)
                }
                part.startsWith("MOVE") -> {
                    val xPosition = part.substringAfter("_").toInt()
                    // Move to the given x position in the game logic
                    movePieceGraduallyToX(xPosition)
                }
            }
        }
    }

    fun movePieceGraduallyToX(targetX: Int) {
        val currentX = gameLogic.currentX

        // Determine the direction to move
        val direction = if (targetX > currentX) 1 else -1

        // Move one step at a time in the target direction
        while (gameLogic.currentX != targetX) {
            if (direction == 1) {
                gameLogic.moveRight()
            } else {
                gameLogic.moveLeft()
            }

            // If the move is invalid or hits a boundary, stop
            if (gameLogic.currentX == currentX) break
        }
    }

    fun rotatePieceGraduallyTo(targetRotation: Int) {
        val currentRotation = gameLogic.currentRotation

        // Rotate step-by-step in one direction until the target rotation is reached
        while (currentRotation != targetRotation) {
            // Rotate using the existing method from game logic
            gameLogic.rotate()

            // Update currentRotation to reflect the new state after rotation
            val newRotation = (currentRotation + 1) % 4

            // Check if the current rotation matches the target
            if (newRotation == targetRotation) break
        }
    }

    // Function to initialize Q-table or model
    private fun initializeQTable() {
        // Populate with initial Q-values if applicable
    }
}
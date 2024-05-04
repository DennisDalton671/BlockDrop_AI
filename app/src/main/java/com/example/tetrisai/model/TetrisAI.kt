package com.example.tetrisai.model

import kotlin.math.abs
import kotlin.math.min

class TetrisAI(val gameLogic: GameLogic) {



    /*fun chooseMove(): Pair<Int, Int> { // Returns rotation and position
        var bestScore = Double.NEGATIVE_INFINITY
        var bestMove: Pair<Int, Int> = Pair(0, 0)

        val currentTetrimino = gameLogic.currentTetrimino ?: return bestMove

        for (rotation in 0 until currentTetrimino.getNumberOfRotations()) {
            for (xPosition in 0 until gameLogic.grid.width) {

                if (isLegalMove(currentTetrimino, rotation, xPosition,gameLogic.grid.width)) {
                // Hypothetically place Tetrimino

                    val hypotheticalBoardBefore = gameLogic.grid.clone()

                    val hypotheticalBoardAfter = gameLogic.grid.placeTetriminoHypothetically(
                    currentTetrimino,
                    rotation,
                    xPosition,
                    gameLogic.grid
                    )

                System.out.println("Block: ${currentTetrimino.getBlockName()}\nxPosition: $xPosition\nRotation: $rotation\n--------------------")

                // Calculate score based on heuristics
                val score = evaluateBoard(hypotheticalBoardBefore, hypotheticalBoardAfter, xPosition)

                System.out.println("Score: $score")

                if (score > bestScore) {
                    bestScore = score
                    bestMove = Pair(rotation, xPosition)
                }
            }
        }
    }
        System.out.println("Best Move: $bestMove\nBest Score: $bestScore\n--------------------")
        return bestMove
    }*/

    fun chooseMove(): Pair<Int, Int> { // Returns rotation and position
        var bestScore = Double.NEGATIVE_INFINITY
        var bestMove: Pair<Int, Int> = Pair(0, 0)

        val currentTetrimino = gameLogic.currentTetrimino ?: return bestMove

        for (rotation in 0 until currentTetrimino.getNumberOfRotations()) {
            // Trim the Tetrimino shape and get the offset
            val (trimmedShape, offset) = trimTetrimino(currentTetrimino, rotation)

            for (xPosition in 0 until gameLogic.grid.width) {

                if (isLegalMove(trimmedShape, xPosition, gameLogic.grid)) {

                    val hypotheticalBoardAfter = gameLogic.grid.placeTetriminoHypothetically(
                        trimmedShape,
                        xPosition,
                        gameLogic.grid
                    )

                    // Calculate score based on heuristics
                    val score = evaluateBoard(hypotheticalBoardAfter)

                    System.out.println("Score: $score\n")

                    if (score > bestScore) {
                        bestScore = score
                        bestMove = Pair(rotation, xPosition - offset)  // Add the offset to report the correct position
                    }
                }
            }
        }
        System.out.println("Piece: " + currentTetrimino)
        System.out.println("Best Move: $bestMove\nBest Score: $bestScore\n--------------------")
        return bestMove
    }

    fun trimTetrimino(tetrimino: TetrisBlock, rotation: Int): Pair<Array<BooleanArray>, Int> {
        val originalShape = tetrimino.getShape(rotation)
        var offset = originalShape[0].size  // Initialize with the maximum column index

        // Find the first column that has a filled cell
        for (x in originalShape[0].indices) {
            for (y in originalShape.indices) {
                if (originalShape[y][x]) {
                    offset = min(offset, x)
                    break
                }
            }
            if (offset != originalShape[0].size) break  // Stop if we've found a filled column
        }

        // If no filled columns are found, return the original shape
        if (offset == originalShape[0].size) {
            return Pair(originalShape, 0)
        }

        // Create a new shape trimmed of leading empty columns
        val trimmedShape = Array(originalShape.size) { y ->
            BooleanArray(originalShape[0].size - offset) { x ->
                originalShape[y][x + offset]
            }
        }

        // The offset is the number of columns removed
        return Pair(trimmedShape, offset)
    }

/*    fun isLegalMove(tetrimino: TetrisBlock, rotation: Int, xPosition: Int, width: Int): Boolean {
        // Get the Tetrimino's shape based on its current rotation
        val shape = tetrimino.getShape(rotation)

        // Calculate the Tetrimino's width based on its shape and rotation
        val tetriminoWidth = shape.maxOf { it.count { cell -> cell } }

        // Check if the Tetrimino, when placed at xPosition, would exceed the board's width
        if (xPosition < 0 || xPosition + tetriminoWidth > width) {
            return false // The move would place the Tetrimino out of bounds
        }

        // Optionally, check if the position is occupied (for more advanced logic)
        // This requires checking each cell of the Tetrimino against the game board's state
        for (y in shape.indices) {
            for (x in shape[y].indices) {
                if (shape[y][x]) { // If this part of the Tetrimino is filled...
                    // Calculate the Tetrimino's absolute position on the board
                    val boardX = xPosition + x
                    // Check if the position is within the board's bounds
                    if (boardX < 0 || boardX >= width) {
                        return false // The Tetrimino would be out of bounds
                    }
                    // Here you could also check if the cell is already occupied:
                    // if (board[y][boardX] == Cell.FILLED) { return false; }
                }
            }
        }

        return true // The move is within bounds and doesn't collide with existing Tetriminos
    }*/

    fun isLegalMove(shape: Array<BooleanArray>, xPosition: Int, grid: GridRepresentation): Boolean {
        val tetriminoWidth = shape.maxOf { row -> row.count { cell -> cell } }

        // Check if the Tetrimino, when placed at xPosition, would exceed the board's width
        if (xPosition < 0 || xPosition + tetriminoWidth > grid.width) {
            return false // The move would place the Tetrimino out of bounds
        }

        for (y in shape.indices) {
            for (x in shape[y].indices) {
                if (shape[y][x]) { // If this part of the Tetrimino is filled...
                    val boardX = xPosition + x
                    // Check if the position is within the board's bounds
                    if (boardX < 0 || boardX >= grid.width) {
                        return false // The Tetrimino would be out of bounds
                    }
                    if (grid.isOccupied(boardX, y)) { // Adjust y to start from the top or calculated drop position
                        return false // The Tetrimino part overlaps with a filled cell
                    }
                }
            }
        }

        return true // The move is within bounds and doesn't collide with existing Tetriminos
    }


    private fun evaluateBoard(gridAfter: GridRepresentation): Double {
        // Implement your heuristic evaluation here, for example:
        val aggregateHeightAfter = calculateAggregateHeight(gridAfter)
        val completeLines = calculateCompleteLines(gridAfter)
        val holes = calculateHoles(gridAfter)
        val bumpiness = calculateBumpiness(gridAfter)

        System.out.println("Aggregate Height After: $aggregateHeightAfter\nComplete Lines: $completeLines\nHoles: $holes\nBumpiness: $bumpiness\n--------------------")


        val aggregateHeightWeight = -15.0
        val completeLinesWeight = 100.0
        val holesWeight = -15.0
        val bumpinessWeight = -5.0

        return ((aggregateHeightAfter * aggregateHeightWeight) +
                (completeLines * completeLinesWeight) +
                (holes + holesWeight) +
                (bumpiness * bumpinessWeight))

    }

    private fun calculateAggregateHeight(grid: GridRepresentation): Double {
        // Example: Sum the height of the tallest block in each column
        var totalHeight = 0
        for (x in 0 until grid.width) {
            for (y in 0 until grid.height) {
                if (grid.getCell(x, y) == Cell.FILLED) {
                    totalHeight += (grid.height - y)
                    break // Stop at the first filled cell from the top
                }
            }
        }
        return totalHeight.toDouble()
    }

    private fun calculateCompleteLines(grid: GridRepresentation): Double {
        // Example: Count the number of complete lines
        var completeLines = 0
        for (y in 0 until grid.height) {
            if ((0 until grid.width).all { x -> grid.getCell(x, y) == Cell.FILLED }) {
                completeLines++
            }
        }
        return completeLines.toDouble()
    }

    private fun calculateHoles(grid: GridRepresentation): Double {
        // Example: Count all empty cells that have a filled cell above them in the column
        var holes = 0
        for (x in 0 until grid.width) {
            var blockFound = false
            for (y in 0 until grid.height) {
                if (grid.getCell(x, y) == Cell.FILLED) blockFound = true
                else if (blockFound && grid.getCell(x, y) == Cell.EMPTY) holes++
            }
        }
        return holes.toDouble()
    }

    private fun calculateBumpiness(grid: GridRepresentation): Double {
        // Example: Calculate the total difference in height between adjacent columns
        var bumpiness = 0.0
        var lastHeight = 0
        for (x in 0 until grid.width) {
            var currentHeight = 0
            for (y in 0 until grid.height) {
                if (grid.getCell(x, y) == Cell.FILLED) {
                    currentHeight = grid.height - y
                    break
                }
            }
            if (x > 0) bumpiness += abs(currentHeight - lastHeight)
            lastHeight = currentHeight
        }
        return bumpiness

    }

    private fun calculateAverageColumnHeight(grid: GridRepresentation): Double {
        val totalHeight = (0 until grid.width).sumOf { getColumnHeight(grid, it) }
        return totalHeight.toDouble() / grid.width
    }

    private fun identifyTargetColumns(grid: GridRepresentation, threshold: Double = 0.5): List<Int> {
        val averageHeight = calculateAverageColumnHeight(grid)
        return (0 until grid.width).filter {
            getColumnHeight(grid, it) < averageHeight * (1 - threshold)
        }
    }

//    private fun calculateBumpiness(grid: GridRepresentation): Double {
//        var totalDifference = 0.0
//        var lastHeight = getColumnHeight(grid, 0)
//        for (x in 1 until grid.width) {
//            val currentHeight = getColumnHeight(grid, x)
//            totalDifference += abs(currentHeight - lastHeight)
//            lastHeight = currentHeight
//        }
//        return totalDifference
//    }
//
    private fun getColumnHeight(grid: GridRepresentation, columnIndex: Int): Int {
        for (y in 0 until grid.height) {
            if (grid.getCell(columnIndex, y) == Cell.FILLED) {
                return grid.height - y
            }
        }
        return 0
    }

}
package com.example.tetrisai.model

import kotlin.math.abs

class TetrisAI(val gameLogic: GameLogic) {

    fun chooseMove(): Pair<Int, Int> { // Returns rotation and position
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
    }

    fun isLegalMove(tetrimino: TetrisBlock, rotation: Int, xPosition: Int, width: Int): Boolean {
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
    }


    private fun evaluateBoard(gridBefore: GridRepresentation, gridAfter: GridRepresentation, xPosition: Int): Double {
        // Implement your heuristic evaluation here, for example:
        val aggregateHeightBefore = calculateAggregateHeight(gridBefore)
        val aggregateHeightAfter = calculateAggregateHeight(gridAfter)
        val completeLines = calculateCompleteLines(gridAfter)
        val holes = calculateHoles(gridAfter)
        val bumpiness = calculateBumpiness(gridAfter)
        val heightImpact = aggregateHeightAfter - aggregateHeightBefore
        val hole = calculateHolePenalties(gridAfter)
        val columnTargetingScore = calculateColumnTargetingScore(gridAfter, xPosition)
        val calculateTopRowsPenalty = calculateTopRowsPenalty(gridAfter)
        val calculateLowerPlacementReward = calculateLowerPlacementReward(gridAfter)

        val heightScore = if (heightImpact <= 0) 1.0 else 1.0 / (1 + heightImpact)

        System.out.println("Aggregate Height After: $aggregateHeightAfter\nComplete Lines: $completeLines\nHoles: $holes\nBumpiness: $bumpiness\n$xPosition\n--------------------")


        val aggregateHeightWeight = -4.0
        val completeLinesWeight = 50.0
        val holesWeight = -0.01
        val bumpinessWeight = -0.5
        val columnTargetingWeight = 2.0
        val topRowsPenaltyWeight = 1.0
        val lowerPlacementWeight = 1.0

        return ((aggregateHeightAfter * aggregateHeightWeight) +
                (completeLines * completeLinesWeight) +
                (hole + holesWeight) +
                (bumpiness * bumpinessWeight) +
                (columnTargetingScore * columnTargetingWeight) +
                (calculateTopRowsPenalty * topRowsPenaltyWeight) +
                (calculateLowerPlacementReward * lowerPlacementWeight))

    }

    private fun calculateTopRowsPenalty(hypotheticalGrid: GridRepresentation, penalty: Double = -1000.0): Double {
        for (y in 0..3) { // Only check the top 4 rows
            for (x in 0 until hypotheticalGrid.width) {
                if (hypotheticalGrid.getCell(x, y) == Cell.FILLED) {
                    return penalty // Apply penalty if any block is found in the top 4 rows
                }
            }
        }
        return 0.0 // No penalty if no blocks are in the top 4 rows
    }

    private fun calculateLowerPlacementReward(hypotheticalGrid: GridRepresentation, startingRow: Int = 4, rewardPerRow: Double = 5.0): Double {
        var reward = 0.0
        for (y in startingRow until hypotheticalGrid.height) { // Skip the top 4 rows
            for (x in 0 until hypotheticalGrid.width) {
                if (hypotheticalGrid.getCell(x, y) == Cell.FILLED) {
                    // Reward increases as we move lower in the grid
                    // Adjust the formula if the row indexing doesn't exactly match your grid's implementation
                    reward += ((y - startingRow + 1) * rewardPerRow) // "+ 1" to ensure we start adding from the first applicable row
                }
            }
        }
        return reward
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

    private fun calculateHolePenalties(grid: GridRepresentation): Double {
        var penalty = 0.0
        val visited = Array(grid.height) { BooleanArray(grid.width) { false } }
        for (x in 0 until grid.width) {
            for (y in 0 until grid.height) {
                if (!visited[y][x] && grid.getCell(x, y) == Cell.EMPTY) {
                    val holeSize = floodFill(grid, x, y, visited)
                    penalty += holeSize * holeSize // Example: square the hole size for the penalty
                }
            }
        }
        return penalty
    }

    private fun floodFill(grid: GridRepresentation, x: Int, y: Int, visited: Array<BooleanArray>): Int {
        if (x !in 0 until grid.width || y !in 0 until grid.height || visited[y][x] || grid.getCell(x, y) != Cell.EMPTY) {
            return 0
        }
        visited[y][x] = true
        // Recursively count the size of the connected empty space
        return 1 + floodFill(grid, x + 1, y, visited) +
                floodFill(grid, x - 1, y, visited) +
                floodFill(grid, x, y + 1, visited) +
                floodFill(grid, x, y - 1, visited)
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

    private fun calculateColumnTargetingScore(grid: GridRepresentation, xPosition: Int, threshold: Double = 0.5): Double {
        val targetColumns = identifyTargetColumns(grid, threshold)
        return if (xPosition in targetColumns) 1.0 else 0.0
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
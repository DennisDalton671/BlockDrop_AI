package com.example.tetrisai.model

import kotlin.math.max
import kotlin.math.min

enum class Cell {
    EMPTY, FILLED // You might expand this for different colors or block types
}

class GridRepresentation(val width: Int, val height: Int) {
    val grid = Array(height) { Array(width) { Cell.EMPTY } }
    private var fallingTetris: TetrisBlock? = null // Your Tetrimino representation

    // Call this method to update the falling Tetrimino's position
    fun updateFallingTetrimino(tetrimino: TetrisBlock?) {
        fallingTetris = tetrimino
    }

    fun clone(): GridRepresentation {
        val newGrid = GridRepresentation(width, height)
        for (y in grid.indices) {
            for (x in grid[y].indices) {
                newGrid.grid[y][x] = this.grid[y][x]
            }
        }
        return newGrid
    }

    fun getCell(x: Int, y: Int): Cell {
        if (x in 0 until width && y in 0 until height) {
            return grid[y][x]
        } else {
            throw IndexOutOfBoundsException("Cell position out of bounds")
        }
    }

//    fun placeTetriminoHypothetically(tetrimino: TetrisBlock, rotation: Int, xPosition: Int, grid: GridRepresentation): GridRepresentation {
//        val hypotheticalGrid = grid.clone() // Clone the current grid
//        val shape = tetrimino.getShape(rotation) // Get the shape
//        val lowestY = findLowestYPositionForTetrimino(tetrimino, rotation, xPosition, grid) // Find the lowest Y
//
//        // Place the Tetrimino shape onto the hypothetical grid at the calculated position
//        for (y in shape.indices) {
//            for (x in shape[y].indices) {
//                if (shape[y][x]) { // If this part of the Tetrimino is filled
//                    val gridX = x + xPosition
//                    val gridY = y + lowestY
//                    if (gridX in 0 until hypotheticalGrid.width && gridY in 0 until hypotheticalGrid.height) {
//                        hypotheticalGrid.setCell(gridX, gridY, Cell.FILLED) // Place the cell
//                    }
//                }
//            }
//        }
//
//        return hypotheticalGrid
//    }
//
//    fun findLowestYPositionForTetrimino(tetrimino: TetrisBlock, rotation: Int, xPosition: Int, grid: GridRepresentation): Int {
//        val shape = tetrimino.getShape(rotation)
//        // Iterate from the top of the grid to the bottom
//        for (potentialY in 0 until grid.height) {
//            // Check each part of the Tetrimino's shape
//            for (row in shape.indices) {
//                for (col in shape[row].indices) {
//                    if (shape[row][col]) { // If this part of the Tetrimino is filled
//                        val gridX = xPosition + col
//                        val gridY = potentialY + row
//
//                        // Check for collision with filled cells or out of bounds
//                        if (gridY >= grid.height || grid.isOccupied(gridX, gridY)) {
//                            // Collision detected or out of bounds, return the position above this one
//                            return max(0, potentialY - 1)
//                        }
//                    }
//                }
//            }
//        }
//        // If no collision is detected throughout the entire grid height, the Tetrimino can be placed at the bottom of the grid
//        return grid.height - shape.size
//    }
//
    fun isValidPlacement(tetrimino: TetrisBlock, rotation: Int, xPosition: Int, yPosition: Int, grid: GridRepresentation): Boolean {
        val shape = tetrimino.getShape(rotation)
        for (y in shape.indices) {
            for (x in shape[y].indices) {
                if (shape[y][x]) { // Part of Tetrimino is filled
                    val gridX = x + xPosition
                    val gridY = y + yPosition
                    if (gridX !in 0 until grid.width || gridY !in 0 until grid.height) {
                        return false // Tetrimino part is out of grid bounds
                    }
                    if (grid.isOccupied(gridX, gridY)) {
                        return false // Tetrimino part overlaps with a filled cell
                    }
                }
            }
        }
        return true // No collisions found, placement is valid
    }


    fun placeTetriminoHypothetically(tetrimino: TetrisBlock, rotation: Int, xPositionOriginal: Int, grid: GridRepresentation): GridRepresentation {
        val hypotheticalGrid = grid.clone()
        val shape = tetrimino.getShape(rotation)

        // Calculate the Tetrimino's leftmost valid X position based on its rotation and the original xPosition intended
        val startXPosition = calculateLeftmostXPosition(tetrimino, rotation, grid) + xPositionOriginal
        val adjustedXPosition = max(0, min(grid.width - shape[0].size, startXPosition)) // Ensure within grid bounds

        // Find the lowest Y position where the Tetrimino can be placed
        val lowestY = simulateDirectDescentWithStartingPosition(tetrimino, rotation, startXPosition, grid)

        // Place the Tetrimino shape onto the hypothetical grid at the calculated position
        for (y in shape.indices) {
            for (x in shape[y].indices) {
                if (shape[y][x]) {
                    val gridX = x + adjustedXPosition
                    val gridY = y + lowestY
                    if (gridX in 0 until hypotheticalGrid.width && gridY in 0 until hypotheticalGrid.height) {
                        hypotheticalGrid.placeBlock(gridX, gridY) // Use your existing method to place block
                    }
                }
            }
        }

        return hypotheticalGrid
    }

    fun simulateDirectDescentWithStartingPosition(tetrimino: TetrisBlock, rotation: Int, startXPosition: Int, grid: GridRepresentation): Int {
        var startY = 0
        while (isValidPlacement(tetrimino, rotation, startXPosition, startY, grid)) {
            startY++
        }
        // Return the last valid Y position before a collision would occur
        return max(0, startY - 1)
    }

    fun calculateLeftmostXPosition(tetrimino: TetrisBlock, rotation: Int, grid: GridRepresentation): Int {
        val shape = tetrimino.getShape(rotation)
        var leftmostX = shape[0].indices.firstOrNull { x -> shape.any { it[x] } } ?: 0
        // This calculation ensures the Tetrimino doesn't start out of the grid's left boundary
        return max(0, leftmostX)
    }

    fun GridRepresentation.setCell(x: Int, y: Int, value: Cell) {
        if (x in 0 until width && y in 0 until height) {
            grid[y][x] = value
        }
    }

    fun isOccupied(x: Int, y: Int): Boolean {
        // Ensure x and y are within bounds before checking
        if (x in 0 until width && y in 0 until height) {
            return grid[y][x] != Cell.EMPTY
        }
        return false
    }

    fun placeBlock(x: Int, y: Int) {
        if (y in 0 until height && x in 0 until width) {
            grid[y][x] = Cell.FILLED
        }
    }

    // This function will check each row to see if it's filled, and clear it if so.
    fun clearLines(): Int {
        var clearedLines = 0

        for (y in 0 until height) {
            if (grid[y].all { it == Cell.FILLED }) { // Check if the row is full
                clearLine(y)
                clearedLines++
            }
        }

        return clearedLines
    }

    fun clear() {
        for (y in 0 until height) {
            if (grid[y].all { it == Cell.FILLED }) { // Check if the row is full
                clearLine(y)
            }
        }
    }

    private fun clearLine(row: Int) {
        for (y in row downTo 1) { // Start from the cleared row, going upwards
            for (x in 0 until width) {
                grid[y][x] = grid[y - 1][x] // Move each cell down one row
            }
        }
        // Clear the top row, which is now empty
        for (x in 0 until width) {
            grid[0][x] = Cell.EMPTY
        }
    }

    fun checkGameOver(spawnX: Int, spawnY: Int, blockShape: Array<BooleanArray>): Boolean {
        for (y in blockShape.indices) {
            for (x in blockShape[0].indices) {
                if (blockShape[y][x] && isOccupied(spawnX + x, spawnY + y)) {
                    return true // There is an overlap, so it's game over
                }
            }
        }
        return false // No overlap, the game can continue
    }

    fun getGridForUI(tetrisBlock: TetrisBlock?, currentX: Int, currentY: Int, rotation: Int): Array<Array<Cell>> {
        // Create a copy of the grid
        val gridCopy = Array(height) { Array(width) { Cell.EMPTY } }

        for (y in 0 until height) {
            for (x in 0 until width) {
                gridCopy[y][x] = grid[y][x]
            }
        }

        // Then, overlay the falling Tetrimino
        tetrisBlock?.let { block ->
            val shape = block.getShape(rotation) // Assuming getShape accounts for rotation
            for (y in shape.indices) {
                for (x in shape[y].indices) {
                    if (shape[y][x]) {
                        val posX = currentX + x
                        val posY = currentY + y
                        // Check bounds to prevent ArrayIndexOutOfBoundsException
                        if (posX in 0 until width && posY in 0 until height) {
                            gridCopy[posY][posX] = Cell.FILLED
                        }
                    }
                }
            }
        }

        return gridCopy
    }

    // Add methods for clearing lines, checking for game over, etc.
}
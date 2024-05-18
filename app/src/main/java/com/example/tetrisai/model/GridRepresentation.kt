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

    fun countHoles(): Int {
        var holes = 0

        // Iterate through each column
        for (x in 0 until width) {
            var blockFound = false

            // Traverse from top to bottom in each column
            for (y in 0 until height) {
                val cell = grid[y][x]

                if (cell == Cell.FILLED) {
                    blockFound = true
                } else if (blockFound && cell == Cell.EMPTY) {
                    // Increment the hole count for each empty cell below a filled cell
                    holes++
                }
            }
        }

        return holes
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
/*    fun isValidPlacement(tetrimino: TetrisBlock, rotation: Int, xPosition: Int, yPosition: Int, grid: GridRepresentation): Boolean {
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
    }*/

    fun isValidPlacement(
        shape: Array<BooleanArray>,
        xPosition: Int,
        yPosition: Int,
        grid: GridRepresentation
    ): Boolean {
        // Iterate over each cell in the shape
        for (y in shape.indices) {
            for (x in shape[y].indices) {
                if (shape[y][x]) { // Check only filled parts of the Tetrimino
                    val gridX = x + xPosition
                    val gridY = y + yPosition
                    if (gridX !in 0 until grid.width || gridY !in 0 until grid.height) {
                        return false // The Tetrimino would be out of grid bounds
                    }
                    if (grid.isOccupied(gridX, gridY)) {
                        return false // The Tetrimino would overlap with an existing filled cell
                    }
                }
            }
        }
        return true // The placement is valid across all checked positions
    }


/*    fun placeTetriminoHypothetically(tetrimino: TetrisBlock, rotation: Int, xPositionOriginal: Int, grid: GridRepresentation): GridRepresentation {
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
    }*/

/*    fun placeTetriminoHypothetically(
        trimmedShape: Array<BooleanArray>,
        xPosition: Int,
        yPosition: Int,
        grid: GridRepresentation
    ): GridRepresentation {
        val hypotheticalGrid = grid.clone()  // Clone the current grid for hypothetical placement

        // Place the Tetrimino shape onto the hypothetical grid at the calculated position
        for (y in trimmedShape.indices) {
            for (x in trimmedShape[y].indices) {
                if (trimmedShape[y][x]) {  // If this part of the Tetrimino is filled
                    val gridX = x + xPosition
                    val gridY = y + yPosition
                    if (gridX in 0 until hypotheticalGrid.width && gridY in 0 until hypotheticalGrid.height) {
                        hypotheticalGrid.placeBlock(gridX, gridY)  // Use your existing method to place the block
                    }
                }
            }
        }

        return hypotheticalGrid
    }*/

    fun placeTetriminoHypothetically(
        trimmedShape: Array<BooleanArray>,
        xPosition: Int,
        grid: GridRepresentation
    ): GridRepresentation {
        val hypotheticalGrid = grid.clone()

        // Calculate the lowest possible Y position for the Tetrimino to be placed
        val lowestY = findLowestYPositionForShape(trimmedShape, xPosition, grid)

        // Place the Tetrimino shape onto the hypothetical grid at the calculated position
        for (y in trimmedShape.indices) {
            for (x in trimmedShape[y].indices) {
                if (trimmedShape[y][x]) {
                    val gridX = x + xPosition
                    val gridY = y + lowestY
                    if (gridX in 0 until hypotheticalGrid.width && gridY in 0 until hypotheticalGrid.height) {
                        hypotheticalGrid.placeBlock(gridX, gridY)
                    }
                }
            }
        }

        return hypotheticalGrid
    }

    fun findLowestYPositionForShape(
        shape: Array<BooleanArray>,
        xPosition: Int,
        grid: GridRepresentation
    ): Int {
        var lowestY = 0
        var valid = true

        while (valid) {
            // Check if the current Y position is valid for the shape
            for (y in shape.indices) {
                for (x in shape[y].indices) {
                    if (shape[y][x]) {
                        val gridX = x + xPosition
                        val gridY = y + lowestY
                        // Check if this part of the shape goes out of grid bounds or collides
                        if (gridY >= grid.height || grid.isOccupied(gridX, gridY)) {
                            valid = false
                            break
                        }
                    }
                }
                if (!valid) break
            }
            if (valid) lowestY++
        }

        return max(0, lowestY - 1)
    }

/*    fun simulateDirectDescentWithStartingPosition(tetrimino: TetrisBlock, rotation: Int, startXPosition: Int, grid: GridRepresentation): Int {
        var startY = 0
        while (isValidPlacement(tetrimino, rotation, startXPosition, startY, grid)) {
            startY++
        }
        // Return the last valid Y position before a collision would occur
        return max(0, startY - 1)
    }*/

    fun simulateDirectDescentWithStartingPosition(
        shape: Array<BooleanArray>,
        startXPosition: Int,
        grid: GridRepresentation
    ): Int {
        var startY = 0  // Start at the topmost possible position
        while (true) {
            // Assume isValidPlacement always returns true since moves are pre-validated
            if (!isValidPlacement(shape, startXPosition, startY, grid)) {
                return max(0, startY - 1) // Return the last valid position
            }
            startY++
        }
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

    fun checkGameOver(): Boolean {
        // Assuming the top row is the first row of the grid
        val topRowIndex = 0
        System.out.println("Checking for game over")
        // Iterate through all columns in the top row
        for (x in 0 until width) {
            if (grid[topRowIndex][x] == Cell.FILLED) {
                System.out.println(grid[x][topRowIndex])
                return true // If any cell is filled in the top row, it's game over
            }
        }

        return false // No filled cells in the top row, so it's not game over
    }

    fun getGridForUI(tetrisBlock: TetrisBlock?, currentX: Int, currentY: Int, rotation: Int): Array<Array<Cell>> {
        val gridCopy = Array(height) { Array(width) { Cell.EMPTY } }

        // Copy existing grid state
        for (y in grid.indices) {
            for (x in grid[y].indices) {
                gridCopy[y][x] = grid[y][x]
            }
        }

        // Overlay the falling Tetrimino
        tetrisBlock?.let { block ->
            val shape = block.getShape(rotation)
            for (y in shape.indices) {
                for (x in shape[y].indices) {
                    if (shape[y][x]) {
                        val posX = currentX + x
                        val posY = currentY + y
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
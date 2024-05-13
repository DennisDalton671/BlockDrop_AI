package com.example.tetrisai.View

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.tetrisai.R
import com.example.tetrisai.model.Cell
import com.example.tetrisai.model.TetrisBlock
import kotlinx.coroutines.delay
import kotlin.random.Random

class MenuScreen {

    @Composable
    fun MenuScreenSetup(navController: NavController) {
        // Grid size
        val columns = 10
        val rows = 20

        // Screen dimensions
        val configuration = LocalConfiguration.current
        val screenWidth = configuration.screenWidthDp.dp
        val screenHeight = configuration.screenHeightDp.dp

        // Calculate the cell size to fit the grid
        val cellSize = calculateMenuGridCellSize(screenWidth, columns, screenHeight, rows)

        // Adjust button width to extend a half-cell on each side
        val extendedButtonWidth = cellSize * 4f  // Use Float to multiply by a fraction

        // Calculate starting padding to center the grid horizontally
        val horizontalPadding = (screenWidth - cellSize * columns) / 2

        // Calculate starting padding to center the grid vertically with 2 rows for the title
        val verticalPadding = (screenHeight - cellSize * rows) / 2

        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // Background grid pattern
            val startUserGamePosition = Pair(3, 10)
            val startAIGamePosition = Pair(3, 12)
            val startMachineAIGamePosition = Pair(3, 14)
            val buttonPositions = listOf(startUserGamePosition, startAIGamePosition, startMachineAIGamePosition)
            var menuBackgroundGrid by remember { mutableStateOf(generateMenuBackgroundGrid(columns, rows, buttonPositions)) }

            // LaunchedEffect to update the grid pattern every 2 seconds
            LaunchedEffect(Unit) {
                while (true) {
                    delay(2000)
                    menuBackgroundGrid = generateMenuBackgroundGrid(columns, rows, buttonPositions)
                }
            }

            // Render the background grid
            TetrisMenuBackground(menuBackgroundGrid, cellSize)

            // Overlay the menu content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = horizontalPadding, vertical = verticalPadding),
                verticalArrangement = Arrangement.spacedBy(cellSize),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Title box, moved up by half a cell
                Spacer(modifier = Modifier.height(cellSize))
                Box(
                    modifier = Modifier
                        .size(extendedButtonWidth, cellSize)
                        .border(1.dp, Color.LightGray)
                        .background(Color.Black)
                ) {
                    Text(
                        text = "Tetris AI",
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.LightGray,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                Spacer(modifier = Modifier.height(cellSize))

                // Create buttons with borders and black text
                listOf(
                    "Start User Game" to "userGameScreen",
                    "Start AI Game" to "aiGameScreen",
                    "Start Machine AI Game" to "machineAiGameScreen"
                ).forEach { (label, screen) ->
                    Button(
                        onClick = { navController.navigate(screen) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                        shape = RectangleShape,
                        border = BorderStroke(1.dp, Color.LightGray),
                        modifier = Modifier.size(extendedButtonWidth, cellSize * 2)
                    ) {
                        Text(
                            label,
                            color = Color.LightGray,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Settings Button
            Box(
                modifier = Modifier
                    .size(cellSize)  // 2x2 blocks
                    .offset(x = (screenWidth - horizontalPadding - cellSize * 2), y = verticalPadding + cellSize) // Positioned one block down and one block left from the right wall
                    .clickable { navController.navigate("settingsScreen") }
                    .background(Color.Black) // Background color to make it visible, replace with your design
                    .border(1.dp, Color.LightGray),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = Color.LightGray,
                    modifier = Modifier.size(cellSize)
                )
            }
        }
    }

    fun generateMenuBackgroundGrid(columns: Int, rows: Int, buttonAreas: List<Pair<Int, Int>>): Array<Array<Cell>> {
        val grid = Array(rows) { Array(columns) { Cell.EMPTY } }

        // Randomly place Tetris-like blocks while avoiding the button positions
        for (x in 0 until columns) {
            for (y in 0 until rows) {
                // Skip cells in specified button areas
                if (!buttonAreas.any { (bx, by) -> x == bx && y == by }) {
                    // Place a random cell to mimic a Tetris block
                    if (Random.nextBoolean()) {
                        grid[y][x] = Cell.FILLED // Simple random filling logic
                    }
                }
            }
        }

        return grid
    }

    // Composable function to render the Tetris grid
    @Composable
    fun TetrisMenuBackground(grid: Array<Array<Cell>>, cellSize: Dp) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            for (row in grid) {
                Row(horizontalArrangement = Arrangement.Center) {
                    for (cell in row) {
                        Box(
                            modifier = Modifier
                                .size(cellSize)
                                .border(1.dp, Color.LightGray)
                                .background(if (cell == Cell.FILLED) Color.Red else Color.Black)
                        )
                    }
                }
            }
        }
    }

    fun calculateMenuGridCellSize(screenWidth: Dp, columns: Int, screenHeight: Dp, rows: Int): Dp {
        // Calculate the size based on width and height
        val widthBasedSize = screenWidth / columns
        val heightBasedSize = screenHeight / rows
        return minOf(widthBasedSize, heightBasedSize)
    }

}


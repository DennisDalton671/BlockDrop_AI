package com.example.tetrisai.View

import android.annotation.SuppressLint
import android.view.ViewConfiguration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.forEachGesture
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.tetrisai.ViewModel.GameStateManager
import com.example.tetrisai.model.Cell
import kotlinx.coroutines.launch
import kotlin.math.abs

class GameScreen() {


    private var startTime: Long = 0
    private var endTime: Long = 0
    @SuppressLint("StateFlowValueCalledInComposition")
    @Composable
    fun GameScreenSetup(gameView: GameStateManager, navController: NavController, gameState: Int) {

        val score by gameView.score.collectAsState()
        val level by gameView.level.collectAsState()
        val lines by gameView.lines.collectAsState()
        val grid by gameView.gridRepresentation.collectAsState()
        val gameOver by gameView.gameOver.collectAsState()
        startTime = remember { System.currentTimeMillis() }
        System.out.println("Start Time: $startTime")

        // Use LaunchedEffect to perform side effects when gameOver changes
        LaunchedEffect(gameOver) {
            if (gameOver) {
                endTime = System.currentTimeMillis()
                System.out.println("End Time: $endTime")
                val finalTime = getFormattedGameDuration()
                val gameStats = gameView.gameStats()

                // Replace "mainMenu" with the name of your actual main menu route
                    if (gameState == 0)
                        navController.navigate("endGameScreen/$score/$lines/$level/$finalTime/${gameStats.singleLinesCleared}/${gameStats.doubleLinesCleared}/${gameStats.tripleLinesCleared}/${gameStats.tetrisLinesCleared}/0")
                    if (gameState == 1)
                        navController.navigate("endGameScreen/$score/$lines/$level/$finalTime/${gameStats.singleLinesCleared}/${gameStats.doubleLinesCleared}/${gameStats.tripleLinesCleared}/${gameStats.tetrisLinesCleared}/1")
                    if (gameState == 2)
                        navController.navigate("endGameScreen/$score/$lines/$level/$finalTime/${gameStats.singleLinesCleared}/${gameStats.doubleLinesCleared}/${gameStats.tripleLinesCleared}/${gameStats.tetrisLinesCleared}/2")

            }
        }

        Column(modifier = Modifier.background(Color.Black)) {
            val topBarPadding = 16.dp
            val modifier = Modifier.padding(topBarPadding)
            GameTopBar(score, level, lines, modifier)
            GameGrid(grid, topBarPadding, gameView)
        }
    }


    @Composable
    fun GameTopBar(score: Int, level: Int, lines: Int, modifier: Modifier = Modifier) {
        // Set the background color to black and text color to white
        val blackBackground = Color.Black
        val lightGrayText = Color.LightGray

        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = modifier
                .fillMaxWidth()
                .background(blackBackground) // Apply black background to the entire row
                .padding(horizontal = 16.dp, vertical = 8.dp) // Optional padding for spacing
        ) {
            Text(
                buildAnnotatedString {
                    withStyle(style = SpanStyle(color = lightGrayText, fontSize = 16.sp)) {
                        append("Score: ")
                    }
                    withStyle(style = SpanStyle(color = lightGrayText, fontSize = calculateFontSize(score))) {
                        append("$score")
                    }
                }
            )
            Text(
                buildAnnotatedString {
                    withStyle(style = SpanStyle(color = lightGrayText, fontSize = 16.sp)) {
                        append("Level: ")
                    }
                    withStyle(style = SpanStyle(color = lightGrayText, fontSize = calculateFontSize(level))) {
                        append("$level")
                    }
                }
            )
            Text(
                buildAnnotatedString {
                    withStyle(style = SpanStyle(color = lightGrayText, fontSize = 16.sp)) {
                        append("Lines Cleared: ")
                    }
                    withStyle(style = SpanStyle(color = lightGrayText, fontSize = calculateFontSize(lines - 1))) {
                        append("${lines - 1}")
                    }
                }
            )
        }
    }

    fun calculateFontSize(number: Int): TextUnit = when {
        number < 1000 -> 16.sp
        number < 10000 -> 14.sp
        number < 100000 -> 12.sp
        number < 1000000 -> 10.sp
        else -> 8.sp
    }

    @Composable
    fun GameGrid(grid: Array<Array<Cell>>, topBarPadding: Dp, gameView: GameStateManager) {

        val configuration = LocalConfiguration.current
        val screenWidth = configuration.screenWidthDp.dp
        val screenHeight = configuration.screenHeightDp.dp

        // Assuming a grid of 10 columns and 20 rows
        val columns = 10
        val rows = 20

        val outerPadding = 0.dp

        // Calculate the cell size to fit the grid within the screen
        val cellSize = calculateCellSize(screenWidth, columns, screenHeight, rows, outerPadding, topBarPadding)

        val coroutineScope = rememberCoroutineScope()
        val touchSlop = ViewConfiguration.get(LocalContext.current).scaledTouchSlop
        var ignoreDrag: Boolean = false

        Box(



            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .background(Color.Black)
                .pointerInput(Unit) {
                    var touchDownPosition = Offset.Unspecified
                    var lastPosition = Offset.Unspecified
                    var touchUpPosition = Offset.Unspecified
                    var moved = false

                    forEachGesture {
                        awaitPointerEventScope {
                            touchDownPosition = Offset.Unspecified
                            lastPosition = Offset.Unspecified
                            touchUpPosition = Offset.Unspecified
                            moved = false

                            val down = awaitFirstDown(false)
                            touchDownPosition = down.position
                            lastPosition = down.position

                            var drag = false

                            do {
                                val event = awaitPointerEvent()
                                touchUpPosition = event.changes[0].position

                                if (!event.changes[0].pressed) {
                                    // Touch was released, determine if it was a tap or a drag
                                    if (!drag && !moved) {
                                        coroutineScope.launch { gameView.rotateTetrimino() }
                                    }
                                    break
                                }

                                moved = (lastPosition - touchUpPosition).getDistance() > touchSlop
                                if (moved) {
                                    drag = true
                                    val moveAmount = touchUpPosition - lastPosition
                                    if (abs(moveAmount.x) > abs(moveAmount.y)) {
                                        // Horizontal movement
                                        if (moveAmount.x > 0) {
                                            coroutineScope.launch { gameView.moveRight() }
                                        } else {
                                            coroutineScope.launch { gameView.moveLeft() }
                                        }
                                    } else {
                                        // Vertical movement, primarily for dropping the Tetrimino
                                        if (moveAmount.y > 0 && !ignoreDrag) {
                                            coroutineScope.launch { gameView.dropTetrimino() }
                                            ignoreDrag = true
                                        }
                                    }
                                    lastPosition = touchUpPosition
                                }
                            } while (event.changes.any { it.pressed })

                            ignoreDrag = false
                        }
                    }
                }

        ){
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(outerPadding),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                for (row in grid) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
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
    }

    suspend fun PointerInputScope.detectTouchGestures(
        onTap: () -> Unit,
        onDrag: (PointerInputChange, Offset) -> Unit
    ) {
        forEachGesture {
            awaitPointerEventScope {
                val down = awaitFirstDown()
                var dragStarted = false
                do {
                    val event = awaitPointerEvent()
                    val dragEvent = event.changes.firstOrNull()
                    if (dragEvent != null && dragEvent.positionChange() != Offset.Zero) {
                        if (!dragStarted) {
                            dragStarted = true
                            onDrag(dragEvent, dragEvent.positionChange())
                        }
                    }
                } while (event.changes.any { it.pressed })

                if (!dragStarted) {
                    onTap()
                }
            }
        }
    }

    fun calculateCellSize(availableWidth: Dp, columns: Int, availableHeight: Dp, rows: Int, outerPadding: Dp, topBarPadding: Dp): Dp {
        // Deduct the outerPadding from both width and height to get the actual drawable area
        val drawableWidth = availableWidth - (outerPadding * 2)
        val drawableHeight = availableHeight - (outerPadding * 2) - (topBarPadding * 7)

        val widthBasedSize = drawableWidth / columns
        val heightBasedSize = drawableHeight / rows
        return minOf(widthBasedSize, heightBasedSize)
    }

    fun getFormattedGameDuration(): String {
        val durationMillis = endTime - startTime
        val seconds = (durationMillis / 1000) % 60
        val minutes = (durationMillis / (1000 * 60)) % 60
        val hours = (durationMillis / (1000 * 60 * 60)) % 24
        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }
}
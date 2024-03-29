package com.example.tetrisai.View

import android.annotation.SuppressLint
import android.view.ViewConfiguration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.consumeAllChanges
import androidx.compose.ui.input.pointer.consumePositionChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import com.example.tetrisai.ViewModel.GameStateManager
import com.example.tetrisai.model.Cell
import com.example.tetrisai.model.GridRepresentation
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.absoluteValue

class GameScreen() {


    @SuppressLint("StateFlowValueCalledInComposition")
    @Composable
    fun GameScreenSetup(gameView: GameStateManager, navController: NavController) {

        val score by gameView.score.collectAsState()
        val level by gameView.level.collectAsState()
        val lines by gameView.lines.collectAsState()
        val grid by gameView.gridRepresentation.collectAsState()

        Column(modifier = Modifier) {
            val topBarPadding = 16.dp
            val modifier = Modifier.padding(topBarPadding)
            GameTopBar(score, level, lines, modifier)
            GameGrid(grid, topBarPadding, gameView)
        }
    }


    @Composable
    fun GameTopBar(score: Int, level: Int, lines: Int, modifier: Modifier = Modifier) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = modifier
                .fillMaxWidth()
        ) {
            Text(text = "Score: $score")
            Text(text = "Level: $level")
            Text(text = "Lines Cleared: ${lines - 1}")
        }
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
                                    .border(1.dp, Color.Black)
                                    .background(if (cell == Cell.FILLED) Color.Red else Color.LightGray)
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
}
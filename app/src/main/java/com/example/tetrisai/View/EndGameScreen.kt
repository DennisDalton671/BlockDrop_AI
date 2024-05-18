package com.example.tetrisai.View

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

class EndGameScreen {

    @Composable
    fun GameEndScreen(
        score: Int = 0,
        linesCleared: Int = 0,
        level: Int = 0,
        timePlayed: String = "N/A",
        singleLines: Int = 0,
        doubleLines: Int = 0,
        tripleLines: Int = 0,
        tetrisLines: Int = 0,
        navController: NavController,
        gameState: Int = 0,
        themeSettings: MutableState<ThemeSettings>
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(themeSettings.value.backgroundColor),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,  // Center content horizontally
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.verticalScroll(rememberScrollState())  // Make the column scrollable
            ) {
                Text("Score: $score", color = themeSettings.value.textColor, fontSize = 24.sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.CenterHorizontally))
                Spacer(Modifier.height(24.dp))
                StatItem("Lines Cleared", linesCleared.toString(), themeSettings.value.textColor)
                StatItem("Level", level.toString(), themeSettings.value.textColor)
                StatItem("Time Played", timePlayed, themeSettings.value.textColor)
                StatItem("Singles", singleLines.toString(), themeSettings.value.textColor)
                StatItem("Doubles", doubleLines.toString(), themeSettings.value.textColor)
                StatItem("Triples", tripleLines.toString(), themeSettings.value.textColor)
                StatItem("Tetrises", tetrisLines.toString(), themeSettings.value.textColor)
                Spacer(Modifier.height(32.dp))
                ButtonRow(navController, gameState, themeSettings.value.blockColor, themeSettings.value.textColor)
            }
        }
    }

    @Composable
    fun ButtonRow(navController: NavController, gameState: Int, buttonColor: Color, textColor: Color) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                onClick = {
                    // Determine game screen route based on game state
                    val gameScreenRoute = when (gameState) {
                        0 -> "userGameScreen"
                        1 -> "aiGameScreen"
                        2 -> "machineAiGameScreen"
                        else -> "defaultGameScreen"
                    }
                    navController.navigate(gameScreenRoute) {
                        popUpTo(gameScreenRoute) { inclusive = true }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = buttonColor)
            ) {
                Text("Reset", color = textColor)
            }
            Button(
                onClick = {
                    navController.navigate("menuScreen") {
                        popUpTo("mainMenuScreen") { inclusive = true }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = buttonColor)
            ) {
                Text("Main Menu", color = textColor)
            }
        }
    }

    @Composable
    fun StatItem(label: String, value: String, textColor: Color) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, color = textColor, fontSize = 18.sp, modifier = Modifier.weight(1f))
            CustomDottedLine(modifier = Modifier.weight(2f))  // Adjust weight as needed to control the line's length
            Text(value, color = textColor, fontSize = 18.sp, modifier = Modifier.weight(1f))
        }
    }

    @Composable
    fun CustomDottedLine(modifier: Modifier = Modifier) {
        Canvas(modifier = modifier.height(1.dp)) {
            val pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
            drawLine(
                color = Color.Gray,
                start = Offset.Zero,
                end = Offset(size.width, 0f),
                pathEffect = pathEffect
            )
        }
    }
}
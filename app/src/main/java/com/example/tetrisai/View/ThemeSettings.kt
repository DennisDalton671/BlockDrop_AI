package com.example.tetrisai.View

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color

data class ThemeSettings(
    var backgroundColor: Color,
    var textColor: Color,
    var blockColor: Color
)

@Composable
fun rememberThemeSettings(): MutableState<ThemeSettings> {
    return remember {
        mutableStateOf(
            ThemeSettings(
                backgroundColor = Color.Black,
                textColor = Color.LightGray,
                blockColor = Color.Red
            )
        )
    }
}

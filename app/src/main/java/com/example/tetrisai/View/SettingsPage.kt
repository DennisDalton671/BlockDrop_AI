package com.example.tetrisai.View

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController


@Composable
fun SettingsPage(navController: NavController) {
    var volume by remember { mutableStateOf(50f) }
    var selectedPreset by remember { mutableStateOf("") }
    var backgroundColor by remember { mutableStateOf(Color.Black) }
    var textColor by remember { mutableStateOf(Color.LightGray) }
    var blockColor by remember { mutableStateOf(Color.Red) }

    Column(
        modifier = Modifier
            .background(Color.Black)
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            color = textColor,
            text = "Settings",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(color = textColor, text = "Volume", fontSize = 18.sp)
        Slider(
            value = volume,
            onValueChange = { volume = it },
            valueRange = 0f..100f,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(color = textColor, text = "Theme", fontSize = 18.sp)

        Text(color = textColor, text = "Presets", fontSize = 16.sp)
        DropdownMenuPreset(
            selectedPreset = selectedPreset,
            onPresetSelected = { selectedPreset = it }
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(color = textColor, text = "Custom Colors", fontSize = 16.sp)
        ColorSelector("Background Color", backgroundColor) { backgroundColor = it }
        ColorSelector("Text Color", textColor) { textColor = it }
        ColorSelector("Block Color", blockColor) { blockColor = it }

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(onClick = { /* Save Action */ }) {
                Text(color = textColor, text = "Save")
            }
            Button(onClick = { navController.navigate("menuScreen") {
                popUpTo("mainMenuScreen") { inclusive = true }
            } }) {
                Text(color = textColor, text = "Cancel")
            }
            Button(onClick = { /* Restore Defaults Action */ }) {
                Text(color = textColor, text = "Restore Defaults")
            }
        }
    }
}

@Composable
fun DropdownMenuPreset(selectedPreset: String, onPresetSelected: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val presets = listOf("Preset 1", "Preset 2", "Preset 3")

    Box {
        TextButton(onClick = { expanded = true }) {
            Text(color = Color.LightGray, text = if (selectedPreset.isEmpty()) "Select Preset" else selectedPreset)
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            presets.forEach { preset ->
                DropdownMenuItem(
                    text = {Text(color = Color.LightGray, text = preset)},
                    onClick = {
                    onPresetSelected(preset)
                    expanded = false
                })
            }
        }
    }
}

@Composable
fun ColorSelector(label: String, color: Color, onColorChange: (Color) -> Unit) {
    var colorPickerVisible by remember { mutableStateOf(false) }

    Column {
        Text(color = Color.LightGray, text = label, fontSize = 14.sp)
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(color)
                .clickable { colorPickerVisible = true }
        )

        if (colorPickerVisible) {
            ColorPicker(
                initialColor = color,
                onColorSelected = {
                    onColorChange(it)
                    colorPickerVisible = false
                },
                onDismissRequest = { colorPickerVisible = false }
            )
        }
    }
}

@Composable
fun ColorPicker(
    initialColor: Color,
    onColorSelected: (Color) -> Unit,
    onDismissRequest: () -> Unit
) {
    // Color picker implementation here (this could be a custom dialog or any other UI element)
    // For simplicity, let's assume you have a color picker dialog that calls onColorSelected
    // with the chosen color and onDismissRequest when the dialog is dismissed.
}
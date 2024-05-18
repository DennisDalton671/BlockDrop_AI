package com.example.tetrisai.View

import android.content.Context
import android.media.MediaPlayer
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.tetrisai.TetrisApplication


@Composable
fun SettingsPage(
    navController: NavController,
    themeSettings: MutableState<ThemeSettings>,
    mediaPlayer: MediaPlayer,
    saveVolume: (Float) -> Unit,
    saveThemeSettings: (ThemeSettings) -> Unit
) {
    // Store initial settings to revert to on cancel
    val initialSettings = remember { mutableStateOf(themeSettings.value.copy()) }

    // Retrieve current volume from SharedPreferences
    val context = LocalContext.current
    val sharedPreferences = context.getSharedPreferences("TetrisPreferences", Context.MODE_PRIVATE)
    var volume by remember { mutableStateOf(sharedPreferences.getFloat("volume", 0.5f) * 100f) } // Convert to percentage

    var selectedPreset by remember { mutableStateOf("Original") }

    val presets = mapOf(
        "Original" to Triple(Color.Black, Color.LightGray, Color.Red),
        "Sunset" to Triple(Color(0xFFFFA500), Color(0xFFFF4500), Color(0xFF8B0000)),
        "Forest" to Triple(Color(0xFF228B22), Color(0xFFADFF2F), Color(0xFF006400)),
        "Ocean" to Triple(Color(0xFF1E90FF), Color(0xFF00CED1), Color(0xFF00008B)),
        "Midnight" to Triple(Color(0xFF191970), Color(0xFF8A2BE2), Color(0xFF4B0082))
    )

    val restoreDefaults: () -> Unit = {
        volume = 50f
        selectedPreset = "Original"
        val (bgColor, txtColor, blkColor) = presets["Original"] ?: Triple(Color.Black, Color.LightGray, Color.Red)
        themeSettings.value = ThemeSettings(bgColor, txtColor, blkColor)
        mediaPlayer.setVolume(volume / 100f, volume / 100f)
        saveVolume(volume / 100f)
        saveThemeSettings(themeSettings.value)
    }

    Column(
        modifier = Modifier
            .background(themeSettings.value.backgroundColor)
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            color = themeSettings.value.textColor,
            text = "Settings",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(color = themeSettings.value.textColor, text = "Volume", fontSize = 18.sp)
        Slider(
            value = volume,
            onValueChange = {
                volume = it
                mediaPlayer.setVolume(volume / 100f, volume / 100f)
                saveVolume(volume / 100f)
            },
            valueRange = 0f..100f,
            modifier = Modifier.fillMaxWidth(),
            colors = SliderDefaults.colors(
                thumbColor = themeSettings.value.blockColor,
                activeTrackColor = themeSettings.value.blockColor,
                inactiveTrackColor = themeSettings.value.textColor
            )
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(color = themeSettings.value.textColor, text = "Theme", fontSize = 18.sp)

        Text(color = themeSettings.value.textColor, text = "Presets", fontSize = 16.sp)
        DropdownMenuPreset(
            selectedPreset = selectedPreset,
            themeSettings = themeSettings,
            onPresetSelected = {
                selectedPreset = it
                val (bgColor, txtColor, blkColor) = presets[it] ?: Triple(Color.Black, Color.LightGray, Color.Red)
                themeSettings.value = ThemeSettings(bgColor, txtColor, blkColor)
                saveThemeSettings(themeSettings.value)
            }
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(color = themeSettings.value.textColor, text = "Custom Colors", fontSize = 16.sp)
        Spacer(modifier = Modifier.height(10.dp))
        ColorSelector("Background Color", themeSettings.value.backgroundColor, themeSettings.value.backgroundColor, themeSettings.value.textColor) {
            themeSettings.value = themeSettings.value.copy(backgroundColor = it)
            saveThemeSettings(themeSettings.value)
        }
        Spacer(modifier = Modifier.height(10.dp))
        ColorSelector("Text Color", themeSettings.value.textColor, themeSettings.value.backgroundColor, themeSettings.value.textColor) {
            themeSettings.value = themeSettings.value.copy(textColor = it)
            saveThemeSettings(themeSettings.value)
        }
        Spacer(modifier = Modifier.height(10.dp))
        ColorSelector("Block Color", themeSettings.value.blockColor, themeSettings.value.backgroundColor, themeSettings.value.textColor) {
            themeSettings.value = themeSettings.value.copy(blockColor = it)
            saveThemeSettings(themeSettings.value)
        }

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(
                onClick = {
                    // Save the settings
                    initialSettings.value = themeSettings.value.copy()
                    saveVolume(volume / 100f) // Save volume to SharedPreferences
                    saveThemeSettings(themeSettings.value)
                    // Optionally, you can save other settings to persistent storage here
                },
                colors = ButtonDefaults.buttonColors(containerColor = themeSettings.value.blockColor)
            ) {
                Text(color = themeSettings.value.textColor, text = "Save")
            }
            Button(
                onClick = {
                    // Restore the initial settings
                    themeSettings.value = initialSettings.value.copy()
                    navController.navigate("menuScreen") {
                        popUpTo("mainMenuScreen") { inclusive = true }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = themeSettings.value.blockColor)
            ) {
                Text(color = themeSettings.value.textColor, text = "Cancel")
            }
            Button(
                onClick = restoreDefaults,
                colors = ButtonDefaults.buttonColors(containerColor = themeSettings.value.blockColor)
            ) {
                Text(color = themeSettings.value.textColor, text = "Restore Defaults")
            }
        }
    }
}


@Composable
fun DropdownMenuPreset(selectedPreset: String, themeSettings: MutableState<ThemeSettings>, onPresetSelected: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val presets = listOf("Original", "Sunset", "Forest", "Ocean", "Midnight")

    Box {
        TextButton(onClick = { expanded = true }) {
            Text(color = themeSettings.value.textColor, text = selectedPreset)
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(themeSettings.value.backgroundColor)
        ) {
            presets.forEach { preset ->
                DropdownMenuItem(
                    text = { Text(color = themeSettings.value.textColor, text = preset) },
                    onClick = {
                        onPresetSelected(preset)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun ColorSelector(label: String, color: Color, backgroundColor: Color, textColor: Color, onColorChange: (Color) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val colors = listOf(
        "Black" to Color.Black,
        "White" to Color.White,
        "Red" to Color.Red,
        "Green" to Color.Green,
        "Blue" to Color.Blue,
        "Yellow" to Color.Yellow,
        "Cyan" to Color.Cyan,
        "Magenta" to Color.Magenta,
        "Gray" to Color.Gray,
        "Light Gray" to Color.LightGray
    )

    Row(
        modifier = Modifier
            .padding(vertical = 8.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(color = textColor, text = label, fontSize = 14.sp, modifier = Modifier.weight(1f))
        CustomDottedLine(modifier = Modifier.weight(1f))
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(color, shape = RoundedCornerShape(4.dp))
                .border(BorderStroke(1.dp, Color.White))
                .clickable { expanded = true }
        )

        if (expanded) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(backgroundColor)
                    .clickable { expanded = false },
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .background(backgroundColor)
                        .border(BorderStroke(1.dp, Color.White))
                        .padding(16.dp)
                        .width(IntrinsicSize.Max)
                ) {
                    colors.forEach { (name, colorOption) ->
                        DropdownMenuItem(
                            text = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .background(colorOption)
                                            .border(BorderStroke(1.dp, Color.White))
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(color = textColor, text = name)
                                }
                            },
                            onClick = {
                                onColorChange(colorOption)
                                expanded = false
                            }
                        )
                    }
                }
            }
        }
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
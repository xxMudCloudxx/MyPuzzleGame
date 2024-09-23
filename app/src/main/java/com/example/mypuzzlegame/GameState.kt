package com.example.mypuzzlegame

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.ui.graphics.ImageBitmap
import java.util.Collections.list

//使用viewModel存储状态

data class GameState(
    val puzzle: List<ImageBitmap?> = emptyList(),
    val orientations: List<Orientation> = List(puzzle.size) {Orientation.Horizontal},
    val isGameOver: Boolean = false,
    val partSize:Float = 0f,
    val puzzleNumber: Int = 3,
    val emptyIndex: Int = puzzleNumber * puzzleNumber - 1,
    val showPhoto: Boolean = false
)
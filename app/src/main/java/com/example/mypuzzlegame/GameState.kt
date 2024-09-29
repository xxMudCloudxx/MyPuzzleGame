package com.example.mypuzzlegame

import android.content.Context
import androidx.compose.ui.graphics.ImageBitmap

//使用viewModel存储状态

data class GameState(
    val puzzle: List<ImageBitmap?> = emptyList(),
    val randomPuzzle: List<ImageBitmap?> = emptyList(),
    val isGameOver: Boolean = false,
    val showPhoto: Boolean = false,
    val showDiffer: Boolean = false,
    val partSize:Float = 0f,
    val puzzleNumber: Int = 3,
    val image: String = "",
    val start: Int = 0,
    val showOption: Boolean = false,
    val screenSize: Int = 0,
)
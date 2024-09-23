package com.example.klotskigame

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb

fun createTransparentBitmap(width: Int, height: Int): ImageBitmap {
    val bitmap = android.graphics.Bitmap.createBitmap(width, height, android.graphics.Bitmap.Config.ARGB_8888)
    bitmap.eraseColor(Color.Transparent.toArgb())
    return bitmap.asImageBitmap()
}
package com.example.mypuzzlegame


import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.awaitTouchSlopOrCancellation
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.klotskigame.ImgPartSize
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun DraggableImage(
    bitmap: ImageBitmap,
    index: Int,
    onDragEnd: (fromIndex: Int, toIndex: Int) -> Unit,
    modifier: Modifier = Modifier,
    gameState: GameState,
    partSize: Dp
) {
    val size = gameState.puzzleNumber
    val density = LocalDensity.current

    var currentDragOffsetX by remember { mutableStateOf(0f) }
    var currentDragOffsetY by remember { mutableStateOf(0f) }
    var isDragging by remember { mutableStateOf(false) }
    Box(
        modifier = modifier
            .fillMaxSize()
            .offset {
                IntOffset(
                    currentDragOffsetX.roundToInt(),
                    currentDragOffsetY.roundToInt()
                )
            }
            .pointerInput(gameState.puzzleNumber) {
                awaitEachGesture {
                    if (isDragging) return@awaitEachGesture

                    val down = awaitFirstDown()
                    isDragging = true

                    awaitTouchSlopOrCancellation(down.id) { dragChange, _ ->
                        val xChange = dragChange.positionChange().x
                        val yChange = dragChange.positionChange().y

                        // 计算方向
                        val orientation = if (abs(xChange) > abs(yChange)) 0 else 1
                        // 移动逻辑
                        when (orientation) {
                            0 -> { // 水平移动
                                if (xChange > 0 && !cannotMoveRight(index, size) && !gameState.isGameOver) {
                                    onDragEnd(index, index + 1)
                                    //currentDragOffsetX += pieceSizePx
                                } else if (xChange < 0 && !cannotMoveLeft(index, size) && !gameState.isGameOver) {
                                    onDragEnd(index, index - 1)
                                    //currentDragOffsetX -= pieceSizePx
                                }
                            }
                            1 -> { // 垂直移动
                                if (yChange < 0 && !cannotMoveUp(index, size) && !gameState.isGameOver) {
                                    onDragEnd(index, index - size)
                                    //currentDragOffsetY += pieceSizePx
                                } else if (yChange > 0 && !cannotMoveDown(index, size) && !gameState.isGameOver) {
                                    onDragEnd(index, index + size)
                                    //currentDragOffsetY -= pieceSizePx
                                }
                            }
                        }

                        // 只在移动后重置偏移
                        if (xChange != 0f || yChange != 0f) {
                            dragChange.consume()
                        }
                    }

                    // 拖动结束，重置状态
                    isDragging = false
                    currentDragOffsetX = 0f
                    currentDragOffsetY = 0f
                }
            }
    ) {
        Image(
            bitmap = bitmap,
            contentDescription = null,
            modifier = Modifier.fillMaxSize().clipToBounds()
        )
    }
}


fun cannotMoveUp(index: Int, size: Int): Boolean {
    return index < size
}

fun cannotMoveDown(index: Int, size: Int): Boolean {
    return (index >= size * (size - 1))
}

fun cannotMoveLeft(index: Int, size: Int): Boolean {
    return (index % size == 0)
}

fun cannotMoveRight(index: Int, size: Int): Boolean {
    return (index % size == size - 1)
}
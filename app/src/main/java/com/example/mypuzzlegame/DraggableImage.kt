package com.example.mypuzzlegame


import android.util.Log
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.awaitTouchSlopOrCancellation
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.core.util.toHalf
import androidx.wear.compose.material.ExperimentalWearMaterialApi
import androidx.wear.compose.material.FractionalThreshold
import androidx.wear.compose.material.rememberSwipeableState
import androidx.wear.compose.material.swipeable
import com.example.klotskigame.HalfPartSize
import com.example.klotskigame.ImgPartSize
import kotlin.math.absoluteValue
import kotlin.math.roundToInt

@Composable
fun DraggableImage(
    bitmap: ImageBitmap,
    index: Int,
    onDragEnd: (fromIndex: Int, toIndex: Int) -> Unit,
    updateOrientation: (emptyIndex: Int) -> Unit,
    size: Int = 3,
    modifier: Modifier = Modifier,
    gameState: GameState
) {
    val emptyIndex = gameState.emptyIndex
    val density = LocalDensity.current
    val pieceSizePx = with(density) { ImgPartSize.toPx() }

    var currentDragOffsetX by remember { mutableStateOf(0f) }
    var currentDragOffsetY by remember { mutableStateOf(0f) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .offset {
                // 使用当前拖动的偏移量调整拼图块位置
                // 确保偏移量不超出拼图块大小
                val limitedOffsetX = currentDragOffsetX.coerceIn(-pieceSizePx.toFloat(), pieceSizePx.toFloat())
                val limitedOffsetY = currentDragOffsetY.coerceIn(-pieceSizePx.toFloat(), pieceSizePx.toFloat())
                IntOffset(limitedOffsetX.roundToInt(), limitedOffsetY.roundToInt())
            }
            .pointerInput(Unit) {
                // 手势处理逻辑
                awaitPointerEventScope {
                    // 等待用户按下触摸屏幕
                    val down = awaitFirstDown()
                    Log.d("DragGesture", "User touched down at position: ${down.position}")

                    // 检测是否满足拖动条件（超过触摸阈值）
                    var drag: PointerInputChange? = null

                    // 检测触摸的移动情况
                    awaitTouchSlopOrCancellation(down.id) { change, _ ->
                        // 确定是水平还是垂直拖动
                        drag = change
                        Log.d("DragGesture", "Movement detected, position change: ${change.positionChange()}")
                    }

                    // 如果发生拖动
                    drag?.let {
                        if (isEmptyAdjacent(index, emptyIndex, size)) {
                            // 水平拖动
                            if (canMoveLeft(index, emptyIndex, size) || canMoveRight(index, emptyIndex, size)) {
                                drag(down.id) { dragChange ->
                                    if (dragChange.positionChange().x <= pieceSizePx || dragChange.positionChange().x >= -pieceSizePx) {
                                        currentDragOffsetX += dragChange.positionChange().x
                                        Log.d("DragGesture", "Horizontal drag offset: $currentDragOffsetX")
                                    }

                                }
                            } else if (canMoveUp(index, emptyIndex, size) || canMoveDown(index, emptyIndex, size)) {
                                Log.d("DragGesture", "Vertical drag detected.")
                                // 垂直拖动
                                drag(down.id) { dragChange ->
                                    if (dragChange.positionChange().y <= pieceSizePx || dragChange.positionChange().y >= -pieceSizePx) {
                                        currentDragOffsetY += dragChange.positionChange().y
                                        Log.d(
                                            "DragGesture",
                                            "Vertical drag offset: $currentDragOffsetY"
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 拖动结束后触发逻辑
                    if (isEmptyAdjacent(index, emptyIndex, size)) {
                        Log.d("end DragGesture", "Drag completed. Triggering onDragEnd. From index: $index, Empty index: $emptyIndex")
                        // 限制拖动的最大偏移量
                        currentDragOffsetX = currentDragOffsetX.coerceIn(-pieceSizePx.toFloat(), pieceSizePx.toFloat())
                        currentDragOffsetY = currentDragOffsetY.coerceIn(-pieceSizePx.toFloat(), pieceSizePx.toFloat())

                        onDragEnd(index, emptyIndex)
                    }

                    // 重置偏移
                    Log.d("final DragGesture", "Resetting offsets after drag end.")
                    currentDragOffsetX = 0f
                    currentDragOffsetY = 0f
                }
            }
    ) {
        Image(
            bitmap = bitmap,
            contentDescription = null,
            modifier = Modifier.fillMaxSize()
        )
    }
}

fun isEmptyAdjacent(index: Int, emptyIndex: Int, size: Int): Boolean {
    // 计算行列数
    val gridSize = size

    // 水平方向相邻
    val isAdjacentHorizontally = (index % gridSize != 0 && index - 1 == emptyIndex) ||
            ((index + 1) % gridSize != 0 && index + 1 == emptyIndex)

    // 垂直方向相邻
    val isAdjacentVertically = index - gridSize == emptyIndex ||
            index + gridSize == emptyIndex

    return isAdjacentHorizontally || isAdjacentVertically
}

fun canMoveRight(index: Int, emptyIndex: Int, size: Int): Boolean {
    // 检查空位是否在右边
    return (index == emptyIndex - 1 && index / size == emptyIndex / size)
}

fun canMoveLeft(index: Int, emptyIndex: Int, size: Int): Boolean {
    // 检查空位是否在左边
    return (index == emptyIndex + 1 && index / size == emptyIndex / size)
}

fun canMoveUp(index: Int, emptyIndex: Int, size: Int): Boolean {
    // 检查空位是否在上边
    return (index == emptyIndex + size)
}

fun canMoveDown(index: Int, emptyIndex: Int, size: Int): Boolean {
    // 检查空位是否在下边
    return (index == emptyIndex - size)
}
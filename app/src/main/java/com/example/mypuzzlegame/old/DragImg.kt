package com.example.klotskigame

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

//@Composable
//fun DraggableImage(
//    bitmap: ImageBitmap,
//    index: Int,
//    emptyIndex: Int,
//    onDragEnd: (fromIndex: Int, toIndex: Int) -> Unit,
//    size: Int = 3,
//    modifier: Modifier = Modifier
//) {
//    val density = LocalDensity.current
//    val pieceSizePx = with(density) { ImgPartSize.toPx() }
//
//    // Current offset for drag movement
//    var offset by remember { mutableStateOf(Offset.Zero) }
//
//    // Determine if the tile is adjacent to the empty space
//    val isEmptyAdjacent = (index == emptyIndex - 1 && index / size == emptyIndex / size) ||
//            (index == emptyIndex + 1 && index / size == emptyIndex / size) ||
//            (index == emptyIndex - size) ||
//            (index == emptyIndex + size)
//
//    // Calculate movement boundaries
//    val boundaryX = if (index / size == emptyIndex / size) pieceSizePx else 0f // horizontal boundary
//    val boundaryY = if (index % size == emptyIndex % size) pieceSizePx else 0f // vertical boundary
//
//    Box(
//        modifier = modifier
//            .fillMaxSize()
//            .offset { IntOffset(offset.x.roundToInt(), offset.y.roundToInt()) }
//            .pointerInput(Unit) {
//                detectDragGestures(
//                    onDragEnd = {
//                        // Check if drag exceeds 50% of the piece size
//                        if (isEmptyAdjacent && (abs(offset.x) > pieceSizePx / 2 || abs(offset.y) > pieceSizePx / 2)) {
//                            onDragEnd(index, emptyIndex)
//                        }
//                        // Reset the offset after drag ends
//                        offset = Offset.Zero
//                    },
//                    onDrag = { change, dragAmount ->
//                        change.consume() // Consume the drag event
//
//                        // Apply the drag amount, but clamp it within the boundaries
//                        val newOffsetX = offset.x + dragAmount.x
//                        val newOffsetY = offset.y + dragAmount.y
//
//                        offset = Offset(
//                            x = max(-boundaryX, min(newOffsetX, boundaryX)),
//                            y = max(-boundaryY, min(newOffsetY, boundaryY))
//                        )
//                    }
//                )
//            }
//    ) {
//        Image(
//            bitmap = bitmap,
//            contentDescription = null,
//            modifier = Modifier.fillMaxSize()
//        )
//    }
//}

//
//import android.util.Log
//import androidx.compose.animation.core.tween
//import androidx.compose.foundation.ExperimentalFoundationApi
//import androidx.compose.foundation.Image
//import androidx.compose.foundation.gestures.AnchoredDraggableState
//import androidx.compose.foundation.gestures.DraggableAnchors
//import androidx.compose.foundation.gestures.Orientation
//import androidx.compose.foundation.gestures.anchoredDraggable
//import androidx.compose.foundation.interaction.MutableInteractionSource
//import androidx.compose.foundation.layout.Box
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.offset
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.LaunchedEffect
//import androidx.compose.runtime.SideEffect
//import androidx.compose.runtime.remember
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.ImageBitmap
//import androidx.compose.ui.platform.LocalDensity
//import androidx.compose.ui.unit.IntOffset
//import kotlin.math.roundToInt
//
//enum class DragAnchors {
//    Start, End
//}
//
//@OptIn(ExperimentalFoundationApi::class)
//@Composable
//fun DraggableImage(
//    bitmap: ImageBitmap,
//    index: Int,
//    emptyIndex: Int,
//    onDragEnd: (fromIndex: Int, toIndex: Int) -> Unit,
//    updateOrientation: (emptyIndex: Int, gridSize: Int) -> Unit,
//    size: Int = 3,
//    orientation: Orientation = if (index % size == emptyIndex % size) Orientation.Vertical else Orientation.Horizontal,
//    modifier: Modifier = Modifier
//) {
//    val density = LocalDensity.current
//    val pieceSizePx = with(density) { ImgPartSize.toPx() }
//    val halfPieceSizePx = with(density) { HalfPartSize.toPx() }
//
//    // 计算空位置和当前块是否相邻
//    val isEmptyAdjacent = (index == emptyIndex - 1 && index / size == emptyIndex / size) ||
//            (index == emptyIndex + 1 && index / size == emptyIndex / size) ||
//            (index == emptyIndex - size) ||
//            (index == emptyIndex + size)
//
//    val state = remember {
//        AnchoredDraggableState(
//            initialValue = DragAnchors.Start,
//            positionalThreshold = { totalDistance -> totalDistance * 0.5f },
//            velocityThreshold = { halfPieceSizePx },
//            animationSpec = tween(),
//            confirmValueChange = { newValue -> true },
//        )
//    }
//
//    // 动态设置 orientation
//    //var orientation by remember { mutableStateOf(if (index % size == emptyIndex % size) Orientation.Vertical else Orientation.Horizontal) }
//
//    SideEffect {
//        updateOrientation(emptyIndex, size)
//        // 更新锚点信息
//        state.updateAnchors(
//            DraggableAnchors {
//                DragAnchors.Start at 0f
//                DragAnchors.End at when {
//                    index == emptyIndex - 1 && index / size == emptyIndex / size -> pieceSizePx // 右移
//                    index == emptyIndex + 1 && index / size == emptyIndex / size -> -pieceSizePx // 左移
//                    index == emptyIndex - size -> pieceSizePx // 下移
//                    index == emptyIndex + size -> -pieceSizePx // 上移
//                    else -> 0f
//                }
//            }
//        )
//    }
//
//    LaunchedEffect(state.currentValue) {
//        if (state.currentValue == DragAnchors.End && isEmptyAdjacent && index != emptyIndex) {
//            onDragEnd(index, emptyIndex)
//            updateOrientation(emptyIndex, size)
//
//            Log.d("DraggableImage", "Drag ended. Index: $index, EmptyIndex: $emptyIndex, Orientation: $orientation")
//        }
//    }
//
////    LaunchedEffect(emptyIndex) {
////        updateOrientation(emptyIndex, 3)
////    }
//
//    Box(modifier = modifier) {
//        val interactionSource = remember { MutableInteractionSource() }
//
//        Image(
//            bitmap = bitmap,
//            contentDescription = null,
//            modifier = Modifier
//                .fillMaxSize()
//                .offset {
//                    val offsetX = if (emptyIndex / size == index / size) state.requireOffset().roundToInt() else 0
//                    val offsetY = if (emptyIndex % size == index % size) state.requireOffset().roundToInt() else 0
//                    IntOffset(offsetX, offsetY)
//                }
//                .anchoredDraggable(
//                    state = state,
//                    orientation = orientation, // 动态设置 orientation
//                    interactionSource = interactionSource
//                )
//        )
//    }
//}

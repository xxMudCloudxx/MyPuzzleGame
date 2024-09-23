package com.example.mypuzzlegame

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.ui.graphics.ImageBitmap

sealed class GameEvent {
    data class pizzlePart(val newPieces:  List<ImageBitmap?>, val size: Float): GameEvent()
    data class move(val fromIndex: Int, val toIndex: Int, val none:ImageBitmap?): GameEvent()        //移动图块
    object restart: GameEvent()     //重新开始游戏
    object setDifficulty: GameEvent()   //设置难度
    object over: GameEvent()
    object start: GameEvent()
    data class updateOrientations(val emptyIndex: Int): GameEvent()
    data class update(val list:  List<ImageBitmap?>) : GameEvent()
    object showFullImage: GameEvent()
}
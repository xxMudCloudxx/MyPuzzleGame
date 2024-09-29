package com.example.mypuzzlegame

import android.content.Context
import androidx.compose.ui.graphics.ImageBitmap

sealed class GameEvent {
    data class PuzzlePart(val context: Context) : GameEvent()
    data class Move(val fromIndex: Int, val toIndex: Int): GameEvent()        //移动图块
    data class Restart(val context: Context) : GameEvent()     //重新开始游戏
    data class SetDifficulty(val grid: Int, val context: Context): GameEvent()   //设置难度
    data object Over: GameEvent()
    data object Set: GameEvent()
    data class GetSize(val int: Int) : GameEvent()
    data class Update(val list:  List<ImageBitmap?>) : GameEvent()
    data object ShowFullImage: GameEvent()
    data object ShowDifficulty: GameEvent()
    data class PhotoReset(val photo: String, val context: Context): GameEvent()
}
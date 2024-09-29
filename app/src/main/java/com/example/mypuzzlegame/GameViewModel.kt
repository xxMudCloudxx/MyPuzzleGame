package com.example.mypuzzlegame

import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.ImageBitmap
import androidx.lifecycle.ViewModel
import java.util.Collections.shuffle

class GameViewModel: ViewModel() {
    var state by mutableStateOf(GameState())
    private val image = R.drawable.main
    private val none = R.drawable.none

    fun onEvents(event: GameEvent) {
        when(event) {
            is GameEvent.Move -> switchPart(event.fromIndex, event.toIndex)
            is GameEvent.Restart -> restartGame(event.context)
            is GameEvent.Over -> gameOver()
            is GameEvent.SetDifficulty -> setDiff(event.grid, event.context)
            is GameEvent.PuzzlePart -> gameStart(event.context)
            is GameEvent.GetSize -> state = state.copy(screenSize = event.int)
            is GameEvent.Update -> updatePizzle(event.list)
            is GameEvent.ShowFullImage -> showFullImage()
            is GameEvent.ShowDifficulty -> showDifficulty()
            is GameEvent.PhotoReset -> photoReset(event.photo, event.context)
            GameEvent.Set -> SetOption()
        }
//        Log.d("size", "${state.puzzleNumber}")
        checkEnd()
    }

    private fun photoReset(photo: String, context: Context) {
//        Log.d("PhotoReset", "开始处理照片: $photo")

        splitImageAsync(context = context, imageUrl = photo, n = state.puzzleNumber) { result ->
            val newPieces = result.first.toList()
            val newPartSize = result.second.toFloat()
            val list = newPieces.toMutableList()
            shuffle(list)

            // 添加日志以确认状态更新
//            Log.d("PhotoReset", "切割后的拼图数量: ${newPieces.size}, 新的部分大小: $newPartSize")

            // 更新状态
            state = state.copy(
                puzzle = list,
                randomPuzzle = newPieces,
                partSize = newPartSize,
                isGameOver = false,
                image = photo,
                start = 1 // 将 start 也放入状态更新中
            )

            // 日志确认状态更新
//            Log.d("PhotoReset", "状态已更新: $state")
        }
    }

    private fun SetOption() {
        if (state.isGameOver && !state.showOption && state.start == 1) return
        state = state.copy(
            isGameOver = !state.isGameOver,
            showOption = !state.showOption
        )
    }

    private fun updatePizzle(list: List<ImageBitmap?>) {
        state = state.copy(puzzle = list)
    }

    private fun gameStart(context: Context) {
        splitImageAsync(context = context, painterResource = image, n = state.puzzleNumber) { result ->
            val newPieces = result.first.toList()
            val newPartSize = result.second.toFloat()
            val list = newPieces.toMutableList()
            shuffle(list)
            state = state.copy(
                puzzle = list,
                randomPuzzle = newPieces,
                partSize = newPartSize,
                isGameOver = false,
            )
        }
        state = state.copy(start = 1)
    }

    private fun setDiff(grid: Int, context: Context) {
        state = state.copy(puzzleNumber = grid)
        if (state.image.isNotEmpty()) {
            photoReset(state.image, context)
        } else
            gameStart(context)
    }

    private fun checkEnd() {
        gameOver()
    }

    private fun gameOver() {
        //Log.d("checkEnd", "checkEnd: ${state.puzzle == state.randomPuzzle} && ${state.start} == 1")
        if (state.puzzle == state.randomPuzzle && !state.isGameOver && state.start == 1) {
            state = state.copy(isGameOver = !state.isGameOver)
        }
    }

    private fun restartGame(context: Context) {
        state = state.copy(
            isGameOver = false,
        )
        if (state.image.isNotEmpty()) {
            photoReset(state.image, context)
        } else
            gameStart(context)
    }


    private fun switchPart(fromIndex: Int, toIndex: Int) {

        if (toIndex < 0 || toIndex > state.puzzleNumber * state.puzzleNumber - 1) return
        // 确保只有当 toIndex 为当前 emptyIndex 时才进行移动
        val list = state.puzzle.toMutableList()
        val temp = list[toIndex]
        list[toIndex] = list[fromIndex]
        list[fromIndex] = temp
        state = state.copy(puzzle = list)

    }

    private fun showFullImage() {
        state = state.copy(showPhoto = !state.showPhoto)
    }

    private fun showDifficulty() {
        state = state.copy(showDiffer = !state.showDiffer)
    }

}
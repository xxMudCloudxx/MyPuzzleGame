package com.example.mypuzzlegame

import android.util.Log
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.ImageBitmap
import androidx.lifecycle.ViewModel

class GameViewModel: ViewModel() {
    var state by mutableStateOf(GameState())

    fun onEvents(event: GameEvent) {
        if (state.puzzle.size == state.puzzleNumber * state.puzzleNumber)
            updateOrientationsAroundEmpty(state.emptyIndex)
        when(event) {
            is GameEvent.move -> movePart(event.fromIndex, event.toIndex, event.none)
            is GameEvent.restart -> restartGame()
            is GameEvent.over -> gameOver()
            is GameEvent.setDifficulty -> setDiff()
            is GameEvent.pizzlePart -> gameStart(event.newPieces, event.size)
            is GameEvent.start -> TODO()
            is GameEvent.update -> updatePizzle(event.list)
            is GameEvent.updateOrientations -> updateOrientationsAroundEmpty(event.emptyIndex)
            GameEvent.showFullImage -> showFullImage()
        }
        Log.d("empty", "emptyIndex: ${state.emptyIndex}")
    }

    private fun updatePizzle(list: List<ImageBitmap?>) {
        state = state.copy(puzzle = list)
    }

    private fun gameStart(newPieces: List<ImageBitmap?>, newPartSize: Float) {
        val orientations: List<Orientation> = newPieces.mapIndexed { index, _ ->
            if (index % state.puzzleNumber == state.emptyIndex % state.puzzleNumber) {
                // 与空块在同一列，设置为垂直方向
                Orientation.Vertical
            } else {
                // 与空块不在同一列，设置为水平方向
                Orientation.Horizontal
            }
        }
        state = state.copy(
            puzzle = newPieces,
            partSize = newPartSize,
            orientations = orientations
        )
    }

    private fun setDiff() {
        TODO("Not yet implemented")
    }

    private fun gameOver() {
        TODO("Not yet implemented")
    }

    private fun restartGame() {
        TODO("Not yet implemented")
    }

    private fun movePart(fromIndex: Int, toIndex: Int, none:  ImageBitmap?) {
        val list = state.puzzle.toMutableList()
        list[toIndex] = list[fromIndex]
        list[fromIndex] = none
        state = state.copy(puzzle = list, emptyIndex = fromIndex)
    }

    private fun updateOrientationsAroundEmpty(emptyIndex: Int){
        val gridSize = state.puzzleNumber
        val updatedPuzzle = state.puzzle
        val orientations = state.orientations.toMutableList()
        val emptyRow = emptyIndex / gridSize
        val emptyCol = emptyIndex % gridSize
        Log.d("UpdateOrientation", "Updating orientations around empty index: $emptyIndex, gridSize: $gridSize")

        if (orientations.size != updatedPuzzle.size) {
            Log.e("UpdateOrientation", "Orientation list size does not match puzzle size.")
            return  // 防止越界
        }
            for (i in updatedPuzzle.indices) {
                val row = i / gridSize
                val col = i % gridSize
                orientations[i] = if (row == emptyRow) {
                    Orientation.Horizontal
                } else if (col == emptyCol) {
                    Orientation.Vertical
                } else {
                    orientations[i]
                }
            }
        Log.d("UpdateOrientation", "Updated puzzle orientations: $orientations")
        state = state.copy(puzzle = updatedPuzzle, orientations = orientations)
    }

    private fun showFullImage() {
        state = state.copy(showPhoto = !state.showPhoto)
    }
}
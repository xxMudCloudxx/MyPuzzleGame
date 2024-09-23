package com.example.mypuzzlegame

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box

import androidx.compose.foundation.layout.Column

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.klotskigame.BUttonVer
import com.example.klotskigame.ButtonColor
import com.example.klotskigame.ButtonHor
import com.example.klotskigame.ImgPartSize
import com.example.klotskigame.InTextColor
import com.example.klotskigame.PaddingVal
import com.example.klotskigame.TextColor
import com.example.klotskigame.backgroundColor
import com.example.klotskigame.barIntextSize
import com.example.klotskigame.barPaddingHeight
import com.example.klotskigame.barSize
import com.example.mypuzzlegame.ui.theme.MyPuzzleGameTheme
import com.example.mypuzzlegame.ui.theme.Pink80
import com.example.mypuzzlegame.ui.theme.Purple20

@Composable
fun GameFace(modifier: Modifier = Modifier) {
    val viewModel = viewModel<GameViewModel>()
    val state = viewModel.state
    Column(modifier = modifier.padding(top = 64.dp)) {
        Bar(modifier, state, onEvents = viewModel::onEvents)
        DisplaySplitImages(modifier, state, onEvents = viewModel::onEvents)
    }
    if (state.showPhoto) {
        Column(modifier = Modifier.background(color = backgroundColor)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.main),
                    contentDescription = "Show image",
                    contentScale = ContentScale.FillWidth,
                    modifier = Modifier.fillMaxWidth(),
                )

            }
        }
    }
}

@Composable
fun DisplaySplitImages(modifier: Modifier = Modifier,
                       state: GameState,
                       onEvents: (GameEvent) -> Unit) {


    val n = state.puzzleNumber
    var flag by rememberSaveable { mutableStateOf(1) }
    val image = R.drawable.main
    val emptyIndex = state.emptyIndex
    // 避免 pieces 未初始化时的崩溃
    var none by rememberSaveable { mutableStateOf<ImageBitmap?>(null) }

    // 生成空图像
    SplitImageComposable(R.drawable.wait, 1) { result ->
        none = result.first[0]
    }

    // 初始化图像拼块
    SplitImageComposable(painterResource = image, n = n) { result ->
        val newPieces = result.first.toList()
        val newPartSize = result.second.toFloat()
        onEvents(GameEvent.pizzlePart(newPieces, newPartSize))
    }

    if (state.puzzle.isNotEmpty() && flag == 1) {
        val updatedPuzzle = state.puzzle.toMutableList()
        if (none != null) {
            flag = 0
            updatedPuzzle[n * n - 1] = none!!
            // 调用事件更新 ViewModel 的状态
            onEvents(GameEvent.update(updatedPuzzle))
        }
    }

    Log.d("flag", "flag: $flag")

    val onDragEnd: (fromIndex: Int, toIndex: Int) -> Unit = { fromIndex, toIndex ->
        onEvents(GameEvent.move(fromIndex, toIndex, none))
    }

    val updateOrientationsAroundEmpty: (emptyIndex: Int) -> Unit = { emptyIndex ->
        if (state.puzzle.size == state.puzzleNumber * state.puzzleNumber)
        onEvents(GameEvent.updateOrientations(emptyIndex))
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = PaddingVal * 3),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 只有当 pieces 已经初始化后再渲染网格
        if (state.puzzle.isNotEmpty()) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(n),
                modifier = Modifier.size(ImgPartSize * n + 4.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                items(state.puzzle.size, key = { it }) { index ->
                    state.puzzle[index]?.let { bitmap ->
                        DraggableImage(
                            bitmap = bitmap,
                            index = index,
                            onDragEnd = onDragEnd,
                            modifier = Modifier.size(ImgPartSize),
                            updateOrientation = updateOrientationsAroundEmpty,
                            gameState = state
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun Bar(modifier: Modifier = Modifier, state: GameState, onEvents: (GameEvent) -> Unit) {
    Row(
        modifier
            .padding(PaddingVal)
            .height(barPaddingHeight)
    ) {
        Column(
            modifier = modifier.align(Alignment.Bottom),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            Text(
                text = "拼图",
                style = TextStyle(
                    fontSize = barSize,
                    fontWeight = FontWeight.Bold,
                    color = TextColor
                )
            )
            ElevatedButton(
                onClick = {},
                colors = ButtonColors(
                    containerColor = ButtonColor,
                    contentColor = InTextColor,
                    disabledContentColor = ButtonColor,
                    disabledContainerColor = InTextColor,
                ),
                contentPadding = PaddingValues(
                    start = ButtonHor,
                    end = ButtonHor,
                    top = BUttonVer,
                    bottom = BUttonVer
                )
            ) {
                Text(
                    text = "NEW GAME",
                    style = TextStyle(fontSize = barIntextSize, fontWeight = FontWeight.Bold),

                    )
            }
        }
        Spacer(modifier = modifier.padding(PaddingVal))
        val img = painterResource(id = R.drawable.main)
        Box(modifier) {
            Image(
                painter = img,
                contentDescription = "Show image",
                contentScale = ContentScale.FillWidth,
                modifier = Modifier.clickable { onEvents(GameEvent.showFullImage) }
            )
        }

    }
}

@Preview(showBackground = true)
@Composable
fun MyPuzzleGame() {
    MyPuzzleGameTheme {
        GameFace()
    }
}
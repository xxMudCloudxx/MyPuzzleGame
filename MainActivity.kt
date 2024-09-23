package com.example.klotskigame
import SplitImageComposable

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

import androidx.compose.foundation.Image
import android.util.Log
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size

import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid

import androidx.compose.material3.ButtonColors

import androidx.compose.material3.ElevatedButton

import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.klotskigame.ui.theme.KlotskiGameTheme
import com.example.klotskigame.ui.theme.*
import androidx.compose.ui.platform.LocalConfiguration


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            KlotskiGameTheme {
                val configuration = LocalConfiguration.current

                val screenHeight = configuration.screenHeightDp.dp
                val screenWidth = configuration.screenWidthDp.dp
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = backgroundColor
                ) {
                    GameFace()
                }
            }
        }
    }
}

@Composable
fun GameFace(modifier: Modifier = Modifier) {
    Column {
        Bar(modifier)
        DisplaySplitImages(modifier)
    }
}

@Composable
fun Body(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = backgroundColor2
    ) {
        DisplaySplitImages(modifier)
    }
}

@Composable
fun DisplaySplitImages(modifier: Modifier = Modifier) {
    val n = 3
    val image = R.drawable.main

    // 避免 pieces 未初始化时的崩溃
    var pieces by rememberSaveable { mutableStateOf<List<ImageBitmap?>>(emptyList()) }
    var PartSize by rememberSaveable { mutableStateOf(0f) }
    var none by rememberSaveable { mutableStateOf<ImageBitmap?>(null) }
    var orientations by rememberSaveable { mutableStateOf(MutableList(pieces.size) { Orientation.Horizontal }) }

    // 生成空图像
    SplitImageComposable(R.drawable.none, 1) { result ->
        none = result.first[0]
    }

    // 初始化图像拼块
    SplitImageComposable(painterResource = image, n = n) { result ->
        pieces = result.first.toList()
        PartSize = result.second.toFloat()

        // 初始化 orientations
        orientations = MutableList(pieces.size) { Orientation.Horizontal }
    }

    var emptyIndex by rememberSaveable { mutableStateOf(n * n - 1) }



    // 更新相邻方块方向
    val updateOrientationsAroundEmpty: (emptyIndex: Int, gridSize: Int) -> Unit = { emptyIndex, gridSize ->
        val emptyRow = emptyIndex / gridSize
        val emptyCol = emptyIndex % gridSize
        Log.d("UpdateOrientation", "Updating orientations around empty index: $emptyIndex, gridSize: $gridSize")
            for (i in pieces.indices) {
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
        Log.d("UpdateOrientation", "Updated orientations: $orientations")
    }

    // 更新方块交换逻辑
    val onDragEnd: (fromIndex: Int, toIndex: Int) -> Unit = { fromIndex, toIndex ->
        pieces = pieces.toMutableList().apply {
            swap(fromIndex, toIndex)
        }
        emptyIndex = fromIndex
        updateOrientationsAroundEmpty(emptyIndex, n)
    }

    // 检查 pieces 是否已初始化，避免未初始化时操作
    if (pieces.isNotEmpty()) {
        pieces = pieces.toMutableList().apply {
            this[emptyIndex] = none
        }
    }
    // LaunchedEffect 只在 pieces 初始化和 emptyIndex 改变时更新方向
    LaunchedEffect(emptyIndex, pieces.size) {
        if (pieces.isNotEmpty()) {
            updateOrientationsAroundEmpty(emptyIndex, n)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = PaddingVal * 3),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 只有当 pieces 已经初始化后再渲染网格
        if (pieces.isNotEmpty()) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(n),
                modifier = Modifier.size(ImgPartSize * n + 4.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                items(pieces.size, key = { it }) { index ->
                    pieces[index]?.let { bitmap ->
                        DraggableImage(
                            bitmap = bitmap,
                            index = index,
                            emptyIndex = emptyIndex,
                            onDragEnd = onDragEnd,
                            modifier = Modifier.size(ImgPartSize)
                        )
                    }
                }
            }
        }
    }
}


fun MutableList<ImageBitmap?>.swap(fromIndex: Int, toIndex: Int) {
    val temp = this[toIndex]
    this[toIndex] = this[fromIndex]
    this[fromIndex] = temp
}

// 判断方块是否可以移动：仅相邻方块才可以移动
fun canMove(fromIndex: Int, toIndex: Int, gridSize: Int): Boolean {
    val fromRow = fromIndex / gridSize
    val fromCol = fromIndex % gridSize
    val toRow = toIndex / gridSize
    val toCol = toIndex % gridSize

    val isAdjacent = (fromRow == toRow && kotlin.math.abs(fromCol - toCol) == 1) ||
            (fromCol == toCol && kotlin.math.abs(fromRow - toRow) == 1)

    return isAdjacent
}



@Composable
fun Bar(modifier: Modifier = Modifier) {
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
                contentPadding = PaddingValues(start = ButtonHor, end = ButtonHor, top = BUttonVer, bottom = BUttonVer)
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
            )
        }
    }
}


@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    KlotskiGameTheme {
        DisplaySplitImages()
    }
}
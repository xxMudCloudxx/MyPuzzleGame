package com.example.mypuzzlegame

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box

import androidx.compose.foundation.layout.Column



import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import coil.compose.rememberImagePainter
import com.example.klotskigame.BUttonVer
import com.example.klotskigame.ButtonColor
import com.example.klotskigame.ButtonHor
import com.example.klotskigame.ImgPartSize
import com.example.klotskigame.InTextColor
import com.example.klotskigame.PaddingVal
import com.example.klotskigame.TextColor
import com.example.klotskigame.backgroundColor
import com.example.klotskigame.backgroundColor2
import com.example.klotskigame.barIntextSize
import com.example.klotskigame.barPaddingHeight
import com.example.klotskigame.barSize
import com.example.mypuzzlegame.ui.theme.MyPuzzleGameTheme
import com.example.mypuzzlegame.ui.theme.Pink80
import com.example.mypuzzlegame.ui.theme.Purple20
import kotlinx.coroutines.delay
import java.net.URL
var photoList = emptyList<String>()
@Composable
fun GameFace(modifier: Modifier = Modifier, onEvents: (GameEvent) -> Unit, state: GameState) {
    val context = LocalContext.current // 获取 Context
    val configuration = LocalConfiguration.current
    onEvents(GameEvent.GetSize(configuration.screenWidthDp))
    // screenWidthDp * 0.02f == 8.dp
    DisplaySplitImages(modifier, state, onEvents = onEvents)
    if (state.isGameOver) {
        GameOver(modifier = modifier, state = state, onEvents = onEvents, context = context)
    }
    if (state.showPhoto) {
        PhotoShow(onEvents, state)
    }
}

@Composable
private fun PhotoShow(onEvents: (GameEvent) -> Unit, state: GameState) {
    val context = LocalContext.current
    Column(modifier = Modifier.background(color = backgroundColor),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            contentAlignment = Alignment.Center,
        ) {
            // 使用 state.image 显示剪切后的图像
            val imagePainter = if (state.image.isNotEmpty()) {
                rememberAsyncImagePainter(state.image) // 使用加载 URI 的方法
            } else {
                painterResource(id = R.drawable.main) // 默认图像
            }
            Image(
                painter = imagePainter,
                contentDescription = "Show image",
                contentScale = ContentScale.FillWidth,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onEvents(GameEvent.ShowFullImage) },
            )
        }
        Column{
            Spacer(modifier = Modifier.size(6.dp))
            ImagePickerWithCrop( state = state ,onEvents = onEvents)
            RandomPhotoButton(onRandomPhotoSelected = { onEvents(GameEvent.PhotoReset(it, context)) })
        }
    }
}


@Composable
fun DisplaySplitImages(modifier: Modifier = Modifier,
                       state: GameState,
                       onEvents: (GameEvent) -> Unit) {
    val context = LocalContext.current // 获取 Context
    val puzzle = state.puzzle
    if (state.start == 0) {
        // 初始化图像拼块
        onEvents(GameEvent.PuzzlePart(context))
    }
    val onDragEnd: (fromIndex: Int, toIndex: Int) -> Unit = { fromIndex, toIndex ->
        onEvents(GameEvent.Move(fromIndex, toIndex))
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = (state.screenSize*0.12 * 1.5).dp, start = (state.screenSize*0.04).dp, end = (state.screenSize*0.04).dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
// 只有当 puzzle 已经初始化后再渲染网格
        //Spacer(modifier = Modifier.size((state.screenSize*0.03).dp))
        Bar(modifier, state, onEvents = onEvents, context = context)
        Spacer(modifier = Modifier.size((state.screenSize*0.15).dp))
        if (puzzle.isNotEmpty()) {
            // 创建 n 行
            Grig(puzzle, onDragEnd, state)
        }
        SetButton(state = state, onEvents = { onEvents(GameEvent.Set) }, context = context, text = "设置")
    }
}

@Composable
private fun Grig(
    puzzle: List<ImageBitmap?>,
    onDragEnd: (fromIndex: Int, toIndex: Int) -> Unit,
    state: GameState
) {
    val n = state.puzzleNumber
    val boxWidth = remember { mutableStateOf(0) } // 存储组件的宽度
    for (rowIndex in 0 until (puzzle.size + n - 1) / n) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .onGloballyPositioned { layoutCoordinates ->
                    boxWidth.value = layoutCoordinates.size.width / n
                },
            horizontalArrangement = Arrangement.spacedBy(2.dp), // 列间距
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 创建 n 列
            for (colIndex in 0 until n) {
                val index = rowIndex * n + colIndex
                if (index < puzzle.size) {
                    puzzle[index]?.let { bitmap ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                        ) {
                            DraggableImage(
                                bitmap = bitmap,
                                index = index,
                                onDragEnd = onDragEnd,
                                modifier = Modifier.fillMaxSize(),
                                gameState = state,
                                partSize = boxWidth.value.dp,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun Bar(modifier: Modifier = Modifier, state: GameState, onEvents: (GameEvent) -> Unit, context: Context) {
    Row(
        modifier

    ) {
        Column(
            modifier = modifier.align(Alignment.Top),
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
                onClick = {onEvents(GameEvent.Restart(context))},
                colors = ButtonColors(
                    containerColor = ButtonColor,
                    contentColor = InTextColor,
                    disabledContentColor = ButtonColor,
                    disabledContainerColor = InTextColor,
                ),
                contentPadding = PaddingValues(
                    start = (state.screenSize * 0.12).dp,
                    end = (state.screenSize * 0.12).dp,
                    top = (state.screenSize * 0.03).dp,
                    bottom = (state.screenSize * 0.03).dp
                ),
            ) {
                Text(
                    text = "重启游戏",
                    style = TextStyle(fontSize = (state.screenSize*0.05).sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp),

                    )
            }
        }
        Spacer(modifier = modifier.padding(PaddingVal))
        Box(modifier.weight(1f)) {
            val imagePainter = if (state.image.isNotEmpty()) {
                rememberAsyncImagePainter(state.image) // 使用加载 URI 的方法
            } else {
                painterResource(id = R.drawable.main) // 默认图像
            }
            Image(
                painter = imagePainter,
                contentDescription = "Show image",
                contentScale = ContentScale.FillWidth,
                modifier = Modifier.clickable { onEvents(GameEvent.ShowFullImage) }
            )
        }

    }
}


@Composable
fun GameOver(modifier: Modifier = Modifier, state: GameState, onEvents: (GameEvent) -> Unit, context: Context) {
    Box(modifier = Modifier
        .padding((state.screenSize*0.04).dp)
        .fillMaxWidth()
        .aspectRatio(1f),
        contentAlignment = Alignment.Center
    ) {
        val context = LocalContext.current
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    color = backgroundColor2.copy(alpha = 0.93f),
                    shape = RoundedCornerShape(28.dp)
                ),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (!state.showOption && state.isGameOver && state.puzzle == state.randomPuzzle) {
                Text(
                    text = "恭喜成功!",
                    style = TextStyle(
                        fontSize = (state.screenSize*0.06).sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                )
            }
            SetButton(
                state = state,
                context = context,
                text = "重新游戏",
                onEvents= {
                onEvents(GameEvent.Restart(context))
            } )
            SetButton(
                state = state,
                context = context,
                text = "更换图片",
                onEvents = { onEvents(GameEvent.ShowFullImage) })

            DifficultySelectionDialog(
                modifier = modifier,
                state = state,
                onDismiss = { onEvents(GameEvent.ShowDifficulty) },
                onSelect = { selectedDifficulty ->
                    onEvents(GameEvent.SetDifficulty(selectedDifficulty, context))
                }
            )

        }

    }
}

@Composable
private fun SetButton(
    state: GameState,
    onEvents: () -> Unit,
    context: Context,
    text: String
) {
    ElevatedButton(
        onClick = onEvents,
        colors = ButtonColors(
            containerColor = ButtonColor,
            contentColor = InTextColor,
            disabledContentColor = Pink80,
            disabledContainerColor = Purple20,
        ),
        contentPadding = PaddingValues(
            start = (state.screenSize * 0.12).dp,
            end = (state.screenSize * 0.12).dp,
            top = (state.screenSize * 0.03).dp,
            bottom = (state.screenSize * 0.03).dp
        ),
        modifier = Modifier.padding(top = (state.screenSize*0.02).dp)
    ) {
        Text(
            text = text,
            style = TextStyle(fontSize = (state.screenSize*0.05).sp, fontWeight = FontWeight.Bold),
        )
    }
}

@Composable
fun SetPhoto(modifier: Modifier = Modifier) {

}

@Composable
fun TimeAttack(modifier: Modifier = Modifier, timeInSec: Int) {
    var trigger by remember { mutableStateOf(timeInSec) }

    val elapsed by animateIntAsState(
        targetValue = trigger * 1000,
        animationSpec = tween(timeInSec * 1000, easing = LinearEasing)
    )

    DisposableEffect(Unit) {
        trigger = 0
        onDispose {  }
    }

    val (hou, min, sec) = remember(elapsed / 1000) {
        val elapsedInSec = elapsed / 1000
        val hou = elapsedInSec / 3600
        val min = elapsedInSec / 60 - hou * 60
        val sec = elapsedInSec % 60
        Triple(hou, min, sec)
    }

    //根据剩余时间设置字体大小
    val (size, labelSize) = when {
        hou > 0 -> 40.sp to 20.sp
        min > 0 -> 80.sp to 30.sp
        else -> 150.sp to 50.sp
    }

    val transition = rememberInfiniteTransition()
    //infiniteRepeatable+reverse实现文字缩放
    val animatedFont by transition.animateFloat(
        initialValue = 1.5f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(tween(500), RepeatMode.Reverse), label = ""
    )


    Row() {
        if (min > 0) {//剩余时间不足1分钟，不显示m
            Text(
                text = "$min:",
                style = TextStyle(
                    fontSize = size,

                    fontWeight = FontWeight.Bold,
                    color = TextColor
                )
            )
        }
        Text(
            text = "$sec",
            style = TextStyle(
                fontSize = size * (if (sec < 10 && min == 0) animatedFont else 1f),
                fontWeight = FontWeight.Bold,
                color = TextColor
            )
        )

    }
}

@Composable
fun DifficultySelectionDialog(
    modifier: Modifier,
    onDismiss: () -> Unit,
    onSelect: (Int) -> Unit,
    state: GameState
) {
    val difficultyOptions = listOf("简单: 3", "中等: 4", "困难: 5")
    val difficultyValues = listOf(3, 4, 5)
    var selectOption by remember { mutableStateOf("Option") }
    var selectedDifficulty by rememberSaveable { mutableIntStateOf(difficultyValues[0]) }
    var paddingSize by rememberSaveable { mutableIntStateOf(difficultyValues[0]) }
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        ElevatedButton(
            onClick = onDismiss,
            colors = ButtonColors(
                containerColor = ButtonColor,
                contentColor = InTextColor,
                disabledContentColor = Pink80,
                disabledContainerColor = Purple20,
            ),
            contentPadding = PaddingValues(
                start = (state.screenSize * 0.12).dp,
                end = (state.screenSize * 0.12).dp,
                top = (state.screenSize * 0.03).dp,
                bottom = (state.screenSize * 0.03).dp
            ),
            modifier = Modifier
                .padding(top = (state.screenSize*0.02).dp)
        ) {
            Text(
                text = "设置难度",
                style = TextStyle(fontSize = (state.screenSize*0.05).sp, fontWeight = FontWeight.Bold),
                modifier = Modifier
            )
        }

        DropdownMenu(
            expanded = state.showDiffer,
            onDismissRequest =  onDismiss,
            modifier = Modifier
                .background(color = backgroundColor2.copy(alpha = 0.94f))
                .padding(start = (state.screenSize * 0.12).dp, end = (state.screenSize * 0.015).dp)

        ) {
            difficultyOptions.forEachIndexed { index, option ->
                DropdownMenuItem(onClick = {
                    selectedDifficulty = difficultyValues[index]
                    selectOption = option
                    onSelect(selectedDifficulty)
                    onDismiss()
                },
                text = {
                    Text(text = option, color = TextColor)
                })
            }
            Button(
                onClick = onDismiss,
                colors = ButtonColors(
                containerColor = ButtonColor,
                contentColor = InTextColor,
                disabledContentColor = Pink80,
                disabledContainerColor = Purple20,
            ),) {
                Text(text = "取消", style =  TextStyle(fontWeight = FontWeight.Bold), )
            }
        }
    }




}

//@Preview(showBackground = true)
//@Composable
//fun MyPuzzleGame() {
//    MyPuzzleGameTheme {
//        val viewModel = viewModel<GameViewModel>()
//        val state = viewModel.state
//        //GameFace(state = state,onEvents = viewModel::onEvents)
//        val context = LocalContext.current // 获取 Context
//        GameOver(state = state, onEvents = viewModel::onEvents, context = context)
//    }
//}

@Preview(showBackground = true)
@Composable
fun Time() {
    MyPuzzleGameTheme {
        val viewModel = viewModel<GameViewModel>()
        val state = viewModel.state
        //GameFace(state = state,onEvents = viewModel::onEvents)
        val context = LocalContext.current // 获取 Context
        TimeAttack(timeInSec = 12)
    }
}


@Composable
fun ImagePickerWithCrop(state: GameState, onEvents: (GameEvent) -> Unit) {
    val context = LocalContext.current
    val cropLauncher = rememberLauncherForActivityResult(CropImage.instance) { result ->
        if (result.isSuccess) {
            // 处理剪切后的图片结果
            result.uri?.let { uri ->
                // 处理裁剪后的图片，比如显示或保存
                onEvents(GameEvent.PhotoReset(uri.toString(), context))
            }
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            val cropImageResult = CropImageResult(it)
            cropLauncher.launch(cropImageResult)
        }
    }

    Button(
        onClick = { galleryLauncher.launch("image/*") },
        colors = ButtonColors(
            containerColor = ButtonColor,
            contentColor = InTextColor,
            disabledContentColor = ButtonColor,
            disabledContainerColor = InTextColor,
        ),) {
        Text(
            text = "选择图片剪切",
            style = TextStyle(fontSize = barIntextSize, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp),
        )

    }
}

@Composable
fun RandomPhotoButton(onRandomPhotoSelected: (String) -> Unit) {
    val context = LocalContext.current
    if (photoList.isEmpty()) {
        val imageUri =  saveMainImageToCache(context, "1", R.drawable.main) // 替换为你的 drawable ID
        val imageUri1 = saveMainImageToCache(context, "2", R.drawable.main2) // 替换为你的 drawable ID
        val imageUri2 = saveMainImageToCache(context, "3", R.drawable.main3) // 替换为你的 drawable ID
        photoList = listOf(
            "$imageUri", // 替换为真实的照片 URI
            "$imageUri1",
            "$imageUri2",
            // 添加更多照片 URI
        )
    }

    Button(onClick = {
        // 随机选择一张照片
        val randomPhotoUri = photoList.random()
        onRandomPhotoSelected(randomPhotoUri)
    },
        colors = ButtonColors(
        containerColor = ButtonColor,
        contentColor = InTextColor,
        disabledContentColor = ButtonColor,
        disabledContainerColor = InTextColor,
    ),) {
        Text(text = "随机选择照片", style = TextStyle(fontSize = barIntextSize, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp),)
    }
}




//@Preview(showBackground = true)
//@Composable
//fun GameOver2() {
//    MyPuzzleGameTheme {
//        val viewModel = viewModel<GameViewModel>()
//        val state = viewModel.state
//        val context = LocalContext.current // 获取 Context
//        GameOver(state = state, onEvents = viewModel::onEvents, context = context)
//    }
//}
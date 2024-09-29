package com.example.mypuzzlegame

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL


fun splitImageAsync(
    context: Context,
    painterResource: Int,
    n: Int = 3,
    onImageSplit: (Pair<List<ImageBitmap>, Int>) -> Unit
) {
    // 使用协程启动异步操作
    CoroutineScope(Dispatchers.Main).launch {
        val pieces = splitImage(context, painterResource, n)
        onImageSplit(pieces)
    }
}

suspend fun splitImage(context: Context, painterResource: Int, n: Int = 3): Pair<List<ImageBitmap>, Int> {
    val image = decodeAsset(context, painterResource) ?: return Pair(emptyList(), 0)

    val pieces = mutableListOf<ImageBitmap>()
    var x = 0
    var y = 0
    val width = (image.width.toFloat() / n).toInt()
    val height = (image.height.toFloat() / n).toInt()

    for (i in 0 until n) {
        for (j in 0 until n) {
            val croppedImage = Bitmap.createBitmap(image, x, y, width, height)
            pieces.add(croppedImage.asImageBitmap())
            x += width
        }
        x = 0
        y += height
    }

    return Pair(pieces, width)
}

suspend fun decodeAsset(context: Context, painterResource: Int): Bitmap? {
    return withContext(Dispatchers.IO) {
        val inputStream = context.resources.openRawResource(painterResource)
        BitmapFactory.decodeStream(inputStream)
    }
}

fun splitImageAsync(
    context: Context,
    imageUrl: String,
    n: Int = 3,
    onImageSplit: (Pair<List<ImageBitmap>, Int>) -> Unit
) {
    Log.d("SplitImage", "开始切割图像")
    CoroutineScope(Dispatchers.Main).launch {
        val pieces = splitImage(context, imageUrl, n)
        onImageSplit(pieces)
    }
}

suspend fun splitImage(context: Context, imageUrl: String, n: Int = 3): Pair<List<ImageBitmap>, Int> {
    val image = downloadImage(context, imageUrl) ?: run {
        Log.e("SplitImage", "图像加载失败: $imageUrl")
        return Pair(emptyList(), 0)
    }

    Log.d("SplitImage", "开始切割图像，大小: ${image.width}x${image.height}, 切割数量: $n")

    val pieces = mutableListOf<ImageBitmap>()
    val width = (image.width.toFloat() / n).toInt()
    val height = (image.height.toFloat() / n).toInt()

    var x = 0
    var y = 0

    for (i in 0 until n) {
        for (j in 0 until n) {
            val croppedImage = Bitmap.createBitmap(image, x, y, width, height)
            pieces.add(croppedImage.asImageBitmap())
            Log.d("SplitImage", "切割部分 [$i, $j]: 大小 ${croppedImage.width}x${croppedImage.height}")
            x += width
        }
        x = 0
        y += height
    }

    Log.d("SplitImage", "切割完成，得到拼图数量: ${pieces.size}, 每部分大小: $width")
    return Pair(pieces, width)
}


suspend fun downloadImage(context: Context, imageUrl: String): Bitmap? {
    return withContext(Dispatchers.IO) {
        try {
            return@withContext if (imageUrl.startsWith("content://")) {
                val inputStream = context.contentResolver.openInputStream(Uri.parse(imageUrl))
                BitmapFactory.decodeStream(inputStream)
            } else {
                // 处理其他类型的 URL，例如 http(s)
                val url = URL(imageUrl)
                val connection = url.openConnection() as HttpURLConnection
                connection.doInput = true
                connection.connect()
                BitmapFactory.decodeStream(connection.inputStream)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}




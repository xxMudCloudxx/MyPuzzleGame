import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun SplitImageComposable(painterResource: Int, n: Int = 3, onImageSplit: (Pair<List<ImageBitmap>, Int>) -> Unit) {
    val context = LocalContext.current

    // 使用 LaunchedEffect 来处理异步操作
    LaunchedEffect(painterResource) {
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

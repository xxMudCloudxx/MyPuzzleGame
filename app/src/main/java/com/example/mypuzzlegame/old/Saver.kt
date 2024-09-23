package com.example.klotskigame

import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.SaverScope
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.drawable.toBitmap
import android.graphics.Bitmap
import java.io.ByteArrayOutputStream
import java.util.Base64
import android.graphics.BitmapFactory

// Convert ImageBitmap to ByteArray for saving
fun ImageBitmap?.toByteArray(): ByteArray? {
    return this?.asAndroidBitmap()?.let { bitmap ->
        ByteArrayOutputStream().use { stream ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            stream.toByteArray()
        }
    }
}

// Convert ByteArray back to ImageBitmap
fun ByteArray?.toImageBitmap(): ImageBitmap? {
    return this?.let {
        val bitmap = BitmapFactory.decodeByteArray(it, 0, size)
        bitmap?.asImageBitmap()
    }
}

// Custom Saver to handle List<ImageBitmap?>
val imageBitmapListSaver = Saver<List<ImageBitmap?>, List<ByteArray?>>(
    save = { list -> list.map { it.toByteArray() } },
    restore = { bytesList -> bytesList.map { it.toImageBitmap() } }
)
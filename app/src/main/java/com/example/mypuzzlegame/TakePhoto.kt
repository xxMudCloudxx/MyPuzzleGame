package com.example.mypuzzlegame

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContract
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.core.content.FileProvider
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID

class SelectPicture : ActivityResultContract<Unit?, PictureResult>() {

    private var context: Context? = null

    override fun createIntent(context: Context, input: Unit?): Intent {
        this.context = context
        return Intent(Intent.ACTION_PICK).setType("image/*")
    }

    override fun parseResult(resultCode: Int, intent: Intent?): PictureResult {
        return PictureResult(intent?.data, resultCode == Activity.RESULT_OK)
    }
}

class TakePhoto : ActivityResultContract<Unit?, PictureResult>() {

    var outUri: Uri? = null
    private var imageName: String? = null

    companion object {
        //定义单例的原因是因为拍照返回的时候页面会重新实例takePhoto，导致拍照的uri始终为空
        val instance get() = Helper.obj
    }

    private object Helper {
        val obj = TakePhoto()
    }

    override fun createIntent(context: Context, input: Unit?): Intent =
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { intent ->
            getFileDirectory(context)?.let {
                outUri = it
                intent.putExtra(MediaStore.EXTRA_OUTPUT, it).apply {
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                }
            }

        }

    override fun parseResult(resultCode: Int, intent: Intent?): PictureResult {
        return PictureResult(outUri, resultCode == Activity.RESULT_OK)
    }

    private fun getFileDirectory(context: Context): Uri? {//获取app内部文件夹
        imageName = "img_${UUID.randomUUID().toString().substring(0, 7)}"
        val fileFolder = File(context.cacheDir, "test_imgs")
        if (!fileFolder.exists()) {
            fileFolder.mkdirs()
        }
        val file = File(fileFolder, "${imageName}.jpeg")
        if (!file.exists()) {
            file.createNewFile()
        }
        return FileProvider.getUriForFile(context, "${context.packageName}.image.provider", file)
    }
}



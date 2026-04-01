package com.nikon.z5ii.ftp

import android.os.FileObserver
import android.util.Log
import java.io.File

/**
 * 监听指定目录的文件变化，捕获新上传的照片
 */
class PhotoObserver(
    private val directoryPath: String,
    private val onNewPhotoReceived: (String) -> Unit
) : FileObserver(directoryPath, CLOSE_WRITE) {

    private val TAG = "PhotoObserver"

    override fun onEvent(event: Int, path: String?) {
        if (path == null) return

        // 仅处理文件写入完成的事件
        if (event == CLOSE_WRITE) {
            // 过滤图片格式 (JPG, RAW 等)
            val extension = path.substringAfterLast('.', "").lowercase()
            if (extension == "jpg" || extension == "jpeg" || extension == "png" || extension == "nef") {
                val fullPath = "$directoryPath/$path"
                Log.i(TAG, "New photo received: $fullPath")
                onNewPhotoReceived(fullPath)
            }
        }
    }

    /**
     * 启动监听
     */
    fun start() {
        startWatching()
        Log.i(TAG, "Watching directory: $directoryPath")
    }

    /**
     * 停止监听
     */
    fun stop() {
        stopWatching()
        Log.i(TAG, "Stopped watching directory: $directoryPath")
    }
}

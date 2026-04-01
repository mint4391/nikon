package com.nikon.z5ii.ftp

import android.Manifest
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import coil.compose.AsyncImage
import coil.request.ImageRequest
import kotlinx.coroutines.flow.collectLatest
import java.io.File

class MainActivity : ComponentActivity() {
    private val viewModel: PhotoViewModel by viewModels()
    private lateinit var ftpManager: FtpServerManager
    private lateinit var photoObserver: PhotoObserver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. 请求权限 (Android 13+ 需要 READ_MEDIA_IMAGES)
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.INTERNET,
                Manifest.permission.ACCESS_WIFI_STATE,
                Manifest.permission.ACCESS_NETWORK_STATE
            ),
            100
        )

        // 2. 初始化 FTP 根目录 (使用公共图片目录，方便用户查看)
        val rootDir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "NikonZ5II")
        if (!rootDir.exists()) {
            val created = rootDir.mkdirs()
            if (!created) Log.e("MainActivity", "无法创建目录: ${rootDir.absolutePath}")
        }

        // 3. 启动 FTP 服务端
        ftpManager = FtpServerManager(this)
        ftpManager.startServer(port = 2121, rootPath = rootDir.absolutePath)

        // 4. 启动文件监控
        photoObserver = PhotoObserver(rootDir.absolutePath) { path ->
            viewModel.onPhotoReceived(path)
        }
        photoObserver.start()

        setContent {
            NikonViewerApp(viewModel)
        }
        
        Toast.makeText(this, "FTP 服务已启动，监听端口: 2121", Toast.LENGTH_LONG).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        ftpManager.stopServer()
        photoObserver.stop()
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NikonViewerApp(viewModel: PhotoViewModel) {
    val photos = viewModel.photos
    val currentIndex by viewModel.currentIndex

    // 使用 HorizontalPager 实现左右滑动查看历史图片
    val pagerState = rememberPagerState(pageCount = { photos.size })

    // 监听新图片到达，自动跳转到最后一页
    LaunchedEffect(photos.size) {
        if (photos.isNotEmpty()) {
            pagerState.animateScrollToPage(photos.size - 1)
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.Black // 摄影师偏好全黑背景
    ) {
        if (photos.isEmpty()) {
            // 空状态提示
            Box(contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "等待相机连接并传输照片...",
                        color = Color.White,
                        style = MaterialTheme.typography.titleLarge
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "FTP 端口: 2121\n账号: nikon / 密码: nikon",
                        color = Color.Gray,
                        style = MaterialTheme.typography.bodyMedium,
                        lineHeight = androidx.compose.ui.unit.TextUnit.Unspecified
                    )
                }
            }
        } else {
            // 全屏图片展示
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                beyondBoundsPageCount = 1
            ) { page ->
                val photoPath = photos[page]
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(File(photoPath))
                        .crossfade(true)
                        .build(),
                    contentDescription = "来自尼康的照片",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit // 保持比例全屏
                )
            }

            // 底部页码指示器
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                Surface(
                    color = Color.Black.copy(alpha = 0.5f),
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = "第 ${pagerState.currentPage + 1} 张 / 共 ${photos.size} 张",
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }
    }
}

package com.nikon.z5ii.ftp

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

/**
 * 管理照片列表与当前显示的 UI 状态
 */
class PhotoViewModel : ViewModel() {
    // 存储所有收到的照片路径
    private val _photos = mutableStateListOf<String>()
    val photos: List<String> get() = _photos

    // 当前选中的照片索引
    var currentIndex = mutableStateOf(0)
    
    // 用于通知 UI 有新图片到达，以便自动全屏弹出
    private val _newPhotoFlow = MutableSharedFlow<String>()
    val newPhotoFlow: SharedFlow<String> get() = _newPhotoFlow

    /**
     * 当有新图片被接收到时调用
     */
    fun onPhotoReceived(path: String) {
        viewModelScope.launch {
            // 添加到列表末尾
            _photos.add(path)
            // 更新当前索引为最新的一张
            currentIndex.value = _photos.size - 1
            // 发射事件通知 UI
            _newPhotoFlow.emit(path)
        }
    }

    /**
     * 切换到上一张/下一张
     */
    fun navigateTo(index: Int) {
        if (index in _photos.indices) {
            currentIndex.value = index
        }
    }
}

package com.nikon.z5ii.ftp

import android.content.Context
import android.util.Log
import org.apache.ftpserver.FtpServer
import org.apache.ftpserver.FtpServerFactory
import org.apache.ftpserver.ftplet.Authority
import org.apache.ftpserver.ftplet.UserManager
import org.apache.ftpserver.listener.ListenerFactory
import org.apache.ftpserver.usermanager.PropertiesUserManagerFactory
import org.apache.ftpserver.usermanager.impl.BaseUser
import org.apache.ftpserver.usermanager.impl.WritePermission
import java.io.File
import java.io.FileOutputStream
import java.util.Properties

/**
 * 封装 Apache FtpServer 的启动与管理逻辑
 */
class FtpServerManager(private val context: Context) {
    private var server: FtpServer? = null
    private val TAG = "FtpServerManager"

    /**
     * 启动 FTP 服务器
     * @param port 监听端口，默认 2121 (避免 21 端口需要 root 权限)
     * @param rootPath FTP 根目录路径
     */
    fun startServer(port: Int = 2121, rootPath: String) {
        if (server != null && !server!!.isStopped) {
            Log.d(TAG, "Server is already running")
            return
        }

        try {
            val serverFactory = FtpServerFactory()
            val listenerFactory = ListenerFactory()

            // 1. 配置监听端口
            listenerFactory.port = port
            serverFactory.addListener("default", listenerFactory.createListener())

            // 2. 配置用户管理 (匿名访问或固定账号)
            val userManagerFactory = PropertiesUserManagerFactory()
            val userManager = userManagerFactory.createUserManager()

            // 创建一个默认用户 'nikon'
            val user = BaseUser()
            user.name = "nikon"
            user.password = "nikon" // 尼康相机连接时填写的密码
            user.homeDirectory = rootPath

            // 赋予写权限，以便相机上传照片
            val authorities = mutableListOf<Authority>()
            authorities.add(WritePermission())
            user.authorities = authorities

            userManager.save(user)
            serverFactory.userManager = userManager

            // 3. 启动服务器
            server = serverFactory.createServer()
            server?.start()
            Log.i(TAG, "FTP Server started on port $port, root: $rootPath")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start FTP server", e)
        }
    }

    /**
     * 停止 FTP 服务器
     */
    fun stopServer() {
        server?.stop()
        server = null
        Log.i(TAG, "FTP Server stopped")
    }

    fun isRunning(): Boolean {
        return server != null && !server!!.isStopped
    }
}

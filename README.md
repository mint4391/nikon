# Nikon Z5 II 联机拍摄安卓助手 (Kotlin + Jetpack Compose)

这是一个专门为尼康 Z5 II 相机设计的联机拍摄助手。通过在安卓设备上建立 FTP 服务器，相机在拍摄后能实时通过 WiFi 将照片上传至手机并自动全屏展示。

## 核心特性
- **极简 UI**：基于 Jetpack Compose 构建，纯黑背景，无广告干扰。
- **自动弹出**：每当接收到新图片，App 立即、自动地全屏展示，无需人工点击。
- **流畅交互**：支持左右滑动查看历史传输图片，使用 Coil 进行高性能图片渲染。
- **FTP 服务端**：基于 Apache FtpServer 实现，监听端口 2121。
- **实时监控**：利用 `FileObserver` 监听文件系统，实现毫秒级响应。
- **简体中文**：界面与提示已全部汉化。

## 技术栈
- **语言**：Kotlin
- **UI**：Jetpack Compose (HorizontalPager)
- **网络**：org.apache.ftpserver:ftpserver-core
- **图片**：io.coil-kt:coil-compose
- **存储**：Android FileObserver

## 🚀 如何获取 APK (GitHub 自动化编译)
如果您本地没有 Android Studio，可以按照以下步骤让 GitHub 为您编译：

1.  **创建 GitHub 仓库**：在您的 GitHub 账号下创建一个新的私有或公开仓库。
2.  **上传源码**：将本压缩包解压后的所有文件（包括 `.github` 文件夹）上传到该仓库。
3.  **触发构建**：
    - 点击仓库上方的 **Actions** 标签。
    - 您会看到一个名为 **"Build Android APK"** 的工作流。
    - 每次您推送代码（或手动点击 `Run workflow`），GitHub 都会自动开始编译。
4.  **下载结果**：
    - 编译完成后（约 2-3 分钟），点击该次构建记录。
    - 在 **Artifacts** 栏目下，您会看到一个名为 `NikonZ5II-FTP-Debug-APK` 的压缩包。
    - 下载并解压，即可获得安装到手机的 `.apk` 文件。

## 使用说明
1.  **手机端配置**：
    - 启动应用，授予必要的文件读写和网络权限。
    - 确保手机开启热点，或与相机处于同一 WiFi 网络下。
    - 默认 FTP 端口：2121，账号：`nikon`，密码：`nikon`。
2.  **相机端配置 (Nikon Z5 II)**：
    - 进入网络菜单 -> 连接至 FTP 服务器。
    - 网络设置：选择手机热点的 SSID 并输入密码。
    - FTP 设置：输入手机的 IP 地址（可在手机 WiFi 设置中查看），端口设为 `2121`。
    - 登录设置：输入账号 `nikon` 和密码 `nikon`。
3.  **拍摄**：
    - 相机拍摄后，手机端收到图片后将立即自动全屏展示。

## 项目结构
- `MainActivity.kt`: UI 容器与权限处理。
- `FtpServerManager.kt`: FTP 服务端生命周期管理。
- `PhotoObserver.kt`: 文件系统实时监控逻辑。
- `PhotoViewModel.kt`: 状态管理与自动弹出逻辑。
- `.github/workflows/android.yml`: GitHub Actions 自动化构建配置。

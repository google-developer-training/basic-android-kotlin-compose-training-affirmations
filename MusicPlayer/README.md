# 乔于恬2024050133 音乐播放器

Android 移动应用开发实验七：基于 Service 的音乐播放应用。

## 功能

- 通过 ContentResolver 查询 MediaStore，显示系统媒体库中的音乐列表（歌名、艺术家、时长）。
- Service 内使用 MediaPlayer 完成播放、暂停、停止、上一首、下一首和进度跳转。
- Activity 通过 bindService 获取 Binder，读取当前歌曲、播放状态和播放进度。
- 独立后台线程每 500 ms 轮询进度并回到主线程刷新 SeekBar，拖动 SeekBar 可跳转到指定位置。
- 播放时以前台服务方式运行，通知栏显示当前歌名，并提供上一首、暂停/播放、下一首三个操作。
- 适配 Android 13（API 33）的 READ_MEDIA_AUDIO 与 POST_NOTIFICATIONS 运行时权限。

## 环境

- Android Studio 2026.1
- JDK 17
- Gradle 8.12，Android Gradle Plugin 8.8.0，Kotlin 2.1.0
- compileSdk 35，minSdk 24，targetSdk 35
- 测试设备：Android Emulator API 35（x86_64，1080×2400）

## 构建

在 Android Studio 中打开本目录，等待 Gradle 同步完成后运行 app 模块；或在命令行执行：

```bat
set JAVA_HOME=D:\Program Files\Java\jdk-17
gradlew.bat :app:assembleDebug --offline
```

生成的调试包位于 `app\build\outputs\apk\debug\app-debug.apk`。

## 运行

1. 安装并启动应用，首次启动会请求音频读取权限与通知权限，请全部允许。
2. 若列表为空，点击右上角“刷新”重新查询媒体库。
3. 点击列表中的歌曲开始播放；底部按钮依次为上一首、播放/暂停、停止、下一首。
4. 拖动进度条可跳转播放位置，时间显示与列表中的“播放中/已暂停”标签会同步更新。

测试时可在设备 `/sdcard/Music` 放置音频文件，并执行媒体扫描：

```bat
adb push 夏日午后.wav /sdcard/Music/
adb shell am broadcast -a android.intent.action.MEDIA_SCANNER_SCAN_FILE -d file:///sdcard/Music/夏日午后.wav
```

WAV 文件在部分模拟器上被扫描后 `IS_MUSIC` 与 `DURATION` 为空，应用会在常规查询结果为空时按 `DURATION IS NULL` 补充查询。

## 目录

```
MusicPlayer
├─ app/src/main/java/com/qiaoyutian/musicplayer
│  ├─ MainActivity.kt
│  ├─ MusicService.kt
│  ├─ Song.kt
│  └─ SongAdapter.kt
├─ app/src/main/res
│  ├─ layout/activity_main.xml
│  ├─ layout/item_song.xml
│  ├─ drawable/ic_*.xml
│  └─ values/strings.xml、colors.xml、themes.xml
├─ app/src/main/AndroidManifest.xml
└─ screenshots
```

## 说明

无窗口模拟器的 SystemUI 不渲染通知面板，因此通知栏截图为空；通知已实际发布，可通过 `adb shell dumpsys notification --noredact` 查看 `com.qiaoyutian.musicplayer` 的 id=1001、channel=music_channel、actions=3、android.title 为当前歌名的记录。真机或有窗口模拟器可正常看到通知。

代码仓库：https://github.com/Tiwen32/2024050133-QiaoYuTian.git

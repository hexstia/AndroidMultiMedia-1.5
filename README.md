# 简介
   项目用于完成Android设备的扬声器音频流（AAC格式）数据;屏幕流数据（H264格式）数据;输入系统:键盘与触摸点击事件指令流的发送；
   项目还使用了Xposed的工具完成对微信的破解，包含 微信好友，群好友，发送消息，接受消息，页面跳转，等功能的破解。
                                                                                                        ——Hexstia

# 开发环境

- Android Studio 3.4
- 测试机器 arm64  Android 8.1 (如果发现项目运行失败，请注意系统是否需要**动态权限**,cpu是否是**arm处理器**)

### 注意：

- 本程序需要system用户权限，请注意root权限破解以及system权限的使用

### 理论基础

- [音视频编码相关名词详解](https://www.jianshu.com/p/c398754e5984)
- [流媒体解码及H.264编码推流](https://www.jianshu.com/p/f83ef0a6f5cc)
- [flv格式详解+实例剖析](https://www.jianshu.com/p/7ffaec7b3be6)
- [基于FFmpeg进行RTMP推流（一）](https://www.jianshu.com/p/69eede147229)
- [基于FFmpeg进行RTMP推流（二）](https://www.jianshu.com/p/6b9ab2652147)



### 项目涉及文章

- [Linux下FFmpeg编译以及Android平台下使用](https://www.jianshu.com/p/4037297d883d)—[源码v1.0](https://github.com/EricLi22/FFmpegSample/releases/tag/v1.0)
- [Android平台下使用FFmpeg进行RTMP推流（视频文件推流)](https://www.jianshu.com/p/dcac5da8f1da)—[源码v1.1](https://github.com/EricLi22/FFmpegSample/releases/tag/v1.1)
- [Android平台下使用FFmpeg进行RTMP推流（摄像头推流）](https://www.jianshu.com/p/462e489b7ce0)—[源码v1.2.1](https://github.com/EricLi22/FFmpegSample/releases/tag/1.2.1)
- [Android RTMP推流之MediaCodec硬编码一（H.264进行flv封装）](https://www.jianshu.com/p/e607e63fb78f)—[源码v1.3](https://github.com/EricLi22/FFmpegSample/releases/tag/v1.3)
- [Android平台下RTMPDump的使用](https://www.jianshu.com/p/3ee9e5e4d630)—[源码v1.4](https://github.com/EricLi22/FFmpegSample/releases/tag/v1.4)




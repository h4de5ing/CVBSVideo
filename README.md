# cvbs video

we support two cvbs video ,take picture and record video
/dev/video6
/dev/video7

Camera mCamera6 = Camera.open(6);
Camera mCamera7 = Camera.open(7);

[download android.jar](https://github.com/h4de5ing/CVBSVideo/releases/tag/android-23)

replace android.jar
Android_sdk\sdk\platforms\android-23\android.jar

## modules:

# 注意：部分Android SDK的源码我们修改过,如果编译报错请在release下载android.jar 替换你的sdk目录下的android.jar

支持4路CVBS Camera 640X480的拍照录像,Camera节点分别为4 5 6 7 参考app module代码
支持4路USB Camera 最大1280X720的拍照录像,Camera节点分别为0 1 2 3 参考 usbcamerarecored module代码

[下载android.jar](https://github.com/h4de5ing/CVBSVideo/releases/tag/android-23)

替换本地 sdk 23目录下的android.jar

Android_sdk\sdk\platforms\android-23\android.jar


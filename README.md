# AppRTC-Android

This Android client is an Enhancement on top of https://github.com/Androidhacks7/AppRTC-Android and is pretty much a mirror of what is present in default WebRTC Android

The app is built pointing to https://github.com/FeelowTang/kurento-tutorial-java/tree/androidOnetoOneCallServer/kurento-one2one-call instead of apprtc.appspot.com

Also, the concept of users joining a room has been replaced with a call based architecture. This AppRTC android client along with the server mentioned, forms the complete stack to establish a video call from one mobile client to another or browser

The app can be built and deployed as is with just changing the server IP in ServerConfiguration.java
The port numbers can be kept as is

Note: This will work only when the mobile client and server are on the same network. If run the app in emulator, it should only have audio output for libjingle unsupport the emulator.

![ScreenShot](https://raw.github.com/Androidhacks7/AppRTC-Android/master/screenshots/Screenshot_2015-12-31-09-19-20.png)
![ScreenShot](https://raw.github.com/Androidhacks7/AppRTC-Android/master/screenshots/Screenshot_2015-12-31-09-19-24.png)
![ScreenShot](https://raw.github.com/Androidhacks7/AppRTC-Android/master/screenshots/Screenshot_20151231-092257.png)
![ScreenShot](https://raw.github.com/Androidhacks7/AppRTC-Android/master/screenshots/Screenshot_2015-12-31-09-21-51.png)

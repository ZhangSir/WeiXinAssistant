# WeiXinAssistant
微信助手，是一个使用Android辅助服务实现的微信自动抢红包的小程序；  

##说明
通过利用AccessibilityService辅助服务，监测屏幕内容，如监听状态栏的信息，屏幕跳转等，以此来实现自动拆红包的功能。  

###逻辑
第一种方式：  
监听通知栏微信消息，如果弹出[微信红包]字样，模拟点击状态栏跳转到微信聊天界面；  
在微信聊天界面查找红包，如果找到则模拟点击打开，弹出打开红包界面；  
在打开红包界面，模拟点击红包“开”按钮；  

第二种方式：  
监听微信聊天界面，如果出现“领取红包”消息，则在该界面查找红包，找到红包则模拟打开，弹出打开红包界面；  
在打开红包界面，模拟点击红包“开”按钮；  

##参考资料
Android中微信抢红包助手的实现详解（http://www.jb51.net/article/104507.htm）  
Android AccessibilityService实现微信抢红包插件 （http://www.jb51.net/article/97982.htm）  
Android微信自动抢红包插件优化和实现 （http://www.jb51.net/article/99327.htm）

##截图
![image1](https://github.com/ZhangSir/WeiXinAssistant/blob/master/Screenshot_2017-02-23-17-38-23.png)
![image2](https://github.com/ZhangSir/WeiXinAssistant/blob/master/Screenshot_2017-02-23-17-38-38.png)
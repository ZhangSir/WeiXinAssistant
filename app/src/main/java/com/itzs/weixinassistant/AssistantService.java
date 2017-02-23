package com.itzs.weixinassistant;

import android.accessibilityservice.AccessibilityService;
import android.app.KeyguardManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.PowerManager;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;

import java.util.List;

/**
 * 助手服务类
 * Created by zhangshuo on 2017/2/23.
 */

public class AssistantService extends AccessibilityService{

    private final String TAG = AssistantService.class.getSimpleName();
    /**
     * 微信几个页面的包名+地址。用于判断在哪个页面 LAUCHER-微信聊天界面，LUCKEY_MONEY_RECEIVER-点击红包弹出的界面
     */
    private final String LAUCHER = "com.tencent.mm.ui.LauncherUI";
    private final String LUCKEY_MONEY_RECEIVER = "com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyReceiveUI";
    private final String LUCKEY_MONEY_DETAIL = "com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyDetailUI";

    /**
     * 是否开启窗口视图变化监听，已实现对在聊天界面时，也能监听“红包”
     */
    public static boolean enbleWindowMonitor = false;

    /**
     * 助手服务是否正在运行
     */
    public static boolean isAssistantRunning = false;

    /**
     * 用于判断是否点击过红包了
     */
    private boolean isOpenRP;

    private PowerManager pm;
    private PowerManager.WakeLock wl;
    private KeyguardManager km;
    private KeyguardManager.KeyguardLock kl;

    /**
     * 必须重写的方法：此方法用了接受系统发来的event。在你注册的event发生时被调用。在整个生命周期会被调用多次。
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        int eventType = event.getEventType();
        switch (eventType) {
            //通知栏来信息，判断是否含有微信红包字样，是的话跳转
            case AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED:
                Log.d(TAG, "通知栏有变化");
                List<CharSequence> texts = event.getText();
                for (CharSequence text : texts) {
                    String content = text.toString();
                    if (!TextUtils.isEmpty(content)) {
                        //判断是否含有[微信红包]字样
                        if (content.contains("[微信红包]")) {
                            //点亮并解锁屏幕
                            wakeAndUnlock(true);
                            //如果有则打开微信红包页面
                            openWeChatPage(event);
                        }
                    }
                }
                break;
            //窗口发生改变时会调用该事件
            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
                String className = event.getClassName().toString();
                Log.d(TAG, "窗口有变化");
                if (LAUCHER.equals(className)) {
                    //判断是否是微信聊天界面
                    //获取当前聊天页面的根布局
                    AccessibilityNodeInfo rootNode = getRootInActiveWindow();
                    //开始找红包
                    findRedPacket(rootNode);
                }else if (LUCKEY_MONEY_RECEIVER.equals(className)) {
                    //判断是否是显示‘开'的那个红包界面
                    AccessibilityNodeInfo rootNode = getRootInActiveWindow();
                    //开始抢红包
                    openRedPacket(rootNode);
                }else if(LUCKEY_MONEY_DETAIL.equals(className)){
                    //判断是否是红包领取后的详情界面
                    //返回桌面
                    back2Home();
                    //关闭屏幕
                    wakeAndUnlock(false);
                }
                break;
            case AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED:
                //窗口视图变化
                if(!enbleWindowMonitor) return;

                String pubclassName = event.getClassName().toString();

                Log.d(TAG, "窗口视图有变化：" + getRootInActiveWindow().getClassName().toString());
                Log.d(TAG, "event class：" + pubclassName);

                for (CharSequence text : event.getText()) {
                    String content = text.toString();
                    if (!TextUtils.isEmpty(content)) {
                        //判断是否含有"领取红包"字样
                        if (content.contains("领取红包")) {
                            findRedPacket(getRootInActiveWindow());
                            break;
                        }
                    }
                }

                break;
        }
    }

    /**
     * 开启红包所在的聊天页面
     */
    private void openWeChatPage(AccessibilityEvent event) {
        //A instanceof B 用来判断内存中实际对象A是不是B类型，常用于强制转换前的判断
        if (event.getParcelableData() != null && event.getParcelableData() instanceof Notification) {
            Notification notification = (Notification) event.getParcelableData();
            //打开对应的聊天界面
            PendingIntent pendingIntent = notification.contentIntent;
            try {
                pendingIntent.send();
            } catch (PendingIntent.CanceledException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 遍历查找红包
     */
    private void findRedPacket(AccessibilityNodeInfo rootNode) {
        if (rootNode != null) {
            //从最后一行开始找起
            for (int i = rootNode.getChildCount() - 1; i >= 0; i--) {
                AccessibilityNodeInfo node = rootNode.getChild(i);
                //如果node为空则跳过该节点
                if (node == null) {
                    continue;
                }
                CharSequence text = node.getText();
                if (text != null && text.toString().equals("领取红包")) {
                    AccessibilityNodeInfo parent = node.getParent();
                    //while循环,遍历"领取红包"的各个父布局，直至找到可点击的为止
                    while (parent != null) {
                        if (parent.isClickable()) {
                            //模拟点击
                            parent.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                            //isOpenRP用于判断该红包是否点击过
                            isOpenRP = true;
                            break;
                        }
                        parent = parent.getParent();
                    }
                }
                //判断是否已经打开过那个最新的红包了，是的话就跳出for循环，不是的话继续遍历
                if (isOpenRP) {
                    break;
                } else {
                    findRedPacket(node);
                }

            }
        }
    }

    /**
     * 开始打开红包
     */
    private void openRedPacket(AccessibilityNodeInfo rootNode) {
        for (int i = 0; i < rootNode.getChildCount(); i++) {
            AccessibilityNodeInfo node = rootNode.getChild(i);
            if ("android.widget.Button".equals(node.getClassName())) {
                node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
            }
            openRedPacket(node);
        }
    }

    /**
     * 返回桌面
     */
    private void back2Home() {
        Intent home=new Intent(Intent.ACTION_MAIN);
        home.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        home.addCategory(Intent.CATEGORY_HOME);
        startActivity(home);
    }

    /**
     * 点亮并解锁屏幕
     * @param b
     */
    private void wakeAndUnlock(boolean b) {
        if (b) {
            //获取电源管理器对象
            pm = (PowerManager) getSystemService(Context.POWER_SERVICE);

            //获取PowerManager.WakeLock对象，后面的参数|表示同时传入两个值，最后的是调试用的Tag
            wl = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.SCREEN_BRIGHT_WAKE_LOCK, TAG);

            //点亮屏幕
            wl.acquire();

            //得到键盘锁管理器对象
            km = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
            kl = km.newKeyguardLock("unLock");

            //解锁
            kl.disableKeyguard();
        } else {
            //锁屏
            kl.reenableKeyguard();

            //释放wakeLock，关灯
            wl.release();
        }

    }

    /**
     * 服务已连接
     */
    @Override
    protected void onServiceConnected() {
        isAssistantRunning = true;
        Log.d(TAG, "抢红包服务开启");
        super.onServiceConnected();
    }

    /**
     * 必须重写的方法：系统要中断此service返回的响应时会调用。在整个生命周期会被调用多次。
     */
    @Override
    public void onInterrupt() {
        Log.d(TAG, "抢红包服务快被终结了");
    }

    /**
     * 服务已断开
     */
    @Override
    public boolean onUnbind(Intent intent) {
        isAssistantRunning = false;
        Log.d(TAG, "抢红包服务已被关闭");
        return super.onUnbind(intent);
    }

    /**
     *   AccessibilityService中常用的方法的介绍
         disableSelf()：禁用当前服务，也就是在服务可以通过该方法停止运行
         findFoucs(int falg)：查找拥有特定焦点类型的控件
         getRootInActiveWindow()：如果配置能够获取窗口内容,则会返回当前活动窗口的根结点
         getSeviceInfo()：获取当前服务的配置信息
         onAccessibilityEvent(AccessibilityEvent event)：有关AccessibilityEvent事件的回调函数，系统通过sendAccessibiliyEvent()不断的发送AccessibilityEvent到此处
         performGlobalAction(int action)：执行全局操作，比如返回，回到主页，打开最近等操作
         setServiceInfo(AccessibilityServiceInfo info)：设置当前服务的配置信息
         getSystemService(String name)：获取系统服务
         onKeyEvent(KeyEvent event)：如果允许服务监听按键操作，该方法是按键事件的回调，需要注意，这个过程发生了系统处理按键事件之前
         onServiceConnected()：系统成功绑定该服务时被触发，也就是当你在设置中开启相应的服务，系统成功的绑定了该服务时会触发，通常我们可以在这里做一些初始化操作
         onInterrupt()：服务中断时的回调
     */

    /**
     *   AccessibilityEvent的方法
         getEventType()：事件类型
         getSource()：获取事件源对应的结点信息
         getClassName()：获取事件源对应类的类型，比如点击事件是有某个Button产生的，那么此时获取的就是Button的完整类名
         getText()：获取事件源的文本信息，比如事件是有TextView发出的,此时获取的就是TextView的text属性。如果该事件源是树结构，那么此时获取的是这个树上所有具有text属性的值的集合
         isEnabled()：事件源(对应的界面控件)是否处在可用状态
         getItemCount()：如果事件源是树结构，将返回该树根节点下子节点的数量
     */
}

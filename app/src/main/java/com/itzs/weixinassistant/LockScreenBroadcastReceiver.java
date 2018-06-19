package com.itzs.weixinassistant;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

/**
 * Created by zhangshuo on 2018/3/22.
 */

public class LockScreenBroadcastReceiver extends BroadcastReceiver {
    private final String TAG = LockScreenBroadcastReceiver.class.getSimpleName();

    private volatile static LockScreenBroadcastReceiver instance;

    public static LockScreenBroadcastReceiver getInstance(){
        if (instance == null) {
            synchronized (LockScreenBroadcastReceiver.class) {
                if (instance == null) {
                    instance = new LockScreenBroadcastReceiver();
                }
            }
        }
        return instance;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        switch (intent.getAction()){
            case Intent.ACTION_SCREEN_OFF:{
                Intent mLockIntent = new Intent(context, LockScreenActivity.class);
                mLockIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                context.startActivity(mLockIntent);
            }
            break;
            default:
                break;
        }
    }

    /**
     * 注册锁屏广播
     * @param context
     */
    public static void register(Context context){
        IntentFilter mScreenOffFilter = new IntentFilter();
        mScreenOffFilter.addAction(Intent.ACTION_SCREEN_OFF);
        context.registerReceiver(LockScreenBroadcastReceiver.getInstance(), mScreenOffFilter);
    }
}

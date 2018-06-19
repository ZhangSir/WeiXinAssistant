package com.itzs.weixinassistant;

import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.Tag;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;

public class LockScreenActivity extends AppCompatActivity {
    private final String TAG = LockScreenActivity.class.getSimpleName();
    /**
     * 自动解锁屏幕的广播
     */
    public static final String ACTION_AUTO_UNLOCK_SCREEN = "com.itzs.weixinassistant.LockScreenActivity.action.auto.unlock.screen";

    private BroadcastReceiver receiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
            KeyguardManager km = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
            km.requestDismissKeyguard(this, new KeyguardManager.KeyguardDismissCallback() {
                @Override
                public void onDismissError() {
                    super.onDismissError();
                    Log.d(TAG, "锁屏关闭错误");
                }

                @Override
                public void onDismissSucceeded() {
                    super.onDismissSucceeded();
                    Log.d(TAG, "锁屏关闭成功");
                }

                @Override
                public void onDismissCancelled() {
                    super.onDismissCancelled();
                    Log.d(TAG, "锁屏关闭取消");
                }
            });
        }else{
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                    | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        }

        setContentView(R.layout.activity_lock_screen);

        //注册自动解锁广播
        registerAutoUnlockBroadcatReceiver();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        //取消注册自动解锁广播
        unregisterAutoUnlockBroadcatReceiver();
        super.onDestroy();
    }

    /**
     * 注册自动解锁屏幕的广播
     */
    private void registerAutoUnlockBroadcatReceiver(){
        if(null == receiver){
            receiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    switch (intent.getAction()){
                        case ACTION_AUTO_UNLOCK_SCREEN:{
                            Log.d(TAG, "自动解锁屏幕");
                            finish();
                        }
                        break;
                        default:
                            break;
                    }
                }
            };
        }
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_AUTO_UNLOCK_SCREEN);
        registerReceiver(receiver, filter);
    }

    private void unregisterAutoUnlockBroadcatReceiver(){
        if(null != receiver){
            unregisterReceiver(receiver);
        }
    }
}

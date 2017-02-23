package com.itzs.weixinassistant;

import android.content.Intent;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;

/**
 * 主页面
 */
public class MainActivity extends AppCompatActivity {

    private final String TAG = MainActivity.class.getSimpleName();

    private TextView tvStatus;
    private SwitchCompat switchAuto;
    private Button btnEnter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.initView();
        this.initListener();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshViewData();
    }

    private void initView(){
        tvStatus = (TextView) this.findViewById(R.id.tv_main_status);
        switchAuto = (SwitchCompat) this.findViewById(R.id.switch_main_auto);
        btnEnter = (Button) this.findViewById(R.id.btn_main_enter);
    }

    private void initListener(){
        switchAuto.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                AssistantService.enbleWindowMonitor = isChecked;
            }
        });
        btnEnter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openAccessibilitySetting();
            }
        });
    }

    private void refreshViewData(){
        if(AssistantService.isAssistantRunning){
            tvStatus.setText(R.string.main_status_opened);
            tvStatus.setTextColor(getResources().getColor(R.color.green));
            btnEnter.setText(R.string.main_close);
        }else{
            tvStatus.setText(R.string.main_status_closed);
            tvStatus.setTextColor(getResources().getColor(R.color.red));
            btnEnter.setText(R.string.main_open);
        }

        switchAuto.setChecked(AssistantService.enbleWindowMonitor);
    }

    /**
     * 打开系统的无障碍功能列表
     */
    private void openAccessibilitySetting(){
        Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
        startActivity(intent);
    }
}

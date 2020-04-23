package com.wwc2.h264aidltest;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;

import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.wwc2.dvr.IRawDataManager;


public class MainActivity extends AppCompatActivity{
    private String TAG = "hzy ";




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG,"onCreate");
        setContentView(R.layout.activity_main);
        Button mStop = findViewById(R.id.stop);

        mStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                WtwdVideoManager.getInstance().unregisterCallBack(WtwdVideoManager.LOCAL_H264_FONT_TYPE);
                WtwdVideoManager.getInstance().unregisterCallBack(WtwdVideoManager.LOCAL_H264_BACK_TYPE);
                WtwdVideoManager.getInstance().unregisterCallBack(WtwdVideoManager.LOCAL_H264_LEFT_TYPE);
                WtwdVideoManager.getInstance().unregisterCallBack(WtwdVideoManager.LOCAL_H264_RIGHT_TYPE);
            }
        });
        //建立翼卡平台连接
        JttManager.getInstance().connect(getApplicationContext());

    }



    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG,"onResume");
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        Log.d(TAG,"onPause");

    }



}

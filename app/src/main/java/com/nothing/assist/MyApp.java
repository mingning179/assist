package com.nothing.assist;

import android.app.Application;

import com.nothing.assist.common.Log;

public class MyApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        initializeComponents();
    }

    private void initializeComponents() {
        Log.initialize(this);
        // 在这里添加其他的初始化操作
    }
}
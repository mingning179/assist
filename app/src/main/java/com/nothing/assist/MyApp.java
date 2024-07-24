package com.nothing.assist;

import android.app.Application;

import com.nothing.assist.common.Log;

public class MyApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Log.initialize(this);
    }
}
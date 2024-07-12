package com.nothing.assist;

import android.app.Application;

public class MyApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        com.nothing.assist.Log.initialize(this);
    }
}
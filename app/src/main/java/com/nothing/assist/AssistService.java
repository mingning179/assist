package com.nothing.assist;

import android.accessibilityservice.AccessibilityService;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

public class AssistService extends AccessibilityService {
    @Override
    protected void onServiceConnected() {
        DataInterceptor.init(this);
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        DataInterceptor.interceptData(event);
    }
    @Override
    public void onInterrupt() {
        DataInterceptor.onInterrupt();
    }
}
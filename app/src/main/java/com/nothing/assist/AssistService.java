package com.nothing.assist;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class AssistService extends AccessibilityService {
    Thread notifyThread = null;
    long notifyTime = 60*1000;
    DataService dataService;

    @Override
    public void onCreate() {
        super.onCreate();
        dataService=new DataService(this);
        notifyThread = new Thread(() -> {
            Log.i("AssistService", "notifyThread is 开始运行");
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    processNotify();
                } catch (InterruptedException e) {
                    Log.i("AssistService", "notifyThread is 被中断");
                    break;
                }
            }
            Log.i("AssistService", "notifyThread is 结束运行");
        });
        notifyThread.start();
    }
    private void processNotify() throws InterruptedException {
        //TODO 检查是否有后台弹出界面的权限
        // 弹出一个窗口通知用户
        Thread.sleep(notifyTime);
        //判断今天是否已经签到
        if (dataService.isTodaySigned()) {
            //已经签到
            //休眠到明天早上8点
            //计算当前时间到明天早上8点的时间差
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_MONTH, 1); // Add one day to the current date
            calendar.set(Calendar.HOUR_OF_DAY, 8); // Set hour to 8 AM
            calendar.set(Calendar.MINUTE, 0); // Set minute to 0
            calendar.set(Calendar.SECOND, 0); // Set second to 0
            calendar.set(Calendar.MILLISECOND, 0); // Set millisecond to 0 for precision

            long tomorrowEightAMTimestamp = calendar.getTimeInMillis();
            System.out.println(sdf.format(calendar.getTime()));
            long currentTime = System.currentTimeMillis();
            long sleepTime = tomorrowEightAMTimestamp - currentTime;

            Log.i("AssistService", "今天已经签到, 休眠到明天早上8点, 时长: " + sleepTime);
            Thread.sleep(sleepTime);
            return;
        }
        //未签到
        Log.i("AssistService", "今天未签到，弹出提醒界面");

        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);
    }

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
        Log.i("AssistService", "onInterrupt");
        DataInterceptor.onInterrupt();
        notifyThread.interrupt();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i("AssistService", "onDestroy");
        DataInterceptor.onInterrupt();
        notifyThread.interrupt();
    }
}
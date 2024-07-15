package com.nothing.assist;

import android.accessibilityservice.AccessibilityService;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.view.accessibility.AccessibilityNodeInfo;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class AssistService extends AccessibilityService implements AccessibilityManager.AccessibilityStateChangeListener {
    private PendingIntent pendingIntent;
    private AlarmManager alarmManager;
    private final Long notifyTime = 2 * 60 * 1000L;
    private Long realSleepTime;
    public static final String ACTION_NOTIFICATION_TASK = "ACTION_NOTIFICATION_TASK";
    private DataService dataService;
    private DataInterceptor dataInterceptor;
    AccessibilityManager accessibilityManager;
    private PowerManager powerManager;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i("AssistService", "onCreate");
        initConfig();
        dataService = new DataService(this);
        dataInterceptor = new DataInterceptor(dataService);
        powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(this, AlarmReceiver.class);
        intent.setAction(ACTION_NOTIFICATION_TASK);
        pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);
        accessibilityManager = (AccessibilityManager) this.getSystemService(Context.ACCESSIBILITY_SERVICE);
        accessibilityManager.addAccessibilityStateChangeListener(this);
        setAlarm(realSleepTime);
    }

    private void initConfig() {
        realSleepTime = notifyTime;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("AssistService", "onStartCommand");
        if (intent != null && ACTION_NOTIFICATION_TASK.equals(intent.getAction())) {
            processNotify();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private void setAlarm(long notifyTime) {
        alarmManager.cancel(pendingIntent);
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + notifyTime, pendingIntent);
        Log.i("AssistService", "添加定时任务(添加之前会取消之前的定时任务): " + notifyTime + "ms");
    }

    private void processNotify() {
        try {
            Log.i("AssistService", "notifyThread is 开始运行");
            AccessibilityNodeInfo rootInActiveWindow = this.getRootInActiveWindow();
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
                realSleepTime = sleepTime;
                return;
            }
            //未签到
            if (rootInActiveWindow != null) {
                String currentPackageName = rootInActiveWindow.getPackageName().toString();
                Log.i("AssistService", "processNotify:" + currentPackageName);
                if (currentPackageName.equals(this.getPackageName())) {
                    Log.i("AssistService", "无需提醒，已经在自己的界面");
                    realSleepTime = notifyTime;
                    return;
                } else if (currentPackageName.equals("com.myway.fxry")) {
                    Log.i("AssistService", "无需提醒，已经在目标应用界面，用户可能正在打卡");
                    realSleepTime = notifyTime;
                    return;
                } else if (currentPackageName.equals("com.android.camera")) {
                    Log.i("AssistService", "无需提醒，在相机界面，用户可能正在打卡");
                    realSleepTime = notifyTime;
                    return;
                }
            } else {
                Log.i("AssistService", "rootInActiveWindow is null, 无法判断当前界面");
            }
            Log.i("AssistService", "今天未签到，弹出提醒界面");
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(intent);
            if (!powerManager.isInteractive()) {
                Log.i("AssistService", "屏幕已熄灭, 3倍时间后再次提醒");
                realSleepTime = notifyTime * 2;
                return;
            } else {
                Log.i("AssistService", "屏幕已点亮，正常弹出提醒界面");
            }
            realSleepTime = notifyTime;
            Log.i("AssistService", "notifyThread is 结束运行");
        } finally {
            //重新设置定时任务
            setAlarm(realSleepTime);
        }
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        dataInterceptor.interceptData(event);
    }

    @Override
    public void onInterrupt() {
        Log.i("AssistService", "onInterrupt");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        accessibilityManager.removeAccessibilityStateChangeListener(this);
        alarmManager.cancel(pendingIntent);
        Log.i("AssistService", "onDestroy 取消定时任务");
        Log.i("AssistService", "onDestroy");
    }

    @Override
    public void onAccessibilityStateChanged(boolean enabled) {
        if (!enabled) {
            Log.i("AssistService", "辅助功能被关闭, 取消定时任务");
            alarmManager.cancel(pendingIntent);
        } else {
            initConfig();
            Log.i("AssistService", "辅助功能被打开");
            setAlarm(realSleepTime);
        }
    }
}
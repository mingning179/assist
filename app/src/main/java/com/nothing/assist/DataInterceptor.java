package com.nothing.assist;

import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DataInterceptor {
    private static final String TAG = "DataIntercepter";
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static DataService dataService;
    public static void interceptData(AccessibilityEvent event) {
        if(event.getEventType()==AccessibilityEvent.TYPE_VIEW_CLICKED){
            if(event.getPackageName().equals("com.myway.fxry"))
            {
                Log.i(TAG, String.format("在矫通点击事件: %s ,%s", event.getText(),sdf.format(new Date())));
                recordMy(event);
            }
        }
        Log.i(TAG, String.format("onAccessibilityEvent: %s ", event.toString()));
    }
    private static void recordMy(AccessibilityEvent event) {
        if(event.getText().get(0).equals("我的"))
        {
            AccessibilityNodeInfo root = event.getSource().getParent();
            AccessibilityNodeInfo contentRoot = root.findAccessibilityNodeInfosByText("今日签到").get(0).getParent();
            //打印所有子节点
            int qdIndex = -1;
            int qdcsIndex = -1;
            for(int i=0;i<contentRoot.getChildCount();i++)
            {
                AccessibilityNodeInfo node = contentRoot.getChild(i);
                if(node.getText()!=null){
                    if(node.getText().equals("最近签到")){
                        qdIndex = i+1;
                    }
                    if(node.getText().toString().contains("今日签到")){
                        qdcsIndex = i+1;
                    }
                }
            }
            if(qdIndex!=-1){
                AccessibilityNodeInfo qdNode = contentRoot.getChild(qdIndex);
                try {
                    Date date = sdf.parse(qdNode.getText().toString().trim());
                    dataService.setLastSignTime(date);
                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }
            }
            if(qdcsIndex!=-1) {
                AccessibilityNodeInfo qdcsNode = contentRoot.getChild(qdcsIndex);
                int qdcs = Integer.parseInt(qdcsNode.getText().toString().trim());
                dataService.setSignCount(qdcs);
            }
        }
    }

    public static void init(AssistService assistService) {
        dataService = new DataService(assistService.getApplicationContext());
        Log.i(TAG, String.format("服务初始化: %s ,%s", assistService.toString(), String.valueOf(System.currentTimeMillis())));
    }
    public static void onInterrupt() {
        Log.i(TAG, String.format("服务中断: %s", String.valueOf(System.currentTimeMillis())));
    }
}

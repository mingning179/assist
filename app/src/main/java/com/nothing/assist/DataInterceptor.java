package com.nothing.assist;

import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

public class DataInterceptor {
    private final String TAG = "DataInterceptor";
    private final SimpleDateFormat dtf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private final DataService dataService;
    private boolean shouldClickMy = false;

    public DataInterceptor(DataService dataService) {
        this.dataService = dataService;
    }

    public void interceptData(AccessibilityEvent event) {
        try {
            String packageName = event.getPackageName().toString();
            // 拦截 在矫通 应用内的数据
            if (packageName.equals("com.myway.fxry")) {
                if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {
//                    Log.i(TAG, String.format("窗口内容改变事件: %s ", event.getContentDescription()));
                    recordMy(event);
                }
                if (event.getEventType() == AccessibilityEvent.TYPE_VIEW_CLICKED) {
//                    Log.i(TAG, String.format("View被点击事件: %s ", !event.getText().isEmpty() ? event.getText().get(0) : null));
                    recordSign(event);
                }
                if (shouldClickMy && event.getEventType() == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {
//                    Log.i(TAG, String.format("待执行点击: %s ", event.getContentDescription()));
                    clickMy(event);
                }
//                Log.i(TAG, String.format("onAccessibilityEvent: %s ", event));
            }
        } catch (Exception e) {
            Log.e(TAG, "处理事件失败", e);
        }
    }

    //记录签到动作
    private void recordSign(AccessibilityEvent event) {
        if (!event.getClassName().toString().equals("android.widget.Button") || !event.getText().get(0).toString().equals("签      到")) {
            Log.i(TAG, String.format("recordSign: 不是签到按钮 %s %s", event.getClassName(), event.getText()));
            return;
        } else {
            Log.i(TAG, String.format("recordSign: 是签到按钮 %s %s", event.getClassName(), event.getText()));
        }
        shouldClickMy = true;
    }

    //点击我的 以便获取签到信息
    private synchronized void clickMy(AccessibilityEvent event) {
        if (contains(event, "我的") && shouldClickMy) {
            AccessibilityNodeInfo root = getRootContent(event);
            if (root == null) {
                return;
            }
            List<AccessibilityNodeInfo> nodes = root.findAccessibilityNodeInfosByText("我的");
            if (!nodes.isEmpty()) {
                AccessibilityNodeInfo myNode = nodes.get(0).getParent();
                myNode.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                shouldClickMy = false;
                Log.i(TAG, "clickMy: 点击我的");
            } else {
                Log.i(TAG, "clickMy: 没有找到我的");
            }
        }
    }

    //记录签到信息
    private void recordMy(AccessibilityEvent event) throws ParseException {
        if (contains(event, "今日签到")) {
            AccessibilityNodeInfo root = getRootContent(event);
            if (root == null) {
                return;
            }
            AccessibilityNodeInfo contentRoot = root.findAccessibilityNodeInfosByText("今日签到").get(0).getParent();
            int qdIndex = -1;
            int qdcsIndex = -1;
            for (int i = 0; i < contentRoot.getChildCount(); i++) {
                AccessibilityNodeInfo node = contentRoot.getChild(i);
                if (node.getText() != null) {
                    if (node.getText().equals("最近签到")) {
                        qdIndex = i + 1;
                    }
                    if (node.getText().toString().contains("今日签到")) {
                        qdcsIndex = i + 1;
                    }
                }
            }
            if (qdIndex != -1) {
                AccessibilityNodeInfo qdNode = contentRoot.getChild(qdIndex);
                if (qdNode.getText() != null) {
                    Log.i(TAG, "recordMy: 最近签到时间 " + qdNode.getText());
                    dataService.setLastSignTime(dtf.parse(qdNode.getText().toString().trim()));
                }
            }
            if (qdcsIndex != -1) {
                AccessibilityNodeInfo signTimesNode = contentRoot.getChild(qdcsIndex);
                if (signTimesNode.getText() != null) {
                    Log.i(TAG, "recordMy: 签到次数 " + signTimesNode.getText());
                    dataService.setSignCount(Integer.parseInt(signTimesNode.getText().toString().trim()));
                }
            }
        }
    }

    //获取根节点
    private AccessibilityNodeInfo getRootContent(AccessibilityEvent event) {
        AccessibilityNodeInfo root = event.getSource();
        if (root == null) {
            return null;
        }
        while (root.getParent() != null) {
            root = root.getParent();
        }
        return root;
    }

    //判断是否包含目标值
    private boolean contains(AccessibilityEvent event, String targetValue) {
        AccessibilityNodeInfo root = getRootContent(event);
        if (root == null) {
            return false;
        }
        return !root.findAccessibilityNodeInfosByText(targetValue).isEmpty();
    }
}

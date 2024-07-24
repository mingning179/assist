package com.nothing.assist.common;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.widget.Toast;

import org.jetbrains.annotations.NotNull;

public class PermissionUtils {
    /**
     * 申请后台弹出界面权限
     *
     * @param context
     */
    public static void openRedmiBackgroundPopupSetting(Context context) {
        try {
            Intent intent = new Intent();
            intent.setComponent(new ComponentName(
                    "com.miui.securitycenter",
                    "com.miui.permcenter.permissions.PermissionsEditorActivity"
            ));
            intent.putExtra("extra_pkgname", context.getPackageName());
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(context, "无法打开设置", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 打开自启动设置页面
     *
     * @param context
     */
    public static void openRedmiAutoStartSetting(@NotNull Context context) {
        try {
            Intent intent = new Intent();
            intent.setComponent(new ComponentName(
                    "com.miui.securitycenter",
                    "com.miui.permcenter.autostart.AutoStartManagementActivity"
            ));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(context, "无法打开设置", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 打开省电优化设置页面
     *
     * @param context
     */
    public static void openBatteryOptimizationSetting(Context context) {
        try {
            Intent intent = new Intent();
            intent.setComponent(new ComponentName(
                    "com.miui.powerkeeper",
                    "com.miui.powerkeeper.ui.HiddenAppsConfigActivity"
            ));
            intent.putExtra("package_name", context.getPackageName());
            intent.putExtra("package_label", context.getString(context.getApplicationInfo().labelRes));
            intent.putExtra("package_label", context.getString(context.getApplicationInfo().labelRes));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(context, "无法打开设置", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 打开无障碍服务设置页面
     *
     * @param context
     */
    public static void openAccessibility_settings(@NotNull Context context) {
        // 引导用户到无障碍服务设置页面
        Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }
}

package com.nothing.assist;

import android.content.Context;
import android.content.SharedPreferences;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

public class DataService {
    private static final String PREFS_NAME = "com.nothing.assist";
    private static final String LAST_SIGN_TIME = "last_sign_time";
    private static final String SIGN_COUNT = "sign_count";

    private final SharedPreferences prefs;

    public DataService(Context context) {
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public void setLastSignTime(Date date) {
        // 存储最后签到时间
        prefs.edit().putLong(LAST_SIGN_TIME, date.getTime()).apply();
    }

    public Date getLastSignTime() {
        // 获取最后签到时间
        long time = prefs.getLong(LAST_SIGN_TIME, 0);
        return time == 0 ? null : new Date(time);
    }

    public void setSignCount(int qdcs) {
        // 存储签到次数
        prefs.edit().putInt(SIGN_COUNT, qdcs).apply();
    }

    public int getSignCount() {
        // 获取签到次数, 先判断最后签到时间是否为今天, 如果不是则重置签到次数
        Date lastSignTime = getLastSignTime();
        if (lastSignTime == null) {
            return -1;
        }
        LocalDate lastSignDate = lastSignTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate now = LocalDate.now();
        if (!lastSignDate.isEqual(now)) {
            setSignCount(0);
        }
        return prefs.getInt(SIGN_COUNT, 0);
    }

    public void cleanData() {
        // 清除数据
        prefs.edit().clear().apply();
    }
}
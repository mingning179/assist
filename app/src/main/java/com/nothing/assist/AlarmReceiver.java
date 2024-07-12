package com.nothing.assist;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent i = new Intent(context, AssistService.class);
        i.setAction(AssistService.ACTION_NOTIFICATION_TASK);
        context.startService(i);
    }
}
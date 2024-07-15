package com.nothing.assist;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AlarmReceiver extends BroadcastReceiver {
    private final static String TAG="Receiver";
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent i = new Intent(context, AssistService.class);
        Log.i(TAG,"收到广播:action="+intent.getAction());
        i.setAction(intent.getAction());
        context.startService(i);
    }
}
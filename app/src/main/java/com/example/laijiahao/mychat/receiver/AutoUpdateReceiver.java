package com.example.laijiahao.mychat.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.laijiahao.mychat.service.AutoUpdateService;

/**
 * Created by laijiahao on 16/10/3.
 */
public class AutoUpdateReceiver extends BroadcastReceiver{
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent i = new Intent(context, AutoUpdateService.class);
        context.startService(i);
    }
}

package com.example.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            Log.d("BootReceiver", "Device booted, starting service...");
            Intent serviceIntent = new Intent(context, MySocketService.class);
            context.startService(serviceIntent);
        }
    }
}

package com.example.app;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;

import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

public class AppMonitorService extends Service {

    private static final String CHANNEL_ID = "example_channel";
    private UsageStatsManager usageStatsManager;

    @Override
    public void onCreate() {
        super.onCreate();
        usageStatsManager = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);

        // Create the notification channel for Android O and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "App Monitor Channel";
            String description = "Channel for monitoring app usage.";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }

        // Start monitoring app usage in a background thread
        monitorAppUsage();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;  // Not used for binding
    }

    private void monitorAppUsage() {
        new Thread(() -> {
            while (true) {
                long currentTime = System.currentTimeMillis();
                long oneHourAgo = currentTime - 1000 * 3600; // Check for the last hour

                List<UsageStats> usageStatsList = usageStatsManager.queryUsageStats(
                        UsageStatsManager.INTERVAL_DAILY, oneHourAgo, currentTime);

                if (usageStatsList != null && !usageStatsList.isEmpty()) {
                    SortedMap<Long, UsageStats> sortedStats = new TreeMap<>();
                    for (UsageStats usageStats : usageStatsList) {
                        sortedStats.put(usageStats.getLastTimeUsed(), usageStats);
                    }

                    if (!sortedStats.isEmpty()) {
                        UsageStats recentStats = sortedStats.get(sortedStats.lastKey());
                        String recentApp = recentStats.getPackageName();
                        showNotification(recentApp);
                    }
                }

                try {
                    Thread.sleep(5000);  // Check every 5 seconds
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void showNotification(String recentApp) {
        Notification notification = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notification = new Notification.Builder(this, CHANNEL_ID)
                    .setContentTitle("App Opened")
                    .setContentText("The most recent app opened is: " + recentApp)
                    .setSmallIcon(android.R.drawable.ic_dialog_info)
                    .build();
        }

        // Show the notification
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        startForeground(1, notification);
    }
}

package com.example.app;

import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;

import java.util.List;
public class PermissionHelper {

    // Check if the app has permission to access usage stats
    public static boolean isUsageStatsPermissionGranted(Context context) {
        long now = System.currentTimeMillis();
        UsageStatsManager usm = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
        List<UsageStats> stats = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, now - 1000 * 60, now);
        return stats != null && !stats.isEmpty();
    }


    // Open the usage access settings page for the user to grant permission
    public static void requestUsageStatsPermission(Context context) {
        Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
        context.startActivity(intent);
    }
}

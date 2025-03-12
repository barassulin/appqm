package com.example.app;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.app.usage.UsageStats;
import android.util.Log;
import android.widget.Toast;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

public class AppUsageMonitor {
    private UsageStatsManager usageStatsManager;
    private Context context; // Store the context for Toast

    public AppUsageMonitor(Context context) {
        this.context = context; // Save context
        usageStatsManager = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
    }
    public String getRecentApps() {
        if (usageStatsManager == null) {
            Log.e("AppUsageMonitor", "UsageStatsManager is null.");
            return "Error: UsageStatsManager not initialized";
        }

        long currentTime = System.currentTimeMillis();
        long oneHourAgo = currentTime - 1000 * 3600; // Check last 1 hour

        List<UsageStats> usageStatsList = usageStatsManager.queryUsageStats(
                UsageStatsManager.INTERVAL_DAILY, oneHourAgo, currentTime);

        if (usageStatsList == null || usageStatsList.isEmpty()) {
            Toast.makeText(context, "No usage stats available.", Toast.LENGTH_SHORT).show();
            return "No recent apps available";
        }

        // Sort usage stats by last used time
        SortedMap<Long, UsageStats> sortedUsageStats = new TreeMap<>();
        for (UsageStats usageStats : usageStatsList) {
            sortedUsageStats.put(usageStats.getLastTimeUsed(), usageStats);
        }

        if (!sortedUsageStats.isEmpty()) {
            UsageStats recentStats = sortedUsageStats.get(sortedUsageStats.lastKey());

            String appName = recentStats.getPackageName();
            long lastUsed = recentStats.getLastTimeUsed();

            // Log to console
            Log.d("AppUsageMonitor", "Most recently used app: " + appName + ", Last time used: " + lastUsed);

            return "Last used app: " + appName;  // Return the app name as a string
        }

        return "No recent apps available";
    }

    public void logRecentAppLaunches() {
        if (usageStatsManager == null) {
            Log.e("AppUsageMonitor", "UsageStatsManager is null.");
            return;
        }

        long currentTime = System.currentTimeMillis();
        long oneHourAgo = currentTime - 1000 * 3600; // Check last 1 hour

        List<UsageStats> usageStatsList = usageStatsManager.queryUsageStats(
                UsageStatsManager.INTERVAL_DAILY, oneHourAgo, currentTime);

        if (usageStatsList == null || usageStatsList.isEmpty()) {
            Toast.makeText(context, "No usage stats available.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Sort usage stats by last used time
        SortedMap<Long, UsageStats> sortedUsageStats = new TreeMap<>();
        for (UsageStats usageStats : usageStatsList) {
            sortedUsageStats.put(usageStats.getLastTimeUsed(), usageStats);
        }

        if (!sortedUsageStats.isEmpty()) {
            UsageStats recentStats = sortedUsageStats.get(sortedUsageStats.lastKey());

            String appName = recentStats.getPackageName();
            long lastUsed = recentStats.getLastTimeUsed();

            // Log to console
            Log.d("AppUsageMonitor", "Most recently used app: " + appName + ", Last time used: " + lastUsed);

            // Show a Toast message with the app name
            Toast.makeText(context, "Last used app: " + appName, Toast.LENGTH_LONG).show();
        }
    }
}
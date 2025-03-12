package com.example.app;

import android.app.usage.UsageStatsManager;
import android.content.Context;

public class UsageStatsHelper {
    private UsageStatsManager usageStatsManager;

    public UsageStatsHelper(Context context) {
        usageStatsManager = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
    }

    public UsageStatsManager getUsageStatsManager() {
        return usageStatsManager;
    }
}


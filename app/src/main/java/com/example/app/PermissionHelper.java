package com.example.app;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.os.Process;
import android.provider.Settings;

public class PermissionHelper {

    // Check if the app has permission to access usage stats
    public static boolean isUsageStatsPermissionGranted(Context context) {
        AppOpsManager appOps = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(), context.getPackageName());
        return mode == AppOpsManager.MODE_ALLOWED;
    }

    // Open the usage access settings page for the user to grant permission
    public static void requestUsageStatsPermission(Context context) {
        Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
        context.startActivity(intent);
    }
}

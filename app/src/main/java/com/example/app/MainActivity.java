package com.example.app;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Build;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;
import android.view.WindowManager;
import android.graphics.PixelFormat;
import android.view.View;
import android.view.LayoutInflater;
import android.content.pm.PackageManager;
import android.content.pm.ApplicationInfo;
import android.util.Log;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

public class MainActivity extends AppCompatActivity {
    private TextView appUsageTextView;
    private AppUsageMonitor appUsageMonitor;
    private Handler handler = new Handler();
    private Runnable updateTask;
    private static final int CODE_DRAW_OVER_OTHER_APP_PERMISSION = 2084;
    private View floatingView;
    private WindowManager windowManager;

    private ActivityResultLauncher<Intent> overlayPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Settings.canDrawOverlays(this)) {
                            Log.d("Overlay", "Permission granted");
                        } else {
                            Log.e("Overlay", "Permission denied");
                        }
                    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        appUsageTextView = findViewById(R.id.appUsageTextView);

        // Check and request Usage Stats permission
        if (!PermissionHelper.isUsageStatsPermissionGranted(this)) {
            PermissionHelper.requestUsageStatsPermission(this);
        }

        // Check and request Overlay permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            systemOverlay();
        }

        // Initialize app usage monitor
        appUsageMonitor = new AppUsageMonitor(this);

        // Runnable task to update the TextView every 3 seconds
        updateTask = new Runnable() {
            @Override
            public void run() {
                String recentApps = appUsageMonitor.getRecentApps();
                if (recentApps != null && !recentApps.isEmpty()) {
                    String appName = getAppNameFromPackage(recentApps);
                    if (appName.equalsIgnoreCase("chrome")) {
                        showFloatingWindow();
                    }
                    appUsageTextView.setText(recentApps);
                }
                handler.postDelayed(this, 3000);
            }
        };

        // Start updating the app list
        handler.post(updateTask);
    }

    private void systemOverlay() {
        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:" + getPackageName()));
        overlayPermissionLauncher.launch(intent);
    }

    private void showFloatingWindow() {
        if (windowManager == null) {
            windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        }

        if (floatingView == null) {
            floatingView = LayoutInflater.from(this).inflate(R.layout.floating_view, null);

            WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT
            );

            windowManager.addView(floatingView, params);
        }
    }

    private String getAppNameFromPackage(String packageName) {
        if (packageName == null || packageName.isEmpty()) {
            return "Unknown";
        }
        String[] myArray = packageName.split("[.]");
        return myArray[myArray.length - 1];
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(updateTask);
    }
}

package com.example.app;

import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private TextView appUsageTextView;
    private AppUsageMonitor appUsageMonitor;
    private Handler handler = new Handler();
    private Runnable updateTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        appUsageTextView = findViewById(R.id.appUsageTextView);

        // Check if Usage Stats Permission is granted
        if (!PermissionHelper.isUsageStatsPermissionGranted(this)) {
            PermissionHelper.requestUsageStatsPermission(this);
        }

        // Initialize the app usage monitor
        appUsageMonitor = new AppUsageMonitor(this);

        // Runnable task to update the TextView every 5 seconds
        updateTask = new Runnable() {
            @Override
            public void run() {
                // Get the most recent app info
                String recentApps = appUsageMonitor.getRecentApps();

                // Update the TextView with the recent app name
                appUsageTextView.setText(recentApps);

                // Repeat every 5 seconds
                handler.postDelayed(this, 3000);

            }
        };

        // Start updating the app list
        handler.post(updateTask);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(updateTask); // Stop the handler when the app closes
    }
}
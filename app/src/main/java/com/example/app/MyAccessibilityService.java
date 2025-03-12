package com.example.app;

import android.accessibilityservice.AccessibilityService;
import android.view.accessibility.AccessibilityEvent;
import android.util.Log;

public class MyAccessibilityService extends AccessibilityService {

    // This method will be called whenever there is an accessibility event
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        // We are interested in the event type when the window (app) state changes
        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            // Get the package name of the app that is currently in the foreground
            String packageName = event.getPackageName().toString();
            // Log the package name of the app that has just been launched or switched to
            Log.d("AppLaunch", "Launched or switched to app: " + packageName);
        }
    }

    // This method is called when the service is interrupted (e.g., stopped or system interrupts)
    @Override
    public void onInterrupt() {
        // Handle interruption if needed (e.g., cleanup or release resources)
        Log.d("AppLaunch", "AccessibilityService interrupted.");
    }
}

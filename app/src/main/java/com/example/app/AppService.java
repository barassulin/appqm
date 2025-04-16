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
import io.socket.emitter.Emitter;
import org.json.JSONObject;
import org.json.JSONException;
import android.content.pm.PackageManager;
import android.content.pm.ApplicationInfo;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import org.json.JSONObject;
import org.json.JSONException;
import java.net.URISyntaxException;
import android.util.Log;
import android.util.Log;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.app.NotificationCompat;

import io.socket.client.IO;
import io.socket.client.Socket;
import java.net.URISyntaxException;

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

public class AppService extends Service {
    private Handler handler = new Handler();
    private Runnable updateTask;
    private Socket mSocket;
    private View floatingView;
    private WindowManager windowManager;
    private AppUsageMonitor appUsageMonitor;
    private String Listi = "";

    @Override
    public void onCreate() {
        super.onCreate();
        appUsageMonitor = new AppUsageMonitor(this);
        mSocket = create_socket();
        connect(mSocket);
        identify(mSocket);
        recv(mSocket);
        startLoop();
    }

    private void startLoop() {
        updateTask = new Runnable() {
            @Override
            public void run() {
                String recentApps = appUsageMonitor.getRecentApps();
                if (recentApps != null && !recentApps.isEmpty()) {
                    String appName = getAppNameFromPackage(recentApps);
                    Log.d("AppDebug", "recentApps returned: " + recentApps);  // <--- Add this
                    Log.d("AppDebug", "listi: " + Listi);  // <--- Add this
                    if (Listi.contains(appName)) {
                        showFloatingWindow();
                    } else if (floatingView != null && windowManager != null) {
                        windowManager.removeView(floatingView);
                        floatingView = null;
                    }
                }
                handler.postDelayed(this, 3000);
            }
        };
        handler.post(updateTask);
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
            if (!Settings.canDrawOverlays(this)) {
                Log.e("AppService", "Overlay permission not granted.");
                return;
            }
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

    public Socket create_socket() {
        try {
            return IO.socket("http://10.0.2.2:20004");
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void connect(Socket socket) {
        try {
            socket.connect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void identify(Socket socket) {
        socket.emit("identify", "name pass");

    }

    public void recv(Socket socket) {
        socket.on("update", args -> {
            if (args.length > 0) {
                Log.d("SocketIO", "Received: " + args[1].toString());
                Listi = args[1].toString();
            }

        });
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Create the channel first (if not already done)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "channel_id",
                    "App Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }

// Build a proper notification
        Notification notification = new NotificationCompat.Builder(this, "channel_id")
                .setContentTitle("Monitoring Apps")
                .setContentText("Service is running in the background")
                .setSmallIcon(android.R.drawable.ic_dialog_info) // Use a known valid system icon
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .build();

        startForeground(1, notification);


        startForeground(1, notification); // Required!
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        if (mSocket != null) {
            mSocket.disconnect();
        }
        handler.removeCallbacks(updateTask);
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}

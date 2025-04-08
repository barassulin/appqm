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
import io.socket.client.IO;
import io.socket.client.Socket;
import java.net.URISyntaxException;




public class MainActivity extends AppCompatActivity {
    private TextView appUsageTextView;
    private AppUsageMonitor appUsageMonitor;
    private Handler handler = new Handler();
    private Runnable updateTask;
    private static final int CODE_DRAW_OVER_OTHER_APP_PERMISSION = 2084;
    private View floatingView;
    private WindowManager windowManager;
    private Socket mSocket;

    private ActivityResultLauncher<Intent> overlayPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Settings.canDrawOverlays(this)) {
                            Log.d("Overlay", "Permission granted");
                        } else {
                            Log.e("Overlay", "Permission denied");
                        }
                    });
    public Socket create_socket(){
        Socket socket;
        try {
            socket = IO.socket("http://10.0.2.2:20004");
        } catch (URISyntaxException e) {
            Log.e("SocketIO", "Failed", e);
            e.printStackTrace();
            socket = null;
        }
        return socket;
    }

    public void connect(Socket socket){
        try {
            socket.connect();
            Log.d("SocketIO", "Socket connection initiated.");
        } catch (Exception e) {
            Log.e("SocketIO", "Error connecting to the server", e);
        }
    }

    public void recv(Socket socket){
        try {
            socket.on("new message", onNewMessage);
            Log.d("SocketIO", "Listener for 'new message' added successfully.");
        } catch (Exception e) {
            Log.e("SocketIO", "Error adding 'new message' listener", e);
        }
    }


    public void disconnect(Socket socket) {
        try {
            socket.disconnect();
            Log.d("SocketIO", "Socket disconnection initiated.");
        } catch (Exception e) {
            Log.e("SocketIO", "Error disconnecting to the server", e);
        }
    }
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

        mSocket = create_socket();

        // Runnable task to update the TextView every 3 seconds
        updateTask = new Runnable() {
            @Override
            public void run() {
                String recentApps = appUsageMonitor.getRecentApps();
                if (recentApps != null && !recentApps.isEmpty()) {
                    String appName = getAppNameFromPackage(recentApps);

                    if (mSocket != null)
                    {
                        connect(mSocket);
                        attemptSend(appName, mSocket);
                        recv(mSocket);
                    }
                    if (appName.equalsIgnoreCase("chrome")) {
                        showFloatingWindow();
                    }
                    else if (floatingView != null && windowManager != null) {
                            windowManager.removeView(floatingView);
                            floatingView = null;
                    }
                    appUsageTextView.setText(appName);
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

    private Emitter.Listener onNewMessage = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    String message;
                    try {
                        message = data.getString("message");
                        Log.d("SocketIO", "msg:" + message);
                        System.out.println(message);

                    } catch (JSONException e) {
                        Log.e("SocketIO", "Failed to parse message", e);
                        System.out.println("fail");
                        return;
                    }

                }
            });
        }
    };

    private void attemptSend(String message, Socket socket) {
        if (socket.connected()) {
            socket.emit("new message", message);  // Send message if connected
        } else {
            Log.e("SocketIO", "Socket is not connected. Message not sent.");
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        mSocket.disconnect();
        mSocket.off("new message", onNewMessage);
        handler.removeCallbacks(updateTask);
    }
}

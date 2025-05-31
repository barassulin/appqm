package com.example.app;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.graphics.PixelFormat;
// import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

public class FloatingViewService extends Service {

    private WindowManager windowManager;
    private View floatingView;

    @Override
    public void onCreate() {
        super.onCreate();
        startForegroundService();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        // This service doesn't bind to anything, so we return null
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        showFloatingView();
        return START_STICKY;  // Keeps the service running even if the activity is destroyed
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (windowManager != null && floatingView != null) {
            windowManager.removeView(floatingView);
        }
    }

    private void startForegroundService() {
        // Create a notification to run the service in the foreground
        NotificationCompat.Builder notification = new NotificationCompat.Builder(this, "default")
                .setContentTitle("Floating View Service")
                .setContentText("The service is running")
                .setSmallIcon(R.mipmap.ic_launcher);

        startForeground(1, notification.build());  // Start the service as a foreground service
    }

    private void showFloatingView() {
        if (floatingView == null) {
            windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

            // Create a new floating view
            floatingView = LayoutInflater.from(this).inflate(R.layout.floating_view, null);

            // Set the layout parameters for the floating view
            WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY, // Makes the view float over other apps
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, // Makes the view not focusable
                    PixelFormat.TRANSLUCENT
            );

            // Add the floating view to the window manager
            windowManager.addView(floatingView, params);
        }
    }
}

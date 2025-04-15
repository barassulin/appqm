package com.example.app;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.Build;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.WindowManager;
import android.view.View;
import android.graphics.PixelFormat;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import android.util.Log;
import java.net.URISyntaxException;

public class MySocketService extends Service {
    private Socket mSocket;
    private View floatingView;
    private WindowManager windowManager;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mSocket = create_socket();
        connect(mSocket);

        // Show floating window
        showFloatingWindow();

        // Listen for server messages
        mSocket.on("messageFromServer", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                if (args.length > 0 && args[0] != null) {
                    String receivedMessage = args[0].toString();
                    Log.d("SocketService", "Received: " + receivedMessage);
                    // Handle the received message and show the floating view if needed
                }
            }
        });

        // Keep the service running in the foreground to prevent being killed
        startForeground(1, new Notification.Builder(this)
                .setContentTitle("App Running in Background")
                .setContentText("Socket service is running")
                .setSmallIcon(R.mipmap.ic_launcher)
                .build());

        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private Socket create_socket() {
        try {
            return IO.socket("http://10.0.2.2:20003");  // Replace with your server URL
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void connect(Socket socket) {
        if (socket != null) {
            socket.connect();
            Log.d("SocketService", "Socket connected.");
        }
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
}

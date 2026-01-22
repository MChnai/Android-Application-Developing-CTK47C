package com.example.neuro_gamesense1.coreService;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.neuro_gamesense1.R;

public class CoreService extends Service {

    private WindowManager windowManager;
    private View overlayView;
    private SystemMonitor systemMonitor;
    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable updateRunnable;
    private DataLogger dataLogger;
    private TextView txtCpuTemp, txtRam, txtCurrent;
    private int initialX, initialY;
    private View viewHandle; // Thêm biến cho cái thanh gạt
    private View layoutStats;
    private float initialTouchX;
    private float startTouchX; // Để đo khoảng cách vuốt
    private boolean isPanelShowing = false;
    private static final float W_TEMP = 0.945123f;
    private static final float W_CURRENT = 0.000102f;
    private static final float W_INTERACT = 0.000004f;
    private static final float W_TEMP_SQ = -0.001200f;
    private static final float BIAS = 2.150000f;
    private float[] currentBuffer = new float[30];
    private int bufferIndex = 0;
    private float currentSum = 0;
    @Override
    public void onCreate()
    {
        super.onCreate();
        systemMonitor = new SystemMonitor(this);
        dataLogger = new DataLogger(this);
        startMyForeground();
        setupOverlay();
        startDataLoop();
    }

    private void startMyForeground()
    {
        String channelId = "neuro_game_sense_channel";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "Neuro GameSense Service",
                    NotificationManager.IMPORTANCE_LOW
            );
            getSystemService(NotificationManager.class).createNotificationChannel(channel);
        }

        Notification notification = new NotificationCompat.Builder(this, channelId)
                .setContentTitle("Neuro-GameSense Running")
                .setContentText("Monitoring system performance and AI context...")
                .setSmallIcon(R.drawable.ic_launcher_foreground) // Ensure you have an icon here
                .build();

        startForeground(1, notification);
    }

    private void setupOverlay() {
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        int layoutType = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                layoutType,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
        );

        params.gravity = Gravity.START | Gravity.TOP;
        params.x = 0;
        params.y = 0;

        overlayView = LayoutInflater.from(this).inflate(R.layout.overlay_debug, null);

        viewHandle = overlayView.findViewById(R.id.viewHandle);
        layoutStats = overlayView.findViewById(R.id.layoutStats);
        txtCpuTemp = overlayView.findViewById(R.id.txtCpuTemp);
        txtRam = overlayView.findViewById(R.id.txtRam);
        txtCurrent = overlayView.findViewById(R.id.txtCurrent);

        viewHandle.setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        startTouchX = event.getRawX();
                        return true;

                    case MotionEvent.ACTION_UP:
                        float endTouchX = event.getRawX();
                        float distance = endTouchX - startTouchX;

                        if (distance > 50) {
                            showPanel();
                        }
                        else {
                            togglePanel();
                        }
                        return true;
                }
                return false;
            }
        });

        windowManager.addView(overlayView, params);
    }

    private void showPanel() {
        layoutStats.setVisibility(View.VISIBLE);
        viewHandle.setAlpha(0.2f);
        isPanelShowing = true;
    }

    private void hidePanel() {
        layoutStats.setVisibility(View.GONE);
        viewHandle.setAlpha(0.6f);
        isPanelShowing = false;
    }

    private void togglePanel() {
        if (isPanelShowing) {
            hidePanel();
        } else {
            showPanel();
        }
    }
    private void startDataLoop() {
        updateRunnable = new Runnable() {
            @Override
            public void run() {
                updateUI();
                // Repeat every 1 second (1000ms)
                handler.postDelayed(this, 1000);
            }
        };
        handler.post(updateRunnable);
    }

    @SuppressLint({"DefaultLocale", "SetTextI18n"})
    private void updateUI() {
        if (overlayView != null) {
            float currentTemp = systemMonitor.getBatteryTemperature();
            int rawCurrent = systemMonitor.getBatteryCurrentNow();

            currentSum -= currentBuffer[bufferIndex];
            currentBuffer[bufferIndex] = rawCurrent;
            currentSum += rawCurrent;
            bufferIndex = (bufferIndex + 1) % 30;

            float smoothCurrent = currentSum / 30.0f;
            float interaction = currentTemp * smoothCurrent;
            float tempSq = currentTemp * currentTemp;

            float predictedTemp = (currentTemp * W_TEMP) +
                    (smoothCurrent * W_CURRENT) +
                    (interaction * W_INTERACT) +
                    (tempSq * W_TEMP_SQ) +
                    BIAS;

            txtCpuTemp.setText(String.format("Temp: %.1f°C", currentTemp));
            txtCurrent.setText("Pwr: " + rawCurrent + " mA");

            if (currentTemp >= 40.0f) {
                txtRam.setText("OVERHEAT: " + currentTemp + "°C");
                txtRam.setTextColor(Color.RED);
            }
            else if (currentTemp >= 38.0f) {
                txtRam.setText("HOT: High Temp");
                txtRam.setTextColor(Color.parseColor("#FF9800"));
            }
            else if (predictedTemp > currentTemp + 0.8f) {
                txtRam.setText("Trend: Heating Up! (->" + String.format("%.1f", predictedTemp) + ")");
                txtRam.setTextColor(Color.YELLOW);
            }
            else if (predictedTemp < -0.1) {
                txtRam.setText("Status: Cooling");
                txtRam.setTextColor(Color.CYAN);
            }
            else {
                txtRam.setText("Status: Stable");
                txtRam.setTextColor(Color.GREEN);
            }

            dataLogger.logData(currentTemp, rawCurrent, systemMonitor.getRamStatus());
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (windowManager != null && overlayView != null) {
            windowManager.removeView(overlayView);
        }
        handler.removeCallbacks(updateRunnable);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
package com.example.neuro_gamesense1.coreService;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import com.example.neuro_gamesense1.R;

public class CoreService extends Service {
    private SystemMonitor systemMonitor;
    private DataLogger dataLogger;
    private OverlayManager overlayManager;
    private ThermalBrain thermalBrain;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable updateRunnable;

    @Override
    public void onCreate() {
        super.onCreate();
        systemMonitor = new SystemMonitor(this);
        dataLogger = new DataLogger(this);
        thermalBrain = new ThermalBrain();
        overlayManager = new OverlayManager(this);

        startMyForeground();
        startDataLoop();
    }

    private void startDataLoop() {
        updateRunnable = new Runnable() {
            @Override
            public void run() {
                float temp = systemMonitor.getBatteryTemperature();
                int rawCurrent = systemMonitor.getBatteryPercent();
                float smoothCurrent = thermalBrain.getSmoothedCurrent(rawCurrent);
                double pared = thermalBrain.getAiPrediction(temp, smoothCurrent);
                String advice = thermalBrain.getThermalAdvice(temp, pared);

                overlayManager.updateDisplay(temp, rawCurrent, advice);
                dataLogger.logData(temp, rawCurrent, systemMonitor.getRamStatus());

                handler.postDelayed(this, calculateInterval(temp, rawCurrent));
            }
        };
        handler.post(updateRunnable);
    }

    private int calculateInterval(float temp, int currentmA) {
        if (temp > 39.0f || Math.abs(currentmA) > 500) return 500;
        if (temp < 35.0f && Math.abs(currentmA) < 200) return 2000;
        return 1000;
    }

    private void startMyForeground() {
        String channelId = "neuro_game_sense_channel";
        NotificationChannel channel = new NotificationChannel(channelId, "Neuro GameSense", NotificationManager.IMPORTANCE_LOW);
        getSystemService(NotificationManager.class).createNotificationChannel(channel);

        Notification notification = new NotificationCompat.Builder(this, channelId)
                .setContentTitle("Neuro-GameSense Running")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .build();
        startForeground(1, notification);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        overlayManager.remove();
        handler.removeCallbacks(updateRunnable);
    }

    @Nullable @Override public IBinder onBind(Intent intent) { return null; }
}
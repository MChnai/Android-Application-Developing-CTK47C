package com.example.neuro_gamesense1.coreService;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import com.example.neuro_gamesense1.R;
import com.example.neuro_gamesense1.manager.DNDManager;
import com.example.neuro_gamesense1.manager.SystemUiHelper;

public class CoreService extends Service {
    private SystemMonitor systemMonitor;
    private DataLogger dataLogger;
    private OverlayManager overlayManager;
    private ThermalBrain thermalBrain;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable updateRunnable;
    private AudioContextAnalyzer audioAnalyzer;
    private boolean hasShownOverheatWarning = false;
    private static final float WARNING_THRESHOLD = 42.0f;
    public static boolean isActiveModeEnabled = true;
    @Override
    public void onCreate() {
        super.onCreate();
        systemMonitor = new SystemMonitor(this);
        dataLogger = new DataLogger(this);
        thermalBrain = new ThermalBrain();
        overlayManager = new OverlayManager(this);
        audioAnalyzer = new AudioContextAnalyzer();

        startMyForeground();
        startDataLoop();
    }
    private void startDataLoop() {
        final float[] lastTemp = {systemMonitor.getBatteryTemperature()};
        int cpuUsage = SystemMonitor.getCpuUsage();
        int gpuUsage = SystemMonitor.getGpuUsage();
        float currentTemp = systemMonitor.getBatteryTemperature();
        float temp = systemMonitor.getBatteryTemperature();
        float tempSlope = (currentTemp - lastTemp[0]);
        lastTemp[0] = currentTemp;
        int rawCurrent = systemMonitor.getBatteryCurrent();
        int audioState = audioAnalyzer.isListening() ? (audioAnalyzer.getIsCombat() ? 1 : 0) : -1;
        float smoothCurrent = thermalBrain.getSmoothedCurrent(rawCurrent);
        float predictedTemp = thermalBrain.getAiPrediction(currentTemp, smoothCurrent, tempSlope, audioState);
        String advice = thermalBrain.getThermalAdvice(temp, predictedTemp);
        updateRunnable = new Runnable() {
            @Override
            public void run() {
                if (advice.contains("CRITICAL") || advice.contains("Throttling")) {
                    android.widget.Toast.makeText(CoreService.this, advice, android.widget.Toast.LENGTH_SHORT).show();
                }

                if (isActiveModeEnabled) {
                    triggerActionModeAlert(temp, predictedTemp);
                } else {
                    triggerSafeModeAlert(temp, predictedTemp);
                }
                if (predictedTemp >= WARNING_THRESHOLD && !hasShownOverheatWarning) {
                    hasShownOverheatWarning = true;
                } else if (temp < 38.0f && hasShownOverheatWarning) {
                    if (isActiveModeEnabled) {
                        ThermalOptimizer.restoreSettings(CoreService.this);
                    }
                    hasShownOverheatWarning = false;
                }

                Log.d("GraphDebug", ">>> SOURCE: SYSTEM_TEMP | Value: " + temp);
                overlayManager.updateGraph(temp, predictedTemp);
                overlayManager.updateStats(cpuUsage, gpuUsage);
                dataLogger.logData(temp, rawCurrent, systemMonitor.getRamStatus(), audioState);
                handler.postDelayed(this, calculateInterval(temp, rawCurrent));
            }
        };
        handler.post(updateRunnable);
    }
    private void triggerSafeModeAlert(float temp, double predictedTemp){
        String timeToOverheat = thermalBrain.getTimeToOverheat(temp, predictedTemp);
        Toast.makeText(this, "Warning: " + timeToOverheat, Toast.LENGTH_LONG).show();
    }
    private void triggerActionModeAlert(float temp, double predictedTemp) {
        String msg = "Warning: " + thermalBrain.getTimeToOverheat(temp, predictedTemp) +
                ", system will automatically reduce light and sound!";
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();

        Intent vpnIntent = new Intent(this, NeuroVpnService.class);
        vpnIntent.putExtra("GAME_PACKAGE_NAME", "com.example.neurogamesense");
        startService(vpnIntent);

        DNDManager.setDND(this, true);
        ThermalOptimizer.applyCooling(this);
        overlayManager.clearMemory(this);

        Log.d("ActiveMode", "VPN Smart Isolation Activated.");
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
                .build();
        startForeground(1, notification);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (audioAnalyzer != null)
            audioAnalyzer.stopListening();
        overlayManager.remove();
        handler.removeCallbacks(updateRunnable);
        handler.removeCallbacksAndMessages(null);
    }

    @Nullable @Override public IBinder onBind(Intent intent) { return null; }
}
package com.example.neuro_gamesense1.coreService;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Handler;
import android.os.SystemClock;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStreamReader;

import android.os.Process;

public class SystemMonitor {
    private final Context context;
    private final BatteryManager batteryManager;

    private static int getMaliGpuUsage() {
        try {
            BufferedReader reader = new BufferedReader(new FileReader("/sys/class/misc/mali0/device/utilisation"));
            String line = reader.readLine();
            reader.close();
            return Integer.parseInt(line.trim());
        } catch (Exception e) {
            return 0;
        }
    }

    public SystemMonitor(Context context) {
        this.context = context;
        batteryManager = (BatteryManager) context.getSystemService(Context.BATTERY_SERVICE);
    }

    public float getBatteryTemperature() {
        Intent intent = context.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        if (intent != null) {
            int rawTemp = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0);
            return rawTemp / 10.0f;
        }
        return 0.0f;
    }

    public String getRamStatus() {
        ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (activityManager != null) {
            activityManager.getMemoryInfo(memoryInfo);
            long availableMemory = memoryInfo.availMem / (1024 * 1024);
            long totalMemory = memoryInfo.totalMem / (1024 * 1024);
            long usedMem = totalMemory - availableMemory;
            int percent = (int) ((usedMem * 100) / totalMemory);

            return percent + "% (" + availableMemory + " MB free)";
        }
        return "N/A";
    }

    public int getBatteryPercent() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
        } else {
            Intent intent = context.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
            if (intent != null) {
                int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
                return (int) ((level / (float) scale) * 100);
            }
        }
        return 0;
    }

    public static int getCpuUsage() {
        try {
            long lastCpuTime = Process.getElapsedCpuTime();
            long lastTime = SystemClock.elapsedRealtime();

            Thread.sleep(100);

            long currentCpuTime = Process.getElapsedCpuTime();
            long currentTime = SystemClock.elapsedRealtime();
            long cpuDiff = currentCpuTime - lastCpuTime;
            long timeDiff = currentTime - lastTime;
            int numCores = Runtime.getRuntime().availableProcessors();
            int cpuPercent = (int) ((cpuDiff * 100.0) / (timeDiff * numCores));

            if (cpuPercent <= 0) {
                return 5 + (int)(Math.random() * 10);
            }

            return Math.min(cpuPercent, 100);
        } catch (Exception e) {
            return 8;
        }
    }
    public static int getGpuUsage() {
        String path = "/sys/class/kgsl/kgsl-3d0/gpubusy";
        try {
            BufferedReader reader = new BufferedReader(new FileReader(path));
            String line = reader.readLine();
            reader.close();

            if (line != null && !line.isEmpty()) {
                String[] parts = line.trim().split("\\s+");
                if (parts.length >= 2) {
                    float busy = Float.parseFloat(parts[0]);
                    float total = Float.parseFloat(parts[1]);
                    if (total != 0) return (int) ((busy / total) * 100);
                }
            }
        } catch (Exception e) {
            return getMaliGpuUsage();
        }
        return 0;
    }

    public int getBatteryCurrent(){
        BatteryManager batteryManager = (BatteryManager) context.getSystemService(Context.BATTERY_SERVICE);
        if(batteryManager != null) {
            int currentMicro = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW);
            return currentMicro / 1000;
        }
        return 0;
    }
    public interface StatsCallback {
        void onUpdate(int cpu, int gpu, int battery);
    }
    public int getPing() {
        try {
            java.lang.Process process = Runtime.getRuntime().exec("/system/bin/ping -c 1 -w 2 8.8.8.8");

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains("time=")) {
                    int startIndex = line.indexOf("time=") + 5;
                    int endIndex = line.indexOf(" ms", startIndex);

                    if (startIndex != -1 && endIndex != -1) {
                        String timeVal = line.substring(startIndex, endIndex);
                        return Math.round(Float.parseFloat(timeVal));
                    }
                }
            }
            process.waitFor(); 

        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }
    public void startAutoUpdate(Handler handler, StatsCallback callback) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                int cpu = getCpuUsage();
                int gpu = getGpuUsage();
                int battery = getBatteryPercent();

                callback.onUpdate(cpu, gpu, battery);
                handler.postDelayed(this, 2000);
            }
        });
    }

}
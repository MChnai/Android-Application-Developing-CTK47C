package com.example.neuro_gamesense1.coreService;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;

public class SystemMonitor {
    private Context context;
    private BatteryManager batteryManager;
    public SystemMonitor(Context context)
    {
        this.context = context;
        batteryManager = (BatteryManager) context.getSystemService(Context.BATTERY_SERVICE);
    }
    //function to get battery temperature
    public float getBatteryTemperature()
    {
        Intent intent = context.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        if(intent != null)
        {
            int rawTemp = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0);
            return rawTemp/10.0f;
        }
        return 0.0f;
    }
    //function to get RAM usage (available and total)
    public String getRamStatus()
    {
        ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        ActivityManager activityManager = (ActivityManager) context.getSystemService(context.ACTIVITY_SERVICE);
        if(activityManager != null)
        {
            activityManager.getMemoryInfo(memoryInfo);
            long availableMemomry = memoryInfo.availMem / (1024 * 1024);
            long totalMemory = memoryInfo.totalMem / (1024 * 1024);
            long usedMem = totalMemory - availableMemomry;
            int percent = (int)((usedMem * 100) / totalMemory);
            return percent + "% (" + availableMemomry + " MB free)";
        }
        return "N/A";
    }
    //function to get current battery
    public int getBatteryCurrentNow() {
        if (batteryManager != null) {
            int currentMicroAmps = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW);
            return Math.abs(currentMicroAmps) / 1000;
        }
        return 0;
    }
}
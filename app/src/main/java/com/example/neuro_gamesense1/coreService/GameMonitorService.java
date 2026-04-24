package com.example.neuro_gamesense1.coreService;

import android.accessibilityservice.AccessibilityService;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import com.example.neuro_gamesense1.coreApplications.AppModel;
import com.example.neuro_gamesense1.manager.GameManager;

import java.util.List;

public class GameMonitorService extends AccessibilityService {
    private String currentPackage = "";
    private List<AppModel> installedGames;

    private void handleAppChange(String packageName){
        boolean isGame = false;
        if(installedGames == null || installedGames.isEmpty()) {
            installedGames = GameManager.getInstalledGames(this);
        }
        else{
            for(AppModel game : installedGames){
                if(game.getPackageName().equals(packageName)){
                    isGame = true;
                    break;
                }
            }
        }
        Intent intent = new Intent(this, CoreService.class);
        if(isGame){
            Log.d("GameMonitor", "Game detected: " + packageName);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent);
            } else {
                startService(intent);
            }
        }
        else{
            if(!packageName.equals(getPackageName())){
                Log.d("GameMonitor", "App detected: " + packageName);
                stopService(intent);
            }
        }
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        installedGames = GameManager.getInstalledGames(this);
        Log.d("GameMonitorService", "Service connected");
        android.content.IntentFilter filter = new android.content.IntentFilter("com.example.neuro_gamesense.ACTION_CLEAR_RECENTS");
        registerReceiver(clearReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
    }
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        int eventType = event.getEventType();
        String eventPackage = (event.getPackageName() != null) ? event.getPackageName().toString() : "";

        if (eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            if (!eventPackage.isEmpty() && !eventPackage.equals(currentPackage)) {
                currentPackage = eventPackage;

                if (!eventPackage.contains("settings") && !eventPackage.equals(getPackageName())) {
                    handleAppChange(eventPackage);
                }
            }
        }
    }
    private final android.content.BroadcastReceiver clearReceiver = new android.content.BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if ("com.example.neuro_gamesense.ACTION_CLEAR_RECENTS".equals(intent.getAction())) {
                performGlobalAction(GLOBAL_ACTION_RECENTS);
            }
        }
    };
    @Override
    public void onInterrupt() {
        Log.d("GameMonitorService", "Service interrupted");
    }
}

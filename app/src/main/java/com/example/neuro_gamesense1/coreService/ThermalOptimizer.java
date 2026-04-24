package com.example.neuro_gamesense1.coreService;

import android.content.Context;
import android.util.Log;

import com.example.neuro_gamesense1.manager.SystemUiHelper;

public class ThermalOptimizer {
    private static int savedVolume = -1;
    private static boolean isOptimized = false;
    public static void applyCooling(Context context) {
        if (isOptimized) return;
        savedVolume = SystemUiHelper.setAndGetAudioVolume(context, 5);
        SystemUiHelper.setSystemBrightness(context, 60);
        isOptimized = true;

        Log.d("ThermalOptimizer", "Cooling system applied!");
    }
    public static void restoreSettings(Context context){
        if(!isOptimized) return;

        if(savedVolume != -1)
            SystemUiHelper.setAndGetAudioVolume(context, savedVolume);

        SystemUiHelper.setSystemBrightness(context, 100);
        isOptimized = false;
    }
}

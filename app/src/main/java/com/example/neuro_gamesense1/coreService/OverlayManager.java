package com.example.neuro_gamesense1.coreService;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.PixelFormat;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.neuro_gamesense1.R;
import com.example.neuro_gamesense1.coreApplications.UtilityAdapter;
import com.example.neuro_gamesense1.coreApplications.UtilityModel;
import com.example.neuro_gamesense1.ui.PerformanceGraphView;

import java.util.ArrayList;
import java.util.List;

public class OverlayManager {
    private final WindowManager windowManager;
    private final View overlayView;
    private final View viewHandle, layoutStats;
    private boolean isPanelShowing = false;
    private float startTouchX;
    private AudioContextAnalyzer audioAnalyzer;
    private TextView txtCpuValue, txtGpuValue;

    @SuppressLint("ClickableViewAccessibility")
    public OverlayManager(Context context) {
        windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        overlayView = LayoutInflater.from(context).inflate(R.layout.overlay, null);

        viewHandle = overlayView.findViewById(R.id.viewHandle);
        layoutStats = overlayView.findViewById(R.id.layoutStats);

        int screenWidth = context.getResources().getDisplayMetrics().widthPixels;

        LinearLayout btnClearMemory = overlayView.findViewById(R.id.btnClearMemory);
        LinearLayout btnOtherGames = overlayView.findViewById(R.id.btnOtherGames);
        LinearLayout btnChooseStat = overlayView.findViewById(R.id.btnChooseStat);
        LinearLayout layoutModeSelection = overlayView.findViewById(R.id.layoutModeSelection);
        LinearLayout layoutDefaultIntro = overlayView.findViewById(R.id.layoutDefaultIntro);
        LinearLayout layoutPassiveMonitoring = overlayView.findViewById(R.id.layoutPassiveMonitoring);
        Button btnBoostSafetyMode = overlayView.findViewById(R.id.btnBoostSafetyMode);
        Button btnBoostActiveMode = overlayView.findViewById(R.id.btnBoostActiveMode);
        Button btnStopMonitoring = overlayView.findViewById(R.id.btnStopMonitoring);
        RecyclerView utilityRecycler = overlayView.findViewById(R.id.utilityRecyclerView);
        RecyclerView sidebarApps = overlayView.findViewById(R.id.sidebarApps);

        sidebarApps.setLayoutManager(new LinearLayoutManager(context, RecyclerView.VERTICAL, false));

        List<String> realApps = getInstalledApps(context);
        SidebarAppAdapter sidebarAdapter = new SidebarAppAdapter(context, realApps);
        sidebarApps.setAdapter(sidebarAdapter);

        utilityRecycler.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
        List<UtilityModel> utilities = new ArrayList<>();
        utilities.add(new UtilityModel("DND", R.drawable.ov_button));

        UtilityAdapter utilityAdapter = new UtilityAdapter(context, utilities);
        utilityRecycler.setAdapter(utilityAdapter);

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                screenWidth,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                        | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                PixelFormat.TRANSLUCENT
        );
        params.gravity = Gravity.START | Gravity.TOP;

        overlayView.setOnTouchListener((v, event) -> {
            if(event.getAction() == MotionEvent.ACTION_DOWN) {
                if (isPanelShowing)
                    hidePanel();
                return true;
            }
            return false;
        });
        viewHandle.setOnTouchListener((v, event) -> {
           switch(event.getAction()) {
               case MotionEvent.ACTION_DOWN:
                   startTouchX = event.getRawX();
                   return true;
               case MotionEvent.ACTION_UP:
                   float endX = event.getRawX();
                   if(endX - startTouchX > 30)
                       showPanel();
                   else
                       togglePanel();
                   return true;
           }
           return false;
        });
        btnClearMemory.setOnClickListener(v -> {
            clearMemory(context);
        });
        btnOtherGames.setOnClickListener(v -> {
            Intent intent = new Intent(context, com.example.neuro_gamesense1.MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
            hidePanel();
        });
        btnChooseStat.setOnClickListener(v -> {
            if (audioAnalyzer != null) audioAnalyzer.stopListening();

            if(layoutModeSelection.getVisibility() == View.VISIBLE){
                layoutModeSelection.setVisibility(View.GONE);
                layoutDefaultIntro.setVisibility(View.VISIBLE);
                layoutPassiveMonitoring.setVisibility(View.GONE);
            } else {
                layoutModeSelection.setVisibility(View.VISIBLE);
                layoutDefaultIntro.setVisibility(View.GONE);
                layoutPassiveMonitoring.setVisibility(View.GONE);
            }
        });
        btnBoostSafetyMode.setOnClickListener(v -> {
            Toast.makeText(context, "Boosting Safety Mode", Toast.LENGTH_SHORT).show();
            layoutModeSelection.setVisibility(View.GONE);
            layoutDefaultIntro.setVisibility(View.GONE);
            layoutPassiveMonitoring.setVisibility(View.VISIBLE);

            CoreService.isActiveModeEnabled = false;
        });
        btnBoostActiveMode.setOnClickListener(v -> {
            Toast.makeText(context, "Boosting Active Mode", Toast.LENGTH_SHORT).show();
            layoutModeSelection.setVisibility(View.GONE);
            layoutDefaultIntro.setVisibility(View.GONE);
            layoutPassiveMonitoring.setVisibility(View.VISIBLE);

            CoreService.isActiveModeEnabled = true;
        });
        btnStopMonitoring.setOnClickListener(v -> {
            if (audioAnalyzer != null) {
                audioAnalyzer.stopListening();
            }
            layoutPassiveMonitoring.setVisibility(View.GONE);
            layoutModeSelection.setVisibility(View.VISIBLE);
            layoutDefaultIntro.setVisibility(View.GONE);

            Intent intent = new Intent(context, CoreService.class);
            intent.putExtra("COMMAND", "STOP_MONITORING");
            context.startService(intent);
        });
        View cpuView = overlayView.findViewById(R.id.cpuChip);
        if (cpuView != null) {
            txtCpuValue = cpuView.findViewById(R.id.statValue);
            txtCpuValue.setTextColor(ContextCompat.getColor(context, R.color.light_blue));
            ImageView imgCpuIcon = cpuView.findViewById(R.id.statIcon);
            imgCpuIcon.setImageResource(R.drawable.ic_stat_cpu_white);
        }
        View gpuView = overlayView.findViewById(R.id.gpuChip);
        if (gpuView != null) {
            txtGpuValue = gpuView.findViewById(R.id.statValue);
            txtGpuValue.setTextColor(ContextCompat.getColor(context, R.color.light_blue));
            ImageView imgGpuIcon = gpuView.findViewById(R.id.statIcon);
            imgGpuIcon.setImageResource(R.drawable.ic_stat_gpu_white);
        }

        windowManager.addView(overlayView, params);
    }
    public void updateStats(int cpuUsage, int gpuUsage) {
        if (txtCpuValue != null) txtCpuValue.setText(cpuUsage + "%");
        if (txtGpuValue != null) txtGpuValue.setText(gpuUsage + "%");
    }
    public void updateGraph(float current, float predicted) {
        PerformanceGraphView graph = overlayView.findViewById(R.id.performanceGraph);
        View passiveLayout = overlayView.findViewById(R.id.layoutPassiveMonitoring);

        if (graph != null && passiveLayout != null && passiveLayout.getVisibility() == View.VISIBLE)
            graph.addDataPoint(current, predicted);
    }
    public void clearMemory(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo memInfo = new ActivityManager.MemoryInfo();

        activityManager.getMemoryInfo(memInfo);
        long memoryBefore = memInfo.availMem;

        List<ActivityManager.RunningAppProcessInfo> runningProcesses = activityManager.getRunningAppProcesses();
        int appsOptimized = 0;

        if (runningProcesses != null)
            for (ActivityManager.RunningAppProcessInfo processInfo : runningProcesses)
                if (processInfo.importance >= ActivityManager.RunningAppProcessInfo.IMPORTANCE_CACHED)
                    for (String pkg : processInfo.pkgList)
                        if (!pkg.equals(context.getPackageName()) && !pkg.contains("launcher")) {
                            activityManager.killBackgroundProcesses(pkg);
                            appsOptimized++;
                        }

        Intent intent = new Intent("com.example.neuro_gamesense.ACTION_CLEAR_RECENTS");
        context.sendBroadcast(intent);

        final int finalAppsCount = appsOptimized;
        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
            ActivityManager.MemoryInfo postMemInfo = new ActivityManager.MemoryInfo();
            activityManager.getMemoryInfo(postMemInfo);
            long releasedMB = (postMemInfo.availMem - memoryBefore) / (1024 * 1024);

            String message = (releasedMB > 0) ?
                    "Released: " + releasedMB + " MB (" + finalAppsCount + " apps)" :
                    "System Optimized: " + finalAppsCount + " bg_app_main apps";

            //Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        }, 1500);
    }
    private List<String> getInstalledApps(Context context) {
        List<String> appPackages = new ArrayList<>();
        PackageManager pm = context.getPackageManager();
        Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);

        List<ResolveInfo> availableActivities = pm.queryIntentActivities(intent, 0);

        for (ResolveInfo ri : availableActivities) {
            String packageName = ri.activityInfo.packageName;
            if (!packageName.equals(context.getPackageName()) && !packageName.contains("launcher"))
                appPackages.add(packageName);

            if (appPackages.size() >= 15) break;
        }
        return appPackages;
    }

    private void showPanel() {
        layoutStats.setVisibility(View.VISIBLE);
        viewHandle.setVisibility(View.GONE);
        isPanelShowing = true;
    }

    private void hidePanel() {
        layoutStats.setVisibility(View.GONE);
        viewHandle.setVisibility(View.VISIBLE);
        isPanelShowing = false;
    }

    private void togglePanel() {
        if (isPanelShowing)
            hidePanel();
        else showPanel();
    }

    public void remove() {
        if (windowManager != null && overlayView != null) windowManager.removeView(overlayView);
    }
}
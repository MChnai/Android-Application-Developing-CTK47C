package com.example.neuro_gamesense1.coreService;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import com.example.neuro_gamesense1.R;

public class OverlayManager {
    private final WindowManager windowManager;
    private final View overlayView;
    private final TextView txtCpuTemp, txtRam, txtCurrent;
    private final View viewHandle, layoutStats;
    private boolean isPanelShowing = false;
    private float startTouchX;

    @SuppressLint("ClickableViewAccessibility")
    public OverlayManager(Context context) {
        windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        overlayView = LayoutInflater.from(context).inflate(R.layout.overlay_debug, null);

        viewHandle = overlayView.findViewById(R.id.viewHandle);
        layoutStats = overlayView.findViewById(R.id.layoutStats);
        txtCpuTemp = overlayView.findViewById(R.id.txtCpuTemp);
        txtRam = overlayView.findViewById(R.id.txtRam);
        txtCurrent = overlayView.findViewById(R.id.txtCurrent);

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
        );
        params.gravity = Gravity.START | Gravity.BOTTOM;

        viewHandle.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                startTouchX = event.getRawX();
                return true;
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                if (event.getRawX() - startTouchX > 50) showPanel();
                else togglePanel();
                return true;
            }
            return false;
        });

        windowManager.addView(overlayView, params);
    }

    public void updateDisplay(float temp, int current, String advice) {
        txtCpuTemp.setText(String.format("Temp: %.1f°C", temp));
        txtCurrent.setText("Pwr: " + current + " mA");
        txtRam.setText(advice);

        if (temp >= 40.0f || advice.contains("CRITICAL")) txtRam.setTextColor(Color.RED);
        else if (temp >= 38.0f || advice.contains("Alert")) txtRam.setTextColor(Color.parseColor("#FF9800"));
        else txtRam.setTextColor(Color.GREEN);
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
        if (isPanelShowing) hidePanel(); else showPanel();
    }

    public void remove() {
        if (windowManager != null && overlayView != null) windowManager.removeView(overlayView);
    }
}
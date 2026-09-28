package com.example.neuro_gamesense1;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import com.example.neuro_gamesense1.coreApplications.AppModel;
import com.example.neuro_gamesense1.manager.GameDbHelper;
import com.example.neuro_gamesense1.manager.SystemUiHelper;
import com.github.mikephil.charting.charts.RadarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.RadarData;
import com.github.mikephil.charting.data.RadarDataSet;
import com.github.mikephil.charting.data.RadarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.util.ArrayList;

public class GpuSettingsActivity extends AppCompatActivity {
    private GameDbHelper dbHelper;
    private AppModel selectedGame;
    private RadarChart radarChart;
    private Button btnDefault, btnSavePower, btnBalance, btnHighQuality, btnCustom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gpu_settings_main);

        dbHelper = new GameDbHelper(this);
        radarChart = findViewById(R.id.radarChart);
        btnDefault = findViewById(R.id.btnDefault);
        btnSavePower = findViewById(R.id.btnSavePower);
        btnBalance = findViewById(R.id.btnBalance);
        btnHighQuality = findViewById(R.id.btnHighQuality);
        btnCustom = findViewById(R.id.btnCustom);
        selectedGame = (AppModel) getIntent().getSerializableExtra("SELECTED_GAME_DATA");

        setupChartAesthetics();

        if (selectedGame != null) {
            TextView txtAppName = findViewById(R.id.appName);
            txtAppName.setText(selectedGame.getName());

            int[] savedGpu = dbHelper.getGpuSettings(selectedGame.getPackageName());

            restoreInitialSettings(savedGpu[0], savedGpu[1], savedGpu[2]);
        } else {
            restoreInitialSettings(80, 80, 60);
        }

        findViewById(R.id.btnSavePower).setOnClickListener(v -> handleGpuUpdate( 30, 20, 40, btnSavePower));
        findViewById(R.id.btnBalance).setOnClickListener(v -> handleGpuUpdate( 60, 60, 60, btnBalance));
        findViewById(R.id.btnHighQuality).setOnClickListener(v -> handleGpuUpdate( 90, 80, 70, btnHighQuality));
        findViewById(R.id.btnDefault).setOnClickListener(v -> handleGpuUpdate(80, 80, 60, btnDefault));

        SystemUiHelper.hideSystemUI(getWindow());
    }
    private void handleGpuUpdate(int quality, int fps, int perf, Button button) {
        updateRadarChart(quality, fps, perf);
        if (selectedGame != null) {
            dbHelper.saveGpuSettings(selectedGame.getPackageName(), quality, fps, perf);
            updateButtonColors(button);
        }
    }
    private void setupChartAesthetics(){
        radarChart.setBackgroundColor(Color.TRANSPARENT);
        radarChart.getDescription().setEnabled(false);
        radarChart.setWebLineWidth(1f);
        radarChart.setScaleX(1.2f);
        radarChart.setScaleY(1.2f);
        radarChart.setWebColor(ContextCompat.getColor(this, R.color.dark_blue));
        radarChart.setWebAlpha(100);
        radarChart.setRotationEnabled(false);

        String[] labels = {"Quality", "FPS", "Performance"};
        XAxis xAxis = radarChart.getXAxis();

        xAxis.setTextColor(ContextCompat.getColor(this, R.color.dark_blue));
        xAxis.setTextSize(10f);
        xAxis.setTypeface(ResourcesCompat.getFont(this, R.font.noto_sans_oriya));
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));

        radarChart.getLegend().setEnabled(false);
        radarChart.getYAxis().setEnabled(false);
        radarChart.getYAxis().setAxisMinimum(0f);
        radarChart.getYAxis().setAxisMaximum(100f);
    }

    public void updateRadarChart(int quality, int fps, int perf) {
        ArrayList<RadarEntry> entries = new ArrayList<>();
        entries.add(new RadarEntry(quality));
        entries.add(new RadarEntry(fps));
        entries.add(new RadarEntry(perf));

        RadarDataSet dataSet = new RadarDataSet(entries, "");

        dataSet.setColor(ContextCompat.getColor(this, R.color.dark_blue));
        dataSet.setLineWidth(1.5f);
        dataSet.setFillColor(Color.TRANSPARENT);
        dataSet.setDrawHighlightCircleEnabled(false);

         RadarData data = new RadarData(dataSet);
         data.setDrawValues(false);

         radarChart.setData(data);
         radarChart.getYAxis().setAxisMinimum(0f);
         radarChart.getYAxis().setAxisMaximum(100f);
         radarChart.invalidate();
    }

    private void restoreInitialSettings(int quality, int fps, int perf) {
        updateRadarChart(quality, fps, perf);

        if (quality == 30 && fps == 20 && perf == 40) {
            updateButtonColors(btnSavePower);
        } else if (quality == 60 && fps == 60 && perf == 60) {
            updateButtonColors(btnBalance);
        } else if (quality == 90 && fps == 80 && perf == 70) {
            updateButtonColors(btnHighQuality);
        } else {
            updateButtonColors(btnDefault);
        }
    }
    private void updateButtonColors(Button button){
        android.content.res.ColorStateList dark_blue = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.dark_blue));
        android.content.res.ColorStateList light_blue = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.light_blue));
        Button[] allButtons = {btnDefault, btnSavePower, btnBalance, btnHighQuality, btnCustom};
        for (Button btn : allButtons) {
            if (btn == button) {
                btn.setBackgroundResource(R.drawable.btn_primary_large);
                btn.setTextColor(light_blue);
            } else {
                btn.setBackgroundResource(R.drawable.btn_primary_large_light);
                btn.setTextColor(dark_blue);
            }
        }
    }
}

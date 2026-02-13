package com.example.neuro_gamesense1;

import android.content.Intent;
import android.graphics.SurfaceTexture;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.view.TextureView;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.example.neuro_gamesense1.coreApplications.AppModel;
import com.example.neuro_gamesense1.coreApplications.GameAdapter;
import com.example.neuro_gamesense1.coreService.CoreService;
import com.example.neuro_gamesense1.coreService.SystemMonitor;
import com.example.neuro_gamesense1.manager.GameManager;
import com.example.neuro_gamesense1.manager.VideoBackgroundManager;
import com.example.neuro_gamesense1.manager.ZoomScrollListener;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private TextureView backgroundVideo;
    private RecyclerView recyclerView;
    private TextView detailTitle;
    private ImageView imgBanner;
    private Button btnStart;
    private TextView txtBatteryPercent, txtCpuValue, txtGpuValue;
    private VideoBackgroundManager videoManager;
    private List<AppModel> games;
    private LinearSnapHelper snapHelper;
    private SystemMonitor monitor;
    private final Handler updateHandler = new Handler(Looper.getMainLooper());


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        checkPermissions();
        setupVideo();
        setupGameList();
        hideSystemBars();
        startRealTimeUpdates();
    }
    private void startRealTimeUpdates() {
        monitor = new SystemMonitor(this);
        Runnable updateRunnable = new Runnable() {
            @Override
            public void run() {
                new Thread(() -> {
                    final int cpu = SystemMonitor.getCpuUsage();
                    final int gpu = SystemMonitor.getGpuUsage();
                    final int battery = monitor.getBatteryPercent();

                    runOnUiThread(() -> {
                        updateSystemStats(battery + "%", cpu + "%", gpu + "%");
                    });
                }).start();
                updateHandler.postDelayed(this, 1000);
            }
        };
        updateHandler.post(updateRunnable);
    }
    private void initViews() {
        backgroundVideo = findViewById(R.id.backgroundVideo);
        recyclerView = findViewById(R.id.listGame);
        detailTitle = findViewById(R.id.txtDetailTitle);
        imgBanner = findViewById(R.id.imgGameBanner);
        btnStart = findViewById(R.id.btnStartGame);
        txtBatteryPercent = findViewById(R.id.txtBatteryPercent);


        View batteryView = findViewById(R.id.batteryStatusInclude);
        if (batteryView != null) {
            txtBatteryPercent = batteryView.findViewById(R.id.txtBatteryPercent);
            ImageView imgBatteryIcon = batteryView.findViewById(R.id.battery_bar);
            imgBatteryIcon.setImageLevel(9000);
        }

        View cpuView = findViewById(R.id.cpuChip);
        if (cpuView != null) {
            txtCpuValue = cpuView.findViewById(R.id.statValue);
            ImageView imgCpuIcon = cpuView.findViewById(R.id.statIcon);
            imgCpuIcon.setImageResource(R.drawable.ic_cpu_chip);
        }

        View gpuView = findViewById(R.id.gpuChip);
        if (gpuView != null) {
            txtGpuValue = gpuView.findViewById(R.id.statValue);
            ImageView imgGpuIcon = gpuView.findViewById(R.id.statIcon);
            imgGpuIcon.setImageResource(R.drawable.ic_gpu_chip);
        }

        videoManager = new VideoBackgroundManager(this);
    }
    private void updateSystemStats(String bat, String cpu, String gpu) {
        if (txtBatteryPercent != null) txtBatteryPercent.setText(bat);
        if (txtCpuValue != null) txtCpuValue.setText(cpu);
        if (txtGpuValue != null) txtGpuValue.setText(gpu);
    }

    private void checkPermissions() {
        if (!Settings.canDrawOverlays(this)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, 123);
        }
    }

    private void setupVideo() {
        backgroundVideo.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surface, int width, int height) {
                videoManager.setupVideo(surface, backgroundVideo, width, height);
            }
            @Override public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture s, int w, int h) {}
            @Override public void onSurfaceTextureUpdated(@NonNull SurfaceTexture s) {}
            @Override
            public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture surface) {
                videoManager.release();
                return true;
            }
        });
    }

    private void setupGameList() {
        games = GameManager.getInstalledGames(this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);

        snapHelper = new LinearSnapHelper();
        snapHelper.attachToRecyclerView(recyclerView);

        GameAdapter adapter = new GameAdapter(games, this::updateDetailView);
        recyclerView.setAdapter(adapter);

        recyclerView.addOnScrollListener(new ZoomScrollListener());

        recyclerView.post(() -> {
            int padding = recyclerView.getHeight() / 2;
            recyclerView.setPadding(0, padding, 0, padding);
        });

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView rv, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    View centerView = snapHelper.findSnapView(layoutManager);
                    if (centerView != null) {
                        int pos = layoutManager.getPosition(centerView);
                        updateDetailView(games.get(pos));
                    }
                }
            }
        });

        if (!games.isEmpty()) updateDetailView(games.get(0));
    }

    private void updateDetailView(AppModel game) {
        detailTitle.setText(game.getName());
        imgBanner.setImageDrawable(game.getIcon());

        btnStart.setOnClickListener(v -> {
            Intent launchIntent = getPackageManager().getLaunchIntentForPackage(game.getPackageName());
            if (launchIntent != null) {
                startActivity(launchIntent);
                startService(new Intent(this, CoreService.class));
            }
        });
    }

    private void hideSystemBars() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowInsetsController controller = getWindow().getInsetsController();
            if (controller != null) {
                controller.hide(WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());
                controller.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
            }
        } else {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                            View.SYSTEM_UI_FLAG_FULLSCREEN |
                            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (videoManager != null) videoManager.release();
    }
}
package com.example.neuro_gamesense1;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.neuro_gamesense1.coreApplications.AppModel;
import com.example.neuro_gamesense1.coreApplications.GameAdapter;
import com.example.neuro_gamesense1.coreService.SystemMonitor;
import com.example.neuro_gamesense1.manager.BannerAdapter;
import com.example.neuro_gamesense1.manager.GameDbHelper;
import com.example.neuro_gamesense1.manager.GameManager;
import com.example.neuro_gamesense1.manager.IGDBManager;
import com.example.neuro_gamesense1.manager.SystemUiHelper;
import com.example.neuro_gamesense1.manager.ZoomScrollListener;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ViewPager2 bannerViewPager;
    private TabLayout bannerIndicator;
    private TextView txtCpuValue, txtGpuValue, txtBatteryValue;
    private List<AppModel> games;
    private GameDbHelper dbHelper;
    private IGDBManager igdbManager;
    private TextView txtAppName;
    private String selectedPackagedName;
    private AppModel currentSelectedGame;
    private final Handler updateHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initUI();
        setUpLaunchButton();
        setupGameList();
        startStatsUpdates();
        SystemUiHelper.hideSystemUI(getWindow());
    }

    private void initUI() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            }
        }

        recyclerView = findViewById(R.id.listGame);
        bannerViewPager = findViewById(R.id.bannerViewPager);
        bannerIndicator = findViewById(R.id.bannerIndicator);
        TextView txtAccessibility = findViewById(R.id.turnOnOffAccessibility);
        txtAccessibility.setOnClickListener(v -> openAccessibilitySettings());

        View batteryContainer = findViewById(R.id.batteryStatusInclude);
        if (batteryContainer != null) {
            txtBatteryValue = batteryContainer.findViewById(R.id.txtBatteryPercent);

            ImageView imgBatteryIcon = batteryContainer.findViewById(R.id.battery_bar);
            if (imgBatteryIcon != null) {
                imgBatteryIcon.setImageLevel(9000);
            }
        }

        View cpuView = findViewById(R.id.Chip);
        if (cpuView != null) {
            txtCpuValue = cpuView.findViewById(R.id.statValue);
            ImageView imgCpuIcon = cpuView.findViewById(R.id.statIcon);
            imgCpuIcon.setImageResource(R.drawable.ic_stat_cpu);
        }

        View gpuView = findViewById(R.id.gpuChip);
        if (gpuView != null) {
            txtGpuValue = gpuView.findViewById(R.id.statValue);
            ImageView imgGpuIcon = gpuView.findViewById(R.id.statIcon);
            imgGpuIcon.setImageResource(R.drawable.ic_stat_gpu);
        }

        Button gpuSettings = findViewById(R.id.btnGPUSetUp);
        gpuSettings.setOnClickListener(v -> {
            if(currentSelectedGame == null){
                Log.e("Notification", "Please select an application");
                return;
            }
            Intent intent = new Intent(this, GpuSettingsActivity.class);
            intent.putExtra("SELECTED_GAME_DATA", currentSelectedGame);
            startActivity(intent);
        });

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO}, 100);
        }

        createNotificationChannel();

        dbHelper = new GameDbHelper(this);
        igdbManager = new IGDBManager();
    }

    private void setupGameList() {
        games = GameManager.getInstalledGames(this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(layoutManager);
        LinearSnapHelper snapHelper = new LinearSnapHelper();
        snapHelper.attachToRecyclerView(recyclerView);

        GameAdapter adapter = new GameAdapter(games, this::onGameSelected);
        recyclerView.setAdapter(adapter);
        recyclerView.addOnScrollListener(new ZoomScrollListener());

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView rv, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    View centerView = snapHelper.findSnapView(layoutManager);
                    if (centerView != null) {
                        int pos = layoutManager.getPosition(centerView);
                        AppModel game = games.get(pos);
                        onGameSelected(game);
                        txtAppName.setText(game.getName());
                    }
                }
            }
        });
        recyclerView.post(() -> {
            int screenWidth = getResources().getDisplayMetrics().widthPixels;
            int defaultWidth = (int) (120 * getResources().getDisplayMetrics().density);
            int itemWidth;
            if (recyclerView.getChildAt(0) != null) {
                itemWidth = recyclerView.getChildAt(0).getWidth();
            } else {
                itemWidth = defaultWidth;
            }
            int padding = (screenWidth / 2) - (itemWidth / 2);

            recyclerView.setClipToPadding(false);
            recyclerView.setPadding(padding, 0, padding, 0);

            if (!games.isEmpty()) {
                onGameSelected(games.get(0));
                recyclerView.scrollToPosition(0);
            }
        });
    }

    private void onGameSelected(AppModel game) {
        this.currentSelectedGame = game;
        this.selectedPackagedName = game.getPackageName();

        if (txtAppName != null) {
            txtAppName.setText(game.getName());
        }

        List<String> cached = dbHelper.getCachedBanners(game.getPackageName());
        if (cached != null && !cached.isEmpty()) {
            Log.d("FLOW_DEBUG", "Loading from Cache");
            updateBannerSlider(cached);
        } else {
            igdbManager.fetchHeroImage(game.getName(), urls -> {
                if (urls != null && !urls.isEmpty()) {
                    Log.d("FLOW_DEBUG", "Data found! Saving to DB...");
                    dbHelper.saveBanner(game.getPackageName(), urls);
                    updateBannerSlider(urls);
                } else {
                    Log.e("FLOW_DEBUG", "API returned nothing. Database will remain empty for this game.");
                }
            });
        }
    }

    private void updateBannerSlider(List<String> urls) {
        BannerAdapter adapter = new BannerAdapter(urls);
        bannerViewPager.setAdapter(adapter);
        new TabLayoutMediator(bannerIndicator, bannerViewPager, (tab, position) -> {}).attach();
    }

    private void startStatsUpdates() {
        SystemMonitor monitor = new SystemMonitor(this);
        ImageView imgBattery = findViewById(R.id.battery_bar);

        monitor.startAutoUpdate(new Handler(Looper.getMainLooper()), (cpu, gpu, battery) -> {
            if (txtCpuValue != null) txtCpuValue.setText(cpu + "%");
            if (txtGpuValue != null) txtGpuValue.setText(gpu + "%");
            if (txtBatteryValue != null) txtBatteryValue.setText(battery + "%");
            if (imgBattery != null) imgBattery.setImageLevel(battery * 100);
        });
    }
    private void createNotificationChannel() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            CharSequence name = "Game Booster Alerts";
            String description = "Notifications for memory cleaning and performance";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;

            NotificationChannel channel = new NotificationChannel("BOOSTER_CHANNEL", name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    private void setUpLaunchButton(){
        Button btnStartApp = findViewById(R.id.btnStartGame);
        txtAppName = findViewById(R.id.AppName);

        btnStartApp.setOnClickListener(v -> {
           if(selectedPackagedName != null && !selectedPackagedName.isEmpty()){
                try{
                    Intent lauchIntent = getPackageManager().getLaunchIntentForPackage(selectedPackagedName);
                    if(lauchIntent != null){
                        lauchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(lauchIntent);

                        Toast.makeText(MainActivity.this, "Launching " + txtAppName.getText(), Toast.LENGTH_SHORT);
                    }
                    else{
                        Toast.makeText(this, "Error: App not found on device", Toast.LENGTH_SHORT).show();
                    }
                }catch (Exception e){
                    e.printStackTrace();
                    Toast.makeText(this, "Launch Failed", Toast.LENGTH_SHORT).show();
                }
           }
           else{
               Toast.makeText(this, "Please select a game first", Toast.LENGTH_SHORT).show();
           }
        });
    }
    private void openAccessibilitySettings() {
        Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
        startActivity(intent);
        Toast.makeText(this, "Please find Neuro Gamesense and turn it on", Toast.LENGTH_LONG).show();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        updateHandler.removeCallbacksAndMessages(null);
    }
}

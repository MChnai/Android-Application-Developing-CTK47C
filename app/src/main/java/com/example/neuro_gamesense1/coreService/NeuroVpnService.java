package com.example.neuro_gamesense1.coreService;

import android.content.Intent;
import android.net.VpnService;
import android.os.ParcelFileDescriptor;
import android.content.pm.PackageManager;
import android.util.Log;

public class NeuroVpnService extends VpnService {
    private ParcelFileDescriptor vpnInterface;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String currentGamePackage = intent.getStringExtra("GAME_PACKAGE_NAME");
        Log.d("NeuroVPN", "onStartCommand: Received request for " + currentGamePackage);

        Builder builder = new Builder();
        builder.addAddress("10.0.0.2", 24);
        builder.addDnsServer("8.8.8.8");

        if (currentGamePackage != null) {
            try {
                builder.addAllowedApplication(currentGamePackage);
                builder.addAllowedApplication("com.google.android.gms");
                builder.addAllowedApplication("com.android.vending");
                Log.i("NeuroVPN", "Success: Tunnel established for " + currentGamePackage);
            } catch (PackageManager.NameNotFoundException e) {
                Log.e("NeuroVPN", "Game package not found");
            }
        }

        vpnInterface = builder.setSession("Neuro-Isolation-Tunnel").establish();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        if (vpnInterface != null) {
            try { vpnInterface.close(); } catch (Exception e) {}
        }
        super.onDestroy();
    }
}
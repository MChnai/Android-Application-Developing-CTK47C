package com.example.neuro_gamesense1.manager;

import android.app.NotificationManager;
import android.content.Context;
import android.provider.Settings;
import android.content.Intent;
import android.util.Log;

public class DNDManager {
    public static void setDND(Context context, boolean enable) {
        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (!nm.isNotificationPolicyAccessGranted()) {
            Log.w("DNDManager", "STATUS: DND Access NOT GRANTED. Cannot toggle DND.");
            Intent intent = new Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
            return;
        }

        if (enable) {
            Log.i("DNDManager", "STATUS: DND IS ON (Interruption Filter: NONE)");
        } else {
            Log.i("DNDManager", "STATUS: DND IS OFF (Interruption Filter: ALL)");
            nm.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL);
        }
    }
}
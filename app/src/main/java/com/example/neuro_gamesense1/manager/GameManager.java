package com.example.neuro_gamesense1.manager;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Build;
import com.example.neuro_gamesense1.coreApplications.AppModel;
import java.util.ArrayList;
import java.util.List;

public class GameManager {

    public static List<AppModel> getInstalledGames(Context context) {
        List<AppModel> gameList = new ArrayList<>();
        PackageManager pm = context.getPackageManager();

        @SuppressLint("QueryPermissionsNeeded")
        List<ApplicationInfo> apps = pm.getInstalledApplications(PackageManager.GET_META_DATA);

        for (ApplicationInfo app : apps) {
            boolean isGame = (app.flags & ApplicationInfo.FLAG_IS_GAME) != 0 ||
                    (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
                            app.category == ApplicationInfo.CATEGORY_GAME);

            if (isGame) {
                String name = pm.getApplicationLabel(app).toString();
                Drawable icon = pm.getApplicationIcon(app);
                gameList.add(new AppModel(name, icon, app.packageName));
            }
        }
        return gameList;
    }
}
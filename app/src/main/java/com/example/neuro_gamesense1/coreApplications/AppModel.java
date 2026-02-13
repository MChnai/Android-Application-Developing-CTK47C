package com.example.neuro_gamesense1.coreApplications;

import android.graphics.drawable.Drawable;

public class AppModel {
    private final String name;
    private final Drawable icon;
    private final String packageName;
    public AppModel(String name, Drawable icon, String packageName) {
        this.name = name;
        this.icon = icon;
        this.packageName = packageName;
    }
    public String getName() {
        return name;
    }
    public Drawable getIcon() {
        return icon;
    }
    public String getPackageName() {
        return packageName;
    }
}

package com.example.neuro_gamesense1.coreApplications;

import java.io.Serializable;

public class UtilityModel implements Serializable {
    private final String name;
    private final int icon;
    private boolean isActive;
    public UtilityModel(String name, int icon) {
        this.name = name;
        this.icon = icon;
    }
    public String getName() {
        return name;
    }
    public int getIcon() {
        return icon;
    }
    public boolean isActive() {
        return isActive;
    }
    public void setActive(boolean active) {
        isActive = active;
    }

}

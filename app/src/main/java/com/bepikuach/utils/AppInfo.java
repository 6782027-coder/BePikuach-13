package com.bepikuach.utils;

import android.graphics.drawable.Drawable;

public class AppInfo {
    public String name;
    public String packageName;
    public Drawable icon;
    public boolean isApproved;

    public AppInfo(String name, String packageName, Drawable icon, boolean isApproved) {
        this.name = name;
        this.packageName = packageName;
        this.icon = icon;
        this.isApproved = isApproved;
    }
}

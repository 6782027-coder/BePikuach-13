package com.bepikuach.utils;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import com.bepikuach.admin.DeviceAdminReceiver;

import java.util.List;
import java.util.Set;

public class LockTaskManager {

    private final Context context;
    private final DevicePolicyManager dpm;
    private final ComponentName admin;
    public final boolean isDeviceOwner;

    public LockTaskManager(Context context) {
        this.context = context;
        this.dpm = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
        this.admin = new ComponentName(context, DeviceAdminReceiver.class);
        this.isDeviceOwner = dpm != null && dpm.isDeviceOwnerApp(context.getPackageName());
    }

    public void applyAll(PrefManager prefs) {
        if (!isDeviceOwner || dpm == null) return;
        updateLockTaskFeatures(prefs);
        updateApprovedPackages(prefs.getApprovedApps());
    }

    public void updateLockTaskFeatures(PrefManager prefs) {
        if (!isDeviceOwner || dpm == null) return;
        try {
            int features = DevicePolicyManager.LOCK_TASK_FEATURE_GLOBAL_ACTIONS |
                           DevicePolicyManager.LOCK_TASK_FEATURE_KEYGUARD;
            // שורת התראות — חסום רק אם מופעל
            if (!prefs.isBlockStatusBar()) {
                features |= DevicePolicyManager.LOCK_TASK_FEATURE_NOTIFICATIONS;
                features |= DevicePolicyManager.LOCK_TASK_FEATURE_SYSTEM_INFO;
            }
            dpm.setLockTaskFeatures(admin, features);
        } catch (Exception ignored) {}
    }

    public void updateApprovedPackages(Set<String> approved) {
        if (!isDeviceOwner || dpm == null) return;
        try {
            approved.add(context.getPackageName());
            dpm.setLockTaskPackages(admin, approved.toArray(new String[0]));
            PackageManager pm = context.getPackageManager();
            List<ApplicationInfo> installed = pm.getInstalledApplications(PackageManager.GET_META_DATA);
            for (ApplicationInfo info : installed) {
                if (info.packageName.equals(context.getPackageName())) continue;
                if (pm.getLaunchIntentForPackage(info.packageName) == null) continue;
                try {
                    dpm.setApplicationHidden(admin, info.packageName, !approved.contains(info.packageName));
                } catch (Exception ignored) {}
            }
        } catch (Exception ignored) {}
    }

    public void showAllAppsTemporarily() {
        if (!isDeviceOwner || dpm == null) return;
        try {
            PackageManager pm = context.getPackageManager();
            List<ApplicationInfo> installed = pm.getInstalledApplications(PackageManager.GET_META_DATA);
            for (ApplicationInfo info : installed) {
                if (info.packageName.equals(context.getPackageName())) continue;
                try { dpm.setApplicationHidden(admin, info.packageName, false); }
                catch (Exception ignored) {}
            }
        } catch (Exception ignored) {}
    }

    public void restoreHiddenApps(Set<String> approved) {
        updateApprovedPackages(approved);
    }
}

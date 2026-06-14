package com.bepikuach.utils;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.os.UserManager;

import com.bepikuach.admin.DeviceAdminReceiver;

public class RestrictionsManager {

    private final DevicePolicyManager dpm;
    private final ComponentName admin;
    public final boolean isDeviceOwner;

    public RestrictionsManager(Context context) {
        this.dpm = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
        this.admin = new ComponentName(context, DeviceAdminReceiver.class);
        this.isDeviceOwner = dpm != null && dpm.isDeviceOwnerApp(context.getPackageName());
    }

    public void applyAll(PrefManager prefs) {
        if (!isDeviceOwner || dpm == null) return;
        setRestriction(UserManager.DISALLOW_FACTORY_RESET, prefs.isBlockFactoryReset());
        setRestriction(UserManager.DISALLOW_UNINSTALL_APPS, prefs.isBlockUninstall());
        setRestriction(UserManager.DISALLOW_INSTALL_APPS, prefs.isBlockInstall());
        setRestriction(UserManager.DISALLOW_INSTALL_UNKNOWN_SOURCES, prefs.isBlockInstall());
        setRestriction(UserManager.DISALLOW_CONFIG_WIFI, prefs.isBlockWifi());
        setRestriction(UserManager.DISALLOW_CONFIG_TETHERING, prefs.isBlockHotspot());
        setRestriction(UserManager.DISALLOW_USB_FILE_TRANSFER, prefs.isBlockUsb());
        setRestriction(UserManager.DISALLOW_DEBUGGING_FEATURES, prefs.isBlockDevOptions());
        setRestriction("no_debugging_features", prefs.isBlockDevOptions());
    }

    private void setRestriction(String restriction, boolean enable) {
        try {
            if (enable) dpm.addUserRestriction(admin, restriction);
            else dpm.clearUserRestriction(admin, restriction);
        } catch (Exception ignored) {}
    }
}

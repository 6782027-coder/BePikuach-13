package com.bepikuach.admin;

import android.content.Context;
import android.content.Intent;

import com.bepikuach.utils.PrefManager;

public class DeviceAdminReceiver extends android.app.admin.DeviceAdminReceiver {

    @Override
    public void onEnabled(Context context, Intent intent) {
        super.onEnabled(context, intent);
    }

    @Override
    public CharSequence onDisableRequested(Context context, Intent intent) {
        PrefManager prefs = new PrefManager(context);
        // חסום השבתה אם יש חסימת הסרה קבועה או נעילת הסרת Device Owner
        if (prefs.getLockRemoveOwner() == 2 ||
            prefs.getBlockUninstall() == 2 ||
            prefs.getBlockFactoryReset() == 2) {
            return "אפליקציה זו נעולה לצמיתות ולא ניתן להשבית את הרשאות המנהל";
        }
        return "האם אתה בטוח שברצונך להשבית את ההגנה?";
    }

    @Override
    public void onDisabled(Context context, Intent intent) {
        super.onDisabled(context, intent);
    }
}

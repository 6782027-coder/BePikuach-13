package com.bepikuach.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.bepikuach.activities.HomeActivity;
import com.bepikuach.services.AppMonitorService;
import com.bepikuach.utils.LockTaskManager;
import com.bepikuach.utils.PrefManager;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action == null) return;

        if (action.equals(Intent.ACTION_BOOT_COMPLETED) ||
            action.equals(Intent.ACTION_LOCKED_BOOT_COMPLETED) ||
            action.equals("android.intent.action.QUICKBOOT_POWERON")) {

            // החל הגדרות LockTask מיד
            PrefManager prefs = new PrefManager(context);
            LockTaskManager ltm = new LockTaskManager(context);
            ltm.applyAll(prefs);

            // הפעל שירות ניטור
            context.startForegroundService(new Intent(context, AppMonitorService.class));

            // פתח דף בית
            Intent homeIntent = new Intent(context, HomeActivity.class);
            homeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                               Intent.FLAG_ACTIVITY_CLEAR_TOP |
                               Intent.FLAG_ACTIVITY_SINGLE_TOP);
            context.startActivity(homeIntent);
        }
    }
}

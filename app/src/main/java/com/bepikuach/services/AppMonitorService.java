package com.bepikuach.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.app.usage.UsageEvents;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;

import androidx.core.app.NotificationCompat;

import com.bepikuach.activities.BlockedActivity;
import com.bepikuach.utils.PrefManager;

public class AppMonitorService extends Service {

    private static final String CHANNEL_ID = "bepikuach_service";
    private static final int NOTIFICATION_ID = 1001;
    private static final long CHECK_INTERVAL_MS = 250;

    private Handler handler;
    private Runnable monitorRunnable;
    private PrefManager prefManager;
    private String lastForegroundPackage = "";

    @Override
    public void onCreate() {
        super.onCreate();
        prefManager = new PrefManager(this);
        createNotificationChannel();
        startForeground(NOTIFICATION_ID, buildNotification());
        startMonitoring();
    }

    private void startMonitoring() {
        handler = new Handler(Looper.getMainLooper());
        monitorRunnable = new Runnable() {
            @Override
            public void run() {
                checkForegroundApp();
                handler.postDelayed(this, CHECK_INTERVAL_MS);
            }
        };
        handler.post(monitorRunnable);
    }

    private void checkForegroundApp() {
        String currentPackage = getForegroundPackage();
        if (currentPackage == null || currentPackage.equals(lastForegroundPackage)) return;
        lastForegroundPackage = currentPackage;

        if (currentPackage.equals("com.bepikuach")) return;
        if (isSystemPackage(currentPackage)) return;

        if (!prefManager.isAppApproved(currentPackage)) {
            prefManager.addToBlockedLog(currentPackage);
            Intent blocked = new Intent(this, BlockedActivity.class);
            blocked.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(blocked);
        }
    }

    private String getForegroundPackage() {
        try {
            UsageStatsManager usm = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);
            if (usm == null) return null;
            long now = System.currentTimeMillis();
            UsageEvents events = usm.queryEvents(now - 2000, now);
            UsageEvents.Event lastEvent = new UsageEvents.Event();
            String lastPackage = null;
            while (events.hasNextEvent()) {
                events.getNextEvent(lastEvent);
                if (lastEvent.getEventType() == UsageEvents.Event.MOVE_TO_FOREGROUND) {
                    lastPackage = lastEvent.getPackageName();
                }
            }
            return lastPackage;
        } catch (Exception e) {
            return null;
        }
    }

    private boolean isSystemPackage(String pkg) {
        return pkg.startsWith("com.android.") ||
               pkg.startsWith("android.") ||
               pkg.equals("com.google.android.gms") ||
               pkg.equals("com.google.android.gsf") ||
               pkg.equals("com.google.android.inputmethod.latin") ||
               pkg.equals("com.samsung.android.honeyboard");
    }

    private void createNotificationChannel() {
        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID, "שירות בפיקוח", NotificationManager.IMPORTANCE_MIN);
        channel.setShowBadge(false);
        NotificationManager nm = getSystemService(NotificationManager.class);
        if (nm != null) nm.createNotificationChannel(channel);
    }

    private Notification buildNotification() {
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("בפיקוח פעיל")
                .setContentText("ההגנה מופעלת")
                .setSmallIcon(android.R.drawable.ic_lock_lock)
                .setPriority(NotificationCompat.PRIORITY_MIN)
                .setOngoing(true)
                .build();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (handler != null) handler.removeCallbacks(monitorRunnable);
        // מפעיל מחדש אוטומטית אם נסגר
        startForegroundService(new Intent(this, AppMonitorService.class));
    }

    @Override
    public IBinder onBind(Intent intent) { return null; }
}

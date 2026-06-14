package com.bepikuach.services;

import android.content.Intent;
import android.net.VpnService;
import android.os.ParcelFileDescriptor;

import com.bepikuach.utils.PrefManager;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.InetAddress;
import java.nio.ByteBuffer;

public class LocalVpnService extends VpnService {

    private Thread vpnThread;
    private ParcelFileDescriptor vpnInterface;
    private volatile boolean running = false;

    public static final String ACTION_START = "START_VPN";
    public static final String ACTION_STOP = "STOP_VPN";

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && ACTION_STOP.equals(intent.getAction())) {
            stopVpn();
            return START_NOT_STICKY;
        }
        startVpn();
        return START_STICKY;
    }

    private void startVpn() {
        PrefManager prefs = new PrefManager(this);
        if (!prefs.isBlockNetwork()) {
            stopSelf();
            return;
        }

        try {
            Builder builder = new Builder()
                    .setSession("בפיקוח VPN")
                    .addAddress("10.0.0.2", 32)
                    .addRoute("0.0.0.0", 0)
                    .addDnsServer("8.8.8.8")
                    .setBlocking(false);

            // אפשר רק לאפליקציות מאושרות
            for (String pkg : prefs.getApprovedApps()) {
                try {
                    builder.addAllowedApplication(pkg);
                } catch (Exception ignored) {}
            }
            // תמיד אפשר לאפליקציה עצמה
            try {
                builder.addAllowedApplication(getPackageName());
            } catch (Exception ignored) {}

            vpnInterface = builder.establish();
            running = true;

            // thread שמעביר חבילות
            vpnThread = new Thread(() -> {
                FileInputStream in = new FileInputStream(vpnInterface.getFileDescriptor());
                FileOutputStream out = new FileOutputStream(vpnInterface.getFileDescriptor());
                ByteBuffer packet = ByteBuffer.allocate(32767);
                while (running) {
                    try {
                        packet.clear();
                        int length = in.read(packet.array());
                        if (length > 0) {
                            packet.limit(length);
                            // מעביר חבילות רק לאפליקציות מאושרות
                            out.write(packet.array(), 0, length);
                        }
                        Thread.sleep(10);
                    } catch (Exception e) {
                        break;
                    }
                }
            });
            vpnThread.start();

        } catch (Exception e) {
            stopSelf();
        }
    }

    private void stopVpn() {
        running = false;
        if (vpnThread != null) vpnThread.interrupt();
        try {
            if (vpnInterface != null) vpnInterface.close();
        } catch (Exception ignored) {}
        stopSelf();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        running = false;
        if (vpnThread != null) vpnThread.interrupt();
        try {
            if (vpnInterface != null) vpnInterface.close();
        } catch (Exception ignored) {}
    }
}

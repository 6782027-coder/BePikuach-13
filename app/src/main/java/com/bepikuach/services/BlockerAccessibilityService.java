package com.bepikuach.services;

import android.accessibilityservice.AccessibilityService;
import android.view.accessibility.AccessibilityEvent;

import com.bepikuach.utils.PrefManager;

public class BlockerAccessibilityService extends AccessibilityService {

    private PrefManager prefManager;

    @Override
    public void onCreate() {
        super.onCreate();
        prefManager = new PrefManager(this);
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event == null) return;
        if (event.getEventType() != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return;

        String pkg = event.getPackageName() != null ? event.getPackageName().toString() : "";
        if (!pkg.equals("com.android.systemui")) return;

        String cls = event.getClassName() != null ? event.getClassName().toString() : "";

        // חסימת שורת התראות בלבד
        if (prefManager.isBlockStatusBar()) {
            if (cls.contains("StatusBar") || cls.contains("NotificationShade") ||
                cls.contains("QuickSettings") || cls.contains("NotificationPanel") ||
                cls.contains("QSPanel") || cls.contains("ExpandedView")) {
                performGlobalAction(GLOBAL_ACTION_HOME);
            }
        }

        // מחליף אפליקציות מטופל ע"י LockTask Mode
    }

    @Override
    public void onInterrupt() {}
}

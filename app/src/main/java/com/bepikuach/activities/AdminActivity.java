package com.bepikuach.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.admin.DevicePolicyManager;
import android.app.role.RoleManager;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bepikuach.R;
import com.bepikuach.admin.DeviceAdminReceiver;
import com.bepikuach.utils.LockTaskManager;
import com.bepikuach.utils.PrefManager;
import com.bepikuach.utils.RestrictionsManager;
import com.google.android.material.textfield.TextInputEditText;

public class AdminActivity extends AppCompatActivity {

    private static final int REQUEST_WALLPAPER = 101;
    private static final int REQUEST_DEVICE_ADMIN = 102;
    private static final int REQUEST_HOME = 103;

    private PrefManager prefs;
    private LockTaskManager ltm;
    private RestrictionsManager rm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        prefs = new PrefManager(this);
        ltm = new LockTaskManager(this);
        rm = new RestrictionsManager(this);

        setupToolbar();
        setupMenuButtons();
        setupRestrictionRows();
        setupIconSize();
        setupWallpaper();
        setupSetAsHome();
        setupPasswordChange();
        setupRemoveDeviceOwner();
        setupSaveButton();
        ensureDeviceAdmin();
    }

    private void setupToolbar() {
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void setupMenuButtons() {
        findViewById(R.id.btnSelectApps).setOnClickListener(v ->
                startActivity(new Intent(this, SelectAppsActivity.class)));
        findViewById(R.id.btnAdbSetup).setOnClickListener(v ->
                startActivity(new Intent(this, AdbSetupActivity.class)));
    }

    interface RestrictionSetter { void set(int value); }

    private void setupRestrictionRow(int statusId, int btnOffId, int btnOnId, int btnPermId,
                                      String name, int currentValue, RestrictionSetter setter) {
        updateRowStatus(statusId, currentValue);
        Button btnOff = findViewById(btnOffId);
        Button btnOn = findViewById(btnOnId);
        Button btnPerm = findViewById(btnPermId);
        highlightRow(currentValue, btnOff, btnOn, btnPerm);

        if (currentValue == 2) {
            btnOff.setEnabled(false);
            btnOn.setEnabled(false);
            btnPerm.setEnabled(false);
            return;
        }

        btnOff.setOnClickListener(v -> {
            setter.set(0); updateRowStatus(statusId, 0); highlightRow(0, btnOff, btnOn, btnPerm); applyAll();
        });
        btnOn.setOnClickListener(v -> {
            setter.set(1); updateRowStatus(statusId, 1); highlightRow(1, btnOff, btnOn, btnPerm); applyAll();
        });
        btnPerm.setOnClickListener(v ->
            new AlertDialog.Builder(this)
                .setTitle("נעילה קבועה: " + name)
                .setMessage("לא ניתן לשנות גם מתפריט הניהול!\n\nהאם אתה בטוח?")
                .setPositiveButton("כן", (d, w) -> {
                    setter.set(2);
                    updateRowStatus(statusId, 2);
                    highlightRow(2, btnOff, btnOn, btnPerm);
                    btnOff.setEnabled(false); btnOn.setEnabled(false); btnPerm.setEnabled(false);
                    applyAll();
                })
                .setNegativeButton("ביטול", null).show()
        );
    }

    private void updateRowStatus(int statusId, int value) {
        TextView tv = findViewById(statusId);
        if (tv == null) return;
        switch (value) {
            case 0: tv.setText("כבוי"); tv.setTextColor(0xFF9E9E9E); break;
            case 1: tv.setText("פעיל"); tv.setTextColor(0xFF4CAF50); break;
            case 2: tv.setText("קבוע 🔐"); tv.setTextColor(0xFFD32F2F); break;
        }
    }

    private void highlightRow(int value, Button off, Button on, Button perm) {
        off.setBackgroundColor(value == 0 ? 0xFF757575 : 0xFFEEEEEE);
        off.setTextColor(value == 0 ? 0xFFFFFFFF : 0xFF212121);
        on.setBackgroundColor(value == 1 ? 0xFF4CAF50 : 0xFFEEEEEE);
        on.setTextColor(value == 1 ? 0xFFFFFFFF : 0xFF212121);
        perm.setBackgroundColor(value == 2 ? 0xFFD32F2F : 0xFFEEEEEE);
        perm.setTextColor(value == 2 ? 0xFFFFFFFF : 0xFF212121);
    }

    private void applyAll() {
        ltm.updateLockTaskFeatures(prefs);
        rm.applyAll(prefs);
    }

    private void setupRestrictionRows() {
        if (!rm.isDeviceOwner) findViewById(R.id.deviceOwnerNotice).setVisibility(View.VISIBLE);

        setupRestrictionRow(R.id.statusStatusBar, R.id.btnStatusBarOff, R.id.btnStatusBarOn, R.id.btnStatusBarPerm,
                "חסימת שורת התראות", prefs.getBlockStatusBar(), prefs::setBlockStatusBar);
        setupRestrictionRow(R.id.statusInstall, R.id.btnInstallOff, R.id.btnInstallOn, R.id.btnInstallPerm,
                "חסימת התקנות", prefs.getBlockInstall(), prefs::setBlockInstall);
        setupRestrictionRow(R.id.statusWifi, R.id.btnWifiOff, R.id.btnWifiOn, R.id.btnWifiPerm,
                "חסימת WiFi", prefs.getBlockWifi(), prefs::setBlockWifi);
        setupRestrictionRow(R.id.statusHotspot, R.id.btnHotspotOff, R.id.btnHotspotOn, R.id.btnHotspotPerm,
                "חסימת נקודה חמה", prefs.getBlockHotspot(), prefs::setBlockHotspot);
        setupRestrictionRow(R.id.statusUsb, R.id.btnUsbOff, R.id.btnUsbOn, R.id.btnUsbPerm,
                "חסימת USB", prefs.getBlockUsb(), prefs::setBlockUsb);
        setupRestrictionRow(R.id.statusFactoryReset, R.id.btnFactoryResetOff, R.id.btnFactoryResetOn, R.id.btnFactoryResetPerm,
                "חסימת איפוס מכשיר", prefs.getBlockFactoryReset(), prefs::setBlockFactoryReset);
        setupRestrictionRow(R.id.statusUninstall, R.id.btnUninstallOff, R.id.btnUninstallOn, R.id.btnUninstallPerm,
                "חסימת הסרת אפליקציות", prefs.getBlockUninstall(), prefs::setBlockUninstall);
        setupRestrictionRow(R.id.statusDevOptions, R.id.btnDevOptionsOff, R.id.btnDevOptionsOn, R.id.btnDevOptionsPerm,
                "חסימת אפשרויות מפתחים", prefs.getBlockDevOptions(), prefs::setBlockDevOptions);
    }

    private void setupIconSize() {
        Button s = findViewById(R.id.btnIconSmall);
        Button m = findViewById(R.id.btnIconMedium);
        Button l = findViewById(R.id.btnIconLarge);
        highlightIconSize(prefs.getIconSize(), s, m, l);
        s.setOnClickListener(v -> { prefs.setIconSize(0); highlightIconSize(0, s, m, l); });
        m.setOnClickListener(v -> { prefs.setIconSize(1); highlightIconSize(1, s, m, l); });
        l.setOnClickListener(v -> { prefs.setIconSize(2); highlightIconSize(2, s, m, l); });
    }

    private void highlightIconSize(int size, Button s, Button m, Button l) {
        s.setBackgroundColor(size == 0 ? 0xFF1A237E : 0xFF9E9E9E);
        m.setBackgroundColor(size == 1 ? 0xFF1A237E : 0xFF9E9E9E);
        l.setBackgroundColor(size == 2 ? 0xFF1A237E : 0xFF9E9E9E);
    }

    private void setupWallpaper() {
        findViewById(R.id.btnWallpaper).setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("image/*");
            startActivityForResult(intent, REQUEST_WALLPAPER);
        });
    }

    private void setupSetAsHome() {
        findViewById(R.id.btnSetAsHome).setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                RoleManager roleManager = getSystemService(RoleManager.class);
                if (roleManager != null && !roleManager.isRoleHeld(RoleManager.ROLE_HOME)) {
                    startActivityForResult(roleManager.createRequestRoleIntent(RoleManager.ROLE_HOME), REQUEST_HOME);
                } else {
                    Toast.makeText(this, "בפיקוח כבר מוגדרת כדף הבית ✓", Toast.LENGTH_SHORT).show();
                }
            } else {
                startActivity(new Intent(android.provider.Settings.ACTION_HOME_SETTINGS));
            }
        });
    }

    private void setupPasswordChange() {
        findViewById(R.id.btnChangePassword).setOnClickListener(v -> {
            TextInputEditText p1 = findViewById(R.id.newPassword);
            TextInputEditText p2 = findViewById(R.id.confirmPassword);
            String pass1 = p1.getText() != null ? p1.getText().toString() : "";
            String pass2 = p2.getText() != null ? p2.getText().toString() : "";
            if (pass1.isEmpty()) { Toast.makeText(this, "הכנס סיסמה", Toast.LENGTH_SHORT).show(); return; }
            if (!pass1.equals(pass2)) { Toast.makeText(this, "הסיסמאות אינן תואמות", Toast.LENGTH_SHORT).show(); return; }
            prefs.setPassword(pass1);
            p1.setText(""); p2.setText("");
            Toast.makeText(this, "הסיסמה עודכנה ✓", Toast.LENGTH_SHORT).show();
        });
    }

    private void setupRemoveDeviceOwner() {
        Button btnRemove = findViewById(R.id.btnRemoveDeviceOwner);
        Button btnLockRemove = findViewById(R.id.btnLockRemoveOwner);
        TextView statusLockRemove = findViewById(R.id.statusLockRemoveOwner);

        DevicePolicyManager dpm = (DevicePolicyManager) getSystemService(DEVICE_POLICY_SERVICE);
        boolean isOwner = dpm != null && dpm.isDeviceOwnerApp(getPackageName());

        if (!isOwner) {
            btnRemove.setText("Device Owner לא מוגדר");
            btnRemove.setEnabled(false);
            btnLockRemove.setVisibility(View.GONE);
            statusLockRemove.setVisibility(View.GONE);
            return;
        }

        // אם נעול לצמיתות — הסתר כפתור הסרה
        int lockLevel = prefs.getLockRemoveOwner();
        if (lockLevel == 2) {
            btnRemove.setVisibility(View.GONE);
            btnLockRemove.setEnabled(false);
            statusLockRemove.setText("כפתור הסרה נעול לצמיתות 🔐");
            statusLockRemove.setTextColor(0xFFD32F2F);
            return;
        }

        // כפתור הסרה — מוגן בסיסמה
        btnRemove.setOnClickListener(v -> showPasswordThenRemove(dpm));

        // כפתור נעילת ההסרה לצמיתות
        btnLockRemove.setOnClickListener(v ->
            new AlertDialog.Builder(this)
                .setTitle("נעל כפתור הסרה לצמיתות")
                .setMessage("לאחר אישור כפתור הסרת Device Owner יוסתר לצמיתות.\n\nלא ניתן לבטל!\n\nהאם אתה בטוח?")
                .setPositiveButton("כן, נעל", (d, w) -> {
                    prefs.setLockRemoveOwner(2);
                    btnRemove.setVisibility(View.GONE);
                    btnLockRemove.setEnabled(false);
                    statusLockRemove.setText("כפתור הסרה נעול לצמיתות 🔐");
                    statusLockRemove.setTextColor(0xFFD32F2F);
                    Toast.makeText(this, "נעול לצמיתות ✓", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("ביטול", null).show()
        );
    }

    private void showPasswordThenRemove(DevicePolicyManager dpm) {
        View view = LayoutInflater.from(this).inflate(R.layout.activity_password, null);
        TextInputEditText input = view.findViewById(R.id.passwordInput);
        TextView error = view.findViewById(R.id.errorText);

        new AlertDialog.Builder(this)
            .setTitle("הסר Device Owner")
            .setMessage("הכנס סיסמת מנהל לאישור:")
            .setView(view)
            .setPositiveButton("אשר והסר", (d, w) -> {
                String entered = input.getText() != null ? input.getText().toString() : "";
                if (prefs.checkPassword(entered)) {
                    try {
                        dpm.clearDeviceOwnerApp(getPackageName());
                        Toast.makeText(this, "Device Owner הוסר ✓", Toast.LENGTH_LONG).show();
                        findViewById(R.id.btnRemoveDeviceOwner).setEnabled(false);
                        ((Button)findViewById(R.id.btnRemoveDeviceOwner)).setText("הוסר ✓");
                    } catch (Exception e) {
                        Toast.makeText(this, "שגיאה: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(this, "סיסמה שגויה", Toast.LENGTH_SHORT).show();
                }
            })
            .setNegativeButton("ביטול", null)
            .show();
    }

    private void ensureDeviceAdmin() {
        DevicePolicyManager dpm = (DevicePolicyManager) getSystemService(DEVICE_POLICY_SERVICE);
        ComponentName admin = new ComponentName(this, DeviceAdminReceiver.class);
        if (dpm != null && !dpm.isAdminActive(admin)) {
            Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, admin);
            intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "נדרש להפעלת הגנת בפיקוח");
            startActivityForResult(intent, REQUEST_DEVICE_ADMIN);
        }
    }

    private void setupSaveButton() {
        findViewById(R.id.btnSave).setOnClickListener(v -> {
            Toast.makeText(this, "ההגדרות נשמרו ✓", Toast.LENGTH_SHORT).show();
            finish();
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_HOME && resultCode == Activity.RESULT_OK)
            Toast.makeText(this, "בפיקוח הוגדרה כדף הבית ✓", Toast.LENGTH_SHORT).show();
        if (requestCode == REQUEST_WALLPAPER && resultCode == Activity.RESULT_OK && data != null) {
            android.net.Uri uri = data.getData();
            if (uri != null) {
                try {
                    java.io.InputStream is = getContentResolver().openInputStream(uri);
                    java.io.File dest = new java.io.File(getFilesDir(), "wallpaper.jpg");
                    java.io.FileOutputStream fos = new java.io.FileOutputStream(dest);
                    byte[] buf = new byte[4096]; int len;
                    while ((len = is.read(buf)) > 0) fos.write(buf, 0, len);
                    is.close(); fos.close();
                    prefs.setWallpaperPath(dest.getAbsolutePath());
                    Toast.makeText(this, "הטפט עודכן ✓", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Toast.makeText(this, "שגיאה בטעינת הטפט", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override public boolean onSupportNavigateUp() { finish(); return true; }
    @Override public void onBackPressed() { finish(); }
}

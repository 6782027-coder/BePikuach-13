package com.bepikuach.activities;

import android.app.admin.DevicePolicyManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.bepikuach.R;

public class AdbSetupActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adb_setup);

        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(getString(R.string.adb_setup));
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        String command = "adb shell dpm set-device-owner com.bepikuach/.admin.DeviceAdminReceiver";
        TextView cmdText = findViewById(R.id.commandText);
        cmdText.setText(command);

        Button copyBtn = findViewById(R.id.btnCopy);
        copyBtn.setOnClickListener(v -> {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
            clipboard.setPrimaryClip(ClipData.newPlainText("adb command", command));
            Toast.makeText(this, "הפקודה הועתקה", Toast.LENGTH_SHORT).show();
        });

        // Check if already Device Owner
        DevicePolicyManager dpm = (DevicePolicyManager) getSystemService(DEVICE_POLICY_SERVICE);
        TextView statusText = findViewById(R.id.statusText);
        if (dpm != null && dpm.isDeviceOwnerApp(getPackageName())) {
            statusText.setText("✅ האפליקציה כבר מוגדרת כמנהל מוחלט!");
            statusText.setTextColor(ContextCompat.getColor(this, R.color.approved_indicator));
        } else {
            statusText.setText("❌ האפליקציה עדיין לא הוגדרה כמנהל מוחלט");
            statusText.setTextColor(0xFFEF5350);
        }
    }

    @Override public boolean onSupportNavigateUp() { finish(); return true; }
}

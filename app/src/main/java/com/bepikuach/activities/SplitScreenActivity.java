package com.bepikuach.activities;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bepikuach.R;
import com.bepikuach.utils.AppInfo;
import com.bepikuach.utils.PrefManager;

import java.util.ArrayList;
import java.util.List;

public class SplitScreenActivity extends AppCompatActivity {

    private List<AppInfo> approvedApps = new ArrayList<>();
    private AppInfo firstApp = null;
    private AppInfo secondApp = null;
    private TextView firstLabel, secondLabel;
    private Button launchBtn;
    private SplitAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_split_screen);

        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("פיצול מסך");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        firstLabel = findViewById(R.id.firstAppLabel);
        secondLabel = findViewById(R.id.secondAppLabel);
        launchBtn = findViewById(R.id.btnLaunchSplit);

        PrefManager prefManager = new PrefManager(this);
        PackageManager pm = getPackageManager();
        for (String pkg : prefManager.getApprovedApps()) {
            try {
                String name = pm.getApplicationLabel(pm.getApplicationInfo(pkg, 0)).toString();
                approvedApps.add(new AppInfo(name, pkg, pm.getApplicationIcon(pkg), true));
            } catch (PackageManager.NameNotFoundException ignored) {}
        }
        approvedApps.sort((a, b) -> a.name.compareToIgnoreCase(b.name));

        RecyclerView grid = findViewById(R.id.appsGrid);
        grid.setLayoutManager(new GridLayoutManager(this, 3));
        adapter = new SplitAdapter();
        grid.setAdapter(adapter);

        launchBtn.setOnClickListener(v -> launchSplitScreen());
    }

    private void launchSplitScreen() {
        if (firstApp == null || secondApp == null) {
            Toast.makeText(this, "בחר שתי אפליקציות", Toast.LENGTH_SHORT).show();
            return;
        }

        PackageManager pm = getPackageManager();
        Intent i1 = pm.getLaunchIntentForPackage(firstApp.packageName);
        Intent i2 = pm.getLaunchIntentForPackage(secondApp.packageName);

        if (i1 == null || i2 == null) {
            Toast.makeText(this, "לא ניתן לפתוח אחת מהאפליקציות", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            // פתח ראשונה
            i1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
            startActivity(i1);

            // פתח שנייה עם ADJACENT אחרי 500ms
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                i2.addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK |
                    Intent.FLAG_ACTIVITY_MULTIPLE_TASK |
                    Intent.FLAG_ACTIVITY_LAUNCH_ADJACENT
                );
                startActivity(i2);
            }, 500);

            finish();
        } catch (Exception e) {
            Toast.makeText(this, "המכשיר אינו תומך בפיצול מסך עם אפליקציות אלו", Toast.LENGTH_LONG).show();
        }
    }

    class SplitAdapter extends RecyclerView.Adapter<SplitAdapter.VH> {
        @NonNull @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_split_app, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH holder, int pos) {
            AppInfo app = approvedApps.get(pos);
            holder.icon.setImageDrawable(app.icon);
            holder.name.setText(app.name);

            boolean isFirst = firstApp != null && firstApp.packageName.equals(app.packageName);
            boolean isSecond = secondApp != null && secondApp.packageName.equals(app.packageName);

            if (isFirst) {
                holder.itemView.setBackgroundColor(0x331A237E);
                holder.badge.setVisibility(View.VISIBLE);
                holder.badge.setText("1");
            } else if (isSecond) {
                holder.itemView.setBackgroundColor(0x33C5A028);
                holder.badge.setVisibility(View.VISIBLE);
                holder.badge.setText("2");
            } else {
                holder.itemView.setBackgroundColor(0x00000000);
                holder.badge.setVisibility(View.GONE);
            }

            holder.itemView.setOnClickListener(v -> {
                if (firstApp == null) {
                    firstApp = app;
                    firstLabel.setText("אפליקציה 1: " + app.name);
                } else if (secondApp == null && !app.packageName.equals(firstApp.packageName)) {
                    secondApp = app;
                    secondLabel.setText("אפליקציה 2: " + app.name);
                    launchBtn.setEnabled(true);
                } else {
                    firstApp = app;
                    secondApp = null;
                    firstLabel.setText("אפליקציה 1: " + app.name);
                    secondLabel.setText("אפליקציה 2: לא נבחרה");
                    launchBtn.setEnabled(false);
                }
                notifyDataSetChanged();
            });
        }

        @Override public int getItemCount() { return approvedApps.size(); }

        class VH extends RecyclerView.ViewHolder {
            ImageView icon; TextView name, badge;
            VH(View v) {
                super(v);
                icon = v.findViewById(R.id.appIcon);
                name = v.findViewById(R.id.appName);
                badge = v.findViewById(R.id.appBadge);
            }
        }
    }

    @Override public boolean onSupportNavigateUp() { finish(); return true; }
}

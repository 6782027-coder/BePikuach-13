package com.bepikuach.utils;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bepikuach.R;

import java.util.ArrayList;
import java.util.List;

public class AdminAppsAdapter extends RecyclerView.Adapter<AdminAppsAdapter.ViewHolder> {

    private List<AppInfo> allApps;
    private List<AppInfo> filteredApps;

    public AdminAppsAdapter(List<AppInfo> apps) {
        this.allApps = apps;
        this.filteredApps = new ArrayList<>(apps);
    }

    public void filter(String query) {
        filteredApps = new ArrayList<>();
        if (query == null || query.isEmpty()) {
            filteredApps.addAll(allApps);
        } else {
            String lower = query.toLowerCase();
            for (AppInfo app : allApps) {
                if (app.name.toLowerCase().contains(lower) ||
                    app.packageName.toLowerCase().contains(lower)) {
                    filteredApps.add(app);
                }
            }
        }
        notifyDataSetChanged();
    }

    public List<AppInfo> getAllApps() {
        return allApps;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_app_admin, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AppInfo app = filteredApps.get(position);
        holder.icon.setImageDrawable(app.icon);
        holder.name.setText(app.name);
        holder.toggle.setChecked(app.isApproved);
        holder.toggle.setOnCheckedChangeListener((btn, checked) -> {
            app.isApproved = checked;
            // Update in allApps too
            for (AppInfo a : allApps) {
                if (a.packageName.equals(app.packageName)) {
                    a.isApproved = checked;
                    break;
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return filteredApps != null ? filteredApps.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView icon;
        TextView name;
        SwitchCompat toggle;

        ViewHolder(View view) {
            super(view);
            icon = view.findViewById(R.id.appIcon);
            name = view.findViewById(R.id.appName);
            toggle = view.findViewById(R.id.appSwitch);
        }
    }
}

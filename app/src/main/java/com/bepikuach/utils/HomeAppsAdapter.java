package com.bepikuach.utils;

import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bepikuach.R;

import java.util.List;

public class HomeAppsAdapter extends RecyclerView.Adapter<HomeAppsAdapter.ViewHolder> {

    public interface OnAppClickListener {
        void onAppClick(AppInfo app);
    }

    private List<AppInfo> apps;
    private final OnAppClickListener listener;
    private int iconSizeDp = 56; // default medium

    public HomeAppsAdapter(List<AppInfo> apps, OnAppClickListener listener) {
        this.apps = apps;
        this.listener = listener;
    }

    public void setApps(List<AppInfo> apps) {
        this.apps = apps;
        notifyDataSetChanged();
    }

    // iconSize: 0=small, 1=medium, 2=large
    public void setIconSize(int size) {
        iconSizeDp = size == 0 ? 42 : size == 1 ? 56 : 72;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_app, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AppInfo app = apps.get(position);
        holder.icon.setImageDrawable(app.icon);
        holder.name.setText(app.name);

        // שינוי גודל אמיתי של האייקון
        int sizePx = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, iconSizeDp,
                holder.itemView.getContext().getResources().getDisplayMetrics());
        ViewGroup.LayoutParams params = holder.icon.getLayoutParams();
        params.width = sizePx;
        params.height = sizePx;
        holder.icon.setLayoutParams(params);

        holder.itemView.setOnClickListener(v -> listener.onAppClick(app));
    }

    @Override
    public int getItemCount() {
        return apps != null ? apps.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView icon;
        TextView name;

        ViewHolder(View view) {
            super(view);
            icon = view.findViewById(R.id.appIcon);
            name = view.findViewById(R.id.appName);
        }
    }
}

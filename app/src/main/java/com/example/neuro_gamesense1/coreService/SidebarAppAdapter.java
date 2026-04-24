package com.example.neuro_gamesense1.coreService;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.neuro_gamesense1.R;

import java.util.List;

public class SidebarAppAdapter extends RecyclerView.Adapter<SidebarAppAdapter.SidebarViewHolder> {

    private final Context context;
    private final List<String> packageNames;

    public SidebarAppAdapter(Context context, List<String> packageNames) {
        this.context = context;
        this.packageNames = packageNames;
    }

    @NonNull
    @Override
    public SidebarViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.app_icon, parent, false);
        return new SidebarViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SidebarViewHolder holder, int position) {
        String pkg = packageNames.get(position);
        PackageManager pm = context.getPackageManager();

        try {
            Drawable icon = pm.getApplicationIcon(pkg);
            holder.imgAppIcon.setImageDrawable(icon);

            holder.itemView.setOnClickListener(v -> {
                Intent launchIntent = pm.getLaunchIntentForPackage(pkg);
                if (launchIntent != null) {
                    launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(launchIntent);
                }
            });
        } catch (PackageManager.NameNotFoundException e) {
            holder.imgAppIcon.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return packageNames.size();
    }

    static class SidebarViewHolder extends RecyclerView.ViewHolder {
        ImageView imgAppIcon;

        public SidebarViewHolder(@NonNull View itemView) {
            super(itemView);
            if (itemView.findViewById(R.id.imgAppIcon) != null) {
                imgAppIcon = itemView.findViewById(R.id.imgAppIcon);
            } else {
                imgAppIcon = (ImageView) ((ViewGroup) itemView).getChildAt(0);
            }
        }
    }
}
package com.example.neuro_gamesense1.coreApplications;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.neuro_gamesense1.R;

import java.util.List;

public class UtilityAdapter extends RecyclerView.Adapter<UtilityAdapter.ViewHolder> {
    private final List<UtilityModel> utilities;
    private final Context context;
    public UtilityAdapter(Context context, List<UtilityModel> utilities) {
        this.context = context;
        this.utilities = utilities;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_utility, parent, false);
        return new ViewHolder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UtilityModel item = utilities.get(position);
        holder.utilityText.setText(item.getName());
        holder.utilityIcon.setImageResource(item.getIcon());
        holder.itemView.setAlpha(item.isActive() ? 1.0f : 0.5f);

        holder.itemView.setOnClickListener(v -> {
            String action = item.getName().toLowerCase();

            switch (action) {
                case "dnd":
                    toggleDND(item);
                    break;

                case "boost":
                    //performBoost();
                    break;

                case "screenshot":
                    //takeScreenshot();
                    break;

                case "record":
                    //startScreenRecord();
                    break;
            }
            notifyItemChanged(position);
        });
    }
    private void toggleDND(UtilityModel item) {
        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (!nm.isNotificationPolicyAccessGranted()) {
            Intent intent = new Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
            return;
        }

        if (item.isActive()) {
            nm.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL);
            item.setActive(false);
        } else {
            nm.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_NONE);
            item.setActive(true);
        }
        notifyDataSetChanged();
    }
    @Override
    public int getItemCount() { return utilities.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView utilityIcon;
        TextView utilityText;

        ViewHolder(View itemView) {
            super(itemView);
            utilityIcon = itemView.findViewById(R.id.utilityIcon);
            utilityText = itemView.findViewById(R.id.utilityText);
        }
    }
}

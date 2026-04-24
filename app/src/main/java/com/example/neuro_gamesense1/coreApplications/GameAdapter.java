package com.example.neuro_gamesense1.coreApplications;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.neuro_gamesense1.R;

import java.util.List;

public class GameAdapter extends RecyclerView.Adapter<GameAdapter.GameViewHolder> {
    private final List<AppModel> games;
    private final OnGameListener listener;
    public GameAdapter(List<AppModel> games, OnGameListener listener) {
        this.games = games;
        this.listener = listener;
    }
    public interface OnGameListener{
        void onGameClick(AppModel game);
    }
    @NonNull
    @Override
    public GameViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.game_info_detail, parent, false);
        return new GameViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GameViewHolder holder, int position) {
        AppModel game = games.get(position);
        holder.gameIcon.setImageDrawable(game.getIcon());

        holder.itemView.setOnClickListener(v -> {
            if(listener != null)
                listener.onGameClick(game);
        });
    }

    @Override
    public int getItemCount() {
        return games.size();
    }
    static class GameViewHolder extends RecyclerView.ViewHolder {
        ImageView gameIcon;
        TextView gameName;

        public GameViewHolder(@NonNull View itemView) {
            super(itemView);
            gameIcon = itemView.findViewById(R.id.imgGameIcon);
            gameName = itemView.findViewById(R.id.AppName);
        }
    }
}

package com.example.neuro_gamesense1.manager;

import android.graphics.Color;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.neuro_gamesense1.R;

public class ZoomScrollListener extends RecyclerView.OnScrollListener {

    @Override
    public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);

        int midPoint = recyclerView.getHeight() / 2;

        for (int i = 0; i < recyclerView.getChildCount(); i++) {
            View child = recyclerView.getChildAt(i);
            View container = child.findViewById(R.id.container);

            if (container != null) {
                float childMidPoint = (child.getTop() + child.getBottom()) / 2f;
                float distance = Math.abs(midPoint - childMidPoint);

                float ratio = Math.min(1.0f, distance / (float) midPoint);

                float scale = 1.0f - (ratio * 0.65f);
                float alpha = Math.max(0.4f, 1.0f - ratio);

                container.setScaleX(scale);
                container.setScaleY(scale);

                if (distance < 50) {
                    container.setBackgroundResource(R.drawable.gradient_focus_bg);
                    container.setAlpha(1.0f);
                } else {
                    container.setBackgroundColor(Color.TRANSPARENT);
                    container.setAlpha(alpha);
                }
            }
        }
    }
}
package com.example.neuro_gamesense1.manager;

import android.view.View;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.neuro_gamesense1.R;

public class ZoomScrollListener extends RecyclerView.OnScrollListener {

    @Override
    public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);

        int midPoint = recyclerView.getWidth() / 2;
        float s0 = 1.0f;
        float s1 = 0.25f;

        for (int i = 0; i < recyclerView.getChildCount(); i++) {
            View child = recyclerView.getChildAt(i);

            View container = child.findViewById(R.id.container);

            if (container != null) {
                float childMidPoint = (child.getLeft() + child.getRight()) / 2f;
                float distance = Math.abs(midPoint - childMidPoint);

                float ratio = Math.min(1.0f, distance / (float) midPoint);
                float scale = s0 + (s1 - s0) * ratio;

                container.setScaleX(scale);
                container.setScaleY(scale);

                if (distance < 100) {
                    container.setAlpha(1.0f);
                } else {
                    float alpha = Math.max(0.25f, 1.0f - (ratio * 0.5f));
                    container.setAlpha(alpha);
                }
            }
        }
    }
}
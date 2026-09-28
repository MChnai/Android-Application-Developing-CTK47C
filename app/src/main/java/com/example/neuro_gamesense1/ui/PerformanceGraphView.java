package com.example.neuro_gamesense1.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import androidx.core.content.ContextCompat;

import com.example.neuro_gamesense1.R;

import java.util.ArrayList;
import java.util.List;

public class PerformanceGraphView extends View {
    private Paint linePaint, predictPaint;
    private final int MAX_POINTS = 100;
    private final List<Float> dataPoints = new ArrayList<>();
    private final List<Float> predictPoints = new ArrayList<>();

    public PerformanceGraphView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        linePaint = new Paint();
        linePaint.setColor(ContextCompat.getColor(getContext(), R.color.blue));
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeWidth(4f);
        linePaint.setAntiAlias(true);

        predictPaint = new Paint();
        predictPaint.setColor(ContextCompat.getColor(getContext(), R.color.status_orange));
        predictPaint.setStyle(Paint.Style.STROKE);
        predictPaint.setStrokeWidth(3f);
        predictPaint.setPathEffect(new DashPathEffect(new float[]{10, 10}, 0));
        predictPaint.setAntiAlias(true);
    }

    public void addDataPoint(float current, float predicted) {
        synchronized (dataPoints) {
            dataPoints.add(current);
            predictPoints.add(predicted);

            if (dataPoints.size() > MAX_POINTS) {
                dataPoints.remove(0);
                predictPoints.remove(0);
            }
        }
        postInvalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        synchronized (dataPoints) {
            if (dataPoints.isEmpty()) return;

            float w = getWidth();
            float h = getHeight();
            float xStep = w / (MAX_POINTS - 1);

            Path currentPath = new Path();
            Path predictPath = new Path();

            for (int i = 0; i < dataPoints.size(); i++) {
                float x = i * xStep;

                float yCurrent = h - ((dataPoints.get(i) - 20) / (50 - 20) * h);
                float yPredict = h - ((predictPoints.get(i) - 20) / (50 - 20) * h);

                if (i == 0) {
                    currentPath.moveTo(x, yCurrent);
                    predictPath.moveTo(x, yPredict);
                } else {
                    currentPath.lineTo(x, yCurrent);
                    predictPath.lineTo(x, yPredict);
                }
            }
            canvas.drawPath(currentPath, linePaint);
            canvas.drawPath(predictPath, predictPaint);
        }
    }
}
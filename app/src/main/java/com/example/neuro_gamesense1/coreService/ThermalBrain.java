package com.example.neuro_gamesense1.coreService;

public class ThermalBrain {
    private static final float THROTTLE_LIMIT = 42.0f;
    private final float[] currentBuffer = new float[30];
    private final float[] tempHistory = new float[5];
    private int bufferIndex = 0;
    private int historyIndex = 0;
    private float currentSum = 0;

    public float getSmoothedCurrent(int rawCurrent) {
        currentSum -= currentBuffer[bufferIndex];
        currentBuffer[bufferIndex] = rawCurrent;
        currentSum += rawCurrent;
        bufferIndex = (bufferIndex + 1) % 30;
        return currentSum / 30.0f;
    }

    public float getAiPrediction(float temp, float smoothCurrent, float tempSlope, int audioState) {
        return (float) ThermalPredictor.score(temp, smoothCurrent, tempSlope, audioState);
    }

    public String getTimeToOverheat(float currentTemp, double predictedTemp) {
        float delta = (float) predictedTemp - currentTemp;

        if (predictedTemp > 45.0f) return "CRITICAL: AI predicts overheat in < 60s!";
        if (delta > 2.0f) return "Warning: Rapid heating detected.";
        return "System Stable";
    }

    public String getThermalAdvice(float currentTemp, double predictedTemp) {
        tempHistory[historyIndex] = currentTemp;
        historyIndex = (historyIndex + 1) % 5;
        float heatingRate = (currentTemp - tempHistory[historyIndex]) / 10.0f;

        if (currentTemp >= THROTTLE_LIMIT) return "System Throttling Active!";
        if (heatingRate > 0.02f) {
            float secondsLeft = (THROTTLE_LIMIT - currentTemp) / heatingRate;
            if (secondsLeft < 60) return "CRITICAL: Lag in ~" + (int)secondsLeft + "s!";
        }
        return (predictedTemp > currentTemp + 1.0f) ? "AI: Rising Fast!" : "Status: Stable";
    }
}
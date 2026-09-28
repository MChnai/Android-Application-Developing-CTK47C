package com.example.neuro_gamesense1.coreService;

import android.annotation.SuppressLint;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class AudioContextAnalyzer {
    private static final int SAMPLE_RATE = 8000;
    private AudioRecord audioRecord;
    private boolean isRecording = false;
    private double dynamicThreshold = 2000.0f;
    private boolean isCalibrated = false;
    private boolean isCombat = false;
    private final Handler samplingHandler = new Handler(Looper.getMainLooper());

    public boolean isListening() {
        return isRecording;
    }
    public void setAudioEnabled(boolean enabled) {
        if (enabled && !isRecording) {
            startListening(state -> Log.d("Audio", "State: " + state));
        } else if (!enabled && isRecording) {
            stopListening();
        }
    }
    public interface AudioStateListener {
        void onStateChanged(String state);
    }
    public boolean getIsCombat() {
        return isCombat;
    }
    private final Runnable samplingRunnable = new Runnable() {
        @Override
        public void run() {
            startListening(state -> Log.d("Audio", "State: " + state));
            samplingHandler.postDelayed(() -> stopListening(), 5000);
            samplingHandler.postDelayed(this, 60000);
        }
    };
    @SuppressLint("MissingPermission")
    public void startListening(AudioStateListener listener) {
        if (isRecording) return;

        int bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize);
        if (audioRecord.getState() != AudioRecord.STATE_INITIALIZED) {
            listener.onStateChanged("AudioRecord not initialized");
            return;
        }

        audioRecord.startRecording();
        isRecording = true;
        new Thread(() -> {
            short[] audioBuffer = new short[bufferSize];
            List<Double> calibrationData = new ArrayList<>();
            long startTime = System.currentTimeMillis();
            while (isRecording) {
                int read = audioRecord.read(audioBuffer, 0, bufferSize);

                if (read > 0) {
                    double rms = calculateRMS(audioBuffer, read);
                    if (!isCalibrated) {
                        calibrationData.add(rms);
                        listener.onStateChanged("AI CALIBRATING...");
                        if (System.currentTimeMillis() - startTime > 30000) {
                            dynamicThreshold = trainKMeansThreshold(calibrationData);
                            isCalibrated = true;
                            Log.d("AudioAI", "AI Self-Trained. New Threshold: " + dynamicThreshold);
                        }
                    } else {
                        if (rms > dynamicThreshold) {
                            listener.onStateChanged("COMBAT");
                            isCombat = true;
                        } else {
                            listener.onStateChanged("LOBBY");
                            isCombat = false;
                        }
                    }
                }
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    break;
                }
            }
        }).start();
    }
    public void stopListening(){
        isRecording = false;
        if(audioRecord != null){
            audioRecord.stop();
            audioRecord.release();
            audioRecord = null;
        }
    }
    private double calculateRMS(short[] buffer, int length) {
        long sum = 0;
        for (int i = 0; i < length; i++) {
            sum += buffer[i] * buffer[i];
        }
        return Math.sqrt(sum / (double) length);
    }
    private double trainKMeansThreshold(List<Double> rmsHistory) {
        if(rmsHistory.size() < 10) return 2000.0f; //failed safe

        double centroidLobby = Collections.min(rmsHistory);
        double centroidCombat = Collections.max(rmsHistory);

        for (int interation = 0; interation < 10; interation++){
            List<Double> clusterLobby = new ArrayList<>();
            List<Double> clusterCombat = new ArrayList<>();

            for(double rms : rmsHistory) {
                if (Math.abs(rms - centroidLobby) < Math.abs(rms - centroidCombat))
                    clusterLobby.add(rms);
                else clusterCombat.add(rms);
            }
            if(!clusterLobby.isEmpty())
                centroidLobby = clusterLobby.stream().mapToDouble(val -> val).average().orElse(centroidLobby);
            if(!clusterCombat.isEmpty())
                centroidCombat = clusterCombat.stream().mapToDouble(val -> val).average().orElse(centroidCombat);
        }
        return (centroidCombat + centroidLobby) / 2.0f;
    }
}

package com.example.neuro_gamesense1.coreService;

import android.content.Context;
import android.util.Log;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DataLogger {
    private File logFile;
    private final Context context;

    public DataLogger(Context context) {
        this.context = context;
        createFile();
    }

    private void createFile() {
        File dir = context.getExternalFilesDir(null);
        logFile = new File(dir, "training_data.csv");

        try {
            if (!logFile.exists()) {
                FileWriter writer = new FileWriter(logFile, true);
                writer.append("Timestamp,Temperature_C,Current_mA,RAM_Free_MB\n");
                writer.flush();
                writer.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void logData(float temp, int currentmA, String ramString) {
        try {
            FileWriter writer = new FileWriter(logFile, true);
            String timeStamp = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
            String cleanRam = ramString.replace(" MB Free", "").trim();
            String dataRow = timeStamp + "," + temp + "," + currentmA + "," + cleanRam + "\n";

            writer.append(dataRow);
            writer.flush();
            writer.close();

            Log.d("DataLogger", "Logged: " + dataRow); // Check Logcat to see it working
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

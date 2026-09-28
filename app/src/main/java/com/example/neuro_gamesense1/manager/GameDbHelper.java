package com.example.neuro_gamesense1.manager;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GameDbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "neuroGamesense.db";
    private static final int DATABASE_VERSION = 5;
    private static final String TABLE_NAME = "games";
    private static final String COL_PACKAGE = "package_name";
    private static final String COL_BANNER_URL = "banner_url";
    private static final String COL_MODE = "performance_mode";
    private static final String COL_QUALITY = "gpu_quality";
    private static final String COL_FPS = "gpu_fps";
    private static final String COL_PERF = "gpu_perf";
    public GameDbHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTableQuery = "CREATE TABLE " + TABLE_NAME + " (" +
                COL_PACKAGE + " TEXT PRIMARY KEY, " +
                COL_BANNER_URL + " TEXT, " +
                COL_MODE + " TEXT DEFAULT 'Balanced', " +
                COL_QUALITY + " INTEGER DEFAULT 80, " +
                COL_FPS + " INTEGER DEFAULT 80, " +
                COL_PERF + " INTEGER DEFAULT 60)";
        db.execSQL(createTableQuery);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public void saveBanner(String packageName, List<String> url){
        if(url == null || url.isEmpty()) {
            android.util.Log.e("DB_DEBUG", "Empty data, nothing to save for: " + packageName);
            return;
        }

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_PACKAGE, packageName);

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < url.size(); i++) {
            sb.append(url.get(i));
            if (i < url.size() - 1) sb.append(",");
        }
        values.put(COL_BANNER_URL, sb.toString());

        db.insertWithOnConflict(TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE);
    }
    public List<String> getCachedBanners(String packageName) {
        List<String> urls = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME, new String[]{COL_BANNER_URL},
                COL_PACKAGE + "=?", new String[]{packageName}, null, null, null);

        if (cursor.moveToFirst()) {
            String data = cursor.getString(0);
            if (data != null && !data.isEmpty()) {
                urls = Arrays.asList(data.split(","));
            }
            cursor.close();
        }
        return urls;
    }
    public void saveGpuSettings(String packageName, int quality, int fps, int perf) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_QUALITY, quality);
        values.put(COL_FPS, fps);
        values.put(COL_PERF, perf);

        db.update(TABLE_NAME, values, COL_PACKAGE + "=?", new String[]{packageName});
    }
    public int[] getGpuSettings(String packageName) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME, new String[]{COL_QUALITY, COL_FPS, COL_PERF},
                COL_PACKAGE + "=?", new String[]{packageName}, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            int[] settings = {cursor.getInt(0), cursor.getInt(1), cursor.getInt(2)};
            cursor.close();
            return settings;
        }
        return new int[]{80, 80, 60};
    }
}

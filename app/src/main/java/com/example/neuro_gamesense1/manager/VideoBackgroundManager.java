package com.example.neuro_gamesense1.manager;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.media.PlaybackParams;
import android.net.Uri;
import android.os.Build;
import android.view.Surface;
import android.view.TextureView;
import com.example.neuro_gamesense1.R;

public class VideoBackgroundManager {
    private MediaPlayer mediaPlayer;
    private final Context context;

    public VideoBackgroundManager(Context context) {
        this.context = context;
    }

    public void setupVideo(SurfaceTexture surfaceTexture, TextureView textureView, int width, int height) {
        mediaPlayer = new MediaPlayer();
        try {
            Uri videoUri = Uri.parse("android.resource://" + context.getPackageName() + "/" + R.raw.bg_video1);
            mediaPlayer.setDataSource(context, videoUri);
            mediaPlayer.setSurface(new Surface(surfaceTexture));
            mediaPlayer.setLooping(true);

            mediaPlayer.setOnPreparedListener(mp -> {
                adjustAspectRatio(mp, textureView, width, height);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    mp.setPlaybackParams(new PlaybackParams().setSpeed(1.25f));
                }
                mp.start();
            });
            mediaPlayer.prepareAsync();
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void adjustAspectRatio(MediaPlayer mp, TextureView tv, int viewWidth, int viewHeight) {
        float videoWidth = mp.getVideoWidth();
        float videoHeight = mp.getVideoHeight();
        float scaleX = (float) viewWidth / videoWidth;
        float scaleY = (float) viewHeight / videoHeight;
        float maxScale = Math.max(scaleX, scaleY);

        Matrix matrix = new Matrix();
        matrix.setScale(maxScale / scaleX, maxScale / scaleY, viewWidth / 2f, viewHeight / 2f);
        tv.setTransform(matrix);
    }

    public void release() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}
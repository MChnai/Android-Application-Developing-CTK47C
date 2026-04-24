package com.example.neuro_gamesense1.manager;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import androidx.annotation.NonNull;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class IGDBManager {
    private static final String CLIENT_ID = "gsnjj4l44u6y5akvi6mi6b53qowvgy";
    private static final String CLIENT_SECRET = "59ljl7ok9bcwmu0g3k9rtjtr6fj7gd";
    private static String currentAccessToken = null;

    private interface TokenListener {
        void onTokenReceived(String token);
    }

    public interface IGDBListener {
        void onScreenshotsFound(List<String> imageUrls);
    }

    public void fetchHeroImage(String gameName, IGDBListener listener) {
        if (currentAccessToken == null) {
            fetchNewToken(newToken -> {
                if (newToken != null) {
                    currentAccessToken = newToken;
                    searchImages(gameName, listener);
                } else {
                    searchImages(gameName, listener);
                }
            });
        } else {
            searchImages(gameName, listener);
        }
    }

    private void fetchNewToken(TokenListener tokenListener) {
        OkHttpClient client = new OkHttpClient();

        RequestBody body = new FormBody.Builder()
                .add("client_id", CLIENT_ID)
                .add("client_secret", CLIENT_SECRET)
                .add("grant_type", "client_credentials")
                .build();

        Request request = new Request.Builder()
                .url("https://id.twitch.tv/oauth2/token")
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                new Handler(Looper.getMainLooper()).post(() -> tokenListener.onTokenReceived(null));
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                try (Response res = response) {
                    if (res.isSuccessful() && res.body() != null) {
                        JSONObject jsonObj = new JSONObject(res.body().string());
                        String token = jsonObj.getString("access_token");
                        new Handler(Looper.getMainLooper()).post(() -> tokenListener.onTokenReceived(token));
                    } else {
                        new Handler(Looper.getMainLooper()).post(() -> tokenListener.onTokenReceived(null));
                    }
                } catch (Exception e) {
                    new Handler(Looper.getMainLooper()).post(() -> tokenListener.onTokenReceived(null));
                }
            }
        });
    }
    private void searchImages(String gameName, IGDBListener listener) {
        OkHttpClient client = new OkHttpClient();
        String query = "search \"" + gameName + "\"; " + "fields screenshots.url, artworks.url, cover.url; " + "limit 1;";
        Log.d("IGDB_DEBUG", "Query: " + query);

        Request request = new Request.Builder()
                .url("https://api.igdb.com/v4/games")
                .addHeader("Client-ID", CLIENT_ID)
                .addHeader("Authorization", "Bearer " + currentAccessToken)
                .post(RequestBody.create(query, MediaType.parse("text/plain")))
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                new Handler(Looper.getMainLooper()).post(() -> listener.onScreenshotsFound(null));
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                try (Response res = response) {
                    if (!res.isSuccessful()) {
                        Log.e("IGDB_ERROR", "Status: " + res.code() + " Message: " + res.message());
                        new Handler(Looper.getMainLooper()).post(() -> listener.onScreenshotsFound(null));
                        return;
                    }
                    JSONArray jsonArray = new JSONArray(res.body().string());
                     List<String> urls = new ArrayList<>();
                    if (jsonArray.length() > 0) {
                        JSONObject game = jsonArray.getJSONObject(0);
                        int MAX_IMAGES = 8;

                        if (game.has("artworks")) {
                            JSONArray artworks = game.getJSONArray("artworks");
                            for (int i = 0; i < artworks.length() && urls.size() < MAX_IMAGES; i++) {
                                urls.add("https:" + artworks.getJSONObject(i).getString("url").replace("t_thumb", "t_1080p"));
                            }
                        }

                        if (game.has("cover") && urls.size() < MAX_IMAGES) {
                            JSONObject cover = game.getJSONObject("cover");
                            urls.add("https:" + cover.getString("url").replace("t_thumb", "t_1080p"));
                        }

                        if (game.has("screenshots") && urls.size() < MAX_IMAGES) {
                            JSONArray screenshots = game.getJSONArray("screenshots");
                            for (int i = 0; i < screenshots.length() && urls.size() < MAX_IMAGES; i++) {
                                urls.add("https:" + screenshots.getJSONObject(i).getString("url").replace("t_thumb", "t_1080p"));
                            }
                        }
                    }
                    new Handler(Looper.getMainLooper()).post(() -> listener.onScreenshotsFound(urls));
                } catch (Exception e) {
                    new Handler(Looper.getMainLooper()).post(() -> listener.onScreenshotsFound(null));
                }
            }
        });
    }
}
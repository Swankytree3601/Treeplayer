package com.dam2.treeplayer.utils;

import android.os.AsyncTask;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class DeezerApiHelper {
    private static final String DEEZER_SEARCH_URL = "https://api.deezer.com/search?q=";

    public interface ImageCallback {
        void onSuccess(String imageUrl);
        void onFailure();
    }

    public void searchAlbumArt(String songTitle, String artist, ImageCallback callback) {
        new FetchImageTask(callback).execute(songTitle + " " + artist);
    }

    private class FetchImageTask extends AsyncTask<String, Void, String> {
        private ImageCallback callback;
        FetchImageTask(ImageCallback callback) { this.callback = callback; }

        @Override
        protected String doInBackground(String... params) {
            try {
                String urlString = DEEZER_SEARCH_URL + params[0].replace(" ", "%20");
                URL url = new URL(urlString);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder content = new StringBuilder();
                String inputLine;
                while ((inputLine = in.readLine()) != null) content.append(inputLine);
                in.close();
                conn.disconnect();

                JSONObject json = new JSONObject(content.toString());
                JSONArray data = json.getJSONArray("data");
                if (data.length() > 0) {
                    JSONObject album = data.getJSONObject(0).getJSONObject("album");
                    return album.getString("cover_medium");
                }
            } catch (Exception e) { e.printStackTrace(); }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null) callback.onSuccess(result);
            else callback.onFailure();
        }
    }
}
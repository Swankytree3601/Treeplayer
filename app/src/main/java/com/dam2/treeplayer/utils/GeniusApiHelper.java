package com.dam2.treeplayer.utils;

import android.os.AsyncTask;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class GeniusApiHelper {
    private static final String TAG = "GeniusApiHelper";
    private static final String GENIUS_API_URL = "https://api.genius.com";

    public interface LyricsCallback {
        void onSuccess(String lyrics);
        void onFailure(String errorMessage);
    }

    public void searchLyrics(String songTitle, String artist, LyricsCallback callback) {
        new LyricsFetcherTask(callback).execute(songTitle, artist);
    }

    private class LyricsFetcherTask extends AsyncTask<String, Void, String> {
        private LyricsCallback callback;
        private String errorMessage = null;

        LyricsFetcherTask(LyricsCallback callback) {
            this.callback = callback;
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                String songTitle = params[0];
                String artist = params[1];

                String searchQuery = URLEncoder.encode(songTitle + " " + artist, "UTF-8");
                String searchUrl = GENIUS_API_URL + "/search?q=" + searchQuery;

                URL url = new URL(searchUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Authorization", "Bearer " + Config.GENIUS_ACCESS_TOKEN);

                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder content = new StringBuilder();
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    content.append(inputLine);
                }
                in.close();
                conn.disconnect();

                JSONObject json = new JSONObject(content.toString());
                JSONArray hits = json.getJSONObject("response").getJSONArray("hits");

                if (hits.length() > 0) {
                    JSONObject hit = hits.getJSONObject(0);
                    String songUrl = hit.getJSONObject("result").getString("url");

                    return scrapeLyricsFromUrl(songUrl);
                } else {
                    errorMessage = "No se encontró la canción en Genius"; //Mensaje para debugear :)
                    return null;
                }

            } catch (Exception e) {
                Log.e(TAG, "Error: " + e.getMessage());
                errorMessage = "Error: " + e.getMessage();
                return null;
            }
        }

        private String scrapeLyricsFromUrl(String songUrl) {
            try {
                org.jsoup.nodes.Document doc = org.jsoup.Jsoup.connect(songUrl)
                        .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                        .get();

                // Genius tiene diferentes estructuras, intentamos varios selectores
                org.jsoup.select.Elements lyricsContainers = doc.select("[class^=Lyrics__Container]");
                if (lyricsContainers.isEmpty()) {
                    lyricsContainers = doc.select("[data-lyrics-container=true]");
                }
                if (lyricsContainers.isEmpty()) {
                    lyricsContainers = doc.select(".lyrics");
                }

                if (!lyricsContainers.isEmpty()) {
                    StringBuilder lyricsBuilder = new StringBuilder();
                    for (org.jsoup.nodes.Element container : lyricsContainers) {
                        container.select("br").append("\\n");
                        container.select("a").remove(); // Quitar enlaces
                        lyricsBuilder.append(container.text()).append("\n");
                    }
                    String lyrics = lyricsBuilder.toString().replace("\\n", "\n");

                    // Limpiar la letra de cosas extrañas
                    lyrics = lyrics.replaceAll("\\[.*?\\]", "").trim();

                    if (!lyrics.isEmpty()) {
                        return lyrics;
                    }
                }

                errorMessage = "No se pudo extraer la letra";
                return null;

            } catch (Exception e) {
                Log.e(TAG, "Error scraping lyrics: " + e.getMessage());
                errorMessage = "Error al obtener la letra";
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null && !result.isEmpty()) {
                callback.onSuccess(result);
            } else {
                callback.onFailure(errorMessage != null ? errorMessage : "No se encontró la letra");
            }
        }
    }
}
package com.dam2.treeplayer.song_logic;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FavoritesManager {
    private static final String PREFS_NAME = "favorites_prefs";
    private static final String KEY_FAVORITES = "favorites_set";
    private SharedPreferences sharedPreferences;
    private Gson gson;

    public FavoritesManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
    }

    public Set<String> getFavorites() {
        String json = sharedPreferences.getString(KEY_FAVORITES, "");
        if (json.isEmpty()) return new HashSet<>();
        Type type = new TypeToken<Set<String>>() {}.getType();
        return gson.fromJson(json, type);
    }

    public void saveFavorites(Set<String> favoriteIds) {
        String json = gson.toJson(favoriteIds);
        sharedPreferences.edit().putString(KEY_FAVORITES, json).apply();
    }

    public void addFavorite(String songId) {
        Set<String> favorites = getFavorites();
        favorites.add(songId);
        saveFavorites(favorites);
    }

    public void removeFavorite(String songId) {
        Set<String> favorites = getFavorites();
        favorites.remove(songId);
        saveFavorites(favorites);
    }

    public boolean isFavorite(String songId) {
        return getFavorites().contains(songId);
    }

    public void updateFavoriteStatus(List<Song> songList) {
        Set<String> favoriteIds = getFavorites();
        for (Song song : songList) {
            song.setFavorite(favoriteIds.contains(song.getId()));
        }
    }
}
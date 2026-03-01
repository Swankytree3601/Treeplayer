package com.dam2.treeplayer.song_logic;

public class Song {
    private String id;
    private String title;
    private String artist;
    private int rawResourceID;
    private String duracion;
    private boolean isFavorite;
    private String imageUrl;

    public Song(String id, String title, String artist, int rawResourceID, String duracion, String imageUrl) {
        this.id = id;
        this.title = title;
        this.artist = artist;
        this.rawResourceID = rawResourceID;
        this.duracion = duracion;
        this.imageUrl = imageUrl;
        this.isFavorite = false;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getArtist() { return artist; }
    public int getRawResourceID() { return rawResourceID; }
    public String getDuracion() { return duracion; }
    public boolean isFavorite() { return isFavorite; }
    public void setFavorite(boolean favorite) { isFavorite = favorite; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}
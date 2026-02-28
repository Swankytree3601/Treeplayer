package com.dam2.treeplayer.song_logic;

public class Song {
    private String titulo;
    private String artista;
    private int recursoRaw;
    private String duracion; // Formato: "3:45"

    public Song(String titulo, String artista, int recursoRaw, String duracion) {
        this.titulo = titulo;
        this.artista = artista;
        this.recursoRaw = recursoRaw;
        this.duracion = duracion;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getArtista() {
        return artista;
    }

    public int getRecursoRaw() {
        return recursoRaw;
    }

    public String getDuracion() {
        return duracion;
    }
}
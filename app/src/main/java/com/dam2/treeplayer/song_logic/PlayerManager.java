package com.dam2.treeplayer.song_logic;

import android.content.Context;
import android.media.MediaPlayer;

import java.io.IOException;

public class PlayerManager {
    private static PlayerManager instancia;
    private MediaPlayer mediaPlayer;
    private Song cancionActual;
    private boolean isPlaying = false;
    private int currentPosition = 0;

    private PlayerManager() {
        mediaPlayer = new MediaPlayer();
    }

    public static PlayerManager getInstance() {
        if (instancia == null) {
            instancia = new PlayerManager();
        }
        return instancia;
    }

    public void playSong(Context context, Song song) {
        // Si es la misma canción y está en pausa, solo reanudar
        if (cancionActual != null && cancionActual.equals(song) && mediaPlayer != null) {
            resumeSong();
            return;
        }

        // Si no, cargar la nueva canción
        try {
            mediaPlayer.reset();
            // Asumiendo que el recurso es un raw, necesitarías el contexto para abrirlo.
            // Una mejor práctica sería pasar un FileDescriptor o un URI.
            // Para este ejemplo, asumimos que el recurso es un raw y usamos create().
            // PERO create() crea un MediaPlayer nuevo, no reutiliza el nuestro.
            // Por eso, usaremos setDataSource con el raw.
            android.content.res.AssetFileDescriptor afd = context.getResources().openRawResourceFd(song.getRecursoRaw());
            if (afd != null) {
                mediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
                afd.close();
                mediaPlayer.prepare();
                mediaPlayer.start();
                this.cancionActual = song;
                this.isPlaying = true;
                this.currentPosition = 0;

                // Listener para cuando la canción termine
                mediaPlayer.setOnCompletionListener(mp -> {
                    this.isPlaying = false;
                    this.currentPosition = 0;
                    // Aquí podrías notificar a las actividades para que actualicen la UI (ej. cambiar el icono a play)
                });
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void pauseSong() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            isPlaying = false;
            currentPosition = mediaPlayer.getCurrentPosition();
        }
    }

    public void resumeSong() {
        if (mediaPlayer != null && !mediaPlayer.isPlaying() && cancionActual != null) {
            mediaPlayer.start();
            isPlaying = true;
        }
    }

    public void stopSong() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            try {
                mediaPlayer.prepare(); // Para poder volver a empezar
            } catch (IOException e) {
                e.printStackTrace();
            }
            isPlaying = false;
            currentPosition = 0;
        }
    }

    public void seekTo(int position) {
        if (mediaPlayer != null && cancionActual != null) {
            mediaPlayer.seekTo(position);
            currentPosition = position;
        }
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public Song getCancionActual() {
        return cancionActual;
    }

    public int getCurrentPosition() {
        if (mediaPlayer != null && isPlaying) {
            return mediaPlayer.getCurrentPosition();
        } else {
            return currentPosition;
        }
    }

    public int getDuration() {
        if (mediaPlayer != null && cancionActual != null) {
            return mediaPlayer.getDuration();
        }
        return 0;
    }

    public void release() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
            instancia = null; // Resetear la instancia para la próxima vez
        }
    }
}
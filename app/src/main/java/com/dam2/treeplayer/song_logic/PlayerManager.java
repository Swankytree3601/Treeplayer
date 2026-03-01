package com.dam2.treeplayer.song_logic;

import android.content.Context;
import android.media.MediaPlayer;
import java.io.IOException;
import java.util.List;

public class PlayerManager {
    private static PlayerManager instance;
    private MediaPlayer mediaPlayer;
    private Song currentSong;
    private boolean isPlaying = false;
    private int currentPosition = 0;
    private List<Song> playlist;
    private int currentSongIndex = -1;

    private PlayerManager() {
        mediaPlayer = new MediaPlayer();
    }

    public static PlayerManager getInstance() {
        if (instance == null) instance = new PlayerManager();
        return instance;
    }

    public void setPlaylist(List<Song> playlist) {
        this.playlist = playlist;
    }

    public void playSong(Context context, Song song) {
        if (currentSong != null && currentSong.equals(song) && mediaPlayer != null) {
            resumeSong();
            return;
        }
        try {
            mediaPlayer.reset();
            android.content.res.AssetFileDescriptor afd = context.getResources().openRawResourceFd(song.getRawResourceID());
            if (afd != null) {
                mediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
                afd.close();
                mediaPlayer.prepare();
                mediaPlayer.start();
                this.currentSong = song;
                this.isPlaying = true;
                this.currentPosition = 0;

                if (playlist != null) currentSongIndex = playlist.indexOf(song);

                mediaPlayer.setOnCompletionListener(mp -> {
                    this.isPlaying = false;
                    this.currentPosition = 0;
                });
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void playNext(Context context) {
        if (playlist == null || playlist.isEmpty() || currentSongIndex < 0) return;
        int nextIndex = (currentSongIndex + 1) % playlist.size();
        playSong(context, playlist.get(nextIndex));
    }

    public void playPrevious(Context context) {
        if (playlist == null || playlist.isEmpty() || currentSongIndex < 0) return;
        int prevIndex = (currentSongIndex - 1 + playlist.size()) % playlist.size();
        playSong(context, playlist.get(prevIndex));
    }

    public void pauseSong() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            isPlaying = false;
            currentPosition = mediaPlayer.getCurrentPosition();
        }
    }

    public void resumeSong() {
        if (mediaPlayer != null && !mediaPlayer.isPlaying() && currentSong != null) {
            mediaPlayer.start();
            isPlaying = true;
        }
    }

    public void stopSong() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            try { mediaPlayer.prepare(); } catch (IOException e) { e.printStackTrace(); }
            isPlaying = false;
            currentPosition = 0;
        }
    }

    public void seekTo(int position) {
        if (mediaPlayer != null && currentSong != null) {
            mediaPlayer.seekTo(position);
            currentPosition = position;
        }
    }

    public boolean isPlaying() { return isPlaying; }
    public Song getCurrentSong() { return currentSong; }
    public int getCurrentPosition() { return (mediaPlayer != null && isPlaying) ? mediaPlayer.getCurrentPosition() : currentPosition; }
    public int getDuration() { return (mediaPlayer != null && currentSong != null) ? mediaPlayer.getDuration() : 0; }
    public void release() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
            instance = null;
        }
    }
}
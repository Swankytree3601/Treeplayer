package com.dam2.treeplayer.views;

import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;

import com.dam2.treeplayer.R;
import com.dam2.treeplayer.song_logic.PlayerManager;
import com.dam2.treeplayer.song_logic.Song;
import com.dam2.treeplayer.utils.GeniusApiHelper;
import com.google.android.material.button.MaterialButton;
import com.bumptech.glide.Glide;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class PlayerActivity extends BaseActivity {
    private ImageButton btnClose, btnPrevious, btnNext;
    private MaterialButton btnPlayPause;
    private SeekBar seekBar;
    private TextView tvSongTitle, tvSongArtist, tvActualTime, tvDurationTotal;
    private ImageView ivCoverLarge;
    private PlayerManager playerManager;
    private Handler handler = new Handler();
    private Runnable runnable;
    private GeniusApiHelper geniusApiHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        playerManager = PlayerManager.getInstance();
        geniusApiHelper = new GeniusApiHelper();

        initViews();
        setupListeners();
        updateSongInfo();
        setupProgressSeekBar();

        if (playerManager.isPlaying()) startUpdatingSeekBar();
    }

    private void initViews() {
        btnClose = findViewById(R.id.btnClose);
        btnPrevious = findViewById(R.id.btnPrevious);
        btnPlayPause = findViewById(R.id.btnPlayPause);
        btnNext = findViewById(R.id.btnNext);
        seekBar = findViewById(R.id.seekBar);
        tvSongTitle = findViewById(R.id.tvTituloCancion);
        tvSongArtist = findViewById(R.id.tvArtistaCancion);
        tvActualTime = findViewById(R.id.tvTiempoActual);
        tvDurationTotal = findViewById(R.id.tvDuracionTotal);
        ivCoverLarge = findViewById(R.id.ivPortadaGrande);
    }

    private void setupListeners() {
        btnClose.setOnClickListener(v -> finish());

        btnPlayPause.setOnClickListener(v -> {
            if (playerManager.isPlaying()) {
                playerManager.pauseSong();
                btnPlayPause.setIconResource(R.drawable.ic_play);
                stopUpdatingSeekBar();
            } else {
                playerManager.resumeSong();
                btnPlayPause.setIconResource(R.drawable.ic_pause);
                startUpdatingSeekBar();
            }
        });

        btnPrevious.setOnClickListener(v -> {
            playerManager.playPrevious(this);
            updateSongInfo();
            setupProgressSeekBar();
            btnPlayPause.setIconResource(R.drawable.ic_pause);
            startUpdatingSeekBar();
        });

        btnNext.setOnClickListener(v -> {
            playerManager.playNext(this);
            updateSongInfo();
            setupProgressSeekBar();
            btnPlayPause.setIconResource(R.drawable.ic_pause);
            startUpdatingSeekBar();
        });

        findViewById(R.id.btnShowLyrics).setOnClickListener(v -> fetchAndShowLyrics());
    }

    private void fetchAndShowLyrics() {
        Song currentSong = playerManager.getCurrentSong();
        if (currentSong == null) {
            Toast.makeText(this, "No hay canción seleccionada", Toast.LENGTH_SHORT).show();
            return;
        }

        geniusApiHelper.searchLyrics(currentSong.getTitle(), currentSong.getArtist(),
                new GeniusApiHelper.LyricsCallback() {
                    @Override
                    public void onSuccess(String lyrics) {
                        runOnUiThread(() -> new AlertDialog.Builder(PlayerActivity.this)
                                .setTitle(R.string.subtittle_lyric)
                                .setMessage(lyrics)
                                .setPositiveButton(R.string.close_lyrics, null)
                                .show());
                    }
                    @Override
                    public void onFailure(String errorMessage) {
                        runOnUiThread(() -> Toast.makeText(PlayerActivity.this,
                                "Error: " + errorMessage, Toast.LENGTH_SHORT).show());
                    }
                });
    }

    private void updateSongInfo() {
        Song cancion = playerManager.getCurrentSong();
        if (cancion != null) {
            tvSongTitle.setText(cancion.getTitle());
            tvSongArtist.setText(cancion.getArtist());

            if (cancion.getImageUrl() != null && !cancion.getImageUrl().isEmpty()) {
                Glide.with(this).load(cancion.getImageUrl()).placeholder(R.drawable.ic_default_album_final).into(ivCoverLarge);
            } else {
                ivCoverLarge.setImageResource(R.drawable.ic_default_album_final);
            }
        }
    }

    private void setupProgressSeekBar() {
        Song cancion = playerManager.getCurrentSong();
        if (cancion != null) {
            int duration = playerManager.getDuration();
            seekBar.setMax(duration);
            tvDurationTotal.setText(millisecondsToTime(duration));
            seekBar.setProgress(playerManager.getCurrentPosition());
            tvActualTime.setText(millisecondsToTime(playerManager.getCurrentPosition()));
        }

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) tvActualTime.setText(millisecondsToTime(progress));
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) { stopUpdatingSeekBar(); }
            @Override public void onStopTrackingTouch(SeekBar seekBar) {
                playerManager.seekTo(seekBar.getProgress());
                if (playerManager.isPlaying()) startUpdatingSeekBar();
            }
        });
    }

    private void startUpdatingSeekBar() {
        stopUpdatingSeekBar();
        runnable = new Runnable() {
            @Override
            public void run() {
                if (playerManager.isPlaying()) {
                    int pos = playerManager.getCurrentPosition();
                    seekBar.setProgress(pos);
                    tvActualTime.setText(millisecondsToTime(pos));
                    handler.postDelayed(this, 1000);
                }
            }
        };
        handler.postDelayed(runnable, 1000);
    }

    private void stopUpdatingSeekBar() { handler.removeCallbacks(runnable); }

    private String millisecondsToTime(int milliseconds) {
        long minutes = TimeUnit.MILLISECONDS.toMinutes(milliseconds);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(milliseconds) - TimeUnit.MINUTES.toSeconds(minutes);
        return String.format(Locale.getDefault(), "%d:%02d", minutes, seconds);
    }

    @Override protected void onPause() { super.onPause(); stopUpdatingSeekBar(); }
    @Override protected void onDestroy() { super.onDestroy(); stopUpdatingSeekBar(); }

    @Override
    protected void onResume() {
        super.onResume();
        updateSongInfo();
        setupProgressSeekBar();
        btnPlayPause.setIconResource(playerManager.isPlaying() ? R.drawable.ic_pause : R.drawable.ic_play);
        if (playerManager.isPlaying()) startUpdatingSeekBar();
    }
}
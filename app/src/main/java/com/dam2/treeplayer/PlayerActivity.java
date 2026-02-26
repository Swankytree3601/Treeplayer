package com.dam2.treeplayer;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.slider.Slider;

import java.io.IOException;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class PlayerActivity extends AppCompatActivity {

    // Vistas
    private ImageButton btnClose, btnMenu, btnShuffle, btnPrevious, btnNext, btnRepeat, btnVolume;
    private MaterialButton btnPlayPause;
    private SeekBar seekBar, seekVolume;
    private TextView tvTituloCancion, tvArtistaCancion, tvTiempoActual, tvDuracionTotal;
    private ImageView ivPortadaGrande; //Para lo de la API
    private CardView cvPortada; //Para lo de la API

    // MediaPlayer
    private MediaPlayer mediaPlayer;
    private Handler handler = new Handler();
    private Runnable runnable;

    // Variables de estado
    private boolean isPlaying = false;
    private boolean isShuffle = false;
    private boolean isRepeat = false;
    private int currentVolume = 10;
    private int maxVolume = 100;

    // Datos de la canción (luego tengo que ponerlos con un intent)
    private String songTitle = "Nombre de la canción"; //Por ahora no tengo canciones, así que así se queda xd - Recordar que aquí va lo de la API
    private String songArtist = "Nombre del artista"; //APIIIIIIIIIIIIIIIIII
    private int songDuration = 228000; // En milisegundos - 228s - aprox. 3:48 min - Tengo que cambiarlo para que cambie dependiendo de la música xdddddd (API jijiji)
    private int songResource = R.raw.sample_song; // Archivo en res/raw/  -  Recordar lo del copy y tal

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        // Recibir datos del Intent (Tengo que ver cómo lo hago)
        getIntentData();

        // Inicializar vistas
        initViews();

        // Configurar listeners
        setupListeners();

        // Inicializar MediaPlayer
        initMediaPlayer();

        // Actualizar UI con datos de la canción
        updateSongInfo();

        // Configurar SeekBar de volumen
        setupVolumeSeekBar();

        //Probar mientras tanto ;)
        mediaPlayer = MediaPlayer.create(this, R.raw.sample_song);

        if (mediaPlayer != null) {
            mediaPlayer.start();
            Toast.makeText(this, "Reproduciendo canción", Toast.LENGTH_SHORT).show();
        }
    }

    private void getIntentData() {
        // Aquí se recibirám los datos de la canción seleccionada en MainActivity - por construir

        /**
        Ejemplo xd
        songTitle = getIntent().getStringExtra("song_title");
        songArtist = getIntent().getStringExtra("song_artist");
        songDuration = getIntent().getIntExtra("song_duration", 0);
        */
    }

    private void initViews() {
        // Botones superiores
        btnClose = findViewById(R.id.btnClose);
        btnMenu = findViewById(R.id.btnMenu);

        // Portada
        cvPortada = findViewById(R.id.cvPortada);
        ivPortadaGrande = findViewById(R.id.ivPortadaGrande);

        // Textos
        tvTituloCancion = findViewById(R.id.tvTituloCancion);
        tvArtistaCancion = findViewById(R.id.tvArtistaCancion);
        tvTiempoActual = findViewById(R.id.tvTiempoActual);
        tvDuracionTotal = findViewById(R.id.tvDuracionTotal);

        // SeekBars
        seekBar = findViewById(R.id.seekBar);
        seekVolume = findViewById(R.id.seekVolume);

        // Controles de reproducción
        btnShuffle = findViewById(R.id.btnShuffle);
        btnPrevious = findViewById(R.id.btnPrevious);
        btnPlayPause = findViewById(R.id.btnPlayPause);
        btnNext = findViewById(R.id.btnNext);
        btnRepeat = findViewById(R.id.btnRepeat);
        btnVolume = findViewById(R.id.btnVolume);
    }

    private void setupListeners() {
        // Botón cerrar
        btnClose.setOnClickListener(v -> finish());

        // Botón menú
        btnMenu.setOnClickListener(v ->
                Toast.makeText(PlayerActivity.this, "Menú de opciones", Toast.LENGTH_SHORT).show()
        );

        // Botón Play/Pause
        btnPlayPause.setOnClickListener(v -> {
            if (isPlaying) {
                pauseSong();
            } else {
                playSong();
            }
        });

        // Botón anterior
        btnPrevious.setOnClickListener(v -> previousSong());

        // Botón siguiente
        btnNext.setOnClickListener(v -> nextSong());

        // Botón aleatorio
        btnShuffle.setOnClickListener(v -> {
            isShuffle = !isShuffle;
            btnShuffle.setAlpha(isShuffle ? 1.0f : 0.5f);
            btnShuffle.setColorFilter(isShuffle ?
                    getColor(R.color.verde_brillante) :
                    getColor(R.color.texto_secundario_dark));
            Toast.makeText(this, isShuffle ? "Aleatorio activado" : "Aleatorio desactivado",
                    Toast.LENGTH_SHORT).show();
        });

        // Botón repetir
        btnRepeat.setOnClickListener(v -> {
            isRepeat = !isRepeat;
            btnRepeat.setAlpha(isRepeat ? 1.0f : 0.5f);
            btnRepeat.setColorFilter(isRepeat ?
                    getColor(R.color.verde_brillante) :
                    getColor(R.color.texto_secundario_dark));
            Toast.makeText(this, isRepeat ? "Repetir activado" : "Repetir desactivado",
                    Toast.LENGTH_SHORT).show();
        });

        // Botón volumen
        btnVolume.setOnClickListener(v -> {
            if (currentVolume == 0) {
                // Silenciado - restaurar volumen
                currentVolume = 70;
                btnVolume.setImageResource(R.drawable.ic_volume_up);
            } else {
                // Silenciar
                currentVolume = 0;
                btnVolume.setImageResource(R.drawable.ic_volume_off);
            }
            seekVolume.setProgress(currentVolume);
            if (mediaPlayer != null) {
                mediaPlayer.setVolume(currentVolume / 100f, currentVolume / 100f);
            }
        });

        // SeekBar de progreso de la canción
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && mediaPlayer != null) {
                    mediaPlayer.seekTo(progress);
                    seekBar.setProgress(progress);
                    tvTiempoActual.setText(millisecondsToTime(progress));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // Pausar actualización del handler mientras el usuario arrastra
                handler.removeCallbacks(runnable);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Reanudar actualización
                updateSeekBar();
            }
        });

        // SeekBar de volumen (Slider)
        seekVolume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    currentVolume = progress;
                    if (mediaPlayer != null) {
                        mediaPlayer.setVolume(currentVolume / 100f, currentVolume / 100f);
                    }

                    // Actualizar ícono según volumen
                    if (currentVolume == 0) {
                        btnVolume.setImageResource(R.drawable.ic_volume_off);
                    } else if (currentVolume < 30) {
                        btnVolume.setImageResource(R.drawable.ic_volume_mute);
                    } else {
                        btnVolume.setImageResource(R.drawable.ic_volume_up);
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    private void initMediaPlayer() {
        try {
            mediaPlayer = MediaPlayer.create(this, songResource);
            if (mediaPlayer != null) {
                mediaPlayer.setLooping(false); // Controlado por isRepeat
                songDuration = mediaPlayer.getDuration();
                seekBar.setMax(songDuration);
                tvDuracionTotal.setText(millisecondsToTime(songDuration));
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error al cargar la canción", Toast.LENGTH_SHORT).show();
        }
    }

    private void playSong() {
        if (mediaPlayer != null) {
            mediaPlayer.start();
            isPlaying = true;
            btnPlayPause.setIconResource(R.drawable.ic_pause);
            updateSeekBar();
        }
    }

    private void pauseSong() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            isPlaying = false;
            btnPlayPause.setIconResource(R.drawable.ic_play);
            handler.removeCallbacks(runnable);
        }
    }

    private void previousSong() {
        // Aquí irá la lógica para canción anterior
        Toast.makeText(this, "Canción anterior", Toast.LENGTH_SHORT).show();

        // Simular cambio de canción
        if (mediaPlayer != null) {
            mediaPlayer.seekTo(0);
            if (!isPlaying) {
                playSong();
            }
        }
    }

    private void nextSong() {
        // Aquí irá la lógica para siguiente canción
        Toast.makeText(this, "Siguiente canción", Toast.LENGTH_SHORT).show();

        // Simular cambio de canción
        if (mediaPlayer != null) {
            mediaPlayer.seekTo(0);
            if (!isPlaying) {
                playSong();
            }
        }
    }

    private void updateSongInfo() {
        tvTituloCancion.setText(songTitle);
        tvArtistaCancion.setText(songArtist);

        // Recordar poner lo de la API
        // ivPortadaGrande.setImageResource(songImageResource);
    }

    private void setupVolumeSeekBar() {
        seekVolume.setMax(maxVolume);
        seekVolume.setProgress(currentVolume);
    }

    private void updateSeekBar() {
        if (mediaPlayer != null) {
            int currentPosition = mediaPlayer.getCurrentPosition();
            seekBar.setProgress(currentPosition);
            tvTiempoActual.setText(millisecondsToTime(currentPosition));

            // Verificar si la canción terminó
            if (mediaPlayer.isPlaying()) {
                runnable = this::updateSeekBar;
                handler.postDelayed(runnable, 1000);
            }

            // Manejar fin de canción
            mediaPlayer.setOnCompletionListener(mp -> {
                if (isRepeat) {
                    // Repetir misma canción
                    mediaPlayer.seekTo(0);
                    mediaPlayer.start();
                } else {
                    // Pasar a siguiente canción
                    nextSong();
                }
            });
        }
    }

    private String millisecondsToTime(int milliseconds) {
        long minutes = TimeUnit.MILLISECONDS.toMinutes(milliseconds);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(milliseconds) -
                TimeUnit.MINUTES.toSeconds(minutes);

        return String.format(Locale.getDefault(), "%d:%02d", minutes, seconds);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Pausar la canción si la actividad no está visible
        if (isPlaying) {
            pauseSong();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Liberar recursos del MediaPlayer
        handler.removeCallbacks(runnable);
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reanudar reproducción si estaba sonando
        if (isPlaying) {
            playSong();
        }
    }
}
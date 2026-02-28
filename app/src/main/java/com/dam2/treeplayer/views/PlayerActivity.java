package com.dam2.treeplayer.views;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.dam2.treeplayer.R;
import com.google.android.material.button.MaterialButton;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

import com.dam2.treeplayer.song_logic.PlayerManager;
import com.dam2.treeplayer.song_logic.Song;

public class PlayerActivity extends AppCompatActivity {

    // Vistas
    private ImageButton btnClose, btnMenu, btnShuffle, btnPrevious, btnNext, btnRepeat, btnVolume;
    private MaterialButton btnPlayPause;
    private SeekBar seekBar, seekVolume;
    private TextView tvTituloCancion, tvArtistaCancion, tvTiempoActual, tvDuracionTotal;
    private ImageView ivPortadaGrande; //Para lo de la API
    private CardView cvPortada; //Para lo de la API

    private PlayerManager playerManager;
    private Handler handler = new Handler();
    private Runnable runnable;

    // Variables de estado
    private boolean isPlaying = false;
    private boolean isShuffle = false;
    private boolean isRepeat = false;
    private boolean isUpdatingSeekBar = false;

    private int currentVolume = 10;
    private int maxVolume = 100;
    private int currentPosition = 0;

    // Datos de la canción (luego tengo que ponerlos con un intent)
    private String songTitle = "Nombre de la canción"; //Por ahora no tengo canciones, así que así se queda xd - Recordar que aquí va lo de la API
    private String songArtist = "Nombre del artista"; //APIIIIIIIIIIIIIIIIII
    private int songDuration = 228000; // En milisegundos - 228s - aprox. 3:48 min - Tengo que cambiarlo para que cambie dependiendo de la música xdddddd (API jijiji)
    private int songResource = R.raw.sample_song_1; // Archivo en res/raw/  -  Recordar lo del copy y tal

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        playerManager = PlayerManager.getInstance(); //Obtenemos la instancia

        // Recibir datos del Intent (Ya no se envían porque lo sacamos a una clase singleton)
        //getIntentData();

        // Inicializar vistas
        initViews();

        // Configurar listeners
        setupListeners();

        // Actualizar UI con datos de la canción
        updateSongInfo();

        // Configurar SeekBar de volumen
        setupVolumeSeekBar();
        setupProgressSeekBar();

        // Iniciar la actualización de la seekbar si está sonando
        if (playerManager.isPlaying()) {
            startUpdatingSeekBar();
        }
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
        btnMenu.setOnClickListener(v -> {Toast.makeText(PlayerActivity.this, R.string.menu_open, Toast.LENGTH_SHORT).show();});

        // Botón Play/Pause
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
        });

        // SeekBar de progreso de la canción
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
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

        seekBar.setMax(songDuration);
        tvDuracionTotal.setText(millisecondsToTime(songDuration));

        seekBar.setProgress(currentPosition);
        tvTiempoActual.setText(millisecondsToTime(currentPosition));

        if (isPlaying){
            btnPlayPause.setIconResource(R.drawable.ic_pause);
        }else{
            btnPlayPause.setIconResource(R.drawable.ic_play);
        }

        updateSeekBar();
    }

    private void playSong() {
        isPlaying = true;
        btnPlayPause.setIconResource(R.drawable.ic_pause);
        updateSeekBar();
    }

    private void pauseSong() {
        isPlaying = false;
        btnPlayPause.setIconResource(R.drawable.ic_play);
        handler.removeCallbacks(runnable);
    }

    private void previousSong() {
        // Aquí irá la lógica para canción anterior - Falta terminar
        Toast.makeText(this, "Canción anterior", Toast.LENGTH_SHORT).show();
    }

    private void nextSong() {
        // Aquí irá la lógica para siguiente canción - Falta terminar
        Toast.makeText(this, "Siguiente canción", Toast.LENGTH_SHORT).show();
    }

    private void updateSongInfo() {
        Song cancion = playerManager.getCancionActual();
        if (cancion != null) {
            tvTituloCancion.setText(cancion.getTitulo());
            tvArtistaCancion.setText(cancion.getArtista());
            // Recordar hacer lo de la API
        }
    }

    private void setupProgressSeekBar() {
        Song cancion = playerManager.getCancionActual();
        if (cancion != null) {
            int duration = playerManager.getDuration();
            seekBar.setMax(duration);
            tvDuracionTotal.setText(millisecondsToTime(duration));

            int currentPosition = playerManager.getCurrentPosition();
            seekBar.setProgress(currentPosition);
            tvTiempoActual.setText(millisecondsToTime(currentPosition));
        }

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    tvTiempoActual.setText(millisecondsToTime(progress));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                stopUpdatingSeekBar();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                playerManager.seekTo(seekBar.getProgress());
                if (playerManager.isPlaying()) {
                    startUpdatingSeekBar();
                }
            }
        });
    }

    private void setupVolumeSeekBar() {
        seekVolume.setMax(maxVolume);
        seekVolume.setProgress(currentVolume);
    }

    private void updateSeekBar() {
        handler.removeCallbacks(runnable); // Siempre limpiar primero

        if (isPlaying) {
            runnable = new Runnable() {
                @Override
                public void run() {
                    int currentPosition = seekBar.getProgress() + 1000;
                    if (currentPosition < songDuration) {
                        seekBar.setProgress(currentPosition);
                        tvTiempoActual.setText(millisecondsToTime(currentPosition));
                        handler.postDelayed(this, 1000);
                    } else {
                        // Llegó al final
                        seekBar.setProgress(songDuration);
                        tvTiempoActual.setText(millisecondsToTime(songDuration));

                        // Importante: Detener el handler antes de cambiar isPlaying
                        handler.removeCallbacks(this);

                        isPlaying = false;
                        btnPlayPause.setIconResource(R.drawable.ic_play);

                        // Opcional: Notificar que terminó
                        // Toast.makeText(PlayerActivity.this, "Canción finalizada", Toast.LENGTH_SHORT).show();
                    }
                }
            };
            handler.postDelayed(runnable, 1000);
        }
    }

    private void startUpdatingSeekBar() {
        stopUpdatingSeekBar(); // Asegurarse de que no haya otro corriendo
        isUpdatingSeekBar = true;
        runnable = new Runnable() {
            @Override
            public void run() {
                if (isUpdatingSeekBar && playerManager.isPlaying()) {
                    int currentPosition = playerManager.getCurrentPosition();
                    seekBar.setProgress(currentPosition);
                    tvTiempoActual.setText(millisecondsToTime(currentPosition));
                    handler.postDelayed(this, 1000);
                } else {
                    stopUpdatingSeekBar();
                }
            }
        };
        handler.postDelayed(runnable, 1000);
    }

    private void stopUpdatingSeekBar() {
        isUpdatingSeekBar = false;
        handler.removeCallbacks(runnable);
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
        // Liberar recursos del MediaPlayer
        stopUpdatingSeekBar();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Liberar recursos del MediaPlayer
        stopUpdatingSeekBar();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Al reanudar, actualizar la UI con la canción actual y su estado
        updateSongInfo();
        setupProgressSeekBar(); // Vuelve a configurar la seekbar con los valores actuales
        if (playerManager.isPlaying()) {
            btnPlayPause.setIconResource(R.drawable.ic_pause);
            startUpdatingSeekBar();
        } else {
            btnPlayPause.setIconResource(R.drawable.ic_play);
        }
    }
}
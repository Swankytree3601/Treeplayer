package com.dam2.treeplayer.views;

import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dam2.treeplayer.R;
import com.dam2.treeplayer.song_logic.Song;
import com.dam2.treeplayer.song_logic.SongAdapter;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

import com.dam2.treeplayer.song_logic.PlayerManager;

public class MainActivity extends AppCompatActivity {

    private RecyclerView rvCanciones;
    private MaterialToolbar topAppBar;
    private MaterialButton btnMiniPlay;
    private TextView tvMiniTitulo, tvMiniArtista, tvContador;
    private SongAdapter adapter;
    private List<Song> listaCanciones;
    private Song actualSong;
    private boolean isPlaying = false;
    private MediaPlayer mediaPlayer;
    private PlayerManager playerManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();  // primero inicializamos vistas

        EdgeToEdge.enable(this);

        ViewCompat.setOnApplyWindowInsetsListener(topAppBar, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.statusBars());
            v.setPadding(0, systemBars.top, 0, 0);
            return insets;
        });
        setupToolbar();
        cargarCanciones();
        setupRecyclerView();

        //Para la barra
        setupPlayerBar();
        setupPlayerBarClick();

        playerManager = PlayerManager.getInstance();
    }

    private void initViews() {
        rvCanciones = findViewById(R.id.rvCanciones);
        topAppBar = findViewById(R.id.topAppBar);
        btnMiniPlay = findViewById(R.id.btnMiniPlay);
        tvMiniTitulo = findViewById(R.id.tvMiniTitulo);
        tvMiniArtista = findViewById(R.id.tvMiniArtista);
        tvContador = findViewById(R.id.tvContador);
    }

    private void setupToolbar() {
        setSupportActionBar(topAppBar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        topAppBar.setNavigationOnClickListener(v -> {
            Toast.makeText(this, R.string.menu_open, Toast.LENGTH_SHORT).show();
        });

        topAppBar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_language) {
                // Aquí debe llamar al método para mostrar el diálogo de idioma, no un Toast
                mostrarDialogoIdioma();
                return true;
            }
            return false;
        });
    }

    private void cargarCanciones() {
        listaCanciones = new ArrayList<>();

        // Añadimos las canciones que querramos aquí (por ahora funciona, pero hay que cambiarlo [o no xddd]):
        listaCanciones.add(new Song("Hear Me Now", "Alok", R.raw.sample_song_1, "3:13"));
        listaCanciones.add(new Song("My Flaws Burn Through My Skin Like Demonic Flames from Hell", "$uicideboy$", R.raw.sample_song_2, "2:48"));

        //Prueba de que funcionan el Listado
        listaCanciones.add(new Song("Hear Me Now", "Alok", R.raw.sample_song_1, "3:13"));
        listaCanciones.add(new Song("Hear Me Now", "Alok", R.raw.sample_song_1, "3:13"));
        listaCanciones.add(new Song("Hear Me Now", "Alok", R.raw.sample_song_1, "3:13"));
        listaCanciones.add(new Song("Hear Me Now", "Alok", R.raw.sample_song_1, "3:13"));
        listaCanciones.add(new Song("Hear Me Now", "Alok", R.raw.sample_song_1, "3:13"));
        listaCanciones.add(new Song("Hear Me Now", "Alok", R.raw.sample_song_1, "3:13"));
        listaCanciones.add(new Song("Hear Me Now", "Alok", R.raw.sample_song_1, "3:13"));
        listaCanciones.add(new Song("Hear Me Now", "Alok", R.raw.sample_song_1, "3:13"));
        listaCanciones.add(new Song("Hear Me Now", "Alok", R.raw.sample_song_1, "3:13"));
        listaCanciones.add(new Song("Hear Me Now", "Alok", R.raw.sample_song_1, "3:13"));
        listaCanciones.add(new Song("Hear Me Now", "Alok", R.raw.sample_song_1, "3:13"));
        listaCanciones.add(new Song("Hear Me Now", "Alok", R.raw.sample_song_1, "3:13"));
        listaCanciones.add(new Song("Hear Me Now", "Alok", R.raw.sample_song_1, "3:13"));

        // Actualizar contador
        tvContador.setText(listaCanciones.size() + " " + getString(R.string.canciones));
    }

    private void setupRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rvCanciones.setLayoutManager(layoutManager);

        adapter = new SongAdapter(listaCanciones, (song, position) -> {
            playerManager.playSong(this, song);
            actualizarUIPorReproduccion(song); // Método para actualizar la UI de la barra
        });

        rvCanciones.setAdapter(adapter);
    }

    private void actualizarUIPorReproduccion(Song song) {
        this.actualSong = song;
        tvMiniTitulo.setText(song.getTitulo());
        tvMiniArtista.setText(song.getArtista());
        // El icono del botón se actualizará en el Listener de PlayerManager (lo añadiremos después)
        btnMiniPlay.setIconResource(R.drawable.ic_pause);
    }

    private void setupPlayerBar() {
        btnMiniPlay.setOnClickListener(v -> {
            if (playerManager.getCancionActual() != null) {
                if (playerManager.isPlaying()) {
                    playerManager.pauseSong();
                    btnMiniPlay.setIconResource(R.drawable.ic_play);
                } else {
                    playerManager.resumeSong();
                    btnMiniPlay.setIconResource(R.drawable.ic_pause);
                }
            } else {
                Toast.makeText(this, R.string.select_song_first, Toast.LENGTH_SHORT).show();
            }
        });
    }

    //Lógica para abrir la wea de la segunda vista.
    private void setupPlayerBarClick(){

        CardView playerBar = findViewById(R.id.playerBar);
        playerBar.setOnClickListener(v -> {

                abriPlayerActivity();
        });

    }

    private void abriPlayerActivity() {

        if (playerManager.getCancionActual() == null) {
            Toast.makeText(this, "Selecciona una canción primero", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent i = new Intent(this, PlayerActivity.class);

        //Como creamos una clase nueva para gestionar todo, no hace falta pasarle nada con el intent
        startActivity(i);
    }

    //Idioma y lógica:
    private void mostrarDialogoIdioma() {
        String[] idiomas = {getString(R.string.spanish), getString(R.string.english)};

        // Obtener idioma actual
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String idiomaActual = prefs.getString("Locale.Helper.Selected.Language", "es");
        int selectedIndex = idiomaActual.equals("es") ? 0 : 1;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.language)
                .setSingleChoiceItems(idiomas, selectedIndex, (dialog, which) -> {
                    String codigoIdioma = which == 0 ? "es" : "en";
                    cambiarIdioma(codigoIdioma);
                    dialog.dismiss();
                })
                .show();
    }

    private void cambiarIdioma(String codigoIdioma) {
        // Guardar preferencia
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.edit().putString("Locale.Helper.Selected.Language", codigoIdioma).apply();

        // Reiniciar actividad para aplicar cambios
        recreate();
    }


}
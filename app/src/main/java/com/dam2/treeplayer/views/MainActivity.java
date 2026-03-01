package com.dam2.treeplayer.views;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.cardview.widget.CardView;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.dam2.treeplayer.R;
import com.dam2.treeplayer.song_logic.*;
import com.dam2.treeplayer.utils.DeezerApiHelper;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends BaseActivity {
    private RecyclerView rvCanciones;
    private MaterialToolbar topAppBar;
    private MaterialButton btnMiniPlay;
    private TextView tvMiniTitle, tvMiniArtist, tvCounter;
    private ImageView languageIcon;
    private SongAdapter adapter;
    private List<Song> songList;
    private PlayerManager playerManager;
    private FavoritesManager favoritesManager;
    private DeezerApiHelper deezerApiHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        EdgeToEdge.enable(this);
        ViewCompat.setOnApplyWindowInsetsListener(topAppBar, (v, insets) -> {
            v.setPadding(0, insets.getInsets(WindowInsetsCompat.Type.statusBars()).top, 0, 0);
            return insets;
        });

        playerManager = PlayerManager.getInstance();
        favoritesManager = new FavoritesManager(this);
        deezerApiHelper = new DeezerApiHelper();

        setupToolbar();
        setupLanguageIcon();
        loadSongs();
        setupRecyclerView();
        setupPlayerBar();
    }

    private void initViews() {
        rvCanciones = findViewById(R.id.rvCanciones);
        topAppBar = findViewById(R.id.topAppBar);
        btnMiniPlay = findViewById(R.id.btnMiniPlay);
        tvMiniTitle = findViewById(R.id.tvMiniTitulo);
        tvMiniArtist = findViewById(R.id.tvMiniArtista);
        tvCounter = findViewById(R.id.tvContador);
        languageIcon = findViewById(R.id.language);
    }

    private void setupToolbar() {
        setSupportActionBar(topAppBar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayShowTitleEnabled(false);
    }

    private void setupLanguageIcon() {
        languageIcon.setOnClickListener(v -> {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            String nuevoIdioma = prefs.getString("Locale.Helper.Selected.Language", "es").equals("es") ? "en" : "es";
            prefs.edit().putString("Locale.Helper.Selected.Language", nuevoIdioma).apply();
            recreate();
        });
    }

    private void loadSongs() {
        songList = new ArrayList<>();

        //Album HELLMODE - Jeff Rosenstock
        songList.add(new Song("1", "WILL U STILL U", "Jeff Rosenstock", R.raw.will_u_still_u_01, "2:24", ""));
        songList.add(new Song("2", "HEAD", "Jeff Rosenstock", R.raw.head_02, "2:59", ""));
        songList.add(new Song("3", "LIKED U BETTER", "Jeff Rosenstock", R.raw.liked_u_better_03, "3:19", ""));
        songList.add(new Song("4", "DOUBT", "Jeff Rosenstock", R.raw.doubt_04, "4:08", ""));
        songList.add(new Song("5", "FUTURE IS DUMB", "Jeff Rosenstock", R.raw.future_is_dumb_05, "4:28", ""));
        songList.add(new Song("6", "SOFT LIVING", "Jeff Rosenstock", R.raw.soft_living_06, "3:34", ""));
        songList.add(new Song("7", "HEALMODE", "Jeff Rosenstock", R.raw.healmode_07, "3:41", ""));
        songList.add(new Song("8", "I WANNA BE WRONG", "Jeff Rosenstock", R.raw.i_wanna_be_wrong_09, "4:14", ""));
        songList.add(new Song("9", "GRAVEYARD SONG", "Jeff Rosenstock", R.raw.graveyard_song_10, "3:47", ""));
        songList.add(new Song("10", "3 SUMMERS", "Jeff Rosenstock", R.raw.three_summers_11, "4:38", ""));

        for (Song song : songList) {
            deezerApiHelper.searchAlbumArt(song.getTitle(), song.getArtist(), new DeezerApiHelper.ImageCallback() {
                @Override
                public void onSuccess(String imageUrl) {
                    song.setImageUrl(imageUrl);
                    runOnUiThread(() -> adapter.notifyDataSetChanged());
                }
                @Override public void onFailure() {}
            });
        }

        favoritesManager.updateFavoriteStatus(songList);
        playerManager.setPlaylist(songList);
        tvCounter.setText(songList.size() + " " + getString(R.string.canciones));
    }

    private void setupRecyclerView() {
        rvCanciones.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SongAdapter(songList, (song, position) -> {
            playerManager.playSong(this, song);
            tvMiniTitle.setText(song.getTitle());
            tvMiniArtist.setText(song.getArtist());
            btnMiniPlay.setIconResource(R.drawable.ic_pause);
        }, favoritesManager);
        rvCanciones.setAdapter(adapter);
    }

    private void setupPlayerBar() {
        CardView playerBar = findViewById(R.id.playerBar);
        btnMiniPlay = findViewById(R.id.btnMiniPlay);

        // Configurar click en el botón de play/pause
        btnMiniPlay.setOnClickListener(v -> {
            if (playerManager.getCurrentSong() != null) {
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

        // Configurar click en la barra del reproductor
        playerBar.setOnClickListener(v -> {
            if (playerManager.getCurrentSong() == null) {
                Toast.makeText(this, R.string.select_song_first, Toast.LENGTH_SHORT).show();
                return;
            }
            startActivity(new Intent(this, PlayerActivity.class));
        });
    }
}
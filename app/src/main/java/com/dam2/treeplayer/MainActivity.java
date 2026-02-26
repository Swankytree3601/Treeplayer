package com.dam2.treeplayer;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;

public class MainActivity extends AppCompatActivity {

    private RecyclerView rvCanciones;
    private MaterialToolbar topAppBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Inicializar vistas
        initViews();

        // Configurar toolbar
        setupToolbar();

        // Configurar RecyclerView
        setupRecyclerView();
    }

    private void initViews() {
        rvCanciones = findViewById(R.id.rvCanciones);
        topAppBar = findViewById(R.id.topAppBar);
    }

    private void setupToolbar() {
        setSupportActionBar(topAppBar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("TreePlayer");
        }

        // Click en el ícono de menú
        topAppBar.setNavigationOnClickListener(v -> {
            Toast.makeText(this, "Menú abierto", Toast.LENGTH_SHORT).show();
            // Falta añadir la lógica del menú (investigar que ahora estoy quemado xd) :)
        });

        // Clicks en los items del menú (próximanete :))
        topAppBar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_search) {
                Toast.makeText(this, "Buscar", Toast.LENGTH_SHORT).show();
                return true;
            } else if (item.getItemId() == R.id.action_settings) {
                Toast.makeText(this, "Ajustes", Toast.LENGTH_SHORT).show();
                return true;
            }
            return false;
        });
    }

    private void setupRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rvCanciones.setLayoutManager(layoutManager);
    }
}
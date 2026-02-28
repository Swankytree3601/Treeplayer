package com.dam2.treeplayer.song_logic;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.dam2.treeplayer.R;

import java.util.List;

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.CancionViewHolder> {

    private List<Song> listaCanciones;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Song song, int position);
    }

    public SongAdapter(List<Song> listaCanciones, OnItemClickListener listener) {
        this.listaCanciones = listaCanciones;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CancionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cancion, parent, false);
        return new CancionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CancionViewHolder holder, int position) {
        Song song = listaCanciones.get(position);

        // Formatear número con 2 dígitos (01, 02, etc.)
        String numero = String.format("%02d", position + 1);
        holder.tvNumero.setText(numero);

        holder.tvTitulo.setText(song.getTitulo());
        holder.tvArtista.setText(song.getArtista());
        holder.tvDuracion.setText(song.getDuracion());

        // La portada está "gone" por defecto en tu layout, así que no hacemos nada

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(song, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return listaCanciones.size();
    }

    public static class CancionViewHolder extends RecyclerView.ViewHolder {
        TextView tvNumero, tvTitulo, tvArtista, tvDuracion;
        ImageView ivPortada, ivMenu; // Aunque estén "gone", los declaramos por si acaso

        public CancionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNumero = itemView.findViewById(R.id.tvNumero);
            tvTitulo = itemView.findViewById(R.id.tvTitulo);
            tvArtista = itemView.findViewById(R.id.tvArtista);
            tvDuracion = itemView.findViewById(R.id.tvDuracion);
            ivPortada = itemView.findViewById(R.id.ivPortada);
            ivMenu = itemView.findViewById(R.id.ivMenu);
        }
    }
}
package com.dam2.treeplayer.song_logic;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.dam2.treeplayer.R;
import com.bumptech.glide.Glide;
import java.util.List;

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.CancionViewHolder> {
    private List<Song> listaCanciones;
    private OnItemClickListener listener;
    private FavoritesManager favoritesManager;

    public interface OnItemClickListener {
        void onItemClick(Song song, int position);
    }

    public SongAdapter(List<Song> listaCanciones, OnItemClickListener listener, FavoritesManager favoritesManager) {
        this.listaCanciones = listaCanciones;
        this.listener = listener;
        this.favoritesManager = favoritesManager;
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
        holder.tvNumero.setText(String.format("%02d", position + 1));
        holder.tvTitulo.setText(song.getTitle());
        holder.tvArtista.setText(song.getArtist());
        holder.tvDuracion.setText(song.getDuracion());

        holder.ivFavorite.setImageResource(song.isFavorite() ? R.drawable.ic_favorite_filled : R.drawable.ic_favorite_border);
        holder.ivFavorite.setOnClickListener(v -> {
            boolean newFavoriteState = !song.isFavorite();
            song.setFavorite(newFavoriteState);
            if (newFavoriteState) favoritesManager.addFavorite(song.getId());
            else favoritesManager.removeFavorite(song.getId());
            notifyItemChanged(position);
        });

        if (song.getImageUrl() != null && !song.getImageUrl().isEmpty()) {
            Glide.with(holder.itemView.getContext()).load(song.getImageUrl()).placeholder(R.drawable.ic_default_album_final).into(holder.ivPortada);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(song, position);
        });
    }

    @Override
    public int getItemCount() { return listaCanciones.size(); }

    public static class CancionViewHolder extends RecyclerView.ViewHolder {
        TextView tvNumero, tvTitulo, tvArtista, tvDuracion;
        ImageView ivPortada, ivFavorite;

        public CancionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNumero = itemView.findViewById(R.id.tvNumero);
            tvTitulo = itemView.findViewById(R.id.tvTitulo);
            tvArtista = itemView.findViewById(R.id.tvArtista);
            tvDuracion = itemView.findViewById(R.id.tvDuracion);
            ivPortada = itemView.findViewById(R.id.ivPortada);
            ivFavorite = itemView.findViewById(R.id.ivFavorite);
        }
    }
}
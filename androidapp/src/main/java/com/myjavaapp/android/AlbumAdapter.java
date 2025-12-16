package com.myjavaapp.android;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textview.MaterialTextView;

import java.util.ArrayList;
import java.util.List;

import coil.Coil;
import coil.request.ImageRequest;

/**
 * Adapter for displaying albums in search results
 */
public class AlbumAdapter extends RecyclerView.Adapter<AlbumAdapter.AlbumViewHolder> {

    public interface OnAlbumClickListener {
        void onAlbumClick(AlbumItem album);
    }

    private List<AlbumItem> albums = new ArrayList<>();
    private final OnAlbumClickListener listener;

    public AlbumAdapter(OnAlbumClickListener listener) {
        this.listener = listener;
    }

    public void submitList(List<AlbumItem> newAlbums) {
        albums = newAlbums;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public AlbumViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_album_search, parent, false);
        return new AlbumViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AlbumViewHolder holder, int position) {
        holder.bind(albums.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return albums.size();
    }

    public static class AlbumViewHolder extends RecyclerView.ViewHolder {
        private final MaterialTextView titleText;
        private final MaterialTextView artistText;
        private final MaterialTextView infoText;
        private final MaterialButton viewButton;
        private final ShapeableImageView thumbnail;

        public AlbumViewHolder(@NonNull View itemView) {
            super(itemView);
            titleText = itemView.findViewById(R.id.albumTitle);
            artistText = itemView.findViewById(R.id.albumArtist);
            infoText = itemView.findViewById(R.id.albumInfo);
            viewButton = itemView.findViewById(R.id.viewButton);
            thumbnail = itemView.findViewById(R.id.albumThumbnail);
        }

        public void bind(AlbumItem album, OnAlbumClickListener listener) {
            titleText.setText(album.getTitle());
            artistText.setText(album.getArtist());

            // Format info text
            StringBuilder info = new StringBuilder();
            if (album.getYear() != null && !album.getYear().isEmpty()) {
                info.append(album.getYear()).append(" • ");
            }
            if (album.getType() != null && !album.getType().isEmpty()) {
                info.append(album.getType());
            } else {
                info.append("Album");
            }
            infoText.setText(info.toString());

            viewButton.setOnClickListener(v -> listener.onAlbumClick(album));

            // Load thumbnail with Coil
            if (album.getThumbnailUrl() != null && !album.getThumbnailUrl().isEmpty()) {
                ImageRequest request = new ImageRequest.Builder(itemView.getContext())
                    .data(album.getThumbnailUrl())
                    .target(thumbnail)
                    .placeholder(R.drawable.ic_play)
                    .error(R.drawable.ic_play)
                    .build();
                Coil.imageLoader(itemView.getContext()).enqueue(request);
            } else {
                thumbnail.setImageResource(R.drawable.ic_play);
            }
        }
    }
}


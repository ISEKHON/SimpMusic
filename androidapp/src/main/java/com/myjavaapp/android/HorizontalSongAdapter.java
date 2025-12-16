package com.myjavaapp.android;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textview.MaterialTextView;

import java.util.ArrayList;
import java.util.List;

import coil.Coil;
import coil.request.ImageRequest;

/**
 * Adapter for horizontal scrolling song items
 */
public class HorizontalSongAdapter extends RecyclerView.Adapter<HorizontalSongAdapter.HorizontalSongViewHolder> {

    public interface OnSongClickListener {
        void onSongClick(SongItem song);
    }

    private List<SongItem> songs = new ArrayList<>();
    private final OnSongClickListener listener;

    public HorizontalSongAdapter(OnSongClickListener listener) {
        this.listener = listener;
    }

    public void submitList(List<SongItem> newSongs) {
        songs = newSongs;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public HorizontalSongViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_song_horizontal, parent, false);
        return new HorizontalSongViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HorizontalSongViewHolder holder, int position) {
        holder.bind(songs.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return songs.size();
    }

    public static class HorizontalSongViewHolder extends RecyclerView.ViewHolder {
        private final MaterialTextView titleText;
        private final MaterialTextView artistText;
        private final ShapeableImageView thumbnail;

        public HorizontalSongViewHolder(@NonNull View itemView) {
            super(itemView);
            titleText = itemView.findViewById(R.id.songTitle);
            artistText = itemView.findViewById(R.id.songArtist);
            thumbnail = itemView.findViewById(R.id.songThumbnail);
        }

        public void bind(SongItem song, OnSongClickListener listener) {
            titleText.setText(song.getTitle());
            artistText.setText(song.getArtist());

            // Make entire card clickable
            itemView.setOnClickListener(v -> listener.onSongClick(song));

            // Load thumbnail with Coil
            if (song.getThumbnailUrl() != null && !song.getThumbnailUrl().isEmpty()) {
                ImageRequest request = new ImageRequest.Builder(itemView.getContext())
                    .data(song.getThumbnailUrl())
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


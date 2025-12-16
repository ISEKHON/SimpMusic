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

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.SongViewHolder> {

    public interface OnPlayClickListener {
        void onPlayClick(SongItem song);
    }

    private List<SongItem> songs = new ArrayList<>();
    private final OnPlayClickListener listener;

    public SongAdapter(OnPlayClickListener listener) {
        this.listener = listener;
    }

    public void submitList(List<SongItem> newSongs) {
        songs = newSongs;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SongViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_song, parent, false);
        return new SongViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SongViewHolder holder, int position) {
        holder.bind(songs.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return songs.size();
    }

    public static class SongViewHolder extends RecyclerView.ViewHolder {
        private final MaterialTextView titleText;
        private final MaterialTextView artistText;
        private final MaterialTextView durationText;
        private final MaterialButton playButton;
        private final ShapeableImageView thumbnail;

        public SongViewHolder(@NonNull View itemView) {
            super(itemView);
            titleText = itemView.findViewById(R.id.songTitle);
            artistText = itemView.findViewById(R.id.songArtist);
            durationText = itemView.findViewById(R.id.songDuration);
            playButton = itemView.findViewById(R.id.playButton);
            thumbnail = itemView.findViewById(R.id.songThumbnail);
        }

        public void bind(SongItem song, OnPlayClickListener listener) {
            titleText.setText(song.getTitle());
            artistText.setText(song.getArtist());
            durationText.setText(song.getDuration());
            playButton.setOnClickListener(v -> listener.onPlayClick(song));

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


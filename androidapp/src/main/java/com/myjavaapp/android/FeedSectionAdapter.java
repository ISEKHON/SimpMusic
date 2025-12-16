package com.myjavaapp.android;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textview.MaterialTextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for displaying feed sections with horizontal scrolling
 */
public class FeedSectionAdapter extends RecyclerView.Adapter<FeedSectionAdapter.SectionViewHolder> {

    public interface OnSongClickListener {
        void onSongClick(SongItem song);
    }

    public interface OnAlbumClickListener {
        void onAlbumClick(AlbumItem album);
    }

    private List<FeedSection> sections = new ArrayList<>();
    private final OnSongClickListener songListener;
    private final OnAlbumClickListener albumListener;

    public FeedSectionAdapter(OnSongClickListener songListener, OnAlbumClickListener albumListener) {
        this.songListener = songListener;
        this.albumListener = albumListener;
    }

    public void submitSections(List<FeedSection> newSections) {
        sections = newSections;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SectionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_section, parent, false);
        return new SectionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SectionViewHolder holder, int position) {
        holder.bind(sections.get(position), songListener, albumListener);
    }

    @Override
    public int getItemCount() {
        return sections.size();
    }

    public static class SectionViewHolder extends RecyclerView.ViewHolder {
        private final MaterialTextView sectionTitle;
        private final MaterialButton playAllButton;
        private final RecyclerView horizontalRecyclerView;

        public SectionViewHolder(@NonNull View itemView) {
            super(itemView);
            sectionTitle = itemView.findViewById(R.id.sectionTitle);
            playAllButton = itemView.findViewById(R.id.playAllButton);
            horizontalRecyclerView = itemView.findViewById(R.id.horizontalRecyclerView);

            // Setup horizontal layout
            LinearLayoutManager layoutManager = new LinearLayoutManager(
                itemView.getContext(),
                LinearLayoutManager.HORIZONTAL,
                false
            );
            horizontalRecyclerView.setLayoutManager(layoutManager);
        }

        public void bind(FeedSection section, OnSongClickListener songListener, OnAlbumClickListener albumListener) {
            sectionTitle.setText(section.getTitle());

            if (section.isSongSection()) {
                // Create horizontal song adapter
                HorizontalSongAdapter adapter = new HorizontalSongAdapter(song -> songListener.onSongClick(song));
                adapter.submitList(section.getSongs());
                horizontalRecyclerView.setAdapter(adapter);

                // Play all button
                playAllButton.setOnClickListener(v -> {
                    if (!section.getSongs().isEmpty()) {
                        songListener.onSongClick(section.getSongs().get(0));
                    }
                });
            } else if (section.isAlbumSection()) {
                // Create horizontal album adapter
                HorizontalAlbumAdapter adapter = new HorizontalAlbumAdapter(album -> albumListener.onAlbumClick(album));
                adapter.submitList(section.getAlbums());
                horizontalRecyclerView.setAdapter(adapter);

                // Play all button (opens first album)
                playAllButton.setOnClickListener(v -> {
                    if (!section.getAlbums().isEmpty()) {
                        albumListener.onAlbumClick(section.getAlbums().get(0));
                    }
                });
            }
        }
    }
}


package com.myjavaapp.android;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.myjavaapp.bridge.YouTubeBridge;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Enhanced feed fragment with horizontal scrolling sections
 */
public class FeedFragmentSections extends Fragment {

    private YouTubeBridge bridge;
    private ExecutorService executor;
    private Handler mainHandler;

    private ChipGroup categoryChips;
    private RecyclerView sectionsRecyclerView;
    private LinearProgressIndicator progressBar;
    private FeedSectionAdapter sectionAdapter;

    public static FeedFragmentSections newInstance() {
        return new FeedFragmentSections();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bridge = new YouTubeBridge();
        executor = Executors.newCachedThreadPool();
        mainHandler = new Handler(Looper.getMainLooper());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_feed_sections, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        categoryChips = view.findViewById(R.id.categoryChips);
        sectionsRecyclerView = view.findViewById(R.id.sectionsRecyclerView);
        progressBar = view.findViewById(R.id.progressBar);

        // Setup sections RecyclerView
        sectionAdapter = new FeedSectionAdapter(this::onSongClick, this::onAlbumClick);
        sectionsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        sectionsRecyclerView.setAdapter(sectionAdapter);

        // Setup chip listeners (for future category filtering)
        setupChips();

        // Load initial data
        loadHomeFeed();
    }

    private void setupChips() {
        for (int i = 0; i < categoryChips.getChildCount(); i++) {
            View child = categoryChips.getChildAt(i);
            if (child instanceof Chip) {
                Chip chip = (Chip) child;
                chip.setOnClickListener(v -> {
                    // Future: filter by category
                    Toast.makeText(requireContext(),
                        "Filter by: " + chip.getText(),
                        Toast.LENGTH_SHORT).show();
                });
            }
        }
    }

    @SuppressWarnings("ConstantConditions")
    private void loadHomeFeed() {
        progressBar.setVisibility(View.VISIBLE);

        executor.execute(() -> {
            try {
                var feedData = bridge.getHomeFeedSections();

                mainHandler.post(() -> {
                    progressBar.setVisibility(View.GONE);

                    if (feedData != null) {
                        List<FeedSection> sections = new ArrayList<>();

                        // Add Trending Songs section
                        if (!feedData.getTrendingSongs().isEmpty()) {
                            sections.add(new FeedSection(
                                "Trending songs for you",
                                convertToSongItems(feedData.getTrendingSongs()),
                                FeedSection.SectionType.TRENDING_SONGS
                            ));
                        }

                        // Add New Albums section (using the albums list)
                        if (!feedData.getNewAlbums().isEmpty()) {
                            List<AlbumItem> albums = convertToAlbumItems(feedData.getNewAlbums());
                            if (!albums.isEmpty()) {
                                sections.add(new FeedSection(
                                    "New releases",
                                    FeedSection.SectionType.NEW_ALBUMS,
                                    albums
                                ));
                            }
                        }

                        // Add Top Tracks section
                        if (!feedData.getTopTracks().isEmpty()) {
                            sections.add(new FeedSection(
                                "Top tracks",
                                convertToSongItems(feedData.getTopTracks()),
                                FeedSection.SectionType.TOP_TRACKS
                            ));
                        }

                        if (!sections.isEmpty()) {
                            sectionAdapter.submitSections(sections);
                        } else {
                            Toast.makeText(requireContext(),
                                "No content available",
                                Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(requireContext(),
                            "Failed to load feed",
                            Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                mainHandler.post(() -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(requireContext(),
                        "Error: " + e.getMessage(),
                        Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    @SuppressWarnings("unchecked")
    private List<SongItem> convertToSongItems(List songList) {
        List<SongItem> result = new ArrayList<>();

        for (Object songObj : songList) {
            var song = (com.maxrave.kotlinytmusicscraper.models.SongItem) songObj;

            String artist = "Unknown";
            try {
                Object artists = song.getArtists();
                if (artists != null) {
                    StringBuilder artistNames = new StringBuilder();
                    int count = 0;
                    for (Object artistObj : (Iterable<?>) artists) {
                        if (count > 0) artistNames.append(", ");
                        var artistItem = (com.maxrave.kotlinytmusicscraper.models.Artist) artistObj;
                        artistNames.append(artistItem.getName());
                        count++;
                    }
                    if (count > 0) {
                        artist = artistNames.toString();
                    }
                }
            } catch (Exception e) {
                artist = "Unknown";
            }

            result.add(new SongItem(
                song.getId(),
                song.getTitle(),
                artist,
                formatDuration(song.getDuration()),
                song.getThumbnail()
            ));
        }

        return result;
    }

    private void onSongClick(SongItem song) {
        Intent intent = new Intent(requireContext(), PlayerActivity.class);
        intent.putExtra("VIDEO_ID", song.getVideoId());
        intent.putExtra("TITLE", song.getTitle());
        intent.putExtra("ARTIST", song.getArtist());
        intent.putExtra("THUMBNAIL", song.getThumbnailUrl());
        startActivity(intent);
    }

    private void onAlbumClick(AlbumItem album) {
        Intent intent = new Intent(requireContext(), AlbumDetailActivity.class);
        intent.putExtra("BROWSE_ID", album.getBrowseId());
        intent.putExtra("TITLE", album.getTitle());
        intent.putExtra("ARTIST", album.getArtist());
        intent.putExtra("THUMBNAIL", album.getThumbnailUrl());
        intent.putExtra("YEAR", album.getYear());
        intent.putExtra("TYPE", album.getType());
        startActivity(intent);
    }

    @SuppressWarnings({"unchecked", "ConstantConditions"})
    private List<AlbumItem> convertToAlbumItems(List albumList) {
        List<AlbumItem> result = new ArrayList<>();

        for (Object albumObj : albumList) {
            try {
                var album = (com.maxrave.kotlinytmusicscraper.models.AlbumItem) albumObj;

                String artist = "Unknown";
                try {
                    Object artists = album.getArtists();
                    if (artists != null) {
                        StringBuilder artistNames = new StringBuilder();
                        int count = 0;
                        for (Object artistObj : (Iterable<?>) artists) {
                            if (count > 0) artistNames.append(", ");
                            var artistItem = (com.maxrave.kotlinytmusicscraper.models.Artist) artistObj;
                            artistNames.append(artistItem.getName());
                            count++;
                        }
                        if (count > 0) {
                            artist = artistNames.toString();
                        }
                    }
                } catch (Exception e) {
                    artist = "Unknown";
                }

                String year = "";
                try {
                    var yearValue = album.getYear();
                    if (yearValue != null) {
                        year = String.valueOf(yearValue);
                    }
                } catch (Exception e) {
                    year = "";
                }

                // Get album fields (Kotlin @NotNull - guaranteed non-null)
                result.add(new AlbumItem(
                    album.getId(),
                    album.getTitle(),
                    artist != null ? artist : "Unknown",
                    year != null ? year : "",
                    album.getThumbnail(),
                    "Album"
                ));
            } catch (Exception e) {
                // Skip invalid items
            }
        }

        return result;
    }

    @SuppressWarnings("ConstantConditions")
    private String formatDuration(@Nullable Integer seconds) {
        if (seconds == null) return "";
        int mins = seconds / 60;
        int secs = seconds % 60;
        return String.format("%d:%02d", mins, secs);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (executor != null) {
            executor.shutdownNow();
        }
    }
}


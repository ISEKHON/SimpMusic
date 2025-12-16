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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.myjavaapp.bridge.YouTubeBridge;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FeedFragment extends Fragment {

    public enum FeedType {
        HOME, TOP_TRACKS, NEW_SINGLES
    }

    private static final String ARG_FEED_TYPE = "feed_type";

    private FeedType feedType;
    private YouTubeBridge bridge;
    private ExecutorService executor;
    private Handler mainHandler;

    private SwipeRefreshLayout swipeRefresh;
    private RecyclerView recyclerView;
    private LinearProgressIndicator progressBar;
    private SongAdapter adapter;

    public static FeedFragment newInstance(FeedType feedType) {
        FeedFragment fragment = new FeedFragment();
        Bundle args = new Bundle();
        args.putString(ARG_FEED_TYPE, feedType.name());
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            String typeName = getArguments().getString(ARG_FEED_TYPE);
            feedType = FeedType.valueOf(typeName);
        }
        bridge = new YouTubeBridge();
        executor = Executors.newCachedThreadPool();
        mainHandler = new Handler(Looper.getMainLooper());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_feed, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        swipeRefresh = view.findViewById(R.id.swipeRefresh);
        recyclerView = view.findViewById(R.id.recyclerView);
        progressBar = view.findViewById(R.id.progressBar);

        // Setup RecyclerView
        adapter = new SongAdapter(this::onSongClick);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);

        // Setup SwipeRefresh
        swipeRefresh.setOnRefreshListener(this::loadFeed);

        // Load initial data
        loadFeed();
    }

    @SuppressWarnings({"unchecked", "ConstantConditions"})
    private void loadFeed() {
        progressBar.setVisibility(View.VISIBLE);

        executor.execute(() -> {
            try {
                List songs = null;
                switch (feedType) {
                    case HOME:
                        songs = bridge.getHomeFeed();
                        break;
                    case TOP_TRACKS:
                        songs = bridge.getTopTracks();
                        break;
                    case NEW_SINGLES:
                        songs = bridge.getNewSingles();
                        break;
                }

                List<com.maxrave.kotlinytmusicscraper.models.SongItem> finalSongs = songs;
                mainHandler.post(() -> {
                    swipeRefresh.setRefreshing(false);
                    progressBar.setVisibility(View.GONE);

                    if (finalSongs != null && !finalSongs.isEmpty()) {
                        List<SongItem> songItems = new ArrayList<>();
                        for (Object songObj : finalSongs) {
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

                            songItems.add(new SongItem(
                                song.getId(),
                                song.getTitle(),
                                artist,
                                formatDuration(song.getDuration()),
                                song.getThumbnail()
                            ));
                        }
                        adapter.submitList(songItems);
                    } else {
                        Toast.makeText(requireContext(), "No content available", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                mainHandler.post(() -> {
                    swipeRefresh.setRefreshing(false);
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(requireContext(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void onSongClick(SongItem song) {
        Intent intent = new Intent(requireContext(), PlayerActivity.class);
        intent.putExtra("VIDEO_ID", song.getVideoId());
        intent.putExtra("TITLE", song.getTitle());
        intent.putExtra("ARTIST", song.getArtist());
        intent.putExtra("THUMBNAIL", song.getThumbnailUrl());
        startActivity(intent);
    }

    @SuppressWarnings("ConstantConditions")
    private String formatDuration(Integer seconds) {
        if (seconds == null) return "--:--";
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


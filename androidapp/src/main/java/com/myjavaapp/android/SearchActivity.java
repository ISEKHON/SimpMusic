package com.myjavaapp.android;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textfield.TextInputEditText;
import com.myjavaapp.bridge.YouTubeBridge;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SearchActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private TextInputEditText searchInput;
    private MaterialButton searchButton;
    private TabLayout tabLayout;
    private RecyclerView recyclerView;
    private LinearProgressIndicator progressBar;

    private YouTubeBridge bridge;
    private SongAdapter songAdapter;
    private AlbumAdapter albumAdapter;
    private ExecutorService executor;
    private Handler mainHandler;

    private String lastQuery = "";
    private boolean isShowingSongs = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        // Initialize
        executor = Executors.newCachedThreadPool();
        mainHandler = new Handler(Looper.getMainLooper());
        bridge = new YouTubeBridge();

        // Find views
        toolbar = findViewById(R.id.toolbar);
        searchInput = findViewById(R.id.searchInput);
        searchButton = findViewById(R.id.searchButton);
        tabLayout = findViewById(R.id.tabLayout);
        recyclerView = findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.progressBar);

        // Setup toolbar
        toolbar.setNavigationOnClickListener(v -> finish());

        // Setup adapters
        songAdapter = new SongAdapter(this::onSongClick);
        albumAdapter = new AlbumAdapter(this::onAlbumClick);

        // Setup RecyclerView with song adapter initially
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(songAdapter);

        // Setup tabs
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                isShowingSongs = (position == 0);

                // Switch adapter
                if (isShowingSongs) {
                    recyclerView.setAdapter(songAdapter);
                } else {
                    recyclerView.setAdapter(albumAdapter);
                }

                // If we have a query, re-search
                if (!lastQuery.isEmpty()) {
                    performSearch();
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        // Setup search
        searchButton.setOnClickListener(v -> performSearch());
        searchInput.setOnEditorActionListener((v, actionId, event) -> {
            performSearch();
            return true;
        });
    }

    @SuppressWarnings({"unchecked", "ConstantConditions"})
    private void performSearch() {
        String query = searchInput.getText() != null ? searchInput.getText().toString().trim() : "";
        if (query.isEmpty()) {
            Toast.makeText(this, "Please enter a search query", Toast.LENGTH_SHORT).show();
            return;
        }

        lastQuery = query;

        // Hide keyboard
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (imm != null && getCurrentFocus() != null) {
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
        searchInput.clearFocus();

        // Show loading
        progressBar.setVisibility(View.VISIBLE);
        searchButton.setEnabled(false);

        String finalQuery = query;
        executor.execute(() -> {
            try {
                if (isShowingSongs) {
                    searchSongs(finalQuery);
                } else {
                    searchAlbums(finalQuery);
                }
            } catch (Exception e) {
                mainHandler.post(() -> {
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    progressBar.setVisibility(View.GONE);
                    searchButton.setEnabled(true);
                });
            }
        });
    }

    @SuppressWarnings("unchecked")
    private void searchSongs(String query) {
        List songs = bridge.searchSongs(query);

        mainHandler.post(() -> {
            if (songs != null && !songs.isEmpty()) {
                List<SongItem> songItems = new ArrayList<>();
                for (Object songObj : songs) {
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
                songAdapter.submitList(songItems);
                Toast.makeText(this, "Found " + songs.size() + " songs", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "No songs found", Toast.LENGTH_SHORT).show();
            }
            progressBar.setVisibility(View.GONE);
            searchButton.setEnabled(true);
        });
    }

    @SuppressWarnings("unchecked")
    private void searchAlbums(String query) {
        List albums = bridge.searchAlbums(query);

        mainHandler.post(() -> {
            if (albums != null && !albums.isEmpty()) {
                List<AlbumItem> albumItems = new ArrayList<>();
                for (Object albumObj : albums) {
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
                        Integer yearValue = album.getYear();
                        if (yearValue != null) {
                            year = String.valueOf(yearValue);
                        }
                    } catch (Exception e) {
                        year = "";
                    }

                    albumItems.add(new AlbumItem(
                        album.getId(),
                        album.getTitle(),
                        artist != null ? artist : "Unknown",
                        year != null ? year : "",
                        album.getThumbnail(),
                        "Album"
                    ));
                }
                albumAdapter.submitList(albumItems);
                Toast.makeText(this, "Found " + albums.size() + " albums", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "No albums found", Toast.LENGTH_SHORT).show();
            }
            progressBar.setVisibility(View.GONE);
            searchButton.setEnabled(true);
        });
    }

    private void onSongClick(SongItem song) {
        Intent intent = new Intent(this, PlayerActivity.class);
        intent.putExtra("VIDEO_ID", song.getVideoId());
        intent.putExtra("TITLE", song.getTitle());
        intent.putExtra("ARTIST", song.getArtist());
        intent.putExtra("THUMBNAIL", song.getThumbnailUrl());
        startActivity(intent);
    }

    private void onAlbumClick(AlbumItem album) {
        Intent intent = new Intent(this, AlbumDetailActivity.class);
        intent.putExtra("BROWSE_ID", album.getBrowseId());
        intent.putExtra("TITLE", album.getTitle());
        intent.putExtra("ARTIST", album.getArtist());
        intent.putExtra("THUMBNAIL", album.getThumbnailUrl());
        intent.putExtra("YEAR", album.getYear());
        intent.putExtra("TYPE", album.getType());
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
    protected void onDestroy() {
        super.onDestroy();
        if (executor != null) {
            executor.shutdownNow();
        }
    }
}


package com.myjavaapp.android;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.textview.MaterialTextView;
import com.myjavaapp.bridge.YouTubeBridge;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import coil.Coil;
import coil.request.ImageRequest;

/**
 * Activity to display album details and tracks
 */
public class AlbumDetailActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private CollapsingToolbarLayout collapsingToolbar;
    private ShapeableImageView albumArtBackground;
    private MaterialTextView albumTitle;
    private MaterialTextView albumArtist;
    private MaterialTextView albumInfo;
    private MaterialButton playAllButton;
    private LinearProgressIndicator loadingProgress;
    private RecyclerView tracksRecyclerView;

    private YouTubeBridge bridge;
    private SongAdapter trackAdapter;
    private ExecutorService executor;
    private Handler mainHandler;

    private String browseId;
    private String title;
    private String artist;
    private String thumbnailUrl;
    private String year;
    private String type;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album_detail);

        // Get intent extras
        browseId = getIntent().getStringExtra("BROWSE_ID");
        title = getIntent().getStringExtra("TITLE");
        artist = getIntent().getStringExtra("ARTIST");
        thumbnailUrl = getIntent().getStringExtra("THUMBNAIL");
        year = getIntent().getStringExtra("YEAR");
        type = getIntent().getStringExtra("TYPE");

        // Initialize
        executor = Executors.newCachedThreadPool();
        mainHandler = new Handler(Looper.getMainLooper());
        bridge = new YouTubeBridge();

        // Find views
        toolbar = findViewById(R.id.toolbar);
        collapsingToolbar = findViewById(R.id.collapsingToolbar);
        albumArtBackground = findViewById(R.id.albumArtBackground);
        albumTitle = findViewById(R.id.albumTitle);
        albumArtist = findViewById(R.id.albumArtist);
        albumInfo = findViewById(R.id.albumInfo);
        playAllButton = findViewById(R.id.playAllButton);
        loadingProgress = findViewById(R.id.loadingProgress);
        tracksRecyclerView = findViewById(R.id.tracksRecyclerView);

        // Setup toolbar
        toolbar.setNavigationOnClickListener(v -> finish());
        collapsingToolbar.setTitle(title);

        // Set album info
        albumTitle.setText(title != null ? title : "Unknown Album");
        albumArtist.setText(artist != null ? artist : "Unknown Artist");

        // Format album info
        StringBuilder infoBuilder = new StringBuilder();
        if (year != null && !year.isEmpty()) {
            infoBuilder.append(year).append(" • ");
        }
        if (type != null && !type.isEmpty()) {
            infoBuilder.append(type);
        } else {
            infoBuilder.append("Album");
        }
        albumInfo.setText(infoBuilder.toString());

        // Load album art
        if (thumbnailUrl != null && !thumbnailUrl.isEmpty()) {
            ImageRequest request = new ImageRequest.Builder(this)
                .data(thumbnailUrl)
                .target(albumArtBackground)
                .placeholder(R.drawable.ic_play)
                .error(R.drawable.ic_play)
                .build();
            Coil.imageLoader(this).enqueue(request);
        }

        // Setup RecyclerView
        trackAdapter = new SongAdapter(this::onTrackClick);
        tracksRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        tracksRecyclerView.setAdapter(trackAdapter);

        // Setup play all button
        playAllButton.setOnClickListener(v -> {
            // Play first track
            List<SongItem> tracks = getCurrentTracks();
            if (!tracks.isEmpty()) {
                onTrackClick(tracks.get(0));
            }
        });

        // Load album tracks if we have browse ID
        if (browseId != null && !browseId.isEmpty()) {
            loadAlbumTracks();
        } else {
            Toast.makeText(this, "Invalid album ID", Toast.LENGTH_SHORT).show();
        }
    }

    private List<SongItem> getCurrentTracks() {
        // Get tracks from adapter if available
        return new ArrayList<>();
    }

    @SuppressWarnings({"unchecked", "ConstantConditions"})
    private void loadAlbumTracks() {
        loadingProgress.setVisibility(View.VISIBLE);
        playAllButton.setEnabled(false);

        executor.execute(() -> {
            try {
                // Fetch real album details with tracks
                var albumDetails = bridge.getAlbumDetails(browseId);

                mainHandler.post(() -> {
                    loadingProgress.setVisibility(View.GONE);
                    playAllButton.setEnabled(true);

                    if (albumDetails != null && !albumDetails.getSongs().isEmpty()) {
                        // Convert to SongItem list
                        List<SongItem> tracks = new ArrayList<>();

                        for (Object songObj : albumDetails.getSongs()) {
                            try {
                                // Songs are kotlinytmusicscraper.models.SongItem
                                var song = (com.maxrave.kotlinytmusicscraper.models.SongItem) songObj;

                                String artistStr = this.artist != null ? this.artist : "Unknown";
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
                                            artistStr = artistNames.toString();
                                        }
                                    }
                                } catch (Exception e) {
                                    artistStr = this.artist != null ? this.artist : "Unknown";
                                }

                                String duration = "--:--";
                                try {
                                    var durationSecs = song.getDuration();
                                    if (durationSecs != null && durationSecs > 0) {
                                        int mins = durationSecs / 60;
                                        int secs = durationSecs % 60;
                                        duration = String.format("%d:%02d", mins, secs);
                                    }
                                } catch (Exception e) {
                                    duration = "--:--";
                                }

                                String trackThumbnail = thumbnailUrl;
                                try {
                                    String songThumb = song.getThumbnail();
                                    if (songThumb != null && !songThumb.isEmpty()) {
                                        trackThumbnail = songThumb;
                                    }
                                } catch (Exception e) {
                                    trackThumbnail = thumbnailUrl;
                                }

                                tracks.add(new SongItem(
                                    song.getId(),
                                    song.getTitle(),
                                    artistStr,
                                    duration,
                                    trackThumbnail
                                ));
                            } catch (Exception e) {
                                // Skip invalid tracks
                                e.printStackTrace();
                            }
                        }

                        if (!tracks.isEmpty()) {
                            trackAdapter.submitList(tracks);

                            // Update track count
                            String currentInfo = albumInfo.getText().toString();
                            if (!currentInfo.contains("tracks")) {
                                albumInfo.setText(currentInfo + " • " + tracks.size() + " tracks");
                            }
                        } else {
                            Toast.makeText(this, "No tracks found in album", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "Could not load album tracks", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                mainHandler.post(() -> {
                    loadingProgress.setVisibility(View.GONE);
                    playAllButton.setEnabled(true);
                    Toast.makeText(this, "Error loading tracks: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void onTrackClick(SongItem track) {
        Intent intent = new Intent(this, PlayerActivity.class);
        intent.putExtra("VIDEO_ID", track.getVideoId());
        intent.putExtra("TITLE", track.getTitle());
        intent.putExtra("ARTIST", track.getArtist());
        intent.putExtra("THUMBNAIL", track.getThumbnailUrl());
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executor != null) {
            executor.shutdownNow();
        }
    }
}


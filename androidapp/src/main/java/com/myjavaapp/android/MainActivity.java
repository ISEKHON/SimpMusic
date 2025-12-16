package com.myjavaapp.android;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.media3.common.MediaItem;
import androidx.media3.common.Player;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textview.MaterialTextView;
import com.myjavaapp.bridge.YouTubeBridge;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import coil.Coil;
import coil.request.ImageRequest;

public class MainActivity extends AppCompatActivity {

    private TextInputEditText searchInput;
    private MaterialButton searchButton;
    private RecyclerView recyclerView;
    private LinearProgressIndicator progressBar;
    private MaterialTextView nowPlayingText;
    private MaterialButton playPauseButton;
    private MaterialButton stopButton;
    private MaterialTextView contentLabel;

    private YouTubeBridge bridge;
    private ExoPlayer player;
    private SongAdapter adapter;
    private ExecutorService executor;
    private Handler mainHandler;

    private SeekBar seekBar;
    private MaterialTextView currentTimeText;
    private MaterialTextView totalTimeText;
    private ShapeableImageView albumArtImage;
    private Runnable updateSeekBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize executor and handler
        executor = Executors.newCachedThreadPool();
        mainHandler = new Handler(Looper.getMainLooper());

        // Initialize bridge
        bridge = new YouTubeBridge();

        // Initialize views
        searchInput = findViewById(R.id.searchInput);
        searchButton = findViewById(R.id.searchButton);
        recyclerView = findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.progressBar);
        nowPlayingText = findViewById(R.id.nowPlayingText);
        playPauseButton = findViewById(R.id.playPauseButton);
        stopButton = findViewById(R.id.stopButton);
        contentLabel = findViewById(R.id.contentLabel);

        seekBar = findViewById(R.id.seekBar);
        currentTimeText = findViewById(R.id.currentTimeText);
        totalTimeText = findViewById(R.id.totalTimeText);
        albumArtImage = findViewById(R.id.albumArtImage);
        

        // Setup RecyclerView
        adapter = new SongAdapter(this::playSong);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Initialize ExoPlayer
        player = new ExoPlayer.Builder(this).build();

        // Setup listeners
        searchButton.setOnClickListener(v -> performSearch());

        playPauseButton.setOnClickListener(v -> togglePlayPause());
        stopButton.setOnClickListener(v -> stopPlayback());

        // SeekBar listener for seeking
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && player != null) {
                    long duration = player.getDuration();
                    if (duration > 0) {
                        long position = (duration * progress) / 100;
                        player.seekTo(position);
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // Setup SeekBar updater
        updateSeekBar = new Runnable() {
            @Override
            public void run() {
                if (player != null && player.isPlaying()) {
                    long currentPosition = player.getCurrentPosition();
                    long duration = player.getDuration();

                    if (duration > 0) {
                        int progress = (int) ((currentPosition * 100) / duration);
                        seekBar.setProgress(progress);
                        currentTimeText.setText(formatTime(currentPosition));
                        totalTimeText.setText(formatTime(duration));
                    }
                }
                mainHandler.postDelayed(this, 500); // Update every 500ms
            }
        };

        // Initially disable player controls
        playPauseButton.setEnabled(false);
        stopButton.setEnabled(false);

        // Load home feed on startup
        loadHomeFeed();
    }

    @SuppressWarnings("ConstantConditions")
    private void loadHomeFeed() {
        progressBar.setVisibility(View.VISIBLE);

        executor.execute(() -> {
            try {
                List<com.maxrave.kotlinytmusicscraper.models.SongItem> homeFeed = bridge.getHomeFeed();

                mainHandler.post(() -> {
                    if (homeFeed != null && !homeFeed.isEmpty()) {
                        List<SongItem> songItems = new ArrayList<>();
                        for (com.maxrave.kotlinytmusicscraper.models.SongItem song : homeFeed) {
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
                        Toast.makeText(this, "Loaded " + songItems.size() + " recommendations", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "No recommendations available", Toast.LENGTH_SHORT).show();
                    }
                    progressBar.setVisibility(View.GONE);
                });
            } catch (Exception e) {
                e.printStackTrace();
                mainHandler.post(() -> {
                    Toast.makeText(this, "Failed to load home feed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    progressBar.setVisibility(View.GONE);
                });
            }
        });
    }

    private String formatTime(long currentPosition) {
        int totalSeconds = (int) (currentPosition / 1000);
        int mins = totalSeconds / 60;
        int secs = totalSeconds % 60;
        return String.format("%d:%02d", mins, secs);
    }

    @SuppressWarnings({"unchecked", "ConstantConditions", "NullableProblems", "DataFlowIssue"})
    private void performSearch() {
        String query = searchInput.getText() != null ? searchInput.getText().toString().trim() : "";
        if (query.isEmpty()) {
            Toast.makeText(this, "Please enter a search query", Toast.LENGTH_SHORT).show();
            return;
        }

        // Hide keyboard
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (imm != null && getCurrentFocus() != null) {
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
        searchInput.clearFocus();

        // Show loading
        progressBar.setVisibility(View.VISIBLE);
        searchButton.setEnabled(false);
        contentLabel.setText("Search Results");

        String finalQuery = query;
        executor.execute(() -> {
            try {
                List songs = bridge.searchSongs(finalQuery);

                mainHandler.post(() -> {
                    if (songs != null) {
                        List<SongItem> songItems = new ArrayList<>();
                        for (Object songObj : songs) {
                            var song = (com.maxrave.kotlinytmusicscraper.models.SongItem) songObj;

                            String artist = "Unknown";
                            try {
                                Object artists = song.getArtists();
                                if (artists != null) {
                                    StringBuilder artistNames = new StringBuilder();
                                    int count = 0;
                                    // Use iterator for Kotlin collection
                                    for (Object artistObj : (Iterable<?>) artists) {
                                        if (count > 0) artistNames.append(", ");
                                        // Cast to Artist and get name
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
                        Toast.makeText(this, "Found " + songs.size() + " songs", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "No results found", Toast.LENGTH_SHORT).show();
                    }
                    progressBar.setVisibility(View.GONE);
                    searchButton.setEnabled(true);
                });
            } catch (Exception e) {
                mainHandler.post(() -> {
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    progressBar.setVisibility(View.GONE);
                    searchButton.setEnabled(true);
                });
            }
        });
    }

    private void playSong(SongItem song) {
        // Show immediate feedback
        nowPlayingText.setText("⏳ Loading: " + song.getTitle());
        progressBar.setVisibility(View.VISIBLE);
        playPauseButton.setEnabled(false);
        stopButton.setEnabled(false);

        // Load thumbnail immediately (parallel)
        if (song.getThumbnailUrl() != null && !song.getThumbnailUrl().isEmpty()) {
            ImageRequest request = new ImageRequest.Builder(this)
                .data(song.getThumbnailUrl())
                .target(albumArtImage)
                .placeholder(R.drawable.ic_play)
                .error(R.drawable.ic_play)
                .build();
            Coil.imageLoader(this).enqueue(request);
        } else {
            albumArtImage.setImageResource(R.drawable.ic_play);
        }

        executor.execute(() -> {
            try {
                long startTime = System.currentTimeMillis();
                var streamData = bridge.getStreamData(song.getVideoId());
                long fetchTime = System.currentTimeMillis() - startTime;

                mainHandler.post(() -> {
                    if (streamData != null && streamData.getUrl() != null) {
                        player.stop();
                        player.clearMediaItems();

                        // Show fetching progress
                        nowPlayingText.setText("🎵 Buffering: " + song.getTitle());

                        MediaItem mediaItem = MediaItem.fromUri(streamData.getUrl());
                        player.setMediaItem(mediaItem);

                        // Add listener to know when player is ready
                        player.addListener(new Player.Listener() {
                            @Override
                            public void onPlaybackStateChanged(int playbackState) {
                                if (playbackState == Player.STATE_READY) {
                                    // Player is ready, hide progress
                                    progressBar.setVisibility(View.GONE);
                                    nowPlayingText.setText("♫ " + song.getTitle() + " - " + song.getArtist());
                                    playPauseButton.setEnabled(true);
                                    stopButton.setEnabled(true);

                                    // Start SeekBar updates
                                    seekBar.setProgress(0);
                                    currentTimeText.setText("0:00");
                                    mainHandler.post(updateSeekBar);

                                    // Remove this listener
                                    player.removeListener(this);
                                } else if (playbackState == Player.STATE_BUFFERING) {
                                    nowPlayingText.setText("🎵 Buffering: " + song.getTitle());
                                }
                            }
                        });

                        player.prepare();
                        player.setPlayWhenReady(true);
                        playPauseButton.setIconResource(R.drawable.ic_pause);

                        // Show quality info
                        String quality = streamData.getMimeType() != null ? streamData.getMimeType() : "audio";
                        Integer bitrate = streamData.getBitrate();
                        String bitrateStr = bitrate != null ? (bitrate / 1000) + " kbps" : "unknown";
                        Toast.makeText(this, "Loaded in " + fetchTime + "ms • " + quality + " (" + bitrateStr + ")",
                            Toast.LENGTH_SHORT).show();
                    } else {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(this, "Failed to get stream URL", Toast.LENGTH_LONG).show();
                    }
                });
            } catch (Exception e) {
                mainHandler.post(() -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void togglePlayPause() {
        if (player.isPlaying()) {
            player.pause();
            playPauseButton.setIconResource(R.drawable.ic_play);
        } else {
            player.play();
            playPauseButton.setIconResource(R.drawable.ic_pause);
        }
    }

    private void stopPlayback() {
        player.stop();
        mainHandler.removeCallbacks(updateSeekBar);
        seekBar.setProgress(0);
        currentTimeText.setText("0:00");
        totalTimeText.setText("0:00");
        nowPlayingText.setText("No song playing");
        albumArtImage.setImageResource(R.drawable.ic_play);
        playPauseButton.setEnabled(false);
        stopButton.setEnabled(false);
        playPauseButton.setIconResource(R.drawable.ic_play);
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
        if (mainHandler != null && updateSeekBar != null) {
            mainHandler.removeCallbacks(updateSeekBar);
        }
        if (player != null) {
            player.release();
        }
        if (executor != null) {
            executor.shutdownNow();
        }
    }

    // Inner class for song data
    public static class SongItem {
        private final String videoId;
        private final String title;
        private final String artist;
        private final String duration;
        private final String thumbnailUrl;

        public SongItem(String videoId, String title, String artist, String duration, String thumbnailUrl) {
            this.videoId = videoId;
            this.title = title;
            this.artist = artist;
            this.duration = duration;
            this.thumbnailUrl = thumbnailUrl;
        }

        public String getVideoId() { return videoId; }
        public String getTitle() { return title; }
        public String getArtist() { return artist; }
        public String getDuration() { return duration; }
        public String getThumbnailUrl() { return thumbnailUrl; }
    }

    // RecyclerView Adapter
    public static class SongAdapter extends RecyclerView.Adapter<SongAdapter.SongViewHolder> {

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
}


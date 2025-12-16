package com.myjavaapp.android;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.media3.common.MediaItem;
import androidx.media3.common.Player;
import androidx.media3.exoplayer.ExoPlayer;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.textview.MaterialTextView;
import com.myjavaapp.bridge.YouTubeBridge;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import coil.Coil;
import coil.request.ImageRequest;

public class PlayerActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private ShapeableImageView albumArtImage;
    private MaterialTextView songTitle;
    private MaterialTextView artistName;
    private SeekBar seekBar;
    private MaterialTextView currentTimeText;
    private MaterialTextView totalTimeText;
    private MaterialButton playPauseButton;
    private MaterialButton stopButton;
    private LinearProgressIndicator loadingProgress;
    private MaterialTextView statusText;

    private YouTubeBridge bridge;
    private ExoPlayer player;
    private ExecutorService executor;
    private Handler mainHandler;
    private Runnable updateSeekBar;

    private String videoId;
    private String title;
    private String artist;
    private String thumbnailUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        // Get intent extras
        videoId = getIntent().getStringExtra("VIDEO_ID");
        title = getIntent().getStringExtra("TITLE");
        artist = getIntent().getStringExtra("ARTIST");
        thumbnailUrl = getIntent().getStringExtra("THUMBNAIL");

        // Initialize
        executor = Executors.newCachedThreadPool();
        mainHandler = new Handler(Looper.getMainLooper());
        bridge = new YouTubeBridge();
        player = new ExoPlayer.Builder(this).build();

        // Find views
        toolbar = findViewById(R.id.toolbar);
        albumArtImage = findViewById(R.id.albumArtImage);
        songTitle = findViewById(R.id.songTitle);
        artistName = findViewById(R.id.artistName);
        seekBar = findViewById(R.id.seekBar);
        currentTimeText = findViewById(R.id.currentTimeText);
        totalTimeText = findViewById(R.id.totalTimeText);
        playPauseButton = findViewById(R.id.playPauseButton);
        stopButton = findViewById(R.id.stopButton);
        loadingProgress = findViewById(R.id.loadingProgress);
        statusText = findViewById(R.id.statusText);

        // Setup toolbar
        toolbar.setNavigationOnClickListener(v -> finish());

        // Set song info
        songTitle.setText(title != null ? title : "Unknown");
        artistName.setText(artist != null ? artist : "Unknown");

        // Load thumbnail
        if (thumbnailUrl != null && !thumbnailUrl.isEmpty()) {
            ImageRequest request = new ImageRequest.Builder(this)
                .data(thumbnailUrl)
                .target(albumArtImage)
                .placeholder(R.drawable.ic_play)
                .error(R.drawable.ic_play)
                .build();
            Coil.imageLoader(this).enqueue(request);
        } else {
            albumArtImage.setImageResource(R.drawable.ic_play);
        }

        // Setup seekbar
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
                mainHandler.postDelayed(this, 500);
            }
        };

        // Setup buttons
        playPauseButton.setOnClickListener(v -> togglePlayPause());
        stopButton.setOnClickListener(v -> stopPlayback());

        // Initially disable controls
        playPauseButton.setEnabled(false);
        stopButton.setEnabled(false);

        // Load and play song
        if (videoId != null) {
            loadAndPlaySong();
        } else {
            Toast.makeText(this, "Invalid video ID", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void loadAndPlaySong() {
        loadingProgress.setVisibility(View.VISIBLE);
        statusText.setText("Loading...");
        playPauseButton.setEnabled(false);
        stopButton.setEnabled(false);

        executor.execute(() -> {
            try {
                long startTime = System.currentTimeMillis();
                var streamData = bridge.getStreamData(videoId);
                long fetchTime = System.currentTimeMillis() - startTime;

                mainHandler.post(() -> {
                    if (streamData != null && streamData.getUrl() != null) {
                        player.stop();
                        player.clearMediaItems();

                        statusText.setText("Buffering...");

                        MediaItem mediaItem = MediaItem.fromUri(streamData.getUrl());
                        player.setMediaItem(mediaItem);

                        // Add listener for player ready state
                        player.addListener(new Player.Listener() {
                            @Override
                            public void onPlaybackStateChanged(int playbackState) {
                                if (playbackState == Player.STATE_READY) {
                                    loadingProgress.setVisibility(View.GONE);
                                    statusText.setText("");
                                    playPauseButton.setEnabled(true);
                                    stopButton.setEnabled(true);

                                    // Start SeekBar updates
                                    seekBar.setProgress(0);
                                    currentTimeText.setText("0:00");
                                    mainHandler.post(updateSeekBar);

                                    player.removeListener(this);
                                } else if (playbackState == Player.STATE_BUFFERING) {
                                    statusText.setText("Buffering...");
                                } else if (playbackState == Player.STATE_ENDED) {
                                    statusText.setText("Playback ended");
                                    playPauseButton.setIconResource(R.drawable.ic_play);
                                    mainHandler.removeCallbacks(updateSeekBar);
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
                        loadingProgress.setVisibility(View.GONE);
                        statusText.setText("Failed to load stream");
                        Toast.makeText(this, "Failed to get stream URL", Toast.LENGTH_LONG).show();
                    }
                });
            } catch (Exception e) {
                mainHandler.post(() -> {
                    loadingProgress.setVisibility(View.GONE);
                    statusText.setText("Error: " + e.getMessage());
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
        statusText.setText("Stopped");
        playPauseButton.setEnabled(false);
        stopButton.setEnabled(false);
        playPauseButton.setIconResource(R.drawable.ic_play);
    }

    private String formatTime(long milliseconds) {
        int totalSeconds = (int) (milliseconds / 1000);
        int mins = totalSeconds / 60;
        int secs = totalSeconds % 60;
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
}


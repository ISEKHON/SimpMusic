package com.myjavaapp.gui;
}
    }
        public String getDuration() { return duration; }
        public String getArtist() { return artist; }
        public String getTitle() { return title; }
        public String getVideoId() { return videoId; }

        }
            this.duration = duration;
            this.artist = artist;
            this.title = title;
            this.videoId = videoId;
        public SongRow(String videoId, String title, String artist, String duration) {

        private final String duration;
        private final String artist;
        private final String title;
        private final String videoId;
    public static class SongRow {
    // Inner class for table rows

    }
        launch(args);
    public static void main(String[] args) {

    }
        }
            executor.shutdownNow();
        if (executor != null) {
        }
            currentPlayer.dispose();
            currentPlayer.stop();
        if (currentPlayer != null) {
    private void cleanup() {

    }
        alert.showAndWait();
        alert.setContentText(message);
        alert.setHeaderText(null);
        alert.setTitle(title);
        Alert alert = new Alert(Alert.AlertType.ERROR);
    private void showError(String title, String message) {

    }
        return String.format("%d:%02d", minutes, seconds);
        seconds = seconds % 60;
        int minutes = seconds / 60;
        int seconds = (int) duration.toSeconds();
        if (duration == null || duration.isUnknown()) return "0:00";
    private String formatTime(Duration duration) {

    }
        statusLabel.setText("Stopped");
        timeLabel.setText("0:00 / 0:00");
        nowPlayingLabel.setText("No song playing");
        playPauseButton.setDisable(true);
        playPauseButton.setText("▶");
        }
            currentPlayer = null;
            currentPlayer.dispose();
            currentPlayer.stop();
        if (currentPlayer != null) {
    private void stopPlayback() {

    }
        }
            statusLabel.setText("Playing");
            playPauseButton.setText("⏸");
            currentPlayer.play();
        } else {
            statusLabel.setText("Paused");
            playPauseButton.setText("▶");
            currentPlayer.pause();
        if (status == MediaPlayer.Status.PLAYING) {
        MediaPlayer.Status status = currentPlayer.getStatus();

        if (currentPlayer == null) return;
    private void togglePlayPause() {

    }
        });
            }
                });
                    playPauseButton.setDisable(false);
                    showError("Stream Error", e.getMessage());
                    statusLabel.setText("Error getting stream: " + e.getMessage());
                javafx.application.Platform.runLater(() -> {
            } catch (Exception e) {

                });
                    }
                        playPauseButton.setDisable(false);
                        showError("Playback Error", e.getMessage());
                        statusLabel.setText("Failed to play: " + e.getMessage());
                    } catch (Exception e) {

                        });
                            statusLabel.setText("Playback finished");
                            playPauseButton.setText("▶");
                        currentPlayer.setOnEndOfMedia(() -> {

                        });
                            showError("Playback Error", currentPlayer.getError().getMessage());
                            statusLabel.setText("Playback error: " + currentPlayer.getError().getMessage());
                        currentPlayer.setOnError(() -> {

                        });
                            });
                                }
                                    timeLabel.setText(formatTime(newTime) + " / " + formatTime(duration));
                                if (duration != null && !duration.isUnknown()) {
                                Duration duration = currentPlayer.getTotalDuration();
                            currentPlayer.currentTimeProperty().addListener((obs, oldTime, newTime) -> {
                            // Update time label

                            statusLabel.setText("Playing: " + song.getTitle() + " | Quality: " + streamInfo.getQualityDescription());
                            playPauseButton.setDisable(false);
                            playPauseButton.setText("⏸");
                            nowPlayingLabel.setText("♫ " + song.getTitle() + " - " + song.getArtist());
                            currentPlayer.play();
                        currentPlayer.setOnReady(() -> {

                        currentPlayer.setVolume(volumeSlider.getValue() / 100.0);

                        currentPlayer = new MediaPlayer(media);
                        Media media = new Media(streamInfo.url);
                        currentlyPlayingUrl = streamInfo.url;

                        }
                            currentPlayer.dispose();
                            currentPlayer.stop();
                        if (currentPlayer != null) {
                        // Stop current playback
                    try {
                javafx.application.Platform.runLater(() -> {

                }
                    return;
                    });
                        showError("Playback Error", "Could not get stream URL for this song");
                        statusLabel.setText("Failed to get stream URL");
                    javafx.application.Platform.runLater(() -> {
                if (streamInfo.url == null || streamInfo.url.isEmpty()) {

                YouTubeMusicService.StreamInfo streamInfo = musicService.getStreamUrl(song.getVideoId());
            try {
        executor.submit(() -> {

        playPauseButton.setDisable(true);
        statusLabel.setText("Loading: " + song.getTitle());
    private void playSong(SongRow song) {

    }
        });
            }
                });
                    showError("Search Error", e.getMessage());
                    progressBar.setVisible(false);
                    searchButton.setDisable(false);
                    statusLabel.setText("Error: " + e.getMessage());
                javafx.application.Platform.runLater(() -> {
            } catch (Exception e) {
                });
                    progressBar.setVisible(false);
                    searchButton.setDisable(false);
                    statusLabel.setText("Found " + tracks.size() + " songs");

                    }
                        ));
                            track.getFormattedDuration()
                            track.artist != null ? track.artist : "Unknown Artist",
                            track.title,
                            track.videoId,
                        resultsTable.getItems().add(new SongRow(
                    for (YouTubeMusicService.Track track : tracks) {
                javafx.application.Platform.runLater(() -> {

                List<YouTubeMusicService.Track> tracks = musicService.searchSongs(query);
            try {
        executor.submit(() -> {

        resultsTable.getItems().clear();
        statusLabel.setText("Searching for: " + query);
        progressBar.setProgress(-1);
        progressBar.setVisible(true);
        searchButton.setDisable(true);

        }
            return;
            statusLabel.setText("Please enter a search query");
        if (query.isEmpty()) {
        String query = searchField.getText().trim();
    private void performSearch() {

    }
        return statusBar;
        statusBar.getChildren().add(statusLabel);
        statusBar.setPadding(new Insets(5));
        statusBar.setAlignment(Pos.CENTER_LEFT);
        HBox statusBar = new HBox();
    private HBox createStatusBar() {

    }
        return playerBox;
        playerBox.getChildren().addAll(nowPlayingLabel, controls);

        );
            timeLabel
            volumeSlider,
            volumeLabel,
            stopButton,
            playPauseButton,
        controls.getChildren().addAll(

        timeLabel.setStyle("-fx-font-family: monospace;");
        timeLabel = new Label("0:00 / 0:00");

        });
            }
                currentPlayer.setVolume(newVal.doubleValue() / 100.0);
            if (currentPlayer != null) {
        volumeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
        volumeSlider.setShowTickMarks(false);
        volumeSlider.setShowTickLabels(false);
        volumeSlider.setPrefWidth(150);
        volumeSlider = new Slider(0, 100, 50);

        volumeLabel.setStyle("-fx-font-size: 16px;");
        Label volumeLabel = new Label("🔊");

        stopButton.setOnAction(e -> stopPlayback());
        stopButton.setPrefSize(50, 50);
        stopButton.setStyle("-fx-font-size: 18px;");
        Button stopButton = new Button("⏹");

        playPauseButton.setOnAction(e -> togglePlayPause());
        playPauseButton.setDisable(true);
        playPauseButton.setPrefSize(50, 50);
        playPauseButton.setStyle("-fx-font-size: 18px; -fx-background-color: #4CAF50; -fx-text-fill: white;");
        playPauseButton = new Button("▶");

        controls.setAlignment(Pos.CENTER_LEFT);
        HBox controls = new HBox(15);

        nowPlayingLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        nowPlayingLabel = new Label("No song playing");

        playerBox.setStyle("-fx-background-color: white; -fx-background-radius: 5;");
        playerBox.setPadding(new Insets(15));
        VBox playerBox = new VBox(10);
    private VBox createPlayerControls() {

    }
        return table;
        table.getColumns().addAll(titleCol, artistCol, durationCol, actionCol);

        });
            }
                setGraphic(empty ? null : playBtn);
                super.updateItem(item, empty);
            protected void updateItem(Void item, boolean empty) {
            @Override

            }
                });
                    playSong(song);
                    SongRow song = getTableView().getItems().get(getIndex());
                playBtn.setOnAction(event -> {
                playBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
            {
            private final Button playBtn = new Button("▶ Play");
        actionCol.setCellFactory(param -> new TableCell<>() {
        actionCol.setPrefWidth(100);
        TableColumn<SongRow, Void> actionCol = new TableColumn<>("Action");
        // Action column

        durationCol.setPrefWidth(80);
        durationCol.setCellValueFactory(new PropertyValueFactory<>("duration"));
        TableColumn<SongRow, String> durationCol = new TableColumn<>("Duration");
        // Duration column

        artistCol.setPrefWidth(200);
        artistCol.setCellValueFactory(new PropertyValueFactory<>("artist"));
        TableColumn<SongRow, String> artistCol = new TableColumn<>("Artist");
        // Artist column

        titleCol.setPrefWidth(300);
        titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
        TableColumn<SongRow, String> titleCol = new TableColumn<>("Title");
        // Title column

        table.setPlaceholder(new Label("Search for songs to see results"));
        table.setStyle("-fx-background-color: white;");
        TableView<SongRow> table = new TableView<>();
    private TableView<SongRow> createResultsTable() {

    }
        return searchBar;

        HBox.setHgrow(searchField, Priority.ALWAYS);
        searchBar.getChildren().addAll(searchField, searchButton, progressBar);

        progressBar.setVisible(false);
        progressBar.setPrefWidth(100);
        progressBar = new ProgressBar(0);

        searchButton.setOnAction(e -> performSearch());
        searchButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");
        searchButton = new Button("🔍 Search");

        searchField.setOnAction(e -> performSearch());
        searchField.setPrefWidth(400);
        searchField.setPromptText("Search for songs...");
        searchField = new TextField();

        searchBar.setStyle("-fx-background-color: white; -fx-background-radius: 5;");
        searchBar.setPadding(new Insets(10));
        searchBar.setAlignment(Pos.CENTER_LEFT);
        HBox searchBar = new HBox(10);
    private HBox createSearchBar() {

    }
        primaryStage.show();
        primaryStage.setOnCloseRequest(e -> cleanup());
        primaryStage.setScene(scene);
        primaryStage.setTitle("YouTube Music Player");
        Scene scene = new Scene(root, 900, 700);

        VBox.setVgrow(resultsTable, Priority.ALWAYS);

        );
            statusBar
            playerControls,
            new Separator(),
            resultsTable,
            new Label("Search Results:"),
            searchBar,
            titleLabel,
        root.getChildren().addAll(

        HBox statusBar = createStatusBar();
        // Status bar

        VBox playerControls = createPlayerControls();
        // Player controls

        resultsTable = createResultsTable();
        // Results table

        HBox searchBar = createSearchBar();
        // Search bar

        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
        Label titleLabel = new Label("🎵 YouTube Music Player");
        // Header

        root.setStyle("-fx-background-color: #f5f5f5;");
        root.setPadding(new Insets(15));
        VBox root = new VBox(10);
        // Create UI

        });
            }
                    statusLabel.setText("Error: " + e.getMessage()));
                javafx.application.Platform.runLater(() ->
            } catch (Exception e) {
                    statusLabel.setText("Ready to search"));
                javafx.application.Platform.runLater(() ->
                musicService = new YouTubeMusicService();
            try {
        executor.submit(() -> {

        statusLabel.setStyle("-fx-text-fill: #666;");
        statusLabel = new Label("Initializing...");
        // Initialize service

        executor = Executors.newCachedThreadPool();
    public void start(Stage primaryStage) {
    @Override

    private String currentlyPlayingUrl;
    private MediaPlayer currentPlayer;

    private ExecutorService executor;
    private Label timeLabel;
    private Slider volumeSlider;
    private Button playPauseButton;
    private Label nowPlayingLabel;
    private ProgressBar progressBar;
    private Label statusLabel;
    private Button searchButton;
    private TextField searchField;
    private TableView<SongRow> resultsTable;
    private YouTubeMusicService musicService;

public class MusicPlayerApp extends Application {
 */
 * JavaFX GUI Application for YouTube Music Player
/**

import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.List;

import com.myjavaapp.YouTubeMusicService;
import javafx.util.Duration;
import javafx.stage.Stage;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.Media;
import javafx.scene.layout.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.*;
import javafx.scene.Scene;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import javafx.application.Application;



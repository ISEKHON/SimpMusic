package com.myjavaapp;

import com.myjavaapp.bridge.YouTubeBridge;

import java.util.ArrayList;
import java.util.List;

/**
 * Java wrapper service for the Kotlin YouTube Music Scraper
 * Uses the Kotlin bridge for Java-friendly API
 */
public class YouTubeMusicService {

    private final YouTubeBridge bridge;

    public YouTubeMusicService() {
        System.out.println("🔧 Initializing YouTube Music Service...");
        this.bridge = new YouTubeBridge();
        System.out.println("✅ Service initialized!");
    }

    /**
     * Search for songs
     */
    public List<Track> searchSongs(String query) {
        // The bridge returns List<SongItem> but Java sees it as List<Object> due to type erasure
        var songItems = bridge.searchSongs(query);

        if (songItems == null) {
            throw new RuntimeException("Failed to search songs");
        }

        List<Track> tracks = new ArrayList<>();

        for (var songItem : songItems) {
            Track track = new Track();
            track.videoId = songItem.getId();
            track.title = songItem.getTitle();

            // Get artist names
            var artists = songItem.getArtists();
            if (artists != null && !artists.isEmpty()) {
                StringBuilder artistNames = new StringBuilder();
                for (int i = 0; i < artists.size(); i++) {
                    if (i > 0) artistNames.append(", ");
                    artistNames.append(artists.get(i).getName());
                }
                track.artist = artistNames.toString();
            }

            track.thumbnailUrl = songItem.getThumbnail();
            track.durationSeconds = songItem.getDuration();

            tracks.add(track);
        }

        return tracks;
    }

    /**
     * Search for albums
     */
    public List<Album> searchAlbums(String query) {
        var albumItems = bridge.searchAlbums(query);

        if (albumItems == null) {
            throw new RuntimeException("Failed to search albums");
        }

        List<Album> albums = new ArrayList<>();

        for (var albumItem : albumItems) {
            Album album = new Album();
            album.browseId = albumItem.getBrowseId();
            album.title = albumItem.getTitle();

            var artists = albumItem.getArtists();
            if (artists != null && !artists.isEmpty()) {
                StringBuilder artistNames = new StringBuilder();
                for (int i = 0; i < artists.size(); i++) {
                    if (i > 0) artistNames.append(", ");
                    artistNames.append(artists.get(i).getName());
                }
                album.artist = artistNames.toString();
            }

            Integer yearInt = albumItem.getYear();
            album.year = yearInt != null ? String.valueOf(yearInt) : null;
            album.thumbnailUrl = albumItem.getThumbnail();

            albums.add(album);
        }

        return albums;
    }

    /**
     * Get stream URL for a video
     */
    public StreamInfo getStreamUrl(String videoId) {
        var streamData = bridge.getStreamData(videoId);

        if (streamData == null) {
            throw new RuntimeException("Failed to get stream data");
        }

        StreamInfo streamInfo = new StreamInfo();
        streamInfo.url = streamData.getUrl();
        streamInfo.title = streamData.getTitle();
        streamInfo.author = streamData.getAuthor();
        streamInfo.durationSeconds = streamData.getDurationSeconds();
        streamInfo.bitrate = streamData.getBitrate();
        streamInfo.mimeType = streamData.getMimeType();
        streamInfo.itag = streamData.getItag();

        return streamInfo;
    }

    /**
     * Get search suggestions
     */
    public List<String> getSearchSuggestions(String query) {
        List<String> suggestions = bridge.getSearchSuggestions(query);

        if (suggestions == null) {
            return new ArrayList<>();
        }

        return new ArrayList<>(suggestions);
    }

    // ========== Data Classes ==========

    public static class Track {
        public String videoId;
        public String title;
        public String artist;
        public String thumbnailUrl;
        public Integer durationSeconds;

        public String getFormattedDuration() {
            if (durationSeconds == null) return "--:--";
            int minutes = durationSeconds / 60;
            int seconds = durationSeconds % 60;
            return String.format("%d:%02d", minutes, seconds);
        }
    }

    public static class Album {
        public String browseId;
        public String title;
        public String artist;
        public String year;
        public String thumbnailUrl;
    }

    public static class StreamInfo {
        public String url;
        public String title;
        public String author;
        public Integer durationSeconds;
        public Integer bitrate;
        public String mimeType;
        public Integer itag;

        public String getQualityDescription() {
            if (bitrate != null) {
                String format = mimeType != null ? mimeType.split("/")[1] : "audio";
                return String.format("%s (%d kbps)", format, bitrate / 1000);
            }
            return "Unknown quality";
        }
    }
}


package com.myjavaapp.android;

public class SongItem {
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


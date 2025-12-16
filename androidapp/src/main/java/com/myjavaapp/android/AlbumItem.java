package com.myjavaapp.android;

/**
 * Data class for album information
 */
public class AlbumItem {
    private final String browseId;
    private final String title;
    private final String artist;
    private final String year;
    private final String thumbnailUrl;
    private final String type; // "Album", "EP", "Single"

    public AlbumItem(String browseId, String title, String artist, String year,
                     String thumbnailUrl, String type) {
        this.browseId = browseId;
        this.title = title;
        this.artist = artist;
        this.year = year;
        this.thumbnailUrl = thumbnailUrl;
        this.type = type;
    }

    public String getBrowseId() { return browseId; }
    public String getTitle() { return title; }
    public String getArtist() { return artist; }
    public String getYear() { return year; }
    public String getThumbnailUrl() { return thumbnailUrl; }
    public String getType() { return type; }
}


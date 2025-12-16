package com.myjavaapp.android;

import java.util.List;

/**
 * Represents a section in the home feed (e.g., "Trending songs for you")
 */
public class FeedSection {
    private final String title;
    private final List<SongItem> songs;
    private final List<AlbumItem> albums;
    private final SectionType type;

    public enum SectionType {
        TRENDING_SONGS,
        NEW_RELEASES,
        RECOMMENDED,
        TOP_TRACKS,
        NEW_ALBUMS
    }

    // Constructor for songs
    public FeedSection(String title, List<SongItem> songs, SectionType type) {
        this.title = title;
        this.songs = songs;
        this.albums = null;
        this.type = type;
    }

    // Constructor for albums
    public FeedSection(String title, SectionType type, List<AlbumItem> albums) {
        this.title = title;
        this.songs = null;
        this.albums = albums;
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public List<SongItem> getSongs() {
        return songs;
    }

    public List<AlbumItem> getAlbums() {
        return albums;
    }

    public SectionType getType() {
        return type;
    }

    public boolean isSongSection() {
        return songs != null;
    }

    public boolean isAlbumSection() {
        return albums != null;
    }
}


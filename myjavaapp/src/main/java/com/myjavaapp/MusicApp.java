package com.myjavaapp;

import java.util.List;
import java.util.Scanner;

/**
 * Main application class with interactive menu
 */
public class MusicApp {

    private final YouTubeMusicService musicService;
    private final Scanner scanner;

    public MusicApp() {
        this.musicService = new YouTubeMusicService();
        this.scanner = new Scanner(System.in);
    }

    public void run() {
        boolean running = true;

        while (running) {
            showMenu();
            String choice = scanner.nextLine().trim();
            System.out.println();

            try {
                switch (choice) {
                    case "1":
                        searchSongs();
                        break;
                    case "2":
                        searchAlbums();
                        break;
                    case "3":
                        getStreamUrl();
                        break;
                    case "4":
                        quickDemo();
                        break;
                    case "5":
                        getSearchSuggestions();
                        break;
                    case "0":
                        System.out.println("👋 Goodbye!");
                        running = false;
                        break;
                    default:
                        System.out.println("❌ Invalid choice. Try again.");
                }
            } catch (Exception e) {
                System.err.println("❌ Error: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void showMenu() {
        System.out.println("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("              MAIN MENU");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("1. 🔍 Search Songs");
        System.out.println("2. 💿 Search Albums");
        System.out.println("3. 🎵 Get Stream URL for Video ID");
        System.out.println("4. 🚀 Quick Demo (Search + Stream)");
        System.out.println("5. 💡 Get Search Suggestions");
        System.out.println("0. ❌ Exit");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.print("Your choice: ");
    }

    private void searchSongs() {
        System.out.print("Enter search query: ");
        String query = scanner.nextLine().trim();

        if (query.isEmpty()) {
            System.out.println("Query cannot be empty!");
            return;
        }

        System.out.println("\n🔍 Searching for songs: \"" + query + "\"");
        System.out.println("⏳ Please wait...\n");

        long startTime = System.currentTimeMillis();
        List<YouTubeMusicService.Track> tracks = musicService.searchSongs(query);
        long duration = System.currentTimeMillis() - startTime;

        if (tracks.isEmpty()) {
            System.out.println("❌ No results found.");
            return;
        }

        System.out.println("✅ Found " + tracks.size() + " songs in " + duration + "ms\n");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

        int displayCount = Math.min(10, tracks.size());
        for (int i = 0; i < displayCount; i++) {
            YouTubeMusicService.Track track = tracks.get(i);
            System.out.printf("%2d. %s\n", (i + 1), track.title);
            System.out.printf("    👤 %s\n", track.artist != null ? track.artist : "Unknown Artist");
            System.out.printf("    🆔 %s\n", track.videoId);
            System.out.printf("    ⏱️  %s\n", track.getFormattedDuration());
            if (track.thumbnailUrl != null) {
                String thumb = track.thumbnailUrl;
                System.out.printf("    🖼️  %s\n", thumb.substring(0, Math.min(60, thumb.length())) + "...");
            }
            System.out.println();
        }

        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
    }

    private void searchAlbums() {
        System.out.print("Enter search query: ");
        String query = scanner.nextLine().trim();

        if (query.isEmpty()) {
            System.out.println("Query cannot be empty!");
            return;
        }

        System.out.println("\n🔍 Searching for albums: \"" + query + "\"");
        System.out.println("⏳ Please wait...\n");

        long startTime = System.currentTimeMillis();
        List<YouTubeMusicService.Album> albums = musicService.searchAlbums(query);
        long duration = System.currentTimeMillis() - startTime;

        if (albums.isEmpty()) {
            System.out.println("❌ No results found.");
            return;
        }

        System.out.println("✅ Found " + albums.size() + " albums in " + duration + "ms\n");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

        for (int i = 0; i < Math.min(10, albums.size()); i++) {
            YouTubeMusicService.Album album = albums.get(i);
            System.out.printf("%2d. %s\n", (i + 1), album.title);
            System.out.printf("    👤 %s\n", album.artist != null ? album.artist : "Unknown Artist");
            System.out.printf("    🆔 %s\n", album.browseId);
            if (album.year != null) {
                System.out.printf("    📅 %s\n", album.year);
            }
            System.out.println();
        }

        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
    }

    private void getStreamUrl() {
        System.out.print("Enter video ID (e.g., dQw4w9WgXcQ): ");
        String videoId = scanner.nextLine().trim();

        if (videoId.isEmpty()) {
            System.out.println("Video ID cannot be empty!");
            return;
        }

        System.out.println("\n🎵 Getting stream data for: " + videoId);
        System.out.println("⏳ Please wait...\n");

        long startTime = System.currentTimeMillis();
        YouTubeMusicService.StreamInfo stream = musicService.getStreamUrl(videoId);
        long duration = System.currentTimeMillis() - startTime;

        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("✅ Stream retrieved in " + duration + "ms");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("📌 Title:    " + stream.title);
        System.out.println("👤 Artist:   " + stream.author);
        System.out.println("⏱️  Duration: " + stream.durationSeconds + " seconds");
        System.out.println("🎚️  Quality:  " + stream.getQualityDescription());
        System.out.println("🔢 Format:   itag " + stream.itag);
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

        if (stream.url != null && !stream.url.isEmpty()) {
            System.out.println("\n🔗 Stream URL:");
            String url = stream.url;
            if (url.length() > 150) {
                System.out.println(url.substring(0, 150) + "...");
                System.out.println("   (Truncated - Full length: " + url.length() + " chars)");
            } else {
                System.out.println(url);
            }

            System.out.println("\n✅ Ready to play! You can use:");
            System.out.println("   • VLC:    vlc \"" + url + "\"");
            System.out.println("   • FFplay: ffplay \"" + url + "\"");
            System.out.println("   • Any media player that supports HTTP streaming");
        } else {
            System.out.println("\n❌ No valid stream URL found!");
        }
    }

    private void quickDemo() {
        String query = "never gonna give you up";

        System.out.println("🚀 QUICK DEMO: Search and Get Stream");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("Query: \"" + query + "\"");
        System.out.println();

        // Step 1: Search
        System.out.println("Step 1/2: Searching...");
        List<YouTubeMusicService.Track> tracks = musicService.searchSongs(query);

        if (tracks.isEmpty()) {
            System.out.println("❌ No results found.");
            return;
        }

        YouTubeMusicService.Track firstTrack = tracks.get(0);
        System.out.println("✅ Found: " + firstTrack.title + " - " + firstTrack.artist);
        System.out.println();

        // Step 2: Get stream
        System.out.println("Step 2/2: Getting stream URL...");
        YouTubeMusicService.StreamInfo stream = musicService.getStreamUrl(firstTrack.videoId);

        System.out.println("✅ Stream ready!");
        System.out.println();
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("🎵 " + stream.title);
        System.out.println("👤 " + stream.author);
        System.out.println("🎚️  " + stream.getQualityDescription());
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

        if (stream.url != null && !stream.url.isEmpty()) {
            System.out.println("\n✅ SUCCESS! Stream URL is ready to play.");
            System.out.println("URL length: " + stream.url.length() + " characters");
        } else {
            System.out.println("\n❌ Failed to get stream URL.");
        }
    }

    private void getSearchSuggestions() {
        System.out.print("Enter partial query: ");
        String query = scanner.nextLine().trim();

        if (query.isEmpty()) {
            System.out.println("Query cannot be empty!");
            return;
        }

        System.out.println("\n💡 Getting suggestions for: \"" + query + "\"");
        System.out.println("⏳ Please wait...\n");

        List<String> suggestions = musicService.getSearchSuggestions(query);

        if (suggestions.isEmpty()) {
            System.out.println("❌ No suggestions found.");
            return;
        }

        System.out.println("✅ Found " + suggestions.size() + " suggestions:\n");
        for (int i = 0; i < suggestions.size(); i++) {
            System.out.printf("%2d. %s\n", (i + 1), suggestions.get(i));
        }
    }
}


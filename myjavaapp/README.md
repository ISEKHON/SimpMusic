# My Java YouTube Music App

A Java application that uses SimpMusic's core Kotlin modules directly (no JAR files needed!).

## 🎯 What This Does

This Java app demonstrates how to:
- ✅ Search for songs and albums on YouTube Music
- ✅ Get streaming URLs for any video
- ✅ Use Kotlin libraries directly from Java
- ✅ Access all SimpMusic core functionality

## 📦 Architecture

```
myjavaapp/
├── build.gradle.kts          # Build configuration
├── src/main/java/com/myjavaapp/
│   ├── Main.java              # Entry point
│   ├── MusicApp.java          # Interactive menu app
│   └── YouTubeMusicService.java  # Wrapper for Kotlin modules
```

## 🚀 How to Build and Run

### Option 1: From Command Line

```bash
# From SimpMusic root directory
gradlew :myjavaapp:run

# Or build a JAR
gradlew :myjavaapp:build
```

### Option 2: From Android Studio / IntelliJ IDEA

1. Open the SimpMusic project in IntelliJ/Android Studio
2. Wait for Gradle sync to complete
3. Navigate to `myjavaapp/src/main/java/com/myjavaapp/Main.java`
4. Right-click and select "Run 'Main.main()'"

### Option 3: Build Standalone JAR

```bash
# Build
gradlew :myjavaapp:shadowJar

# Run
java -jar myjavaapp/build/libs/myjavaapp-all.jar
```

## 🎮 Features

### 1. Search Songs
```
Enter search query: never gonna give you up
```
- Searches YouTube Music
- Returns song title, artist, duration, thumbnail
- Shows video IDs for streaming

### 2. Search Albums
```
Enter search query: abbey road
```
- Finds albums with artists and release years

### 3. Get Stream URL
```
Enter video ID: dQw4w9WgXcQ
```
- Extracts direct audio stream URL
- Shows quality, bitrate, format
- URL can be played in VLC, FFmpeg, etc.

### 4. Quick Demo
- Automatically searches and gets stream for a popular song
- Shows the complete workflow

### 5. Search Suggestions
- Get auto-complete suggestions as you type

### 6. Browse Home
- Browse YouTube Music home page content

## 📚 Core Modules Used

This Java app directly uses these Kotlin modules:

- **`:core:domain`** - Domain models and interfaces
- **`:core:common`** - Common utilities
- **`:core:service:kotlinYtmusicScraper`** - YouTube Music API scraper

## 🔧 How It Works

### Java ↔ Kotlin Interop

The `YouTubeMusicService.java` class wraps Kotlin coroutines for easy Java usage:

```java
// Kotlin coroutine call
youtube.search(query, SearchFilter.FILTER_SONG, continuation)

// Wrapped for Java (blocking call)
BuildersKt.runBlocking(
    EmptyCoroutineContext.INSTANCE,
    (scope, continuation) -> youtube.search(query, SearchFilter.FILTER_SONG, continuation)
)
```

### Direct Module Dependency

No JAR files! Direct Gradle dependencies:

```kotlin
dependencies {
    implementation(project(":core:domain"))
    implementation(project(":core:common"))
    implementation(project(":core:service:kotlinYtmusicScraper"))
}
```

## 📖 Code Examples

### Search Songs
```java
YouTubeMusicService service = new YouTubeMusicService();
List<YouTubeMusicService.Track> tracks = service.searchSongs("hello");

for (Track track : tracks) {
    System.out.println(track.title + " - " + track.artist);
}
```

### Get Stream URL
```java
YouTubeMusicService.StreamInfo stream = service.getStreamUrl("dQw4w9WgXcQ");
System.out.println("Stream URL: " + stream.url);
System.out.println("Quality: " + stream.getQualityDescription());

// Play in VLC
Runtime.getRuntime().exec("vlc " + stream.url);
```

### Search Suggestions
```java
List<String> suggestions = service.getSearchSuggestions("nev");
// ["never gonna give you up", "never", "never let you go", ...]
```

## 🎨 Extending the App

Want to add more features? You have access to all SimpMusic core functionality:

- **Playlists**: Get playlist contents, create playlists
- **Artists**: Get artist details, songs, albums
- **Albums**: Get full album track lists
- **Lyrics**: Fetch synchronized lyrics
- **Related Songs**: Get recommendations

Check the `YouTube.kt` class in `kotlinYtmusicScraper` for all available methods!

## ⚠️ Important Notes

1. **No API Key Required** - Uses web scraping (like the browser)
2. **YouTube ToS** - This is for educational purposes only
3. **Rate Limiting** - Don't spam requests too fast
4. **Stream URLs Expire** - URLs are valid for ~6 hours
5. **Kotlin Runtime** - Your app includes Kotlin stdlib (~2MB)

## 🐛 Troubleshooting

### Error: Cannot resolve symbol 'YouTube'
**Solution:** Run Gradle sync in IntelliJ/Android Studio

### Error: Module not found
**Solution:** Build the core modules first:
```bash
gradlew :core:service:kotlinYtmusicScraper:build
```

### Error: Coroutine suspended
**Solution:** Make sure you're using `BuildersKt.runBlocking` wrapper

## 📝 Next Steps

1. **Add Media Player** - Use VLCJ to actually play the streams
2. **Build GUI** - Create JavaFX interface
3. **Add Database** - Cache songs locally
4. **Implement Queue** - Playlist management
5. **Add Downloads** - Save audio files

## 💡 Tips

- Use IntelliJ IDEA for best Kotlin-Java interop experience
- Enable Kotlin plugin in your IDE
- Check the Kotlin source code for documentation
- Look at `SimpMusic` app for implementation examples

## 🙏 Credits

Built on top of [SimpMusic](https://github.com/maxrave-dev/SimpMusic) core modules.

---

**Happy coding! 🎵**


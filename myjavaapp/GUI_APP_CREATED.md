# 🎵 YouTube Music Player GUI

## New GUI Application Created!

I've created a complete JavaFX GUI application for you with:

### ✨ Features

1. **Search Interface**
   - Search bar with instant search
   - Results displayed in a table
   - Shows: Title, Artist, Duration

2. **Song Selection**
   - Click "Play" button on any song
   - Loads and plays the stream automatically

3. **Media Player Controls**
   - Play/Pause button
   - Stop button
   - Volume slider (0-100%)
   - Time display (current / total)
   - Now Playing label

4. **Status Bar**
   - Shows search status
   - Shows playback status
   - Shows quality information

### 🚀 How to Run

```powershell
cd C:\Users\Jagdeep\AndroidStudioProjects\SimpMusic
.\gradlew.bat :myjavaapp:run
```

The GUI will open automatically!

### 🎨 UI Layout

```
┌─────────────────────────────────────────────┐
│ 🎵 YouTube Music Player                     │
├─────────────────────────────────────────────┤
│ [Search Box.....................] [🔍 Search]│
├─────────────────────────────────────────────┤
│ Search Results:                             │
│ ┌─────────────────────────────────────────┐ │
│ │ Title    │ Artist  │ Duration │ Action │ │
│ │──────────┼─────────┼──────────┼────────│ │
│ │ Song 1   │ Artist1 │ 3:45     │[▶ Play]│ │
│ │ Song 2   │ Artist2 │ 4:12     │[▶ Play]│ │
│ └─────────────────────────────────────────┘ │
├─────────────────────────────────────────────┤
│ Now Playing: Song Title - Artist            │
│ [▶/⏸] [⏹] 🔊 [====|----] 0:45 / 3:45       │
├─────────────────────────────────────────────┤
│ Status: Playing | Quality: mp4 (128 kbps)   │
└─────────────────────────────────────────────┘
```

### 📝 What Changed

1. **Added JavaFX Plugin** to `build.gradle.kts`
   ```kotlin
   id("org.openjfx.javafxplugin") version "0.1.0"
   
   javafx {
       version = "21"
       modules = listOf("javafx.controls", "javafx.fxml", "javafx.media")
   }
   ```

2. **Updated Main Class**
   - Changed from `com.myjavaapp.Main`
   - To `com.myjavaapp.gui.MusicPlayerApp`

3. **Created New GUI App**
   - Location: `src/main/java/com/myjavaapp/gui/MusicPlayerApp.java`
   - Full-featured music player with search and playback

### 🎮 How to Use

1. **Search for Songs**
   - Type song name in search box
   - Press Enter or click "Search"
   - Wait for results to load

2. **Play a Song**
   - Click the "▶ Play" button next to any song
   - Player will fetch stream URL
   - Starts playing automatically

3. **Control Playback**
   - **Play/Pause**: Click the large play/pause button
   - **Stop**: Click the stop button
   - **Volume**: Adjust the slider
   - **Time**: Shows current position and total duration

### 🔧 Technical Details

- **Async Loading**: Search and stream loading happen in background threads
- **Media Player**: Uses JavaFX MediaPlayer for audio playback
- **Direct Streaming**: Plays audio directly from YouTube Music URLs
- **Volume Control**: Adjustable from 0-100%
- **Error Handling**: Shows alerts for any errors

### 💡 Features Explained

#### Search
- Queries YouTube Music via your Kotlin bridge
- Displays results in a table
- Shows song title, artist, and duration

#### Playback
1. Click "Play" on a song
2. Gets streaming URL via `getStreamUrl()`
3. Creates JavaFX Media player
4. Streams and plays audio
5. Updates UI with current time

#### Controls
- **▶/⏸ Button**: Toggle play/pause
- **⏹ Button**: Stop and reset
- **🔊 Slider**: Adjust volume
- **Time Label**: Shows progress

### 🎯 Old Console App Still Available

The old console app (`Main.java`) is still there if you need it:

```powershell
# Run console version
.\gradlew.bat :myjavaapp:run -PmainClass=com.myjavaapp.Main
```

### 🚀 Build and Run

```powershell
cd C:\Users\Jagdeep\AndroidStudioProjects\SimpMusic

# Sync Gradle (to download JavaFX)
# File → Sync Project with Gradle Files in IDE

# Build
.\gradlew.bat build

# Run GUI
.\gradlew.bat :myjavaapp:run
```

### 🎨 Customization

Want to customize? Edit `MusicPlayerApp.java`:

- **Colors**: Change `-fx-background-color` in styles
- **Sizes**: Adjust `setPrefWidth()`, `setPrefHeight()`
- **Layout**: Modify VBox/HBox arrangements
- **Features**: Add playlist support, favorites, etc.

### 🎉 Enjoy!

You now have a fully functional YouTube Music player with GUI! 

Search, select, and play any song from YouTube Music! 🎵


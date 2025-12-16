package com.myjavaapp.bridge

import com.maxrave.kotlinytmusicscraper.YouTube
import com.maxrave.kotlinytmusicscraper.models.AlbumItem
import com.maxrave.kotlinytmusicscraper.models.SongItem
import kotlinx.coroutines.runBlocking

/**
 * Java-friendly bridge for YouTube Music API
 * Converts Kotlin suspend functions and Result types to simple Java-friendly methods
 */
class YouTubeBridge {
    private val youtube = YouTube()

    /**
     * Search for songs - returns null on error
     */
    fun searchSongs(query: String): List<SongItem>? {
        return runBlocking {
            youtube.search(query, YouTube.SearchFilter.FILTER_SONG)
                .getOrNull()
                ?.items
                ?.filterIsInstance<SongItem>()
        }
    }

    /**
     * Search for albums - returns null on error
     */
    fun searchAlbums(query: String): List<AlbumItem>? {
        return runBlocking {
            youtube.search(query, YouTube.SearchFilter.FILTER_ALBUM)
                .getOrNull()
                ?.items
                ?.filterIsInstance<AlbumItem>()
        }
    }

    /**
     * Get stream data for a video - returns StreamData or null
     */
    fun getStreamData(videoId: String): StreamData? {
        return runBlocking {
            youtube.player(videoId, null, false, false)
                .getOrNull()
                ?.let { triple ->
                    val playerResponse = triple.second

                    // Extract video details
                    val title = playerResponse.videoDetails?.title
                    val author = playerResponse.videoDetails?.author
                    val durationSeconds = playerResponse.videoDetails?.lengthSeconds?.toIntOrNull()

                    // Find best audio format
                    val adaptiveFormats = playerResponse.streamingData?.adaptiveFormats
                    val audioFormats = adaptiveFormats?.filter { format ->
                        format.mimeType?.startsWith("audio/") == true &&
                        !format.url.isNullOrEmpty()
                    }?.sortedByDescending { it.bitrate ?: 0 }

                    val bestFormat = audioFormats?.firstOrNull()

                    StreamData(
                        url = bestFormat?.url,
                        title = title,
                        author = author,
                        durationSeconds = durationSeconds,
                        bitrate = bestFormat?.bitrate,
                        mimeType = bestFormat?.mimeType,
                        itag = bestFormat?.itag
                    )
                }
        }
    }

    /**
     * Get search suggestions
     */
    fun getSearchSuggestions(query: String): List<String>? {
        return runBlocking {
            youtube.getYTMusicSearchSuggestions(query)
                .getOrNull()
                ?.queries
        }
    }

    /**
     * Get home feed - returns mixed list of songs and playlists
     * Falls back to trending/popular songs if home feed is empty
     */
    fun getHomeFeed(): List<SongItem>? {
        return runBlocking {
            try {
                println("YouTubeBridge: Fetching home feed...")

                // Try to get home feed
                val homeFeedResult = youtube.customQuery("FEmusic_home", null, null, null, false)
                    .getOrNull()

                if (homeFeedResult == null) {
                    println("YouTubeBridge: Home feed query returned null, trying alternative method...")
                    // Fallback: Search for popular/trending songs
                    return@runBlocking searchSongs("trending music 2024")
                }

                val songs = mutableListOf<SongItem>()

                // Parse the response
                val contents = homeFeedResult.contents
                    ?.singleColumnBrowseResultsRenderer
                    ?.tabs
                    ?.firstOrNull()
                    ?.tabRenderer
                    ?.content
                    ?.sectionListRenderer
                    ?.contents

                println("YouTubeBridge: Found ${contents?.size ?: 0} sections in home feed")

                contents?.forEach { content ->
                    // Check for music carousel shelf (most common in home feed)
                    content.musicCarouselShelfRenderer?.contents?.forEach { item ->
                        item.musicTwoRowItemRenderer?.let { renderer ->
                            try {
                                val title = renderer.title.runs?.firstOrNull()?.text
                                if (title == null) return@let

                                val thumbnail = renderer.thumbnailRenderer.musicThumbnailRenderer
                                    ?.thumbnail?.thumbnails?.lastOrNull()?.url ?: ""

                                // Check if it's a song (has watchEndpoint)
                                val watchEndpoint = renderer.navigationEndpoint.watchEndpoint
                                if (watchEndpoint != null) {
                                    val videoId = watchEndpoint.videoId
                                    if (videoId != null) {
                                        // Parse duration from subtitle if available
                                        var duration: Int? = null
                                        val subtitleRuns = renderer.subtitle?.runs
                                        subtitleRuns?.forEach { run ->
                                            val text = run.text
                                            // Try to find duration pattern like "3:45"
                                            if (text?.contains(":") == true) {
                                                try {
                                                    val parts = text.split(":")
                                                    if (parts.size == 2) {
                                                        val minutes = parts[0].toIntOrNull()
                                                        val seconds = parts[1].toIntOrNull()
                                                        if (minutes != null && seconds != null) {
                                                            duration = minutes * 60 + seconds
                                                        }
                                                    }
                                                } catch (e: Exception) {
                                                    // Ignore parsing errors
                                                }
                                            }
                                        }

                                        songs.add(
                                            SongItem(
                                                id = videoId,
                                                title = title,
                                                artists = emptyList(),
                                                album = null,
                                                duration = duration,
                                                thumbnail = thumbnail,
                                                thumbnails = null
                                            )
                                        )
                                    }
                                }
                            } catch (e: Exception) {
                                println("YouTubeBridge: Error parsing item: ${e.message}")
                            }
                        }
                    }

                    // Also check for music shelf renderer (list format)
                    content.musicShelfRenderer?.contents?.forEach { item ->
                        item.musicResponsiveListItemRenderer?.let { renderer ->
                            try {
                                val flexColumns = renderer.flexColumns
                                val videoId = renderer.playlistItemData?.videoId

                                if (videoId != null && flexColumns.isNotEmpty()) {
                                    val title = flexColumns.firstOrNull()
                                        ?.musicResponsiveListItemFlexColumnRenderer
                                        ?.text?.runs?.firstOrNull()?.text

                                    if (title != null) {
                                        val thumbnail = renderer.thumbnail?.musicThumbnailRenderer
                                            ?.thumbnail?.thumbnails?.lastOrNull()?.url ?: ""

                                        songs.add(
                                            SongItem(
                                                id = videoId,
                                                title = title,
                                                artists = emptyList(),
                                                album = null,
                                                duration = null,
                                                thumbnail = thumbnail,
                                                thumbnails = null
                                            )
                                        )
                                    }
                                }
                            } catch (e: Exception) {
                                println("YouTubeBridge: Error parsing shelf item: ${e.message}")
                            }
                        }
                    }
                }

                println("YouTubeBridge: Parsed ${songs.size} songs from home feed")

                // If still no songs, fallback to search
                if (songs.isEmpty()) {
                    println("YouTubeBridge: No songs in home feed, falling back to search...")
                    return@runBlocking searchSongs("popular music")
                }

                songs
            } catch (e: Exception) {
                println("YouTubeBridge: Exception in getHomeFeed: ${e.message}")
                e.printStackTrace()
                // Fallback to search for popular songs
                searchSongs("top hits 2024")
            }
        }
    }

    /**
     * Get top tracks/charts
     */
    fun getTopTracks(): List<SongItem>? {
        return runBlocking {
            try {
                println("YouTubeBridge: Fetching top tracks...")

                // Try to get charts
                val chartsResult = youtube.customQuery("FEmusic_charts", null, null, null, false)
                    .getOrNull()

                if (chartsResult != null) {
                    val songs = mutableListOf<SongItem>()

                    val contents = chartsResult.contents
                        ?.singleColumnBrowseResultsRenderer
                        ?.tabs
                        ?.firstOrNull()
                        ?.tabRenderer
                        ?.content
                        ?.sectionListRenderer
                        ?.contents

                    contents?.forEach { content ->
                        content.musicCarouselShelfRenderer?.contents?.forEach { item ->
                            item.musicTwoRowItemRenderer?.let { renderer ->
                                parseItemToSong(renderer)?.let { songs.add(it) }
                            }
                        }

                        content.musicShelfRenderer?.contents?.forEach { item ->
                            item.musicResponsiveListItemRenderer?.let { renderer ->
                                parseShelfItemToSong(renderer)?.let { songs.add(it) }
                            }
                        }
                    }

                    if (songs.isNotEmpty()) {
                        return@runBlocking songs
                    }
                }

                // Fallback to search
                searchSongs("top tracks 2024")
            } catch (e: Exception) {
                println("YouTubeBridge: Exception in getTopTracks: ${e.message}")
                searchSongs("top songs")
            }
        }
    }

    /**
     * Get new singles/releases
     */
    fun getNewSingles(): List<SongItem>? {
        return runBlocking {
            try {
                println("YouTubeBridge: Fetching new singles...")

                // Try to get new releases
                val newReleasesResult = youtube.customQuery("FEmusic_new_releases", null, null, null, false)
                    .getOrNull()

                if (newReleasesResult != null) {
                    val songs = mutableListOf<SongItem>()

                    val contents = newReleasesResult.contents
                        ?.singleColumnBrowseResultsRenderer
                        ?.tabs
                        ?.firstOrNull()
                        ?.tabRenderer
                        ?.content
                        ?.sectionListRenderer
                        ?.contents

                    contents?.forEach { content ->
                        content.musicCarouselShelfRenderer?.contents?.forEach { item ->
                            item.musicTwoRowItemRenderer?.let { renderer ->
                                parseItemToSong(renderer)?.let { songs.add(it) }
                            }
                        }

                        content.musicShelfRenderer?.contents?.forEach { item ->
                            item.musicResponsiveListItemRenderer?.let { renderer ->
                                parseShelfItemToSong(renderer)?.let { songs.add(it) }
                            }
                        }
                    }

                    if (songs.isNotEmpty()) {
                        return@runBlocking songs
                    }
                }

                // Fallback to search
                searchSongs("new singles 2024")
            } catch (e: Exception) {
                println("YouTubeBridge: Exception in getNewSingles: ${e.message}")
                searchSongs("new music")
            }
        }
    }

    private fun parseItemToSong(renderer: com.maxrave.kotlinytmusicscraper.models.MusicTwoRowItemRenderer): SongItem? {
        try {
            val title = renderer.title.runs?.firstOrNull()?.text ?: return null
            val thumbnail = renderer.thumbnailRenderer.musicThumbnailRenderer
                ?.thumbnail?.thumbnails?.lastOrNull()?.url ?: ""
            val watchEndpoint = renderer.navigationEndpoint.watchEndpoint ?: return null
            val videoId = watchEndpoint.videoId ?: return null

            var duration: Int? = null
            val subtitleRuns = renderer.subtitle?.runs
            subtitleRuns?.forEach { run ->
                val text = run.text
                if (text?.contains(":") == true) {
                    try {
                        val parts = text.split(":")
                        if (parts.size == 2) {
                            val minutes = parts[0].toIntOrNull()
                            val seconds = parts[1].toIntOrNull()
                            if (minutes != null && seconds != null) {
                                duration = minutes * 60 + seconds
                            }
                        }
                    } catch (e: Exception) {}
                }
            }

            return SongItem(
                id = videoId,
                title = title,
                artists = emptyList(),
                album = null,
                duration = duration,
                thumbnail = thumbnail,
                thumbnails = null
            )
        } catch (e: Exception) {
            return null
        }
    }

    private fun parseShelfItemToSong(renderer: com.maxrave.kotlinytmusicscraper.models.MusicResponsiveListItemRenderer): SongItem? {
        try {
            val flexColumns = renderer.flexColumns
            val videoId = renderer.playlistItemData?.videoId ?: return null
            val title = flexColumns.firstOrNull()
                ?.musicResponsiveListItemFlexColumnRenderer
                ?.text?.runs?.firstOrNull()?.text ?: return null
            val thumbnail = renderer.thumbnail?.musicThumbnailRenderer
                ?.thumbnail?.thumbnails?.lastOrNull()?.url ?: ""

            return SongItem(
                id = videoId,
                title = title,
                artists = emptyList(),
                album = null,
                duration = null,
                thumbnail = thumbnail,
                thumbnails = null
            )
        } catch (e: Exception) {
            return null
        }
    }

    /**
     * Get new release albums
     */
    fun getNewAlbums(): List<AlbumItem>? {
        return runBlocking {
            try {
                println("YouTubeBridge: Fetching new albums...")

                // Search for new albums
                searchAlbums("new albums 2024")
            } catch (e: Exception) {
                println("YouTubeBridge: Exception in getNewAlbums: ${e.message}")
                null
            }
        }
    }

    /**
     * Get album details with tracks
     */
    fun getAlbumDetails(browseId: String): AlbumDetails? {
        return runBlocking {
            try {
                println("YouTubeBridge: Fetching album details for $browseId...")

                youtube.album(browseId, withSongs = true)
                    .getOrNull()
                    ?.let { albumPage ->
                        AlbumDetails(
                            browseId = albumPage.album.browseId,
                            title = albumPage.album.title,
                            thumbnail = albumPage.album.thumbnail,
                            year = albumPage.album.year,
                            artists = albumPage.album.artists?.map { it.name } ?: emptyList(),
                            songs = albumPage.songs
                        )
                    }
            } catch (e: Exception) {
                println("YouTubeBridge: Exception in getAlbumDetails: ${e.message}")
                e.printStackTrace()
                null
            }
        }
    }

    /**
     * Data class for album details with tracks
     */
    data class AlbumDetails(
        val browseId: String,
        val title: String,
        val thumbnail: String,
        val year: Int?,
        val artists: List<String>,
        val songs: List<com.maxrave.kotlinytmusicscraper.models.SongItem>
    )

    /**
     * Get structured home feed with sections
     */
    fun getHomeFeedSections(): HomeFeedData? {
        return runBlocking {
            try {
                println("YouTubeBridge: Fetching structured home feed...")

                val trendingSongs = getHomeFeed()
                val topTracks = getTopTracks()
                val newSingles = getNewSingles()
                val newAlbums = getNewAlbums()

                HomeFeedData(
                    trendingSongs = trendingSongs ?: emptyList(),
                    topTracks = topTracks ?: emptyList(),
                    newReleases = newSingles ?: emptyList(),
                    newAlbums = newAlbums ?: emptyList()
                )
            } catch (e: Exception) {
                println("YouTubeBridge: Exception in getHomeFeedSections: ${e.message}")
                null
            }
        }
    }

    /**
     * Data class for structured home feed
     */
    data class HomeFeedData(
        val trendingSongs: List<SongItem>,
        val topTracks: List<SongItem>,
        val newReleases: List<SongItem>,
        val newAlbums: List<AlbumItem>
    )

    /**
     * Simple data class for stream information
     */
    data class StreamData(
        val url: String?,
        val title: String?,
        val author: String?,
        val durationSeconds: Int?,
        val bitrate: Int?,
        val mimeType: String?,
        val itag: Int?
    )
}


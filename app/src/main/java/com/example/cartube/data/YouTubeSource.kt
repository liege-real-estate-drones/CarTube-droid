package com.example.cartube.data

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.schabi.newpipe.extractor.ServiceList
import org.schabi.newpipe.extractor.stream.StreamInfoItem
import shalva97.newvalve.initNewPipe

object YouTubeSource {
    private const val TAG = "YouTubeSource"
    @Volatile private var inited = false

    private fun ensureInit() {
        if (!inited) {
            initNewPipe()
            inited = true
        }
    }

    suspend fun getBestVideoUrl(youtubeUrl: String): Result<String?> {
        return try {
            val url = withContext(Dispatchers.IO) {
                ensureInit()
                val service = ServiceList.YouTube
                val extractor = service.getStreamExtractor(youtubeUrl)
                extractor.fetchPage()
                extractor.videoStreams
                    .filter { !it.isVideoOnly }
                    .maxByOrNull { it.resolution.substringBefore("p").toIntOrNull() ?: 0 }
                    ?.content
            }
            Result.Success(url)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching video URL for $youtubeUrl", e)
            Result.Error(e)
        }
    }

    suspend fun search(query: String): Result<List<StreamInfoItem>> {
        return try {
            val items = withContext(Dispatchers.IO) {
                ensureInit()
                val service = ServiceList.YouTube
                val extractor = service.getSearchExtractor(query)
                extractor.fetchPage()
                extractor.initialPage.items
            }
            Result.Success(items)
        } catch (e: Exception) {
            Log.e(TAG, "Error searching for $query", e)
            Result.Error(e)
        }
    }

    suspend fun getPlaylistStreams(playlistUrl: String): Result<List<StreamInfoItem>> {
        return try {
            val items = withContext(Dispatchers.IO) {
                ensureInit()
                val service = ServiceList.YouTube
                val extractor = service.getPlaylistExtractor(playlistUrl)
                extractor.fetchPage()
                extractor.initialPage.items
            }
            Result.Success(items)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching playlist $playlistUrl", e)
            Result.Error(e)
        }
    }

    // bestAudioUrl is not used anymore, but let's keep it for completeness and refactor it as well.
    suspend fun bestAudioUrl(youtubeUrl: String): Result<String?> {
        return try {
            val url = withContext(Dispatchers.IO) {
                ensureInit()
                val service = ServiceList.YouTube
                val extractor = service.getStreamExtractor(youtubeUrl)
                extractor.fetchPage()
                extractor.audioStreams
                    .maxByOrNull { it.averageBitrate }
                    ?.content
            }
            Result.Success(url)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching audio URL for $youtubeUrl", e)
            Result.Error(e)
        }
    }

    suspend fun getSuggestions(query: String): List<String> {
        return try {
            withContext(Dispatchers.IO) {
                ensureInit()
                val service = ServiceList.YouTube
                service.suggestionExtractor.fetchPage(query)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching suggestions for $query", e)
            emptyList()
        }
    }
}

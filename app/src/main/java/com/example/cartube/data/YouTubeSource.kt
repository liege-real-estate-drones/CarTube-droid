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

    suspend fun bestAudioUrl(youtubeUrl: String): String? {
        return try {
            withContext(Dispatchers.IO) {
                ensureInit()
                val service = ServiceList.YouTube
                val extractor = service.getStreamExtractor(youtubeUrl)
                extractor.fetchPage()
                extractor.audioStreams
                    .maxByOrNull { it.averageBitrate }
                    ?.content
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching audio URL for $youtubeUrl", e)
            null
        }
    }

    suspend fun getBestVideoUrl(youtubeUrl: String): String? {
        return try {
            withContext(Dispatchers.IO) {
                ensureInit()
                val service = ServiceList.YouTube
                val extractor = service.getStreamExtractor(youtubeUrl)
                extractor.fetchPage()
                extractor.videoStreams
                    .filter { !it.isVideoOnly }
                    .maxByOrNull { it.resolution.substringBefore("p").toIntOrNull() ?: 0 }
                    ?.content
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching video URL for $youtubeUrl", e)
            null
        }
    }

    suspend fun search(query: String): List<StreamInfoItem> {
        return try {
            withContext(Dispatchers.IO) {
                ensureInit()
                val service = ServiceList.YouTube
                val extractor = service.getSearchExtractor(query)
                extractor.fetchPage()
                extractor.initialPage.items
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error searching for $query", e)
            emptyList()
        }
    }

    suspend fun getPlaylistStreams(playlistUrl: String): List<StreamInfoItem> {
        return try {
            withContext(Dispatchers.IO) {
                ensureInit()
                val service = ServiceList.YouTube
                val extractor = service.getPlaylistExtractor(playlistUrl)
                extractor.fetchPage()
                extractor.initialPage.items
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching playlist $playlistUrl", e)
            emptyList()
        }
    }
}

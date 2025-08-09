package com.example.cartube.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.schabi.newpipe.extractor.ServiceList
import org.schabi.newpipe.extractor.stream.AudioStream
import shalva97.newvalve.initNewPipe   // importé depuis NewValve

object YouTubeSource {
    @Volatile private var inited = false

    private fun ensureInit() {
        if (!inited) {
            initNewPipe()   // initialise NewPipeExtractor avec OkHttp
            inited = true
        }
    }

    /**
     * @return l’URL du meilleur flux audio (ex. opus/webm) ou null si rien.
     */
    suspend fun bestAudioUrl(youtubeUrl: String): String? = withContext(Dispatchers.IO) {
        ensureInit()
        val service = ServiceList.YouTube
        val extractor = service.getStreamExtractor(youtubeUrl)
        extractor.fetchPage() // réseau — à faire en IO

        extractor.audioStreams
            .maxByOrNull { it.averageBitrate }
            ?.content   // <- URL directe à passer à ExoPlayer
    }

    /**
     * @return l’URL du meilleur flux vidéo avec audio, ou null si rien.
     */
    suspend fun getBestVideoUrl(youtubeUrl: String): String? = withContext(Dispatchers.IO) {
        ensureInit()
        val service = ServiceList.YouTube
        val extractor = service.getStreamExtractor(youtubeUrl)
        extractor.fetchPage() // réseau — à faire en IO

        // Find the best quality stream that has both video and audio.
        extractor.videoStreams
            .filter { !it.isVideoOnly }
            .maxByOrNull { it.resolution.substringBefore("p").toIntOrNull() ?: 0 }
            ?.content
    }
}

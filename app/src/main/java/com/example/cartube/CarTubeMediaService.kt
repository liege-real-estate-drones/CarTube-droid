package com.example.cartube

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.session.MediaSessionCompat
import android.view.Surface
import androidx.core.app.NotificationCompat
import androidx.media.MediaBrowserServiceCompat
import androidx.media.app.NotificationCompat.MediaStyle
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import com.example.cartube.data.YouTubeSource
import kotlinx.coroutines.*

private const val ROOT_ID = "root"
private const val CHANNEL_ID = "cartube_playback"

class CarTubeMediaService : MediaBrowserServiceCompat(), CoroutineScope by MainScope() {

    companion object {
        var instance: CarTubeMediaService? = null
    }

    private lateinit var session: MediaSessionCompat
    lateinit var player: ExoPlayer

    // Pour la démo: id -> (title, youtubeUrl)
    val demo = listOf(
        Triple("id1", "Lofi hip hop", "https://www.youtube.com/watch?v=jfKfPfyJRdk"),
        Triple("id2", "NCS Radio",  "https://www.youtube.com/watch?v=iKzRIweSBLA")
    ).associateBy({ it.first }, { it })

    override fun onCreate() {
        super.onCreate()
        instance = this

        player = ExoPlayer.Builder(this).build()

        session = MediaSessionCompat(this, "CarTube").apply {
            setCallback(object : MediaSessionCompat.Callback() {
                override fun onPlayFromMediaId(mediaId: String?, extras: Bundle?) {
                    val triple = demo[mediaId] ?: return
                    launch {
                        val url = withContext(Dispatchers.IO) {
                            YouTubeSource.getBestVideoUrl(triple.third)
                        } ?: return@launch

                        val item = MediaItem.fromUri(url)
                        player.setMediaItem(item)
                        player.prepare()
                        player.play()

                        isActive = true
                        setPlaybackState(android.support.v4.media.session.PlaybackStateCompat.Builder()
                            .setActions(android.support.v4.media.session.PlaybackStateCompat.ACTION_PLAY_PAUSE or
                                        android.support.v4.media.session.PlaybackStateCompat.ACTION_PLAY or
                                        android.support.v4.media.session.PlaybackStateCompat.ACTION_PAUSE)
                            .setState(android.support.v4.media.session.PlaybackStateCompat.STATE_PLAYING, 0, 1f)
                            .build())

                        startForegroundWithNotification()
                    }
                }
                override fun onPause() { player.pause() }
                override fun onPlay() { player.play() }
                override fun onStop() { player.stop(); stopSelf() }
            })
            setSessionToken(sessionToken)
        }

        createNotificationChannel()
    }

    override fun onDestroy() {
        cancel() // coroutines
        player.release()
        session.release()
        instance = null
        super.onDestroy()
    }

    override fun onGetRoot(clientPackageName: String, clientUid: Int, rootHints: Bundle?): BrowserRoot =
        BrowserRoot(ROOT_ID, null)

    override fun onLoadChildren(parentId: String, result: Result<List<MediaBrowserCompat.MediaItem>>) {
        if (parentId != ROOT_ID) { result.sendResult(emptyList()); return }

        val items = demo.values.map { (id, title, _) ->
            val desc = MediaDescriptionCompat.Builder()
                .setMediaId(id)
                .setTitle(title)
                .build()
            MediaBrowserCompat.MediaItem(desc, MediaBrowserCompat.MediaItem.FLAG_PLAYABLE)
        }
        result.sendResult(items)
    }

    private fun startForegroundWithNotification() {
        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentTitle("CarTube")
            .setContentText("Lecture en cours")
            .setStyle(MediaStyle().setMediaSession(session.sessionToken))
            .build()
        startForeground(1, notification)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= 26) {
            val nm = getSystemService(NotificationManager::class.java)
            nm?.createNotificationChannel(
                NotificationChannel(CHANNEL_ID, "Playback", NotificationManager.IMPORTANCE_LOW)
            )
        }
    }
}

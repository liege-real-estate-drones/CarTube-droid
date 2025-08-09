package com.example.cartube

import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.Action
import androidx.car.app.model.ActionStrip
import androidx.car.app.model.CarIcon
import androidx.car.app.navigation.model.NavigationTemplate
import androidx.car.app.navigation.model.SurfaceCallback
import androidx.car.app.navigation.model.SurfaceContainer
import androidx.core.graphics.drawable.IconCompat
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner

class VideoPlayerScreen(carContext: CarContext) : Screen(carContext) {

    private var mediaController: MediaControllerCompat? = null
    private var playbackState: PlaybackStateCompat? = null
    private var metadata: MediaMetadataCompat? = null

    init {
        lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onStart(owner: LifecycleOwner) {
                val mediaSessionToken = CarTubeMediaService.instance?.session?.sessionToken
                if (mediaSessionToken != null) {
                    mediaController = MediaControllerCompat(carContext, mediaSessionToken)
                    mediaController?.registerCallback(controllerCallback)
                }
                playbackState = mediaController?.playbackState
                metadata = mediaController?.metadata
                invalidate()
            }

            override fun onStop(owner: LifecycleOwner) {
                mediaController?.unregisterCallback(controllerCallback)
            }
        })
    }

    private val surfaceCallback = object : SurfaceCallback {
        override fun onSurfaceAvailable(surfaceContainer: SurfaceContainer) {
            CarTubeMediaService.instance?.player?.setVideoSurface(surfaceContainer.surface)
        }

        override fun onSurfaceDestroyed(surfaceContainer: SurfaceContainer) {
            CarTubeMediaService.instance?.player?.setVideoSurface(null)
        }
    }

    private val controllerCallback = object : MediaControllerCompat.Callback() {
        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
            playbackState = state
            invalidate()
        }

        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
            this@VideoPlayerScreen.metadata = metadata
            invalidate()
        }
    }

    override fun onGetTemplate(): Template {
        val playPauseAction = createPlayPauseAction()
        val title = metadata?.getString(MediaMetadataCompat.METADATA_KEY_TITLE) ?: "CarTube"

        return NavigationTemplate.Builder()
            .setTitle(title)
            .setSurfaceCallback(surfaceCallback)
            .setActionStrip(
                ActionStrip.Builder()
                    .addAction(playPauseAction)
                    .addAction(Action.BACK)
                    .build()
            )
            .build()
    }

    private fun createPlayPauseAction(): Action {
        val isPlaying = playbackState?.state == PlaybackStateCompat.STATE_PLAYING
        val iconRes = if (isPlaying) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play
        val title = if (isPlaying) "Pause" else "Play"

        return Action.Builder()
            .setTitle(title)
            .setIcon(CarIcon.Builder(IconCompat.createWithResource(carContext, iconRes)).build())
            .setOnClickListener {
                if (isPlaying) {
                    mediaController?.transportControls?.pause()
                } else {
                    mediaController?.transportControls?.play()
                }
            }
            .build()
    }
}

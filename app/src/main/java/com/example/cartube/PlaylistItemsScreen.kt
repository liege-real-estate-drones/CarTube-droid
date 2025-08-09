package com.example.cartube

import android.os.Bundle
import android.support.v4.media.MediaMetadataCompat
import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.Action
import androidx.car.app.model.ItemList
import androidx.car.app.model.ListTemplate
import androidx.car.app.model.Row
import androidx.car.app.model.Template
import com.example.cartube.data.YouTubeSource
import kotlinx.coroutines.launch
import org.schabi.newpipe.extractor.stream.StreamInfoItem

class PlaylistItemsScreen(
    carContext: CarContext,
    private val playlistUrl: String,
    private val playlistTitle: String
) : Screen(carContext) {

    private enum class LoadState { LOADING, SUCCESS, EMPTY, ERROR }
    private var loadState = LoadState.LOADING
    private var playlistItems: List<StreamInfoItem>? = null

    init {
        lifecycle.addObserver(object : androidx.lifecycle.DefaultLifecycleObserver {
            override fun onCreate(owner: androidx.lifecycle.LifecycleOwner) {
                lifecycleScope.launch {
                    try {
                        val items = YouTubeSource.getPlaylistStreams(playlistUrl)
                        playlistItems = items
                        loadState = if (items.isEmpty()) LoadState.EMPTY else LoadState.SUCCESS
                    } catch (e: Exception) {
                        loadState = LoadState.ERROR
                    }
                    invalidate()
                }
            }
        })
    }

    override fun onGetTemplate(): Template {
        val listBuilder = ItemList.Builder()

        when (loadState) {
            LoadState.LOADING -> listBuilder.setNoItemsMessage("Loading playlist...")
            LoadState.ERROR -> listBuilder.setNoItemsMessage("Error loading playlist.")
            LoadState.EMPTY -> listBuilder.setNoItemsMessage("Playlist is empty or could not be loaded.")
            LoadState.SUCCESS -> {
                playlistItems?.forEach { item ->
                    listBuilder.addItem(
                        Row.Builder()
                            .setTitle(item.name)
                            .addText(item.uploaderName)
                            .setOnClickListener {
                                val extras = Bundle().apply {
                                    putString(MediaMetadataCompat.METADATA_KEY_TITLE, item.name)
                                }
                                CarTubeMediaService.instance?.session?.controller?.transportControls?.playFromMediaId(item.url, extras)
                                screenManager.push(VideoPlayerScreen(carContext))
                            }
                            .build()
                    )
                }
            }
        }

        return ListTemplate.Builder()
            .setSingleList(listBuilder.build())
            .setTitle(playlistTitle)
            .setHeaderAction(Action.BACK)
            .build()
    }
}

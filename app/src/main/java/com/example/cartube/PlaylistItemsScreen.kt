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
import com.example.cartube.data.Result
import kotlinx.coroutines.launch
import org.schabi.newpipe.extractor.exceptions.ContentNotAvailableException
import org.schabi.newpipe.extractor.stream.StreamInfoItem

class PlaylistItemsScreen(
    carContext: CarContext,
    private val playlistUrl: String,
    private val playlistTitle: String
) : Screen(carContext) {

    private sealed class LoadState {
        object LOADING : LoadState()
        data class SUCCESS(val results: List<StreamInfoItem>) : LoadState()
        object EMPTY : LoadState()
        data class ERROR(val message: String) : LoadState()
    }
    private var loadState: LoadState = LoadState.LOADING

    init {
        lifecycle.addObserver(object : androidx.lifecycle.DefaultLifecycleObserver {
            override fun onCreate(owner: androidx.lifecycle.LifecycleOwner) {
                lifecycleScope.launch {
                    loadState = when (val result = YouTubeSource.getPlaylistStreams(playlistUrl)) {
                        is Result.Success -> if (result.data.isEmpty()) LoadState.EMPTY else LoadState.SUCCESS(result.data)
                        is Result.Error -> LoadState.ERROR(getErrorMessage(result.exception))
                    }
                    invalidate()
                }
            }
        })
    }

    override fun onGetTemplate(): Template {
        val listBuilder = ItemList.Builder()
        var title = playlistTitle

        when (val state = loadState) {
            LoadState.LOADING -> listBuilder.setNoItemsMessage("Loading playlist...")
            is LoadState.ERROR -> {
                listBuilder.setNoItemsMessage(state.message)
                title = "Error"
            }
            LoadState.EMPTY -> listBuilder.setNoItemsMessage("Playlist is empty or could not be loaded.")
            is LoadState.SUCCESS -> {
                state.results.forEach { item ->
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
            .setTitle(title)
            .setHeaderAction(Action.BACK)
            .build()
    }

    private fun getErrorMessage(exception: Exception): String {
        return when (exception) {
            is ContentNotAvailableException -> "Playlist not available (private?)"
            else -> "Error loading playlist"
        }
    }
}

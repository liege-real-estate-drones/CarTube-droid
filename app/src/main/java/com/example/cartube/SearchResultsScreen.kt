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

class SearchResultsScreen(carContext: CarContext, private val query: String) : Screen(carContext) {

    private enum class LoadState { LOADING, SUCCESS, EMPTY, ERROR }
    private var loadState = LoadState.LOADING
    private var searchResults: List<StreamInfoItem>? = null

    init {
        lifecycle.addObserver(object : androidx.lifecycle.DefaultLifecycleObserver {
            override fun onCreate(owner: androidx.lifecycle.LifecycleOwner) {
                lifecycleScope.launch {
                    try {
                        val results = YouTubeSource.search(query)
                        searchResults = results
                        loadState = if (results.isEmpty()) LoadState.EMPTY else LoadState.SUCCESS
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
            LoadState.LOADING -> listBuilder.setNoItemsMessage("Searching...")
            LoadState.ERROR -> listBuilder.setNoItemsMessage("Error loading results.")
            LoadState.EMPTY -> listBuilder.setNoItemsMessage("No results found for \"$query\"")
            LoadState.SUCCESS -> {
                searchResults?.forEach { item ->
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
            .setTitle("Results for '$query'")
            .setHeaderAction(Action.BACK)
            .build()
    }
}

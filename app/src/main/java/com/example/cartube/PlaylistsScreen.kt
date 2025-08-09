package com.example.cartube

import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.Action
import androidx.car.app.model.ItemList
import androidx.car.app.model.ListTemplate
import androidx.car.app.model.Row
import androidx.car.app.model.Template
import com.example.cartube.data.PlaylistRepository

class PlaylistsScreen(carContext: CarContext) : Screen(carContext) {

    private lateinit var playlistRepository: PlaylistRepository
    private var playlists: List<String> = emptyList()

    init {
        lifecycle.addObserver(object : androidx.lifecycle.DefaultLifecycleObserver {
            override fun onCreate(owner: androidx.lifecycle.LifecycleOwner) {
                playlistRepository = PlaylistRepository(carContext)
                playlists = playlistRepository.getPlaylists().toList()
                invalidate()
            }
        })
    }

    override fun onGetTemplate(): Template {
        val listBuilder = ItemList.Builder()
        if (playlists.isEmpty()) {
            listBuilder.setNoItemsMessage("No custom playlists added yet.\nAdd them from the phone app.")
        } else {
            playlists.forEach { url ->
                listBuilder.addItem(
                    Row.Builder()
                        // For now, use the URL as the title. A future improvement could be to fetch the real title.
                        .setTitle(url)
                        .setOnClickListener {
                            // The title is unknown here, so pass the URL as the title for now.
                            screenManager.push(PlaylistItemsScreen(carContext, url, url))
                        }
                        .build()
                )
            }
        }

        return ListTemplate.Builder()
            .setSingleList(listBuilder.build())
            .setTitle("My Playlists")
            .setHeaderAction(Action.BACK)
            .build()
    }
}

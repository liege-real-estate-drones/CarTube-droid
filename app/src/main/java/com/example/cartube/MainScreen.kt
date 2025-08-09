package com.example.cartube

import android.os.Bundle
import android.support.v4.media.MediaMetadataCompat
import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.Action
import androidx.car.app.model.CarIcon
import androidx.car.app.model.ItemList
import androidx.car.app.model.ListTemplate
import androidx.car.app.model.Row
import androidx.car.app.model.Template
import androidx.core.graphics.drawable.IconCompat

class MainScreen(carContext: CarContext) : Screen(carContext) {
    override fun onGetTemplate(): Template {
        val listBuilder = ItemList.Builder()

        listBuilder.addItem(
            Row.Builder()
                .setTitle("Browse Playlists")
                .setOnClickListener {
                    screenManager.push(PlaylistsScreen(carContext))
                }
                .build()
        )

        CarTubeMediaService.instance?.demo?.forEach { (_, title, youtubeUrl) ->
            listBuilder.addItem(
                Row.Builder()
                    .setTitle(title)
                    .setOnClickListener {
                        val extras = Bundle().apply {
                            putString(MediaMetadataCompat.METADATA_KEY_TITLE, title)
                        }
                        CarTubeMediaService.instance?.session?.controller?.transportControls?.playFromMediaId(youtubeUrl, extras)
                        screenManager.push(VideoPlayerScreen(carContext))
                    }
                    .build()
            )
        }

        return ListTemplate.Builder()
            .setSingleList(listBuilder.build())
            .setTitle("CarTube")
            .setHeaderAction(
                Action.Builder()
                    .setIcon(CarIcon.Builder(IconCompat.createWithResource(carContext, android.R.drawable.ic_menu_search)).build())
                    .setOnClickListener {
                        screenManager.push(SearchScreen(carContext))
                    }
                    .build()
            )
            .build()
    }
}
